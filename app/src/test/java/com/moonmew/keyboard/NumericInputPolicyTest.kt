package com.moonmew.keyboard

import android.text.InputType
import org.junit.Assert.assertEquals
import org.junit.Test

class NumericInputPolicyTest {
    @Test fun textFieldsKeepTheNormalKeyboard() {
        assertEquals(NumericInputConfig(false, false, false),
            NumericInputPolicy.fromInputType(InputType.TYPE_CLASS_TEXT))
    }

    @Test fun integerFieldsUseTheNumericKeyboardWithoutOptionalKeys() {
        assertEquals(NumericInputConfig(true, false, false),
            NumericInputPolicy.fromInputType(InputType.TYPE_CLASS_NUMBER))
    }

    @Test fun decimalAndSignedFlagsAreRespectedIndependently() {
        assertEquals(NumericInputConfig(true, true, false),
            NumericInputPolicy.fromInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL))
        assertEquals(NumericInputConfig(true, false, true),
            NumericInputPolicy.fromInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED))
    }

    @Test fun decimalSignedAndNumericPasswordVariationsRemainNumeric() {
        assertEquals(NumericInputConfig(true, true, true),
            NumericInputPolicy.fromInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED))
        assertEquals(NumericInputConfig(true, false, false),
            NumericInputPolicy.fromInputType(InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD))
    }

    @Test fun numericFlagsOnTextFieldsDoNotEnableNumericMode() {
        val inputType = InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_NUMBER_FLAG_DECIMAL or
            InputType.TYPE_NUMBER_FLAG_SIGNED

        assertEquals(NumericInputConfig(false, false, false),
            NumericInputPolicy.fromInputType(inputType))
    }

    @Test fun nonNumberClassesNeverEnableNumericMode() {
        val inputTypes = listOf(
            InputType.TYPE_CLASS_PHONE,
            InputType.TYPE_CLASS_DATETIME,
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )

        inputTypes.forEach { inputType ->
            assertEquals(NumericInputConfig(false, false, false),
                NumericInputPolicy.fromInputType(inputType))
        }
    }

}
