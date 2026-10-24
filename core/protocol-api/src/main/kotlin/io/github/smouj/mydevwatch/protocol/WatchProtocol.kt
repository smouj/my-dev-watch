package io.github.smouj.mydevwatch.protocol

import io.github.smouj.mydevwatch.model.Capability
import io.github.smouj.mydevwatch.model.ProtocolId

/**
 * Contract every device family implements.
 *
 * Anything a family cannot do returns [ProtocolResult.Unsupported] with the exact
 * capability, so the interface can hide a feature for the right reason.
 */
interface WatchProtocol {
    val id: ProtocolId
    val capabilities: Set<Capability>

    suspend fun initialize(transport: WatchTransport): ProtocolResult<Unit>
    suspend fun readBattery(): ProtocolResult<Int>
    suspend fun syncClock(): ProtocolResult<Unit>
    suspend fun readDeviceInfo(): ProtocolResult<Map<String, String>>

    suspend fun syncActivity(): ProtocolResult<Unit> = unsupported(Capability.STEPS)
    suspend fun syncHealth(): ProtocolResult<Unit> = unsupported(Capability.HEART_RATE)
    suspend fun syncSleep(): ProtocolResult<Unit> = unsupported(Capability.SLEEP)
    suspend fun configureNotifications(enabled: Boolean): ProtocolResult<Unit> =
        unsupported(Capability.NOTIFICATIONS)

    suspend fun disconnect()

    private fun unsupported(capability: Capability): ProtocolResult<Nothing> =
        ProtocolResult.Unsupported(capability)
}
