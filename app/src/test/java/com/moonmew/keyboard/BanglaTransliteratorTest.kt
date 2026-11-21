package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BanglaTransliteratorTest {
    @Test fun commonWordsUseLocalDictionary() {
        assertEquals("আমি", BanglaTransliterator.convert("ami"))
        assertEquals("তুমি", BanglaTransliterator.convert("tumi"))
        assertEquals("বাংলা", BanglaTransliterator.convert("bangla"))
        assertEquals("ভালো", BanglaTransliterator.convert("bhalo"))
    }

    @Test fun fallbackHandlesConsonantDigraphsAndVowelSigns() {
        assertEquals("খা", BanglaTransliterator.convert("kha"))
        assertEquals("ক্ষ", BanglaTransliterator.convert("ksh"))
        assertEquals("ছ", BanglaTransliterator.convert("chh"))
        assertEquals("আমি তুমি", BanglaTransliterator.convert("ami tumi"))
    }

    @Test fun suggestionsAreLocalAndPrefixMatched() {
        val suggestions = BanglaWordSuggestions.suggest("am")
        assertEquals(listOf("আমি", "আমার", "আমরা"), suggestions)
        assertTrue(BanglaWordSuggestions.suggest("").isEmpty())
    }
}
