package com.moonmew.keyboard

/**
 * Chooses readable text for a vertical two-colour theme preview.
 * Uses relative luminance rather than theme indices, so newly added palettes
 * do not inherit an arbitrary foreground colour.
 */
object ThemeContrast {
    private const val DARK_TEXT = 0xFF49334E.toInt()
    private const val LIGHT_TEXT = 0xFFF8F2FF.toInt()

    fun foregroundFor(topColor: Int, bottomColor: Int): Int {
        // A single label is drawn over the entire gradient. Optimizing the
        // average can make it unreadable at one end, so maximize the weaker
        // contrast against the two actual background endpoints instead.
        val darkContrast = minOf(
            contrastRatio(DARK_TEXT, topColor),
            contrastRatio(DARK_TEXT, bottomColor)
        )
        val lightContrast = minOf(
            contrastRatio(LIGHT_TEXT, topColor),
            contrastRatio(LIGHT_TEXT, bottomColor)
        )
        return if (darkContrast >= lightContrast) DARK_TEXT else LIGHT_TEXT
    }

    private fun contrastRatio(foreground: Int, background: Int): Double {
        val foregroundLuminance = relativeLuminance(foreground)
        val backgroundLuminance = relativeLuminance(background)
        val lighter = maxOf(foregroundLuminance, backgroundLuminance)
        val darker = minOf(foregroundLuminance, backgroundLuminance)
        return (lighter + 0.05) / (darker + 0.05)
    }

    private fun relativeLuminance(color: Int): Double {
        fun linear(channel: Int): Double {
            val normalized = channel / 255.0
            return if (normalized <= 0.04045) normalized / 12.92
            else Math.pow((normalized + 0.055) / 1.055, 2.4)
        }
        val red = linear((color shr 16) and 0xFF)
        val green = linear((color shr 8) and 0xFF)
        val blue = linear(color and 0xFF)
        return 0.2126 * red + 0.7152 * green + 0.0722 * blue
    }
}
