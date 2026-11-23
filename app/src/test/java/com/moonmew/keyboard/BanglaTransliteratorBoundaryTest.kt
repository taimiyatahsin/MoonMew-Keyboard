package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class BanglaTransliteratorBoundaryTest {
    @Test fun longestConsonantAndVowelSequencesWin() {
        assertEquals("খা", BanglaTransliterator.convert("kha"))
        assertEquals("শ", BanglaTransliterator.convert("sh"))
        assertEquals("কৈ", BanglaTransliterator.convert("koi"))
    }

    @Test fun uppercaseDigraphsAreNormalizedBeforeMatching() {
        assertEquals("খা", BanglaTransliterator.convert("KHA"))
        assertEquals("শ", BanglaTransliterator.convert("SH"))
    }

    @Test fun digitsAndExistingBanglaTextPassThrough() {
        assertEquals("123-45", BanglaTransliterator.convert("123-45"))
        assertEquals("বাংলা", BanglaTransliterator.convert("বাংলা"))
    }

    @Test fun punctuationAroundWordsIsPreserved() {
        assertEquals("কা, তা?", BanglaTransliterator.convert("ka, ta?"))
        assertEquals("(খা)!", BanglaTransliterator.convert("(kha)!"))
    }

    @Test fun nasalDigraphIsMatchedAsOneSequence() {
        assertEquals("ং", BanglaTransliterator.convert("ng"))
    }
}
