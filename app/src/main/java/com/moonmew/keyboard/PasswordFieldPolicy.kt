package com.moonmew.keyboard

import android.text.InputType

/**
 * Identifies password editors so optional features that could expose text
 * (such as swipe suggestions and clipboard history) can be disabled.
 */
internal object PasswordFieldPolicy {
    fun isPasswordField(inputType: Int): Boolean {
        val inputClass = inputType and InputType.TYPE_MASK_CLASS
        val variation = inputType and InputType.TYPE_MASK_VARIATION
        return (inputClass == InputType.TYPE_CLASS_TEXT &&
            (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)) ||
            (inputClass == InputType.TYPE_CLASS_NUMBER &&
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)
    }
}
