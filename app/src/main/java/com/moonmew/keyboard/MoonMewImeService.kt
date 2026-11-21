package com.moonmew.keyboard

import android.inputmethodservice.InputMethodService
import android.content.ClipboardManager
import org.json.JSONArray
import org.json.JSONObject
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo

class MoonMewImeService : InputMethodService(), KeyboardSurface.Listener {
    private var keyboard: KeyboardSurface? = null
    private var mode = KeyboardSurface.Mode.ENGLISH
    private var shifted = false
    private var composing = ""
    private var passwordField = false
    private var modeBeforePassword = KeyboardSurface.Mode.ENGLISH
    private var swipePending = false

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onCreateInputView(): View {
        return KeyboardSurface(this, this).also {
            keyboard = it
            it.setMode(mode)
            it.setShifted(shifted)
            it.setPasswordField(passwordField)
            it.setSwipeEnabled(!passwordField)
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        setExtractViewShown(false)
        val wasPasswordField = passwordField
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
        if (passwordField && !wasPasswordField) {
            modeBeforePassword = mode
            mode = KeyboardSurface.Mode.ENGLISH
        } else if (!passwordField && wasPasswordField) {
            mode = modeBeforePassword
        }
        composing = ""
        swipePending = false
        keyboard?.refreshPreferences()
        keyboard?.setComposition("")
        keyboard?.setPreview("")
        keyboard?.setSuggestions(emptyList())
        keyboard?.setShifted(false)
        keyboard?.setMode(mode)
        keyboard?.setPasswordField(passwordField)
        keyboard?.setSwipeEnabled(!passwordField)
    }

    override fun onKey(key: String) {
        if (swipePending && !key.startsWith("SUGGEST:")) {
            swipePending = false
            keyboard?.setComposition("")
            keyboard?.setPreview("")
            keyboard?.setSuggestions(emptyList())
        }
        when {
            key.startsWith("SWIPE:") -> {
                if (mode == KeyboardSurface.Mode.ENGLISH && !passwordField) {
                    val candidates = SwipeWordMatcher.suggest(key.removePrefix("SWIPE:"))
                    if (candidates.isNotEmpty()) {
                        swipePending = true
                        keyboard?.setComposition("Swipe: " + key.removePrefix("SWIPE:"))
                        keyboard?.setPreview("Choose a word")
                        keyboard?.setSuggestions(candidates)
                    }
                }
            }
            key.startsWith("SUGGEST:") -> {
                val chosen = key.removePrefix("SUGGEST:")
                if (swipePending) {
                    currentInputConnection?.commitText(chosen + " ", 1)
                    swipePending = false
                    keyboard?.setComposition("")
                    keyboard?.setPreview("")
                    keyboard?.setSuggestions(emptyList())
                } else if (composing.isNotEmpty()) {
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
            key == "CLIP" -> openClipboardHistory()
            key == "CLIP_CLOSE" -> keyboard?.setClipboard(false)
            // Informational/empty clipboard rows are not text keys. Never let their
            // internal action IDs fall through to the printable-character handler.
            key == "CLIP_NOTICE" || key == "CLIP_EMPTY" -> Unit
            key == "CLIP_CLEAR" -> { saveClipboardItems(emptyList()); keyboard?.setClipboardItems(emptyList()) }
            key.startsWith("CLIP_ITEM:") -> {
                val index = key.substringAfter(":").toIntOrNull() ?: -1
                val item = loadClipboardItems().getOrNull(index)
                if (!passwordField && item != null) { commitPending(""); currentInputConnection?.commitText(item, 1); keyboard?.setClipboard(false) }
            }
            key == "SYMBOLS_MORE" -> keyboard?.nextSymbolsPage()
            key == "ABC" -> keyboard?.setSymbols(false)
            key == "DELETE" -> {
                if (composing.isNotEmpty()) {
                    composing = composing.dropLast(1)
                    updateComposition()
                } else {
                    val connection = currentInputConnection
                    // Delete a whole user-perceived character (including joined emoji and flags).
                    val beforeCursor = connection?.getTextBeforeCursor(32, 0)
                    val units = BackspaceDeletion.utf16UnitsBeforeCursor(beforeCursor)
                    connection?.deleteSurroundingText(units, 0)
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


    private fun openClipboardHistory() {
        if (passwordField) return
        val prefs = getSharedPreferences("moonmew_settings", MODE_PRIVATE)
        if (!prefs.getBoolean("clipboard_history_enabled", false)) {
            keyboard?.setClipboardItems(emptyList())
            keyboard?.setClipboard(true)
            return
        }
        val manager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = manager.primaryClip
        val captured = if (clip != null && clip.itemCount > 0) clip.getItemAt(0).coerceToText(this)?.toString() else null
        if (!captured.isNullOrBlank() && captured.length <= 1000) {
            val items = (listOf(captured) + loadClipboardItems().filterNot { it == captured }).take(5)
            saveClipboardItems(items)
        }
        keyboard?.setClipboardItems(loadClipboardItems())
        keyboard?.setClipboard(true)
    }

    private fun loadClipboardItems(): List<String> {
        val raw = getSharedPreferences("moonmew_settings", MODE_PRIVATE).getString("clipboard_history", "[]") ?: "[]"
        val cutoff = System.currentTimeMillis() - 24L * 60L * 60L * 1000L
        return try {
            val array = JSONArray(raw)
            val values = mutableListOf<Pair<Long, String>>()
            for (i in 0 until array.length()) {
                val entry = array.getJSONObject(i)
                val timestamp = entry.optLong("time", 0L)
                val value = entry.optString("text", "")
                if (timestamp >= cutoff && value.isNotBlank()) values.add(timestamp to value)
            }
            saveClipboardEntries(values)
            values.sortedByDescending { it.first }.map { it.second }.take(5)
        } catch (_: Exception) { emptyList() }
    }

    private fun saveClipboardItems(items: List<String>) {
        saveClipboardEntries(items.take(5).map { System.currentTimeMillis() to it })
    }

    private fun saveClipboardEntries(entries: List<Pair<Long, String>>) {
        val array = JSONArray()
        entries.take(5).forEach { (time, value) ->
            array.put(JSONObject().put("time", time).put("text", value))
        }
        getSharedPreferences("moonmew_settings", MODE_PRIVATE).edit()
            .putString("clipboard_history", array.toString()).apply()
    }

    private fun updateComposition() {
        swipePending = false
        val converted = BanglaTransliterator.convert(composing)
        currentInputConnection?.setComposingText(converted, 1)
        keyboard?.setComposition(composing)
        keyboard?.setPreview(converted)
        keyboard?.setSuggestions(BanglaWordSuggestions.suggest(composing))
    }

    private fun clearComposition() {
        swipePending = false
        composing = ""
        keyboard?.setComposition("")
        keyboard?.setPreview("")
        keyboard?.setSuggestions(emptyList())
    }

    private fun commitPending(separator: String) {
        if (swipePending) {
            swipePending = false
            keyboard?.setComposition("")
            keyboard?.setPreview("")
            keyboard?.setSuggestions(emptyList())
        }
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
