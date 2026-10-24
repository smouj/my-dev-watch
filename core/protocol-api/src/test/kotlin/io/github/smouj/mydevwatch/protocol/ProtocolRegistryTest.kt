package io.github.smouj.mydevwatch.protocol

import io.github.smouj.mydevwatch.model.ProtocolId
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProtocolRegistryTest {

    private val registry = ProtocolRegistry()

    @Test
    fun shortAndFullUuidsNormaliseToTheSameToken() {
        assertEquals("55ff", normalizeUuid("55FF"))
        assertEquals("55ff", normalizeUuid("000055ff-0000-1000-8000-00805f9b34fb"))
        assertEquals("2a19", normalizeUuid("00002A19-0000-1000-8000-00805F9B34FB"))
    }

    @Test
    fun gloryFitIsIdentifiedFromItsServicesEvenWithAnOpaqueName() {
        val fingerprint = DeviceFingerprint(
            name = "S10",
            gattServices = setOf(
                "000055ff-0000-1000-8000-00805f9b34fb",
                "000056ff-0000-1000-8000-00805f9b34fb"
            )
        )
        val result = registry.identify(fingerprint)
        assertEquals(ProtocolId.GLORYFIT, result.protocol)
        assertTrue(result.confidence >= 0.9)
        assertTrue(result.matched.any { it.contains("55FF") })
        assertTrue(result.isConclusive)
    }

    @Test
    fun aRetailNameAloneIsNotEvidence() {
        val result = registry.identify(DeviceFingerprint(name = "S10"))
        assertEquals(ProtocolId.UNKNOWN, result.protocol)
        assertFalse(result.isConclusive)
    }

    @Test
    fun unrelatedDevicesStayUnknown() {
        val result = registry.identify(
            DeviceFingerprint(
                name = "Mi Band 8",
                gattServices = setOf("0000180d-0000-1000-8000-00805f9b34fb")
            )
        )
        assertEquals(ProtocolId.UNKNOWN, result.protocol)
        assertEquals(0.0, result.confidence)
    }

    @Test
    fun partialEvidenceIsScoredButNotPromoted() {
        val fingerprint = DeviceFingerprint(name = "Ute Watch")
        val candidates = registry.candidates(fingerprint)
        val gloryFit = candidates.first { it.protocol == ProtocolId.GLORYFIT }
        assertTrue(gloryFit.confidence > 0.0)
        assertTrue(gloryFit.confidence < 0.6)
        assertEquals(ProtocolId.UNKNOWN, registry.identify(fingerprint).protocol)
    }

    @Test
    fun descriptorRejectsEmptyRules() {
        val failure = runCatching {
            ProtocolDescriptor(ProtocolId.MOYOUNG, "MoYoung", emptyList())
        }
        assertTrue(failure.isFailure)
    }
}
