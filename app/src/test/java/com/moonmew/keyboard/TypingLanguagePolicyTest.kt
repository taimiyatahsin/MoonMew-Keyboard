package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class TypingLanguagePolicyTest {
    @Test fun knownBanglishWordsBecomeBangla() {
        assertEquals("আমি", TypingLanguagePolicy.resolvePhoneticWord("ami"))
        assertEquals("ভালো", TypingLanguagePolicy.resolvePhoneticWord("bhalo"))
    }

    @Test fun commonEnglishWordsStayEnglish() {
        assertEquals("hello", TypingLanguagePolicy.resolvePhoneticWord("hello"))
        assertEquals("keyboard", TypingLanguagePolicy.resolvePhoneticWord("keyboard"))
    }
}
