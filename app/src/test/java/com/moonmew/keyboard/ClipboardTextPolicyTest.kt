package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClipboardTextPolicyTest {
    @Test fun acceptsPlainText() {
        assertEquals("hello", ClipboardTextPolicy.capture("hello"))
    }

    @Test fun rejectsMissingOrBlankText() {
        assertNull(ClipboardTextPolicy.capture(null))
        assertNull(ClipboardTextPolicy.capture(""))
        assertNull(ClipboardTextPolicy.capture(" \n\t "))
    }

    @Test fun acceptsTextAtMaximumLength() {
        val text = "x".repeat(ClipboardTextPolicy.MAX_LENGTH)
        assertEquals(text, ClipboardTextPolicy.capture(text))
    }

    @Test fun rejectsTextLongerThanMaximumLength() {
        val text = "x".repeat(ClipboardTextPolicy.MAX_LENGTH + 1)
        assertNull(ClipboardTextPolicy.capture(text))
    }

    @Test fun convertsCharSequenceToStableString() {
        val text: CharSequence = StringBuilder("clipboard")
        assertEquals("clipboard", ClipboardTextPolicy.capture(text))
    }
}
