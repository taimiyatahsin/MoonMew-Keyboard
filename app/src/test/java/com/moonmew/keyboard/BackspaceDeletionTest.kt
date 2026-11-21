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

    @Test fun banglaCharacterDeletesOneUnit() {
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor("বাংলা"))
    }

    @Test fun combiningCharacterDeletesAsOneUnit() {
        assertEquals(2, BackspaceDeletion.utf16UnitsBeforeCursor("e\u0301"))
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

    @Test fun emptyOrUnavailableCursorTextUsesSafeSingleUnitFallback() {
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor(""))
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor(null))
    }
}
