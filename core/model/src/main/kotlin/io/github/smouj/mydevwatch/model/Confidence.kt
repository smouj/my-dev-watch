package io.github.smouj.mydevwatch.model

/**
 * Evidence level attached to every protocol fact and device attribute.
 *
 * The project rule is simple: nothing enters the codebase without a level, and
 * nothing is presented to the user as stable while its level is below [VERIFIED].
 */
enum class Confidence {
    /** Nobody has looked. Treat as absent. */
    UNKNOWN,

    /** Seen in a capture or a vendor document, not reproduced. */
    OBSERVED,

    /** Reproduced, but not enough samples to rule out firmware or model variants. */
    PROBABLE,

    /** Reproduced on real hardware, with the device and firmware recorded. */
    VERIFIED;

    fun atLeast(other: Confidence): Boolean = ordinal >= other.ordinal

    companion object {
        fun parse(raw: String): Confidence =
            entries.firstOrNull { it.name.equals(raw.trim(), ignoreCase = true) } ?: UNKNOWN
    }
}
