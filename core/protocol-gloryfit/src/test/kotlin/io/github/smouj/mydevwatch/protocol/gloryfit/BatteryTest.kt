package io.github.smouj.mydevwatch.protocol.gloryfit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BatteryTest {

    @Test
    fun decodesTheDocumentedAnswer() {
        // Captured example: TX A2, RX A2 56, reported as 86 %.
        assertEquals(86, GloryFitBattery.decode(byteArrayOf(0xA2.toByte(), 0x56)))
    }

    @Test
    fun rejectsShortForeignAndOutOfRangePayloads() {
        assertNull(GloryFitBattery.decode(byteArrayOf(0xA2.toByte())))
        assertNull(GloryFitBattery.decode(byteArrayOf(0xE5.toByte(), 0x54)))
        assertNull(GloryFitBattery.decode(byteArrayOf(0xA2.toByte(), 0x65)))
    }

    @Test
    fun requestIsTheSingleDocumentedByte() {
        assertEquals(1, GloryFitBattery.request().size)
        assertEquals(0xA2, GloryFitBattery.request()[0].toInt() and 0xFF)
    }

    @Test
    fun catalogueNeverClaimsImplementationItDoesNotHave() {
        val battery = GloryFitCommand.byOpcode(0xA2)
        assertEquals(GloryFitCommand.BATTERY, battery)
        assertEquals(true, battery?.implemented)
        assertEquals(false, GloryFitCommand.byOpcode(0xE5)?.implemented)
        assertEquals(listOf(GloryFitCommand.BATTERY), GloryFitCommand.implementedCommands)
    }
}
