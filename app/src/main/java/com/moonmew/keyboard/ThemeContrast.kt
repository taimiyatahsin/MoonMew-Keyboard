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
        val luminance = (relativeLuminance(topColor) + relativeLuminance(bottomColor)) / 2.0
        val darkLuminance = relativeLuminance(DARK_TEXT)
        val darkContrast = (luminance + 0.05) / (darkLuminance + 0.05)
        val lightContrast = 1.05 / (luminance + 0.05)
        return if (darkContrast >= lightContrast) DARK_TEXT else LIGHT_TEXT
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
