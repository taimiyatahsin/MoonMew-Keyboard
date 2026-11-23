package com.moonmew.keyboard

/** UTF-16 length of the complete user-perceived character immediately before the cursor. */
internal object BackspaceDeletion {
    // Bound cursor context while allowing long joined emoji/conjunct graphemes.
    const val MAX_CONTEXT_UTF16_UNITS = 256

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

        // Regional indicators are paired from the start of each consecutive run.
        // If the run has odd length, its final indicator is a standalone grapheme;
        // if even, the final two indicators form the last flag.
        if (isRegionalIndicator(last)) {
            var runStart = start
            while (runStart > 0 && isRegionalIndicator(codePoints[runStart - 1])) runStart--
            val runLength = start - runStart + 1
            if (runLength % 2 == 0 && start > runStart) start--
            return textBeforeCursor.length - offsets[start]
        }

        // Include combining marks, variation selectors, emoji modifiers, and tag characters.
        while (start > 0 && isExtend(codePoints[start])) start--

        // Walk backwards over emoji ZWJ sequences and Indic conjuncts.
        // Indic shaping may place a ZWJ between virama and the next consonant
        // (for example, KA + VIRAMA + ZWJ + SSA), so handle that link together.
        while (start > 0) {
            when {
                codePoints[start - 1] == ZERO_WIDTH_JOINER -> {
                    val hasViramaBeforeJoiner = start > 1 && isVirama(codePoints[start - 2])
                    start = if (hasViramaBeforeJoiner) start - 2 else (start - 2).coerceAtLeast(0)
                    if (hasViramaBeforeJoiner && start > 0) start--
                    while (start > 0 && isExtend(codePoints[start])) start--
                }
                isVirama(codePoints[start - 1]) -> {
                    start--
                    if (start > 0) start--
                    while (start > 0 && isExtend(codePoints[start])) start--
                }
                else -> break
            }
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

    private fun isVirama(codePoint: Int) = codePoint in VIRAMA_CODE_POINTS

    // Reuse the immutable lookup set on every backspace instead of allocating
    // a new set for each grapheme-boundary check.
    private val VIRAMA_CODE_POINTS = setOf(        0x094D, 0x09CD, 0x0A4D, 0x0ACD, 0x0B4D, 0x0BCD,
        0x0C4D, 0x0CCD, 0x0D3B, 0x0D3C, 0x0D4D, 0x0DCA,
        0x1039, 0x103A, 0x1714, 0x1734, 0x17D2, 0x1A60,
        0x1B44, 0x1BAA, 0x1BAB, 0xA806, 0xA8C4, 0xA953,
        0xA9C0, 0xAAF6, 0xABED, 0x10A3F, 0x11046, 0x11070,
        0x11133, 0x111C0, 0x11235, 0x112EA, 0x1134D, 0x11442,
        0x114C2, 0x115BF, 0x1163F, 0x116B6, 0x1172B, 0x11839,
        0x1193D, 0x119E0, 0x11A34, 0x11A47, 0x11A99, 0x11C3F,
        0x11D44, 0x11D45, 0x11D97
    )

    private const val ZERO_WIDTH_JOINER = 0x200D
}
