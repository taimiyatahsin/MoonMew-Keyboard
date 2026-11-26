package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class TypingLanguagePolicyTest {
    @Test fun knownBanglishWordsBecomeBangla() {
        assertEquals("আমি", TypingLanguagePolicy.resolvePhoneticWord("ami"))
        assertEquals("ভালো", TypingLanguagePolicy.resolvePhoneticWord("bhalo"))
        assertEquals("আছি", TypingLanguagePolicy.resolvePhoneticWord("achi"))
    }

    @Test fun commonEnglishWordsStayEnglish() {
        listOf("hello", "keyboard", "please", "today", "beautiful", "download")
            .forEach { word -> assertEquals(word, TypingLanguagePolicy.resolvePhoneticWord(word)) }
    }

    @Test fun everydayEnglishWordsRemainEnglishWithOriginalCapitalization() {
        listOf("I", "Want", "HOME", "Please", "Come", "Back")
            .forEach { word -> assertEquals(word, TypingLanguagePolicy.resolvePhoneticWord(word)) }
    }

    @Test fun trimsWhitespaceBeforeResolving() {
        assertEquals("আমি", TypingLanguagePolicy.resolvePhoneticWord("  ami  "))
        assertEquals("", TypingLanguagePolicy.resolvePhoneticWord("   "))
    }

    @Test fun englishAndBanglishChoicesAreCaseInsensitive() {
        assertEquals("আমি", TypingLanguagePolicy.resolvePhoneticWord("AMI"))
        assertEquals("hello", TypingLanguagePolicy.resolvePhoneticWord("HELLO"))
    }
}
