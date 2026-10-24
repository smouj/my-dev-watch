package io.github.smouj.mydevwatch.model

/** Physical pixel layout of a watch display. */
data class DisplayProfile(
    val width: Int,
    val height: Int,
    val shape: Shape = Shape.RECTANGULAR,
    val colorFormat: ColorFormat = ColorFormat.UNKNOWN,
    val touch: Boolean = false,
    val confidence: Confidence = Confidence.UNKNOWN
) {
    init {
        require(width > 0 && height > 0) { "Display dimensions must be positive" }
    }

    val aspectRatio: Float get() = width.toFloat() / height.toFloat()

    enum class Shape { CIRCULAR, RECTANGULAR, SQUARE }

    enum class ColorFormat { RGB565, RGB888, ARGB8888, INDEXED, MONOCHROME, UNKNOWN }
}
