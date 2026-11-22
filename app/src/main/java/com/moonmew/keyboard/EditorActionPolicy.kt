package com.moonmew.keyboard

import android.view.inputmethod.EditorInfo

/**
 * Keeps the keyboard's Enter key consistent with the editor's IME options.
 * IME_FLAG_NO_ENTER_ACTION means Enter must insert a newline instead of
 * triggering the configured Go/Search/Send/Next/Done action.
 */
internal object EditorActionPolicy {
    fun keyboardAction(imeOptions: Int): Int =
        if ((imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) {
            EditorInfo.IME_ACTION_NONE
        } else {
            imeOptions and EditorInfo.IME_MASK_ACTION
        }

    fun shouldPerformEditorAction(imeOptions: Int): Boolean {
        if ((imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION) != 0) return false
        val action = imeOptions and EditorInfo.IME_MASK_ACTION
        return action != EditorInfo.IME_ACTION_NONE &&
            action != EditorInfo.IME_ACTION_UNSPECIFIED
    }
}
