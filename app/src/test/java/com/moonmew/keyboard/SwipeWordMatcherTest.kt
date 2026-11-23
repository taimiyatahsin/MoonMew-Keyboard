package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeWordMatcherTest {
    @Test fun exactAndNearMatchesAreSuggestedOffline() {
        assertEquals("the", SwipeWordMatcher.suggest("the").first())
        assertTrue(SwipeWordMatcher.suggest("helo").contains("hello"))
    }
    @Test fun shortPathsDoNotSuggest() {
        assertTrue(SwipeWordMatcher.suggest("ab").isEmpty())
        assertTrue(SwipeWordMatcher.suggest("").isEmpty())
    }

    @Test fun resultsRespectRequestedLimitAndStayDeterministic() {
        val first = SwipeWordMatcher.suggest("helo", limit = 2)
        val second = SwipeWordMatcher.suggest("helo", limit = 2)
        assertEquals(first, second)
        assertTrue(first.size <= 2)
    }

    @Test fun nonEnglishCharactersAreIgnoredSafely() {
        assertEquals(SwipeWordMatcher.suggest("hello"), SwipeWordMatcher.suggest("h-é-l-l-o!"))
    }

    @Test fun nonPositiveLimitReturnsNoResults() {
        assertTrue(SwipeWordMatcher.suggest("hello", limit = 0).isEmpty())
        assertTrue(SwipeWordMatcher.suggest("hello", limit = -1).isEmpty())
    }
    @Test fun reusedDistanceBuffersDoNotLeakAcrossDifferentPaths() {
        assertEquals("hello", SwipeWordMatcher.suggest("hello").first())
        assertEquals("the", SwipeWordMatcher.suggest("the").first())
        assertEquals("world", SwipeWordMatcher.suggest("world").first())
    }

    @Test fun longerPathsRemainBoundedAndDeterministic() {
        val path = "qwertyuiopasdfghjklzxcvbnm"
        assertEquals(SwipeWordMatcher.suggest(path), SwipeWordMatcher.suggest(path))
    }
}


class BanglaTransliteratorRegressionTest {
    @Test fun emptyInputStaysEmpty() {
        assertEquals("", BanglaTransliterator.convert(""))
    }

    @Test fun commonConsonantAndVowelCombinationsCompose() {
        assertEquals("কা", BanglaTransliterator.convert("ka"))
        assertEquals("খা", BanglaTransliterator.convert("kha"))
        assertEquals("ক্ষ", BanglaTransliterator.convert("ksh"))
    }

    @Test fun independentVowelsUseIndependentForms() {
        assertEquals("আ", BanglaTransliterator.convert("a"))
        assertEquals("ই", BanglaTransliterator.convert("i"))
        assertEquals("ঐ", BanglaTransliterator.convert("oi"))
    }

    @Test fun exactDictionaryEntriesTakePrecedence() {
        assertEquals("আমি", BanglaTransliterator.convert("ami"))
        assertEquals("বাংলাদেশ", BanglaTransliterator.convert("BANGLADESH"))
    }

    @Test fun spacesAndPunctuationArePreserved() {
        assertEquals("কা তা", BanglaTransliterator.convert("ka ta"))
        assertEquals("কা!", BanglaTransliterator.convert("ka!"))
    }

    @Test fun unknownCharactersPassThroughWithoutCrashing() {
        assertEquals("@", BanglaTransliterator.convert("@"))
        assertEquals("কা🙂", BanglaTransliterator.convert("ka🙂"))
    }
}
