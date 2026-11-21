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

    @Test fun emptyOrUnavailableCursorTextUsesSafeSingleUnitFallback() {
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor(""))
        assertEquals(1, BackspaceDeletion.utf16UnitsBeforeCursor(null))
    }
}
