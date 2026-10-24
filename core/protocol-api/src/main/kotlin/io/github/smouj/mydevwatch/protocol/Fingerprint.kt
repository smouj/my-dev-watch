package io.github.smouj.mydevwatch.protocol

import io.github.smouj.mydevwatch.model.ProtocolId

private const val BASE_UUID_SUFFIX = "-0000-1000-8000-00805f9b34fb"

/**
 * Normalises a 16-bit or 128-bit GATT UUID to its short form ("55ff").
 *
 * Vendor services are defined as 0000xxxx-0000-1000-8000-00805f9b34fb, but Android
 * and iOS disagree about how they print them, so fingerprints compare short forms.
 */
fun normalizeUuid(raw: String): String {
    val value = raw.trim().lowercase().removePrefix("0x")
    return when {
        value.length == 4 -> value
        value.endsWith(BASE_UUID_SUFFIX) -> value.substring(4, 8)
        value.length == 8 -> value.takeLast(4)
        else -> value
    }
}

/** What we can see about a watch before any vendor command is sent. */
data class DeviceFingerprint(
    val name: String? = null,
    val advertisedServices: Set<String> = emptySet(),
    val gattServices: Set<String> = emptySet(),
    val manufacturerData: List<ByteArray> = emptyList(),
    val firmwareVersion: String? = null,
    val mtu: Int? = null
) {
    private val advertised = advertisedServices.map(::normalizeUuid).toSet()
    private val exposed = gattServices.map(::normalizeUuid).toSet()

    fun advertises(service: String): Boolean = normalizeUuid(service) in advertised
    fun exposes(service: String): Boolean = normalizeUuid(service) in exposed
}

/** One heuristic signal. Weights are explicit so scores can be explained. */
sealed interface MatchRule {
    val weight: Double
    val label: String
    fun test(fingerprint: DeviceFingerprint): Boolean

    data class NameMatches(
        val pattern: Regex,
        override val weight: Double,
        override val label: String
    ) : MatchRule {
        override fun test(fingerprint: DeviceFingerprint): Boolean =
            fingerprint.name?.let { pattern.containsMatchIn(it) } == true
    }

    data class Advertises(
        val service: String,
        override val weight: Double,
        override val label: String = "advertises " + service
    ) : MatchRule {
        override fun test(fingerprint: DeviceFingerprint): Boolean = fingerprint.advertises(service)
    }

    data class Exposes(
        val service: String,
        override val weight: Double,
        override val label: String = "exposes " + service
    ) : MatchRule {
        override fun test(fingerprint: DeviceFingerprint): Boolean = fingerprint.exposes(service)
    }
}

data class ProtocolDescriptor(
    val id: ProtocolId,
    val displayName: String,
    val rules: List<MatchRule>
) {
    init {
        require(rules.isNotEmpty()) { "A protocol descriptor needs at least one rule" }
        require(rules.all { it.weight > 0 }) { "Rule weights must be positive" }
    }

    val totalWeight: Double get() = rules.sumOf { it.weight }
}

/** A scored hypothesis, never a certainty. */
data class Identification(
    val protocol: ProtocolId,
    val confidence: Double,
    val matched: List<String>,
    val alternatives: List<Pair<ProtocolId, Double>> = emptyList()
) {
    val isConclusive: Boolean get() = protocol != ProtocolId.UNKNOWN && confidence >= 0.60
}

/**
 * Weighted, explainable protocol identification.
 *
 * A device named "S10" proves nothing: the same retail name has shipped with at least
 * four different firmwares. Only observed GATT structure and advertised services are
 * treated as evidence, and the reasons are kept for the diagnostics screen.
 */
class ProtocolRegistry(descriptors: List<ProtocolDescriptor> = defaultDescriptors()) {
    private val descriptors = descriptors.toList()

    fun candidates(fingerprint: DeviceFingerprint): List<Identification> =
        this.descriptors.map { descriptor ->
            val matched = descriptor.rules.filter { it.test(fingerprint) }
            val score = matched.sumOf { it.weight } / descriptor.totalWeight
            Identification(
                protocol = descriptor.id,
                confidence = (score * 1000).toInt() / 1000.0,
                matched = matched.map { it.label }
            )
        }.sortedWith(compareByDescending<Identification> { it.confidence }.thenBy { it.protocol.ordinal })

    fun identify(fingerprint: DeviceFingerprint): Identification {
        val ranked = candidates(fingerprint)
        val best = ranked.firstOrNull() ?: return Identification(ProtocolId.UNKNOWN, 0.0, emptyList())
        if (best.confidence < 0.60) {
            return Identification(
                protocol = ProtocolId.UNKNOWN,
                confidence = best.confidence,
                matched = best.matched,
                alternatives = ranked.take(3).map { it.protocol to it.confidence }
            )
        }
        return best.copy(alternatives = ranked.drop(1).take(3).map { it.protocol to it.confidence })
    }

    companion object {
        /**
         * Only documented signals are allowed here. Adding a vendor name by guesswork
         * would make every future identification untrustworthy.
         */
        fun defaultDescriptors(): List<ProtocolDescriptor> = listOf(
            ProtocolDescriptor(
                id = ProtocolId.GLORYFIT,
                displayName = "GloryFit",
                rules = listOf(
                    MatchRule.Exposes("55ff", weight = 3.0, label = "primary service 55FF"),
                    MatchRule.Exposes("56ff", weight = 2.0, label = "secondary service 56FF"),
                    MatchRule.Advertises("55ff", weight = 1.0, label = "advertises 55FF"),
                    MatchRule.NameMatches(
                        pattern = Regex("(?i)glory\s?fit|\bute\b"),
                        weight = 1.0,
                        label = "name mentions GloryFit"
                    )
                )
            ),
            ProtocolDescriptor(
                id = ProtocolId.GLORYFIT_PRO,
                displayName = "GloryFit Pro",
                rules = listOf(
                    MatchRule.NameMatches(
                        pattern = Regex("(?i)glory\s?fit\s?pro"),
                        weight = 1.0,
                        label = "name mentions GloryFit Pro"
                    )
                )
            )
        )
    }
}
