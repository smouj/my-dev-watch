package io.github.smouj.mydevwatch.model

/**
 * Everything the app knows about a watch model, with evidence attached.
 *
 * [supportLevel] is the highest level proven for this profile; individual features
 * are reported through [statuses] so the UI can show a matrix instead of a badge.
 */
data class DeviceProfile(
    val id: String,
    val vendor: String?,
    val model: String,
    val aliases: Set<String> = emptySet(),
    val protocol: ProtocolId = ProtocolId.UNKNOWN,
    val display: DisplayProfile? = null,
    val capabilities: Set<Capability> = emptySet(),
    val quirks: Set<DeviceQuirk> = emptySet(),
    val statuses: Map<Capability, SupportStatus> = emptyMap(),
    val supportLevel: SupportLevel = SupportLevel.DETECTED,
    val confidence: Confidence = Confidence.UNKNOWN,
    val evidence: List<Evidence> = emptyList()
) {
    fun statusOf(capability: Capability): SupportStatus =
        statuses[capability] ?: if (capability in capabilities) SupportStatus.EXPERIMENTAL else SupportStatus.UNAVAILABLE

    fun withStatus(capability: Capability, status: SupportStatus): DeviceProfile =
        copy(statuses = statuses + (capability to status))
}

/** A single durable record of where a fact came from. */
data class Evidence(
    val kind: Kind,
    val reference: String,
    val note: String = "",
    val recordedAt: String? = null
) {
    enum class Kind { CAPTURE, FIRMWARE, VENDOR_DOC, FIELD_TEST, PUBLIC_RESEARCH }
}

/**
 * Hardware and firmware behaviours that cannot be expressed as a capability.
 * Quirks are data, never scattered "if model == ..." branches.
 */
enum class DeviceQuirk(val description: String) {
    BATTERY_NEEDS_POLLING("Battery is not pushed by the watch; it must be requested."),
    REQUIRES_DELAY_AFTER_CONNECT("Some commands fail if sent immediately after discovery."),
    BROKEN_MTU_NEGOTIATION("MTU negotiation reports success but payloads must stay at 20 bytes."),
    ALARM_LIMIT_3("Firmware accepts at most three alarms."),
    SECONDARY_BLE_SERVICE("Health and configuration live on the secondary 56FF service."),
    SPO2_HISTORY_UNRELIABLE("SpO2 history is incomplete on some firmware builds."),
    CALL_AUDIO_CLASSIC_BT("Calls use Bluetooth Classic, not the LE link."),
    MONOLITHIC_FIRMWARE("No separate theme or menu partition is exposed over BLE.")
}
