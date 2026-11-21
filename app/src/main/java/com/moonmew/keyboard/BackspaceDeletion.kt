package com.moonmew.keyboard

/** Number of UTF-16 code units occupied by the code point before the cursor. */
internal object BackspaceDeletion {
    fun utf16UnitsBeforeCursor(textBeforeCursor: CharSequence?): Int {
        if (textBeforeCursor.isNullOrEmpty()) return 1
        val last = textBeforeCursor[textBeforeCursor.length - 1]
        if (Character.isLowSurrogate(last) && textBeforeCursor.length >= 2 &&
            Character.isHighSurrogate(textBeforeCursor[textBeforeCursor.length - 2])) return 2
        return 1
    }
}
