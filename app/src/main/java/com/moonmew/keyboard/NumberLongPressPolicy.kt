package com.moonmew.keyboard

/**
 * Long-press alternatives for the dedicated number row.
 * Fractions are intentionally included alongside superscript digits because
 * they are frequently used in short messages and schoolwork.
 */
object NumberLongPressPolicy {
    fun alternateFor(digit: Char): String? = when (digit) {
        '0' -> "⁰"
        '1' -> "¹"
        '2' -> "²"
        '3' -> "⅔"
        '4' -> "¾"
        '5' -> "½"
        '6' -> "⅚"
        '7' -> "⅞"
        '8' -> "⅜"
        '9' -> "⅝"
        else -> null
    }
}
