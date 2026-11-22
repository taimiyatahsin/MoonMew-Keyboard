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
}
