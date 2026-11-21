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

    @Test fun expandedEverydayDictionaryCoversCommonWords() {
        assertEquals("আচ্ছা", BanglaTransliterator.convert("accha"))
        assertEquals("ঠিক আছে", BanglaTransliterator.convert("thikache"))
        assertEquals("সবাই", BanglaTransliterator.convert("sobai"))
        assertEquals("সমস্যা", BanglaTransliterator.convert("somossa"))
        assertEquals("প্রশ্ন", BanglaTransliterator.convert("proshno"))
    }

    @Test fun expandedWordsAppearInPrefixSuggestions() {
        assertTrue(BanglaWordSuggestions.suggest("acc").contains("আচ্ছা"))
        assertTrue(BanglaWordSuggestions.suggest("shu", 5).contains("শুধু"))
    }

    @Test fun suggestionsAreLocalAndPrefixMatched() {
        val suggestions = BanglaWordSuggestions.suggest("am")
        assertEquals(listOf("আমি", "আমার", "আমরা"), suggestions)
        assertTrue(BanglaWordSuggestions.suggest("").isEmpty())
    }

    @Test fun suggestionsNormalizeCaseAndWhitespace() {
        assertEquals("আমি", BanglaWordSuggestions.exact(" AMI "))
        assertEquals(
            BanglaWordSuggestions.suggest("ami"),
            BanglaWordSuggestions.suggest(" AMI ")
        )
    }

    @Test fun suggestionLimitIsSafeAndNeverReturnsDuplicateSpellings() {
        assertTrue(BanglaWordSuggestions.suggest("am", -1).isEmpty())
        assertTrue(BanglaWordSuggestions.suggest("am", 0).isEmpty())
        assertEquals(1, BanglaWordSuggestions.suggest("am", 1).size)
        val expanded = BanglaWordSuggestions.suggest("am", 5)
        assertEquals(expanded.size, expanded.distinct().size)
        assertTrue(expanded.size <= 5)
    }
}
