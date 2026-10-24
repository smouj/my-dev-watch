package io.github.smouj.mydevwatch.firmware

import java.security.MessageDigest

/**
 * Read-only firmware inspection.
 *
 * This module never writes to a device. It answers the questions that must be
 * answered *before* anyone considers touching firmware: what is this file, how
 * random does it look, what strings does it contain, and does it start with
 * something we recognise?
 */
data class FirmwareReport(
    val sha256: String,
    val size: Long,
    val entropy: Double,
    val signatures: List<String>,
    val strings: List<String>,
    val stringCount: Int,
    val looksCompressed: Boolean,
    val printableRatio: Double
)

object FirmwareAnalyzer {

    private val SIGNATURES: List<Pair<ByteArray, String>> = listOf(
        byteArrayOf(0x7F, 0x45, 0x4C, 0x46) to "ELF executable",
        byteArrayOf(0x1F, 0x8B.toByte()) to "gzip stream",
        byteArrayOf(0x78, 0x9C.toByte()) to "zlib stream (default)",
        byteArrayOf(0x78, 0x01) to "zlib stream (fast)",
        byteArrayOf(0x78, 0xDA.toByte()) to "zlib stream (best)",
        byteArrayOf(0x04, 0x22, 0x4D, 0x18) to "LZ4 frame",
        byteArrayOf(0x50, 0x4B, 0x03, 0x04) to "ZIP archive",
        byteArrayOf(0x27, 0x05, 0x19, 0x56) to "U-Boot uImage"
    )

    fun analyze(bytes: ByteArray, maxStrings: Int = 40): FirmwareReport {
        val strings = extractStrings(bytes, maxStrings)
        val printable = countPrintable(bytes)
        return FirmwareReport(
            sha256 = sha256(bytes),
            size = bytes.size.toLong(),
            entropy = entropy(bytes),
            signatures = detectSignatures(bytes),
            strings = strings,
            stringCount = strings.size,
            looksCompressed = entropy(bytes) >= 7.2,
            printableRatio = if (bytes.isEmpty()) 0.0 else printable.toDouble() / bytes.size
        )
    }

    fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    /** Shannon entropy in bits per byte, 0.0 to 8.0. */
    fun entropy(bytes: ByteArray): Double {
        if (bytes.isEmpty()) return 0.0
        val counts = IntArray(256)
        bytes.forEach { counts[it.toInt() and 0xFF]++ }
        var result = 0.0
        val total = bytes.size.toDouble()
        counts.forEach { count ->
            if (count > 0) {
                val probability = count / total
                result -= probability * (kotlin.math.ln(probability) / kotlin.math.ln(2.0))
            }
        }
        return (result * 1000).toInt() / 1000.0
    }

    fun detectSignatures(bytes: ByteArray): List<String> = SIGNATURES.mapNotNull { (magic, name) ->
        if (bytes.size >= magic.size && magic.indices.all { bytes[it] == magic[it] }) name else null
    }

    /** Printable ASCII runs of [minimum] characters or more. */
    fun extractStrings(bytes: ByteArray, limit: Int = 40, minimum: Int = 6): List<String> {
        val found = mutableListOf<String>()
        val current = StringBuilder()
        bytes.forEach { raw ->
            val value = raw.toInt() and 0xFF
            if (value in 0x20..0x7E) {
                current.append(value.toChar())
            } else {
                if (current.length >= minimum && found.size < limit) found += current.toString()
                current.setLength(0)
            }
        }
        if (current.length >= minimum && found.size < limit) found += current.toString()
        return found
    }

    private fun countPrintable(bytes: ByteArray): Int =
        bytes.count { val value = it.toInt() and 0xFF; value in 0x20..0x7E }
}
