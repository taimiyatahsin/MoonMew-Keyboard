package com.moonmew.keyboard

import android.text.InputType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordFieldPolicyTest {
    @Test fun detectsAllSupportedTextPasswordVariations() {
        assertTrue(PasswordFieldPolicy.isPasswordField(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD))
        assertTrue(PasswordFieldPolicy.isPasswordField(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD))
        assertTrue(PasswordFieldPolicy.isPasswordField(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD))
    }

    @Test fun detectsNumericPasswordVariation() {
        assertTrue(PasswordFieldPolicy.isPasswordField(
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD))
    }

    @Test fun doesNotClassifyOrdinaryTextAndNumericFieldsAsPasswords() {
        assertFalse(PasswordFieldPolicy.isPasswordField(InputType.TYPE_CLASS_TEXT))
        assertFalse(PasswordFieldPolicy.isPasswordField(
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS))
        assertFalse(PasswordFieldPolicy.isPasswordField(InputType.TYPE_CLASS_NUMBER))
        assertFalse(PasswordFieldPolicy.isPasswordField(InputType.TYPE_CLASS_PHONE))
    }
}
