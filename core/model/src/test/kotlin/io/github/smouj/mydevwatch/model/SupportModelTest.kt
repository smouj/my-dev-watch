package io.github.smouj.mydevwatch.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SupportModelTest {

    @Test
    fun levelsAreOrderedFromDetectionToOpenSystem() {
        assertEquals(0, SupportLevel.DETECTED.level)
        assertEquals(6, SupportLevel.OPEN_SYSTEM.level)
        assertTrue(SupportLevel.FIRMWARE.isAtLeast(SupportLevel.COMPANION))
        assertFalse(SupportLevel.COMMUNICATION.isAtLeast(SupportLevel.FIRMWARE))
    }

    @Test
    fun levelLookupRejectsOutOfRangeValues() {
        assertEquals(SupportLevel.PERSONALIZATION, SupportLevel.fromLevel(3))
        assertNull(SupportLevel.fromLevel(7))
        assertNull(SupportLevel.fromLevel(-1))
    }

    @Test
    fun statusSeparatesUnofficialFromUnavailable() {
        assertTrue(SupportStatus.EXPERIMENTAL.isUsable)
        assertTrue(SupportStatus.STABLE.isUsable)
        assertFalse(SupportStatus.RESEARCH.isUsable)
        assertFalse(SupportStatus.UNAVAILABLE.isUsable)
        assertFalse(SupportStatus.UNKNOWN.isUsable)
    }

    @Test
    fun deviceProfileDefaultsToUnavailableRatherThanFakingSupport() {
        val profile = DeviceProfile(id = "gloryfit-s10", vendor = null, model = "S10")
        assertEquals(SupportStatus.UNAVAILABLE, profile.statusOf(Capability.WATCHFACE))
        assertEquals(SupportLevel.DETECTED, profile.supportLevel)
        assertEquals(Confidence.UNKNOWN, profile.confidence)
    }

    @Test
    fun withStatusKeepsTheRestOfTheProfile() {
        val profile = DeviceProfile(id = "gloryfit-s10", vendor = "Generic", model = "S10")
        val updated = profile.withStatus(Capability.BATTERY, SupportStatus.EXPERIMENTAL)
        assertEquals(SupportStatus.EXPERIMENTAL, updated.statusOf(Capability.BATTERY))
        assertEquals("S10", updated.model)
    }

    @Test
    fun confidenceParsingIsForgivingButNeverInvents() {
        assertEquals(Confidence.VERIFIED, Confidence.parse(" verified "))
        assertEquals(Confidence.UNKNOWN, Confidence.parse("certain"))
    }

    @Test
    fun protocolKeysAreStable() {
        assertEquals(ProtocolId.GLORYFIT, ProtocolId.fromKey("GloryFit"))
        assertEquals(ProtocolId.UNKNOWN, ProtocolId.fromKey("mystery"))
    }
}
