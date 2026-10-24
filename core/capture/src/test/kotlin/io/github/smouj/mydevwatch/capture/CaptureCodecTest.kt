package io.github.smouj.mydevwatch.capture

import io.github.smouj.mydevwatch.model.ProtocolId
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CaptureCodecTest {

    private val sample = """
        MDCAP/1
        device: S10
        protocol: gloryfit
        created: 2026-10-24T09:00:00Z
        note: baseline
        --
        0 TX 33f1 A2
        140 RX 33f2 A2 56
    """.trimIndent()

    @Test
    fun parsesAManifestAndItsPackets() {
        val parsed = CaptureCodec.parse(sample)
        assertIs<CaptureParse.Success>(parsed)
        val file = parsed.file
        assertEquals("S10", file.manifest.device)
        assertEquals(ProtocolId.GLORYFIT, file.manifest.protocol)
        assertEquals(2, file.packets.size)
        assertContentEquals(byteArrayOf(0xA2.toByte(), 0x56), file.packets[1].payload)
        assertEquals(86, file.packets[1].opcode)
    }

    @Test
    fun encodingIsStableAndRoundTrips() {
        val parsed = CaptureCodec.parse(sample)
        assertIs<CaptureParse.Success>(parsed)
        val encoded = CaptureCodec.encode(parsed.file)
        val again = CaptureCodec.parse(encoded)
        assertIs<CaptureParse.Success>(again)
        assertEquals(parsed.file.packets.size, again.file.packets.size)
        assertEquals(parsed.file.manifest.device, again.file.manifest.device)
        assertTrue(encoded.startsWith(CaptureCodec.VERSION_HEADER))
    }

    @Test
    fun rejectsFilesWithoutHeader() {
        val parsed = CaptureCodec.parse("0 TX 33f1 A2")
        assertIs<CaptureParse.Invalid>(parsed)
        assertEquals(1, parsed.line)
    }

    @Test
    fun rejectsBadHexAndBadDirection() {
        val withOddHex = listOf("MDCAP/1", "--", "0 TX 33f1 A2 5").joinToString("\n")
        val withBadDirection = listOf("MDCAP/1", "--", "0 XX 33f1 A2").joinToString("\n")
        assertIs<CaptureParse.Invalid>(CaptureCodec.parse(withOddHex))
        assertIs<CaptureParse.Invalid>(CaptureCodec.parse(withBadDirection))
    }

    @Test
    fun hexAcceptsSeveralHumanNotations() {
        assertEquals("A2 56", HexFormat.encode(HexFormat.decode("a2:56")))
        assertEquals("A2 56", HexFormat.encode(HexFormat.decode("0xA2 0x56")))
        assertEquals("A2 56", HexFormat.encode(HexFormat.decode(listOf("A2", "56").joinToString("\n"))))
        assertTrue(runCatching { HexFormat.decode("A2 5") }.isFailure)
        assertTrue(runCatching { HexFormat.decode("ZZ") }.isFailure)
    }
}
