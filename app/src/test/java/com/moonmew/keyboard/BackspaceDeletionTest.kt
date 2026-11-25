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

    @Test fun banglaConjunctWithZeroWidthJoinerDeletesAsOneUnit() {
        val conjunct = "\u0995\u09CD\u200D\u09B7"
        assertEquals(conjunct.length, BackspaceDeletion.utf16UnitsBeforeCursor(conjunct))
    }

    @Test fun devanagariConjunctWithZeroWidthJoinerDeletesAsOneUnit() {
        val conjunct = "\u0915\u094D\u200D\u0937"
        assertEquals(conjunct.length, BackspaceDeletion.utf16UnitsBeforeCursor(conjunct))
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
        assertEquals(true, sequence.length > 64)
        assertEquals(true, sequence.length <= BackspaceDeletion.MAX_CONTEXT_UTF16_UNITS)
        assertEquals(sequence.length, BackspaceDeletion.utf16UnitsBeforeCursor(sequence))
    }

    @Test fun emptyOrUnavailableCursorTextUsesSafeSingleUnitFallback() {
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor(""))
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor(null))
    }


    @Test fun chainedBanglaConjunctWithMultipleJoinersDeletesAsOneUnit() {
        val conjunct = "\u0995\u09CD\u200D\u09B7\u09CD\u200D\u09AE"
        assertEquals(conjunct.length, BackspaceDeletion.utf16UnitsBeforeCursor(conjunct))
    }

    @Test fun emojiKeycapSequenceDeletesAsOneUnit() {
        val keycap = "1\uFE0F\u20E3"
        assertEquals(keycap.length, BackspaceDeletion.utf16UnitsBeforeCursor(keycap))
    }

    @Test fun emojiWithTagCharactersDeletesAsOneUnit() {
        val flag = "\uD83C\uDFF4\uDB40\uDC67\uDB40\uDC62\uDB40\uDC7F"
        assertEquals(flag.length, BackspaceDeletion.utf16UnitsBeforeCursor(flag))
    }

    @Test fun isolatedSurrogatesUseAValidUtf16DeletionLength() {
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor("\\uD83D"))
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor("\\uDE00"))
        assertEquals(2, BackspaceDeletion.utf16UnitsBeforeCursor("x\\uD83D\\uDE00"))
    }

    @Test fun malformedUtf16InputNeverProducesAnOutOfRangeDeletionLength() {
        val random = java.util.Random(0x4D4F4F4EL)
        repeat(500) {
            val length = 1 + random.nextInt(64)
            val value = CharArray(length) { random.nextInt(0x10000).toChar() }.concatToString()
            val deletionLength = BackspaceDeletion.utf16UnitsBeforeCursor(value)
            assertTrue("Deletion length must be positive", deletionLength >= 1)
            assertTrue("Deletion length must not exceed available UTF-16 text", deletionLength <= value.length)
        }
    }
}
