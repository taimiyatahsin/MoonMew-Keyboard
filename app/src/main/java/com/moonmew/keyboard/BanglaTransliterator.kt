package com.moonmew.keyboard

/**
 * Small deterministic offline starter mapping. It is not an Avro-compatible
 * transliteration engine; spelling and conjunct handling are intentionally limited.
 */
object BanglaTransliterator {
    private val map = linkedMapOf(
        "kh" to "খ", "gh" to "ঘ", "ch" to "চ", "jh" to "ঝ", "th" to "থ",
        "dh" to "ধ", "ph" to "ফ", "bh" to "ভ", "sh" to "শ", "ng" to "ং",
        "a" to "আ", "b" to "ব", "c" to "ক", "d" to "দ", "e" to "এ",
        "f" to "ফ", "g" to "গ", "h" to "হ", "i" to "ই", "j" to "জ",
        "k" to "ক", "l" to "ল", "m" to "ম", "n" to "ন", "o" to "ও",
        "p" to "প", "q" to "ক", "r" to "র", "s" to "স", "t" to "ত",
        "u" to "উ", "v" to "ভ", "w" to "ও", "x" to "ক্স", "y" to "য়", "z" to "য"
    )
    fun convert(input: String): String {
        val out = StringBuilder()
        var i = 0
        while (i < input.length) {
            val pair = if (i + 1 < input.length) input.substring(i, i + 2) else ""
            val paired = map[pair]
            if (paired != null) { out.append(paired); i += 2 }
            else { out.append(map[input.substring(i, i + 1)] ?: input[i]); i++ }
        }
        return out.toString()
    }
}
