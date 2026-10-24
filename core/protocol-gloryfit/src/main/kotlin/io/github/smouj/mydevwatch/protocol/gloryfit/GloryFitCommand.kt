package io.github.smouj.mydevwatch.protocol.gloryfit

import io.github.smouj.mydevwatch.model.Confidence

/**
 * The command catalogue.
 *
 * This table is documentation that happens to be executable. [opcode] is what the
 * public research reports, [confidence] is how strongly it is corroborated, and
 * [implemented] says whether My Dev Watch can actually send it. A command can be
 * catalogued for years without being implemented, and that gap is intentional:
 * sending bytes to unknown firmware is how watches get bricked.
 */
enum class GloryFitCommand(
    val opcode: Int,
    val title: String,
    val direction: Direction,
    val confidence: Confidence,
    val implemented: Boolean,
    val evidence: String
) {
    BATTERY(
        opcode = 0xA2,
        title = "Battery level",
        direction = Direction.REQUEST_RESPONSE,
        confidence = Confidence.PROBABLE,
        implemented = true,
        evidence = "0xA2 request, answer 0xA2 + percentage byte; observed at 0x56 = 86%."
    ),
    LIVE_HEART_RATE(
        opcode = 0xE5,
        title = "Live heart rate",
        direction = Direction.REQUEST_RESPONSE,
        confidence = Confidence.OBSERVED,
        implemented = false,
        evidence = "Reported as live heart-rate request in air5 and RyzeWaveWatch notes."
    ),
    HEART_RATE_HISTORY(
        opcode = 0xF7,
        title = "Heart-rate history",
        direction = Direction.REQUEST_RESPONSE,
        confidence = Confidence.OBSERVED,
        implemented = false,
        evidence = "Reported as historical heart-rate dump; record layout not yet mapped."
    ),
    SPO2(
        opcode = 0x34,
        title = "Blood oxygen",
        direction = Direction.REQUEST_RESPONSE,
        confidence = Confidence.OBSERVED,
        implemented = false,
        evidence = "Documented as 34 FA; whether FA is a sub-command or payload is unresolved."
    ),
    SLEEP_0X32(
        opcode = 0x32,
        title = "Sleep history (variant A)",
        direction = Direction.REQUEST_RESPONSE,
        confidence = Confidence.OBSERVED,
        implemented = false,
        evidence = "Sleep data reported under opcode 0x32 in one family."
    ),
    SLEEP_0XCB(
        opcode = 0xCB,
        title = "Sleep history (variant B)",
        direction = Direction.REQUEST_RESPONSE,
        confidence = Confidence.OBSERVED,
        implemented = false,
        evidence = "Sleep data reported under opcode 0xCB in another family; not interchangeable."
    ),
    NOTIFICATION(
        opcode = 0x12,
        title = "Phone notification",
        direction = Direction.PUSH,
        confidence = Confidence.OBSERVED,
        implemented = false,
        evidence = "Notification forwarding command; payload encoding still under research."
    ),
    VISIBLE_PAGES(
        opcode = 0xF9,
        title = "Visible watch pages",
        direction = Direction.PUSH,
        confidence = Confidence.PROBABLE,
        implemented = false,
        evidence = "Configures which built-in pages the watch shows; does not add new apps."
    ),
    WATCHFACE_TRANSFER(
        opcode = 0x6001,
        title = "Watch-face transfer",
        direction = Direction.PUSH,
        confidence = Confidence.OBSERVED,
        implemented = false,
        evidence = "Upload channel on service 60FF; chunking rules not yet verified."
    ),
    WATCHFACE_ACK(
        opcode = 0x6002,
        title = "Watch-face acknowledgement",
        direction = Direction.NOTIFY,
        confidence = Confidence.OBSERVED,
        implemented = false,
        evidence = "Completion/acknowledgement notification for the 6001 upload."
    );

    val opcodeByte: Byte get() = opcode.toByte()

    enum class Direction { REQUEST_RESPONSE, PUSH, NOTIFY }

    companion object {
        fun byOpcode(opcode: Int): GloryFitCommand? = entries.firstOrNull { it.opcode == opcode }

        val implementedCommands: List<GloryFitCommand> get() = entries.filter { it.implemented }
    }
}
