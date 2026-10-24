package io.github.smouj.mydevwatch.protocol.gloryfit

/** Decodes the battery answer: opcode 0xA2 followed by a percentage. */
object GloryFitBattery {
    /**
     * Returns null on a short, foreign or out-of-range payload instead of guessing.
     * A wrong battery number is worse than no battery number.
     */
    fun decode(payload: ByteArray): Int? {
        if (payload.size < 2) return null
        if (payload[0].toInt() and 0xFF != GloryFitCommand.BATTERY.opcode) return null
        val percent = payload[1].toInt() and 0xFF
        return if (percent in 0..100) percent else null
    }

    fun request(): ByteArray = byteArrayOf(GloryFitCommand.BATTERY.opcodeByte)
}
