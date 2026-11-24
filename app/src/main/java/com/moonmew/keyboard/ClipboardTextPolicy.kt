package com.moonmew.keyboard

/**
 * Keeps optional clipboard history limited to non-blank plain-text values.
 * Non-text ClipData items should be filtered by reading Item.text before calling this policy.
 */
internal object ClipboardTextPolicy {
    const val MAX_LENGTH = 1000

    fun capture(text: CharSequence?): String? {
        val value = text?.toString()
        return value?.takeIf { it.isNotBlank() && it.length <= MAX_LENGTH }
    }
}
