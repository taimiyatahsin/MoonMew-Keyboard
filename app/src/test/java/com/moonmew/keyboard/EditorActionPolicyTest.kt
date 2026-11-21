package com.moonmew.keyboard

import android.view.inputmethod.EditorInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EditorActionPolicyTest {
    @Test fun configuredActionIsShownNormally() {
        val options = EditorInfo.IME_ACTION_SEARCH
        assertEquals(EditorInfo.IME_ACTION_SEARCH, EditorActionPolicy.keyboardAction(options))
        assertTrue(EditorActionPolicy.shouldPerformEditorAction(options))
    }

    @Test fun noEnterActionFlagShowsEnterAndPreventsEditorAction() {
        val options = EditorInfo.IME_ACTION_SEARCH or EditorInfo.IME_FLAG_NO_ENTER_ACTION
        assertEquals(EditorInfo.IME_ACTION_NONE, EditorActionPolicy.keyboardAction(options))
        assertFalse(EditorActionPolicy.shouldPerformEditorAction(options))
    }

    @Test fun unspecifiedActionDoesNotTriggerEditorAction() {
        assertFalse(EditorActionPolicy.shouldPerformEditorAction(EditorInfo.IME_ACTION_UNSPECIFIED))
    }

    @Test fun noActionDefaultsToNewlineBehavior() {
        assertEquals(EditorInfo.IME_ACTION_NONE, EditorActionPolicy.keyboardAction(EditorInfo.IME_ACTION_NONE))
        assertFalse(EditorActionPolicy.shouldPerformEditorAction(EditorInfo.IME_ACTION_NONE))
    }
}
