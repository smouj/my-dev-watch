package io.github.smouj.mydevwatch.capture

import io.github.smouj.mydevwatch.model.ProtocolId
import io.github.smouj.mydevwatch.protocol.GattChannel
import io.github.smouj.mydevwatch.protocol.WatchNotification
import io.github.smouj.mydevwatch.protocol.WatchTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.Instant

/**
 * .mdcap — a deliberately boring, diff-friendly capture format.
 *
 * Text was chosen over JSON so a session can be reviewed, edited and blamed in git
 * without tooling, and so the core keeps zero serialisation dependencies.
 *
 *   MDCAP/1
 *   device: S10
 *   protocol: gloryfit
 *   created: 2026-10-24T12:00:00Z
 *   note: baseline
 *   --
 *   0 TX 33f1 A2
 *   128 RX 33f2 A2 56
 */
data class CapturePacket(
    val offsetMs: Long,
    val direction: Direction,
    val characteristic: String,
    val payload: ByteArray
) {
    val opcode: Int? get() = payload.firstOrNull()?.toInt()?.and(0xFF)

    enum class Direction { TX, RX }
}

data class CaptureManifest(
    val device: String? = null,
    val protocol: ProtocolId = ProtocolId.UNKNOWN,
    val createdAt: Instant = Instant.now(),
    val note: String? = null
)

data class CaptureFile(
    val manifest: CaptureManifest,
    val packets: List<CapturePacket>
) {
    fun rx(capabilityOrOpcode: Int? = null): List<CapturePacket> =
        packets.filter { it.direction == CapturePacket.Direction.RX && (capabilityOrOpcode == null || it.opcode == capabilityOrOpcode) }
}

sealed interface CaptureParse {
    data class Success(val file: CaptureFile) : CaptureParse
    data class Invalid(val line: Int, val reason: String) : CaptureParse
}

object CaptureCodec {
    const val VERSION_HEADER = "MDCAP/1"
    private const val SEPARATOR = "--"

    fun encode(file: CaptureFile): String = buildString {
        appendLine(VERSION_HEADER)
        file.manifest.device?.let { appendLine("device: " + it) }
        appendLine("protocol: " + file.manifest.protocol.key)
        appendLine("created: " + file.manifest.createdAt.toString())
        file.manifest.note?.let { appendLine("note: " + it.replace("\n", " ")) }
        appendLine(SEPARATOR)
        file.packets.forEach { packet ->
            append(packet.offsetMs)
            append(' ')
            append(packet.direction.name)
            append(' ')
            append(packet.characteristic)
            append(' ')
            appendLine(HexFormat.encode(packet.payload))
        }
    }

    fun parse(text: String): CaptureParse {
        // Capture files travel between editors and shells: tolerate a leading BOM,
        // trailing newline or an empty first line instead of rejecting the session.
        val lines = text.trim().lines()
        if (lines.firstOrNull()?.trim() != VERSION_HEADER) {
            return CaptureParse.Invalid(1, "Missing " + VERSION_HEADER + " header")
        }
        var device: String? = null
        var protocol = ProtocolId.UNKNOWN
        var created = Instant.now()
        var note: String? = null
        val packets = mutableListOf<CapturePacket>()
        var inBody = false

        lines.drop(1).forEachIndexed { index, rawLine ->
            val lineNumber = index + 2
            val line = rawLine.trim()
            if (line.isEmpty()) return@forEachIndexed
            if (!inBody) {
                when {
                    line == SEPARATOR -> inBody = true
                    line.startsWith("device:") -> device = line.removePrefix("device:").trim()
                    line.startsWith("protocol:") -> protocol = ProtocolId.fromKey(line.removePrefix("protocol:").trim())
                    line.startsWith("created:") -> created = runCatching { Instant.parse(line.removePrefix("created:").trim()) }
                        .getOrElse { return CaptureParse.Invalid(lineNumber, "Invalid timestamp") }
                    line.startsWith("note:") -> note = line.removePrefix("note:").trim()
                    else -> return CaptureParse.Invalid(lineNumber, "Unknown manifest field")
                }
                return@forEachIndexed
            }
            val parts = line.split(' ', '\t').filter { it.isNotEmpty() }
            // The payload is every byte after the characteristic: a battery answer is
            // "A2 56", not a single field. Requiring exactly four tokens silently
            // rejected every multi-byte frame.
            if (parts.size < 4) return CaptureParse.Invalid(lineNumber, "Expected: offset direction characteristic payload")
            val offset = parts[0].toLongOrNull()
                ?: return CaptureParse.Invalid(lineNumber, "Invalid offset")
            val direction = CapturePacket.Direction.entries.firstOrNull { it.name == parts[1].uppercase() }
                ?: return CaptureParse.Invalid(lineNumber, "Invalid direction")
            val payload = runCatching { HexFormat.decode(parts.drop(3).joinToString(" ")) }
                .getOrElse { return CaptureParse.Invalid(lineNumber, "Invalid hex payload") }
            if (payload.isEmpty()) return CaptureParse.Invalid(lineNumber, "Empty payload")
            packets += CapturePacket(offset, direction, parts[2].lowercase(), payload)
        }
        if (!inBody) return CaptureParse.Invalid(lines.size, "Missing " + SEPARATOR + " separator")
        return CaptureParse.Success(CaptureFile(CaptureManifest(device, protocol, created, note), packets))
    }
}

/** Replays a capture through the real protocol stack, with no watch and no radio. */
class ReplayTransport(
    private val packets: List<CapturePacket>,
    override val mtu: Int = 23
) : WatchTransport {

    // Replay keeps a short history so a collector that subscribes a moment late
    // still receives the frame that was just written.
    private val sink = MutableSharedFlow<WatchNotification>(replay = 16, extraBufferCapacity = 64)
    override val notifications: Flow<WatchNotification> = sink.asSharedFlow()
    private val subscriptions = mutableSetOf<GattChannel>()

    val written: MutableList<CapturePacket> = mutableListOf()

    override suspend fun write(channel: GattChannel, payload: ByteArray) {
        written += CapturePacket(written.size.toLong(), CapturePacket.Direction.TX, channel.characteristic, payload)
        packets.filter { it.direction == CapturePacket.Direction.RX }.forEach { packet -> emit(packet) }
    }

    private suspend fun emit(packet: CapturePacket) {
        val channel = subscriptions.firstOrNull { it.characteristic.contains(packet.characteristic) }
            ?: GattChannel("", packet.characteristic)
        sink.emit(WatchNotification(channel, packet.payload))
    }

    override suspend fun subscribe(channel: GattChannel) {
        subscriptions += channel
    }

    override suspend fun requestMtu(desired: Int): Int = mtu

    override suspend fun close() = Unit
}
