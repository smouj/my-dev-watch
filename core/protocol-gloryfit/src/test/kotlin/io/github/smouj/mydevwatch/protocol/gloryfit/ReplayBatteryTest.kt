package io.github.smouj.mydevwatch.protocol.gloryfit

import io.github.smouj.mydevwatch.capture.CaptureCodec
import io.github.smouj.mydevwatch.capture.CaptureParse
import io.github.smouj.mydevwatch.capture.ReplayTransport
import io.github.smouj.mydevwatch.protocol.ProtocolResult
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ReplayBatteryTest {

    private val session = """
        MDCAP/1
        device: S10
        protocol: gloryfit
        created: 2026-10-24T09:00:00Z
        note: battery baseline
        --
        0 TX 33f1 A2
        140 RX 33f2 A2 56
    """.trimIndent()

    @Test
    fun aRecordedSessionDrivesTheRealProtocolStack() = runBlocking {
        val parsed = CaptureCodec.parse(session)
        assertIs<CaptureParse.Success>(parsed, "parse returned " + parsed)
        val transport = ReplayTransport(parsed.file.packets)
        val protocol = GloryFitProtocol(transport)

        assertIs<ProtocolResult.Success<Unit>>(protocol.initialize(transport))
        val battery = protocol.readBattery()
        assertIs<ProtocolResult.Success<Int>>(battery, "battery returned " + battery)
        assertEquals(86, battery.value)
    }
}
