package com.moonmew.keyboard

import android.inputmethodservice.InputMethodService
import android.content.ClipboardManager
import org.json.JSONArray
import org.json.JSONObject
import android.text.InputType
import android.view.View
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo

class MoonMewImeService : InputMethodService(), KeyboardSurface.Listener {
    private var keyboard: KeyboardSurface? = null
    private var mode = KeyboardSurface.Mode.ENGLISH
    private var shifted = false
    private var capsLocked = false
    private var composing = ""
    private var passwordField = false
    private var modeBeforePassword = KeyboardSurface.Mode.ENGLISH
    private var swipePending = false
    private var numericInput = false
    private var numericDecimal = false
    private var numericSigned = false

    override fun onEvaluateFullscreenMode(): Boolean = false

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        keyboard?.onTrimMemory(level)
    }

    override fun onCreateInputView(): View {
        return KeyboardSurface(this, this).also {
            keyboard = it
            it.setMode(mode)
            it.setEditorAction(currentInputEditorInfo?.imeOptions ?: 0)
            it.setShifted(shifted)
            it.setPasswordField(passwordField)
            it.setSwipeEnabled(!passwordField)
            it.setNumericInput(numericInput, numericDecimal, numericSigned)
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        setExtractViewShown(false)
        val wasPasswordField = passwordField
        val type = attribute?.inputType ?: InputType.TYPE_CLASS_TEXT
        val numericConfig = NumericInputPolicy.fromInputType(type)
        numericInput = numericConfig.enabled
        numericDecimal = numericConfig.decimal
        numericSigned = numericConfig.signed
        passwordField = PasswordFieldPolicy.isPasswordField(type)

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
        // Keep the service's typing state in sync with the visual shift key.
        shifted = false
        capsLocked = false
        keyboard?.setShifted(false)
        keyboard?.setMode(mode)
        keyboard?.setEditorAction(attribute?.imeOptions ?: 0)
        keyboard?.setPasswordField(passwordField)
        keyboard?.setSwipeEnabled(!passwordField)
        keyboard?.setNumericInput(numericInput, numericDecimal, numericSigned)
    }

    override fun onUpdateSelection(
        oldSelStart: Int,
        oldSelEnd: Int,
        newSelStart: Int,
        newSelEnd: Int,
        candidatesStart: Int,
        candidatesEnd: Int
    ) {
        super.onUpdateSelection(
            oldSelStart, oldSelEnd, newSelStart, newSelEnd, candidatesStart, candidatesEnd
        )

        // Keep our local phonetic buffer only while the editor cursor remains
        // immediately after the composing text we last sent to the host app.
        // Editors such as browsers and chat apps can move the cursor or replace
        // text independently; retaining stale composition would duplicate text.
        if (composing.isEmpty() || newSelStart < 0 || newSelEnd < 0) return
        if (newSelStart != newSelEnd) {
            clearComposition()
            return
        }

        val expectedText = if (mode == KeyboardSurface.Mode.BANGLA_PHONETIC) {
            TypingLanguagePolicy.resolvePhoneticWord(composing)
        } else composing
        val beforeCursor = currentInputConnection
            ?.getTextBeforeCursor(expectedText.length, 0)
            ?.toString()

        // Some InputConnection implementations return null for surrounding text.
        // In that case keep the composition rather than destroying valid input.
        if (beforeCursor != null && !beforeCursor.endsWith(expectedText)) {
            clearComposition()
        }
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
            key == "TOOLBAR_BRAND" -> keyboard?.playBrandSparkle()
            key == "MORE" -> keyboard?.setMoreMenu(true)
            key == "MORE_CLOSE" -> keyboard?.setMoreMenu(false)
            key == "MORE_CLIP" -> openClipboardHistory()
            key == "SHIFT" -> { if (!capsLocked) shifted = !shifted; keyboard?.setShifted(shifted) }
            key == "CAPS_LOCK_TOGGLE" -> { capsLocked = !capsLocked; shifted = capsLocked; keyboard?.setShifted(shifted) }
            key == "SYMBOLS" -> { commitPending(""); keyboard?.setSymbols(true) }
            key == "EMOJI" -> { commitPending(""); keyboard?.setEmoji(true) }
            key == "EMOJI_BACK" -> keyboard?.setEmoji(false)
            key.startsWith("EMOJI_CAT:") -> keyboard?.setEmojiCategory(key.substringAfter(":").toIntOrNull() ?: 0)
            key == "EMOJI_PAGE_PREV" -> keyboard?.moveEmojiPage(-1)
            key == "EMOJI_PAGE_NEXT" -> keyboard?.moveEmojiPage(1)
            key == "EMOJI_PAGE_STATUS" -> Unit
            key == "CLIP" -> openClipboardHistory()
            key == "CLIP_CLOSE" -> keyboard?.setClipboard(false)
            // Informational/empty clipboard rows are not text keys. Never let their
            // internal action IDs fall through to the printable-character handler.
            key == "CLIP_NOTICE" || key == "CLIP_EMPTY" -> Unit
            key == "CLIP_CLEAR" -> { saveClipboardEntries(emptyList()); keyboard?.setClipboardItems(emptyList()) }
            key.startsWith("CLIP_ITEM:") -> {
                val index = key.substringAfter(":").toIntOrNull() ?: -1
                val item = loadClipboardEntries().getOrNull(index)?.text
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
                    // Backspace should remove a selected range before deleting one grapheme.
                    val selectedText = connection?.getSelectedText(0)
                    if (!selectedText.isNullOrEmpty()) {
                        connection?.commitText("", 1)
                    } else {
                        // Delete a whole user-perceived character (including joined emoji and flags).
                        val beforeCursor = connection?.getTextBeforeCursor(BackspaceDeletion.MAX_CONTEXT_UTF16_UNITS, 0)
                        val units = BackspaceDeletion.utf16UnitsBeforeCursor(beforeCursor)
                        connection?.deleteSurroundingText(units, 0)
                    }
                }
            }
            key == "CURSOR_LEFT" -> sendCursorKey(KeyEvent.KEYCODE_DPAD_LEFT)
            key == "CURSOR_RIGHT" -> sendCursorKey(KeyEvent.KEYCODE_DPAD_RIGHT)
            key == "SPACE" -> commitPending(" ")
            key == "ENTER" -> {
                commitPending("")
                val imeOptions = currentInputEditorInfo?.imeOptions
                val action = imeOptions?.let(EditorActionPolicy::keyboardAction)
                val handled = imeOptions != null && EditorActionPolicy.shouldPerformEditorAction(imeOptions) &&
                    action != null && currentInputConnection?.performEditorAction(action) == true
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
                    // Keep repeated transliteration and selection validation bounded.
                    // Commit a completed long sequence before accepting another letter.
                    if (CompositionPolicy.shouldCommitBeforeAppend(composing.length)) {
                        commitPending("")
                    }
                    composing += if (shifted) key.uppercase() else key.lowercase()
                    if (shifted && !capsLocked) { shifted = false; keyboard?.setShifted(false) }
                    updateComposition()
                } else {
                    commitPending("")
                    currentInputConnection?.commitText(if (shifted) key.uppercase() else key, 1)
                    if (shifted && !capsLocked) { shifted = false; keyboard?.setShifted(false) }
                }
            }
        }
    }


    private fun sendCursorKey(keyCode: Int) {
        val connection = currentInputConnection ?: return
        connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
        connection.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
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
        val captured = ClipboardTextPolicy.capture(clip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text)
        val existing = loadClipboardEntries()
        if (captured != null) {
            saveClipboardEntries(ClipboardHistoryPolicy.add(captured, existing, System.currentTimeMillis()))
        }
        keyboard?.setClipboardItems(loadClipboardEntries().map { it.text })
        keyboard?.setClipboard(true)
    }

    private fun loadClipboardEntries(): List<ClipboardHistoryEntry> {
        val raw = getSharedPreferences("moonmew_settings", MODE_PRIVATE).getString("clipboard_history", "[]") ?: "[]"
        return try {
            val array = JSONArray(raw)
            val entries = mutableListOf<ClipboardHistoryEntry>()
            for (i in 0 until array.length()) {
                val entry = array.getJSONObject(i)
                entries.add(
                    ClipboardHistoryEntry(
                        timestampMillis = entry.optLong("time", 0L),
                        text = entry.optString("text", "")
                    )
                )
            }
            ClipboardHistoryPolicy.prune(entries, System.currentTimeMillis()).also(::saveClipboardEntries)
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun saveClipboardEntries(entries: List<ClipboardHistoryEntry>) {
        val array = JSONArray()
        entries.take(ClipboardHistoryPolicy.MAX_ITEMS).forEach { entry ->
            array.put(JSONObject().put("time", entry.timestampMillis).put("text", entry.text))
        }
        getSharedPreferences("moonmew_settings", MODE_PRIVATE).edit()
            .putString("clipboard_history", array.toString()).apply()
    }

    private fun updateComposition() {
        swipePending = false
        val converted = if (mode == KeyboardSurface.Mode.BANGLA_PHONETIC) {
            TypingLanguagePolicy.resolvePhoneticWord(composing)
        } else composing
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
            // Resolve the whole word once: English remains English, Banglish is
            // transliterated to Bangla, and direct/English modes keep their own script.
            val result = if (mode == KeyboardSurface.Mode.BANGLA_PHONETIC) {
                TypingLanguagePolicy.resolvePhoneticWord(composing)
            } else composing
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
