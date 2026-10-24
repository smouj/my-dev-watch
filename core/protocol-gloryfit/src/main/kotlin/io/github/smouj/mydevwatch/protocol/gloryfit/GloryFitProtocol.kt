package io.github.smouj.mydevwatch.protocol.gloryfit

import io.github.smouj.mydevwatch.model.Capability
import io.github.smouj.mydevwatch.model.ProtocolId
import io.github.smouj.mydevwatch.protocol.ProtocolResult
import io.github.smouj.mydevwatch.protocol.WatchProtocol
import io.github.smouj.mydevwatch.protocol.WatchTransport
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout

/**
 * First protocol implementation.
 *
 * It is deliberately narrow: initialisation, battery and a clean disconnect. Clock
 * sync, activity and sleep exist in the catalogue but are not implemented because
 * their payload layouts have not been proven on hardware yet. The UI shows those as
 * research, not as features.
 */
class GloryFitProtocol(
    private val transport: WatchTransport,
    private val timeoutMs: Long = 4_000
) : WatchProtocol {

    override val id: ProtocolId = ProtocolId.GLORYFIT

    // Only what this implementation can actually deliver today. The catalogue
    // documents heart rate, SpO2 and sleep, but catalogued is not implemented.
    override val capabilities: Set<Capability> = setOf(Capability.BATTERY)

    override suspend fun initialize(transport: WatchTransport): ProtocolResult<Unit> = runCatching {
        withTimeout(timeoutMs) {
            transport.subscribe(GloryFitGatt.primaryNotify)
            transport.subscribe(GloryFitGatt.secondaryNotify)
        }
    }.fold(
        onSuccess = { ProtocolResult.Success(Unit) },
        onFailure = { ProtocolResult.Failure("Could not subscribe to GloryFit notifications", it) }
    )

    override suspend fun readBattery(): ProtocolResult<Int> = try {
        val answer = coroutineScope {
            // UNDISPATCHED guarantees the collector is installed before the write,
            // so a fast answer cannot be lost.
            val reply = async(start = CoroutineStart.UNDISPATCHED) {
                withTimeout(timeoutMs) {
                    transport.notifications.first { candidate ->
                        val opcode = candidate.opcode
                        opcode == GloryFitCommand.BATTERY.opcode && candidate.payload.size >= 2
                    }
                }
            }
            transport.write(GloryFitGatt.primaryWrite, GloryFitBattery.request())
            reply.await()
        }
        val percent = GloryFitBattery.decode(answer.payload)
        if (percent == null) {
            ProtocolResult.Failure("Battery payload did not validate")
        } else {
            ProtocolResult.Success(percent)
        }
    } catch (error: Throwable) {
        ProtocolResult.Failure("Battery request failed", error)
    }

    override suspend fun syncClock(): ProtocolResult<Unit> = ProtocolResult.Unsupported(Capability.TIME_SYNC)

    override suspend fun readDeviceInfo(): ProtocolResult<Map<String, String>> =
        ProtocolResult.Unsupported(Capability.DEVICE_INFO)

    override suspend fun disconnect() {
        runCatching { transport.close() }
    }
}
