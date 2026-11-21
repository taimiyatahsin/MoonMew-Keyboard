package com.moonmew.keyboard

/** UTF-16 length of the complete user-perceived character immediately before the cursor. */
internal object BackspaceDeletion {
    fun utf16UnitsBeforeCursor(textBeforeCursor: CharSequence?): Int {
        if (textBeforeCursor.isNullOrEmpty()) return 1

        val codePoints = mutableListOf<Int>()
        val offsets = mutableListOf<Int>()
        var offset = 0
        while (offset < textBeforeCursor.length) {
            offsets.add(offset)
            val codePoint = Character.codePointAt(textBeforeCursor, offset)
            codePoints.add(codePoint)
            offset += Character.charCount(codePoint)
        }
        if (codePoints.isEmpty()) return 1

        var start = codePoints.lastIndex
        val last = codePoints[start]

        // A flag is a pair of regional-indicator code points.
        if (isRegionalIndicator(last) && start > 0 && isRegionalIndicator(codePoints[start - 1])) {
            start--
            return textBeforeCursor.length - offsets[start]
        }

        // Include combining marks, variation selectors, emoji modifiers, and tag characters.
        while (start > 0 && isExtend(codePoints[start])) start--

        // Walk backwards over joined emoji sequences (for example, family emoji).
        while (start > 0 && codePoints[start - 1] == ZERO_WIDTH_JOINER) {
            start = (start - 2).coerceAtLeast(0)
            while (start > 0 && isExtend(codePoints[start])) start--
        }

        // A malformed/trailing joiner should be removed with the preceding character too.
        if (codePoints[start] == ZERO_WIDTH_JOINER && start > 0) start--

        return textBeforeCursor.length - offsets[start]
    }

    private fun isExtend(codePoint: Int): Boolean {
        val type = Character.getType(codePoint)
        return type == Character.NON_SPACING_MARK.toInt() ||
            type == Character.COMBINING_SPACING_MARK.toInt() ||
            type == Character.ENCLOSING_MARK.toInt() ||
            codePoint in 0xFE00..0xFE0F ||
            codePoint in 0xE0100..0xE01EF ||
            codePoint in 0x1F3FB..0x1F3FF ||
            codePoint in 0xE0020..0xE007F ||
            codePoint == 0x20E3
    }

    private fun isRegionalIndicator(codePoint: Int) = codePoint in 0x1F1E6..0x1F1FF

    private const val ZERO_WIDTH_JOINER = 0x200D
}
