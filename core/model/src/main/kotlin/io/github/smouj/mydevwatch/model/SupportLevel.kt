package io.github.smouj.mydevwatch.model

/**
 * Compatibility is never a boolean.
 *
 * Every device moves through levels of support, and a device may sit at level 2 for
 * one year while level 5 is still being researched. Reporting a single "compatible"
 * flag hides exactly the information a user needs before trusting the app with a watch.
 */
enum class SupportLevel(val level: Int, val label: String, val description: String) {
    DETECTED(0, "Detected", "Bluetooth advertisement seen; protocol unknown."),
    COMMUNICATION(1, "Communication", "GATT services reachable, basic reads and writes succeed."),
    COMPANION(2, "Companion", "Battery, clock, activity, health, sleep and settings work."),
    PERSONALIZATION(3, "Personalization", "Watch faces and visual resources can be replaced."),
    INTERFACE(4, "Interface", "Icons, menus and themes can be modified."),
    FIRMWARE(5, "Firmware", "Firmware can be backed up, verified, installed and recovered."),
    OPEN_SYSTEM(6, "Open system", "An alternative open firmware runs on documented hardware.");

    fun isAtLeast(other: SupportLevel): Boolean = level >= other.level

    companion object {
        fun fromLevel(level: Int): SupportLevel? = entries.firstOrNull { it.level == level }
    }
}

/**
 * How a single feature behaves on a device right now.
 *
 * UNAVAILABLE means "the hardware or protocol does not expose it".
 * UNKNOWN means "we have not verified it", which is deliberately different:
 * one is a fact about the watch, the other is a fact about our knowledge.
 */
enum class SupportStatus(val label: String, val symbol: String) {
    STABLE("Stable", "OK"),
    EXPERIMENTAL("Experimental", "EXP"),
    RESEARCH("Research", "RES"),
    UNAVAILABLE("Not available", "--"),
    UNKNOWN("Unverified", "?");

    val isUsable: Boolean get() = this == STABLE || this == EXPERIMENTAL
}
