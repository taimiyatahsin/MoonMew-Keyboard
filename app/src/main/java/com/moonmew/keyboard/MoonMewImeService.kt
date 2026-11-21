package com.moonmew.keyboard

import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo

class MoonMewImeService : InputMethodService(), KeyboardSurface.Listener {
    private var keyboard: KeyboardSurface? = null
    private var mode = KeyboardSurface.Mode.ENGLISH
    private var shifted = false
    private var composing = ""
    private var passwordField = false

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onCreateInputView(): View {
        return KeyboardSurface(this, this).also {
            keyboard = it
            it.setMode(mode)
            it.setShifted(shifted)
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        setExtractViewShown(false)
        val type = attribute?.inputType ?: InputType.TYPE_CLASS_TEXT
        val cls = type and InputType.TYPE_MASK_CLASS
        val variation = type and InputType.TYPE_MASK_VARIATION
        passwordField = (cls == InputType.TYPE_CLASS_TEXT &&
            (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)) ||
            (cls == InputType.TYPE_CLASS_NUMBER &&
                variation == InputType.TYPE_NUMBER_VARIATION_PASSWORD)

        currentInputConnection?.finishComposingText()
        if (passwordField) mode = KeyboardSurface.Mode.ENGLISH
        composing = ""
        keyboard?.refreshPreferences()
        keyboard?.setComposition("")
        keyboard?.setPreview("")
        keyboard?.setSuggestions(emptyList())
        keyboard?.setShifted(false)
        keyboard?.setMode(mode)
    }

    override fun onKey(key: String) {
        when {
            key.startsWith("SUGGEST:") -> {
                val chosen = key.removePrefix("SUGGEST:")
                if (composing.isNotEmpty()) {
                    currentInputConnection?.setComposingText(chosen, 1)
                    currentInputConnection?.finishComposingText()
                    currentInputConnection?.commitText(" ", 1)
                    clearComposition()
                }
            }
            key.startsWith("LONG:") -> {
                commitPending("")
                currentInputConnection?.commitText(key.removePrefix("LONG:"), 1)
            }
            key == "SHIFT" -> { shifted = !shifted; keyboard?.setShifted(shifted) }
            key == "SYMBOLS" -> { commitPending(""); keyboard?.setSymbols(true) }
            key == "EMOJI" -> { commitPending(""); keyboard?.setEmoji(true) }
            key == "EMOJI_BACK" -> keyboard?.setEmoji(false)
            key == "SYMBOLS_MORE" -> keyboard?.nextSymbolsPage()
            key == "ABC" -> keyboard?.setSymbols(false)
            key == "DELETE" -> {
                if (composing.isNotEmpty()) {
                    composing = composing.dropLast(1)
                    updateComposition()
                } else {
                    val connection = currentInputConnection
                    if (android.os.Build.VERSION.SDK_INT >= 24) connection?.deleteSurroundingTextInCodePoints(1, 0)
                    else connection?.deleteSurroundingText(1, 0)
                }
            }
            key == "SPACE" -> commitPending(" ")
            key == "ENTER" -> {
                commitPending("")
                val action = currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
                val handled = action != null && action != EditorInfo.IME_ACTION_NONE &&
                    action != EditorInfo.IME_ACTION_UNSPECIFIED &&
                    currentInputConnection?.performEditorAction(action) == true
                if (!handled) currentInputConnection?.commitText("\n", 1)
            }
            key == "MODE" -> {
                commitPending("")
                mode = when (mode) {
                    KeyboardSurface.Mode.ENGLISH -> if (passwordField) KeyboardSurface.Mode.ENGLISH else KeyboardSurface.Mode.BANGLA_PHONETIC
                    KeyboardSurface.Mode.BANGLA_PHONETIC -> KeyboardSurface.Mode.BANGLA_DIRECT
                    KeyboardSurface.Mode.BANGLA_DIRECT -> KeyboardSurface.Mode.ENGLISH
                }
                keyboard?.setMode(mode)
            }
            else -> {
                if (mode == KeyboardSurface.Mode.BANGLA_PHONETIC && !passwordField && key.length == 1 && key[0].isLetter()) {
                    composing += if (shifted) key.uppercase() else key.lowercase()
                    if (shifted) { shifted = false; keyboard?.setShifted(false) }
                    updateComposition()
                } else {
                    commitPending("")
                    currentInputConnection?.commitText(if (shifted) key.uppercase() else key, 1)
                    if (shifted) { shifted = false; keyboard?.setShifted(false) }
                }
            }
        }
    }

    private fun updateComposition() {
        val converted = BanglaTransliterator.convert(composing)
        currentInputConnection?.setComposingText(converted, 1)
        keyboard?.setComposition(composing)
        keyboard?.setPreview(converted)
        keyboard?.setSuggestions(BanglaWordSuggestions.suggest(composing))
    }

    private fun clearComposition() {
        composing = ""
        keyboard?.setComposition("")
        keyboard?.setPreview("")
        keyboard?.setSuggestions(emptyList())
    }

    private fun commitPending(separator: String) {
        if (composing.isNotEmpty()) {
            val result = BanglaTransliterator.convert(composing).ifEmpty { composing }
            currentInputConnection?.setComposingText(result, 1)
            currentInputConnection?.finishComposingText()
            clearComposition()
        }
        if (separator.isNotEmpty()) currentInputConnection?.commitText(separator, 1)
    }

    override fun onFinishInput() {
        commitPending("")
        currentInputConnection?.finishComposingText()
        super.onFinishInput()
    }
}
