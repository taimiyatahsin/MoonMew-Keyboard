package com.moonmew.keyboard

import android.text.InputType

data class NumericInputConfig(
    val enabled: Boolean,
    val decimal: Boolean,
    val signed: Boolean
)

object NumericInputPolicy {
    fun fromInputType(inputType: Int): NumericInputConfig {
        val isNumber = (inputType and InputType.TYPE_MASK_CLASS) == InputType.TYPE_CLASS_NUMBER
        return NumericInputConfig(
            enabled = isNumber,
            decimal = isNumber && (inputType and InputType.TYPE_NUMBER_FLAG_DECIMAL) != 0,
            signed = isNumber && (inputType and InputType.TYPE_NUMBER_FLAG_SIGNED) != 0
        )
    }
}
