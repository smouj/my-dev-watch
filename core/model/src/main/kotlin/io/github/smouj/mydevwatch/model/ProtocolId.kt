package io.github.smouj.mydevwatch.model

/**
 * Stable protocol identifiers. These values are persisted in device profiles and
 * capture files, so they must never be renamed without a migration.
 */
enum class ProtocolId(val key: String, val displayName: String) {
    GLORYFIT("gloryfit", "GloryFit"),
    GLORYFIT_PRO("gloryfit-pro", "GloryFit Pro"),
    MOYOUNG("moyoung", "MoYoung"),
    DA_FIT("da-fit", "Da Fit"),
    UNKNOWN("unknown", "Unknown");

    companion object {
        fun fromKey(key: String): ProtocolId =
            entries.firstOrNull { it.key.equals(key.trim(), ignoreCase = true) } ?: UNKNOWN
    }
}
