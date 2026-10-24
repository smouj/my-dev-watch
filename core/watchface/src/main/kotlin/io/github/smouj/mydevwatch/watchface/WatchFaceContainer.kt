package io.github.smouj.mydevwatch.watchface

import java.nio.ByteOrder
import java.util.zip.CRC32

/**
 * Binary container used by UTE / GloryFit watch faces.
 *
 * Layout, as reported by the GloryDial Studio research and kept configurable here:
 *
 *   0x00  4   identifier (opaque; vendor-defined)
 *   0x04  4   payload length
 *   0x08  4   CRC32 of the payload
 *   0x0C  2   width in pixels
 *   0x0E 10   metadata (opaque)
 *   0x18  ..  payload, usually compressed
 *
 * The height is not part of the documented header, so it is not invented: a project
 * carries it in its manifest, and the container only exposes what the bytes prove.
 */
object WatchFaceFormat {
    const val HEADER_SIZE = 0x18
    const val MAGIC_SIZE = 4
    const val METADATA_SIZE = 10
    const val PAYLOAD_LENGTH_OFFSET = 0x04
    const val CRC_OFFSET = 0x08
    const val WIDTH_OFFSET = 0x0C
    const val METADATA_OFFSET = 0x0E
    const val MAX_PAYLOAD = 8 * 1024 * 1024
}

data class WatchFaceContainer(
    val magic: ByteArray,
    val width: Int,
    val metadata: ByteArray,
    val payload: ByteArray,
    val byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN
) {
    init {
        require(magic.size == WatchFaceFormat.MAGIC_SIZE) { "Identifier must be exactly 4 bytes" }
        require(metadata.size == WatchFaceFormat.METADATA_SIZE) { "Metadata must be exactly 10 bytes" }
        require(width in 1..2048) { "Width out of range: " + width }
    }

    val totalSize: Int get() = WatchFaceFormat.HEADER_SIZE + payload.size
    val checksum: Long get() = WatchFaceCodec.crc32(payload)
}

sealed interface WatchFaceRead {
    data class Success(
        val container: WatchFaceContainer,
        val declaredCrc: Long,
        val checksumValid: Boolean
    ) : WatchFaceRead

    data class Invalid(val reason: Reason) : WatchFaceRead {
        enum class Reason { TOO_SHORT, EMPTY_PAYLOAD, IMPLAUSIBLE_LENGTH, PAYLOAD_OUT_OF_BOUNDS; }
    }
}

object WatchFaceCodec {

    fun crc32(bytes: ByteArray): Long {
        val crc = CRC32()
        crc.update(bytes)
        return crc.value
    }

    fun read(bytes: ByteArray, byteOrder: ByteOrder = ByteOrder.LITTLE_ENDIAN): WatchFaceRead {
        if (bytes.size < WatchFaceFormat.HEADER_SIZE + 1) {
            return WatchFaceRead.Invalid(WatchFaceRead.Invalid.Reason.TOO_SHORT)
        }
        val declaredLength = readInt(bytes, WatchFaceFormat.PAYLOAD_LENGTH_OFFSET, byteOrder)
        if (declaredLength <= 0) {
            return WatchFaceRead.Invalid(WatchFaceRead.Invalid.Reason.EMPTY_PAYLOAD)
        }
        if (declaredLength > WatchFaceFormat.MAX_PAYLOAD) {
            return WatchFaceRead.Invalid(WatchFaceRead.Invalid.Reason.IMPLAUSIBLE_LENGTH)
        }
        if (WatchFaceFormat.HEADER_SIZE + declaredLength != bytes.size) {
            return WatchFaceRead.Invalid(WatchFaceRead.Invalid.Reason.PAYLOAD_OUT_OF_BOUNDS)
        }
        val declaredCrc = readUnsignedInt(bytes, WatchFaceFormat.CRC_OFFSET, byteOrder)
        val width = (bytes[WatchFaceFormat.WIDTH_OFFSET].toInt() and 0xFF) or
            ((bytes[WatchFaceFormat.WIDTH_OFFSET + 1].toInt() and 0xFF) shl 8)
        if (width !in 1..2048) {
            return WatchFaceRead.Invalid(WatchFaceRead.Invalid.Reason.IMPLAUSIBLE_LENGTH)
        }
        val metadata = bytes.copyOfRange(
            WatchFaceFormat.METADATA_OFFSET,
            WatchFaceFormat.METADATA_OFFSET + WatchFaceFormat.METADATA_SIZE
        )
        val payload = bytes.copyOfRange(WatchFaceFormat.HEADER_SIZE, bytes.size)
        val container = WatchFaceContainer(
            magic = bytes.copyOfRange(0, WatchFaceFormat.MAGIC_SIZE),
            width = width,
            metadata = metadata,
            payload = payload,
            byteOrder = byteOrder
        )
        return WatchFaceRead.Success(container, declaredCrc, declaredCrc == crc32(payload))
    }

    fun write(container: WatchFaceContainer): ByteArray {
        val out = ByteArray(container.totalSize)
        container.magic.copyInto(out, 0)
        writeInt(out, WatchFaceFormat.PAYLOAD_LENGTH_OFFSET, container.payload.size, container.byteOrder)
        writeUnsignedInt(out, WatchFaceFormat.CRC_OFFSET, crc32(container.payload), container.byteOrder)
        out[WatchFaceFormat.WIDTH_OFFSET] = (container.width and 0xFF).toByte()
        out[WatchFaceFormat.WIDTH_OFFSET + 1] = ((container.width shr 8) and 0xFF).toByte()
        container.metadata.copyInto(out, WatchFaceFormat.METADATA_OFFSET)
        container.payload.copyInto(out, WatchFaceFormat.HEADER_SIZE)
        return out
    }

    private fun readInt(bytes: ByteArray, offset: Int, order: ByteOrder): Int {
        val raw = readUnsignedInt(bytes, offset, order)
        return raw.toInt()
    }

    private fun readUnsignedInt(bytes: ByteArray, offset: Int, order: ByteOrder): Long {
        var value = 0L
        for (index in 0 until 4) {
            val byte = (bytes[offset + index].toInt() and 0xFF).toLong()
            value = if (order == ByteOrder.LITTLE_ENDIAN) {
                value or (byte shl (8 * index))
            } else {
                (value shl 8) or byte
            }
        }
        return value
    }

    private fun writeInt(target: ByteArray, offset: Int, value: Int, order: ByteOrder) {
        writeUnsignedInt(target, offset, value.toLong() and 0xFFFFFFFFL, order)
    }

    private fun writeUnsignedInt(target: ByteArray, offset: Int, value: Long, order: ByteOrder) {
        for (index in 0 until 4) {
            val shift = if (order == ByteOrder.LITTLE_ENDIAN) 8 * index else 8 * (3 - index)
            target[offset + index] = ((value shr shift) and 0xFF).toByte()
        }
    }
}
