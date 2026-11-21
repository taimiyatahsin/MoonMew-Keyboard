package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class BackspaceDeletionTest {
    @Test fun supplementaryEmojiDeletesBothUtf16Units() {
        assertEquals(2, BackspaceDeletion.utf16UnitsBeforeCursor("a😀"))
    }

    @Test fun basicLatinDeletesOneUnit() {
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor("abc"))
    }

    @Test fun banglaVowelSignStaysWithItsConsonant() {
        assertEquals("লা".length, BackspaceDeletion.utf16UnitsBeforeCursor("বাংলা"))
    }

    @Test fun combiningCharacterDeletesAsOneUnit() {
        assertEquals(2, BackspaceDeletion.utf16UnitsBeforeCursor("e\u0301"))
    }

    @Test fun banglaConjunctDeletesAsOneUnit() {
        assertEquals("ক্ষ".length, BackspaceDeletion.utf16UnitsBeforeCursor("ক্ষ"))
    }

    @Test fun emojiWithSkinToneDeletesAsOneUnit() {
        assertEquals("👍🏽".length, BackspaceDeletion.utf16UnitsBeforeCursor("👍🏽"))
    }

    @Test fun familyEmojiJoinedWithZeroWidthJoinersDeletesAsOneUnit() {
        assertEquals("👨‍👩‍👧‍👦".length, BackspaceDeletion.utf16UnitsBeforeCursor("👨‍👩‍👧‍👦"))
    }

    @Test fun flagDeletesAsOneUnit() {
        assertEquals("🇧🇩".length, BackspaceDeletion.utf16UnitsBeforeCursor("🇧🇩"))
    }

    @Test fun trailingRegionalIndicatorInOddRunDeletesAlone() {
        assertEquals(2, BackspaceDeletion.utf16UnitsBeforeCursor("🇧🇩🇺"))
    }

    @Test fun finalFlagInEvenRegionalIndicatorRunDeletesAsPair() {
        assertEquals("🇺🇸".length, BackspaceDeletion.utf16UnitsBeforeCursor("🇧🇩🇺🇸"))
    }


    @Test fun longJoinedEmojiSequenceDeletesAsOneUnit() {
        val sequence = List(6) { "👨‍👩‍👧‍👦" }.joinToString("\u200D")
        assertEquals(sequence.length, BackspaceDeletion.utf16UnitsBeforeCursor(sequence))
    }

    @Test fun emptyOrUnavailableCursorTextUsesSafeSingleUnitFallback() {
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor(""))
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor(null))
    }
}
