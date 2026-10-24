package io.github.smouj.mydevwatch.capture

/**
 * Hex parsing shared by the CLI, the capture format and the diagnostics screen.
 * Accepts "A2 56", "a2:56", "0xA2 0x56" and newlines; rejects anything ambiguous.
 */
object HexFormat {
    fun encode(bytes: ByteArray, separator: String = " "): String =
        bytes.joinToString(separator) { byte -> "%02X".format(byte.toInt() and 0xFF) }

    fun decode(input: String): ByteArray {
        val cleaned = input
            .replace("0x", " ", ignoreCase = true)
            .replace(Regex("[\\s:,-]+"), "")
        require(cleaned.isNotEmpty()) { "Empty hex input" }
        require(cleaned.length % 2 == 0) { "Hex input must have an even number of digits" }
        require(cleaned.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) { "Hex input contains invalid characters" }
        return ByteArray(cleaned.length / 2) { index ->
            cleaned.substring(index * 2, index * 2 + 2).toInt(16).toByte()
        }
    }
}
