package com.moonmew.keyboard

/**
 * Offline Bangla phonetic converter.
 * This lightweight rule-based starter is not an Avro-compatible engine.
 */
object BanglaTransliterator {
    private val consonants = linkedMapOf(
        "ksh" to "ক্ষ", "kkh" to "ক্ষ", "chh" to "ছ", "kh" to "খ", "gh" to "ঘ",
        "ch" to "চ", "jh" to "ঝ", "th" to "থ", "dh" to "ধ",
        "ph" to "ফ", "bh" to "ভ", "sh" to "শ", "ng" to "ং", "ny" to "ঞ",
        "k" to "ক", "g" to "গ", "c" to "ক", "j" to "জ", "t" to "ত",
        "d" to "দ", "n" to "ন", "p" to "প", "f" to "ফ", "b" to "ব",
        "m" to "ম", "r" to "র", "l" to "ল", "s" to "স", "h" to "হ",
        "y" to "য়", "w" to "ও", "v" to "ভ", "z" to "য", "q" to "ক", "x" to "ক্স"
    )
    private val vowelSigns = linkedMapOf(
        "aa" to "া", "ii" to "ী", "ee" to "ী", "uu" to "ূ", "oo" to "ূ",
        "oi" to "ৈ", "ou" to "ৌ", "a" to "া", "i" to "ি", "e" to "ে",
        "u" to "ু", "o" to "ো"
    )
    private val independentVowels = linkedMapOf(
        "aa" to "আ", "ii" to "ঈ", "ee" to "ঈ", "uu" to "ঊ", "oo" to "ঊ",
        "oi" to "ঐ", "ou" to "ঔ", "a" to "আ", "i" to "ই", "e" to "এ",
        "u" to "উ", "o" to "ও"
    )

    fun convert(input: String): String {
        if (input.isEmpty()) return ""
        val lower = input.lowercase()
        BanglaWordSuggestions.exact(lower)?.let { return it }
        val out = StringBuilder()
        var index = 0
        var previousWasConsonant = false
        while (index < lower.length) {
            val remaining = lower.substring(index)
            val consonant = consonants.keys.firstOrNull { remaining.startsWith(it) }
            if (consonant != null) {
                out.append(consonants.getValue(consonant))
                index += consonant.length
                previousWasConsonant = true
                val vowel = vowelSigns.keys.firstOrNull { lower.substring(index).startsWith(it) }
                if (vowel != null) {
                    out.append(vowelSigns.getValue(vowel))
                    index += vowel.length
                    previousWasConsonant = false
                }
                continue
            }
            val vowelMap = if (previousWasConsonant) vowelSigns else independentVowels
            val vowel = vowelMap.keys.firstOrNull { remaining.startsWith(it) }
            if (vowel != null) {
                out.append(vowelMap.getValue(vowel))
                index += vowel.length
                previousWasConsonant = false
                continue
            }
            val ch = lower[index]
            if (ch == ' ') out.append(' ') else out.append(ch)
            previousWasConsonant = false
            index++
        }
        return out.toString()
    }
}
