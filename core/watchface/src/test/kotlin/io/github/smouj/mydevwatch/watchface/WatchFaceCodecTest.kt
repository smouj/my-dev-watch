package io.github.smouj.mydevwatch.watchface

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WatchFaceCodecTest {

    private fun payload(size: Int = 96): ByteArray = ByteArray(size) { index -> (index * 7).toByte() }

    private fun container(size: Int = 96) = WatchFaceContainer(
        magic = byteArrayOf(0x57, 0x46, 0x46, 0x31),
        width = 240,
        metadata = ByteArray(10) { 0x11 },
        payload = payload(size)
    )

    @Test
    fun roundTripPreservesEveryFieldAndValidatesTheChecksum() {
        val original = container()
        val bytes = WatchFaceCodec.write(original)
        assertEquals(24 + original.payload.size, bytes.size)

        val read = WatchFaceCodec.read(bytes)
        assertIs<WatchFaceRead.Success>(read)
        assertEquals(240, read.container.width)
        assertEquals(original.payload.toList(), read.container.payload.toList())
        assertEquals(original.metadata.toList(), read.container.metadata.toList())
        assertTrue(read.checksumValid)
        assertEquals(original.checksum, read.declaredCrc)
    }

    @Test
    fun corruptPayloadIsDetectedButStillInspectable() {
        val bytes = WatchFaceCodec.write(container())
        bytes[WatchFaceFormat.HEADER_SIZE + 5] = (bytes[WatchFaceFormat.HEADER_SIZE + 5].toInt() xor 0xFF).toByte()
        val read = WatchFaceCodec.read(bytes)
        assertIs<WatchFaceRead.Success>(read)
        assertFalse(read.checksumValid)
    }

    @Test
    fun truncatedHeaderIsRejected() {
        val read = WatchFaceCodec.read(ByteArray(10))
        assertIs<WatchFaceRead.Invalid>(read)
        assertEquals(WatchFaceRead.Invalid.Reason.TOO_SHORT, read.reason)
    }

    @Test
    fun declaredLengthMustMatchTheFile() {
        val bytes = WatchFaceCodec.write(container())
        val shorter = bytes.copyOf(bytes.size - 1)
        val read = WatchFaceCodec.read(shorter)
        assertIs<WatchFaceRead.Invalid>(read)
        assertEquals(WatchFaceRead.Invalid.Reason.PAYLOAD_OUT_OF_BOUNDS, read.reason)
    }

    @Test
    fun emptyPayloadIsRejectedInsteadOfGuessed() {
        val bytes = WatchFaceCodec.write(container())
        val read = WatchFaceCodec.read(bytes.copyOfRange(0, WatchFaceFormat.HEADER_SIZE))
        assertIs<WatchFaceRead.Invalid>(read)
        assertEquals(WatchFaceRead.Invalid.Reason.TOO_SHORT, read.reason)
    }

    @Test
    fun crcMatchesTheJvmReferenceImplementation() {
        assertEquals(java.util.zip.CRC32().apply { update(payload()) }.value, WatchFaceCodec.crc32(payload()))
    }
}
