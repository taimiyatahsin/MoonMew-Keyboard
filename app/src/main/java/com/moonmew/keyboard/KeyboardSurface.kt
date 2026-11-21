package com.moonmew.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.os.SystemClock
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup

class KeyboardSurface(context: Context, private val listener: Listener) : View(context) {
    interface Listener { fun onKey(key: String) }
    enum class Mode { ENGLISH, BANGLA_PHONETIC, BANGLA_DIRECT }
    private data class Key(val label: String, val value: String, val weight: Float = 1f, val special: Boolean = false, val longPressValue: String? = null)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val keyTypeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private val preferences = context.getSharedPreferences("moonmew_settings", Context.MODE_PRIVATE)
    private var themeId = preferences.getInt("theme_id", if (preferences.getBoolean("rose_theme", true)) 0 else 1)
    private var keySound = preferences.getBoolean("key_sound", false)
    private var mode = Mode.ENGLISH
    private var shifted = false
    private var symbols = false
    private var emojiMode = false
    private var clipboardMode = false
    private var clipboardItems = emptyList<String>()
    private var downKeyIndex = -1
    private var downSuggestionIndex = -1
    private var downTime = 0L
    private val swipePath = StringBuilder()
    private var lastSwipeKeyIndex = -1
    private var swipeTravel = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var swipeDetected = false
    private var swipeEnabled = true
    private var passwordField = false
    private var symbolsPage = 0
    private var composition = ""
    private var preview = ""
    private var suggestions = emptyList<String>()
    private val bounds = mutableListOf<RectF>()
    private val suggestionBounds = mutableListOf<RectF>()
    private val rows = mutableListOf<List<Key>>()
    private val flatKeys = mutableListOf<Key>()

    init {
        isClickable = true
        setBackgroundColor(backgroundBottomColor())
        applyHeight()
        rebuild()
    }

    private fun applyHeight() {
        val heightDp = preferences.getInt("keyboard_height_dp", 240).coerceIn(160, 320)
        val params = layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(heightDp.toFloat()).toInt()
        )
        params.height = dp(heightDp.toFloat()).toInt()
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams = params
        requestLayout()
    }

    fun refreshPreferences() {
        themeId = preferences.getInt("theme_id", if (preferences.getBoolean("rose_theme", true)) 0 else 1).coerceIn(0, 2)
        keySound = preferences.getBoolean("key_sound", false)
        // If clipboard history was switched off in Settings while this IME was alive,
        // immediately discard the in-memory panel contents as well as the saved data.
        if (!preferences.getBoolean("clipboard_history_enabled", false)) {
            clipboardItems = emptyList()
            clipboardMode = false
        }
        setBackgroundColor(backgroundBottomColor())
        applyHeight()
        rebuild()
        invalidate()
    }

    private fun backgroundTopColor() = when (themeId) {
        1 -> 0xFF172B43.toInt()
        2 -> 0xFF30213F.toInt()
        else -> 0xFF352333.toInt()
    }
    private fun backgroundBottomColor() = when (themeId) {
        1 -> 0xFF0B1422.toInt()
        2 -> 0xFF171020.toInt()
        else -> 0xFF17121B.toInt()
    }
    private fun accentColor() = when (themeId) {
        1 -> 0xFFB9DFFF.toInt()
        2 -> 0xFFE0C8FF.toInt()
        else -> 0xFFFFC6D9.toInt()
    }
    private fun keyColor(special: Boolean, modeKey: Boolean = false) = when (themeId) {
        1 -> if (modeKey) 0xFF416B8B.toInt() else if (special) 0xFF2B405B.toInt() else 0xFF1D2B3E.toInt()
        2 -> if (modeKey) 0xFF795A99.toInt() else if (special) 0xFF49365F.toInt() else 0xFF302541.toInt()
        else -> if (modeKey) 0xFF85536D.toInt() else if (special) 0xFF503548.toInt() else 0xFF342633.toInt()
    }
    private fun keyTextColor() = when (themeId) {
        1 -> 0xFFEAF5FF.toInt()
        2 -> 0xFFF4EAFF.toInt()
        else -> 0xFFFFF0F5.toInt()
    }

    fun setSwipeEnabled(value: Boolean) { swipeEnabled = value; if (!value) resetSwipeTracking() }
    fun setPasswordField(value: Boolean) {
        passwordField = value
        if (value) {
            // A panel opened in the previous field must never survive into a password field.
            clipboardMode = false
            clipboardItems = emptyList()
        }
        rebuild()
        invalidate()
    }
    private fun resetSwipeTracking() { swipePath.setLength(0); lastSwipeKeyIndex = -1; swipeTravel = 0f; swipeDetected = false }
    fun setMode(value: Mode) { mode = value; symbols = false; emojiMode = false; clipboardMode = false; symbolsPage = 0; rebuild(); invalidate() }
    fun setShifted(value: Boolean) { shifted = value; rebuild(); invalidate() }
    fun setSymbols(value: Boolean) { symbols = value; emojiMode = false; clipboardMode = false; symbolsPage = 0; rebuild(); invalidate() }
    fun setEmoji(value: Boolean) { emojiMode = value; clipboardMode = false; symbols = false; rebuild(); invalidate() }
    fun setClipboard(value: Boolean) { clipboardMode = value; emojiMode = false; symbols = false; rebuild(); invalidate() }
    fun setClipboardItems(value: List<String>) { clipboardItems = value.take(5); if (clipboardMode) rebuild(); invalidate() }
    fun nextSymbolsPage() { if (symbols) { symbolsPage = 1 - symbolsPage; rebuild(); invalidate() } }
    fun setComposition(value: String) { composition = value; invalidate() }
    fun setPreview(value: String) { preview = value; invalidate() }
    fun setSuggestions(value: List<String>) { suggestions = value.take(3); invalidate() }

    private fun rebuild() {
        rows.clear()
        flatKeys.clear()
        if (clipboardMode) {
            if (clipboardItems.isEmpty()) rows.add(listOf(Key("No saved clips", "CLIP_EMPTY", 1f, true)))
            else clipboardItems.forEachIndexed { index, item -> rows.add(listOf(Key(item.replace("\n", " ").take(32), if (item == "Enable in MoonMew settings") "CLIP_NOTICE" else "CLIP_ITEM:$index", 1f, true))) }
            rows.add(listOf(Key("Clear history", "CLIP_CLEAR", 1.4f, true), Key("ABC", "CLIP_CLOSE", 1f, true)))
        } else if (emojiMode) {
            listOf(
                listOf("😀","😃","😄","😁","😆","😅","😂","🤣"),
                listOf("😊","😇","🙂","🙃","😉","😍","🥰","😘"),
                listOf("😋","😛","😜","🤪","🤗","🤭","🤫","🤔"),
                listOf("😭","😢","🥺","😡","😎","🤩","🥳","❤️")
            ).forEach { line -> rows.add(line.map { emoji -> Key(emoji, emoji) }) }
            rows.add(listOf(Key("ABC", "EMOJI_BACK", 1.2f, true), Key("⌫", "DELETE", 1.2f, true), Key("Space", "SPACE", 3.6f, true)))
        } else if (symbols) {
            val lines = if (symbolsPage == 0)
                listOf("1234567890", "!@#$%^&*()", "-_=+[]{}<>", "?/;:,.~")
            else
                listOf("1234567890", "`£€¥₹¢©®", "±×÷√∞≠≤≥", "\\|_~^%#@")
            lines.forEach { line -> rows.add(line.map { ch -> Key(ch.toString(), ch.toString()) }) }
            rows.add(listOf(Key("ABC", "ABC", 1.2f, true), Key("☾", "MODE", 1.1f, true),
                Key("⌫", "DELETE", 1.2f, true)))
            rows.add(listOf(Key(if (symbolsPage == 0) "=\\<" else "123", "SYMBOLS_MORE", 1.1f, true),
                Key(",", ",", .7f), Key("English / বাংলা", "SPACE", 4.6f, true),
                Key(".", ".", .7f), Key("↵", "ENTER", 1.1f, true)))
        } else {
            val lines = if (mode == Mode.BANGLA_DIRECT)
                listOf(
                    "অআইঈউঊএঐওঔ",
                    "কখগঘঙচছজঝঞ",
                    "টঠডঢণতথদধন",
                    "পফবভমযরলশষসহ",
                    "ািীুূৃেৈোৌংঃ্"
                )
            else listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
            lines.forEach { line ->
                rows.add(line.map { ch ->
                    val label = if (mode == Mode.ENGLISH && shifted) ch.uppercaseChar().toString() else ch.toString()
                    Key(label, ch.toString(), longPressValue = if (mode == Mode.ENGLISH) longPressFor(ch) else null)
                })
            }
            rows.add(listOf(Key("⇧", "SHIFT", 1.2f, true),
                Key(if (mode == Mode.ENGLISH) "EN" else if (mode == Mode.BANGLA_PHONETIC) "বাংলা" else "অক্ষর", "MODE", 1.4f, true),
                Key("⌫", "DELETE", 1.2f, true)))
            val bottomRow = mutableListOf(
                Key("?123", "SYMBOLS", 1.1f, true), Key(",", ",", .7f),
                Key("English / বাংলা", "SPACE", 3.5f, true), Key(".", ".", .7f),
                Key("😊", "EMOJI", .8f, true)
            )
            // Do not display clipboard access at all while a password field is focused.
            if (!passwordField) bottomRow.add(Key("📋", "CLIP", .8f, true))
            bottomRow.add(Key("↵", "ENTER", 1.1f, true))
            rows.add(bottomRow)
        }
        rows.forEach { flatKeys.addAll(it) }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rows.isEmpty()) return
        paint.shader = android.graphics.LinearGradient(0f, 0f, 0f, height.toFloat(),
            backgroundTopColor(), backgroundBottomColor(), android.graphics.Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        paint.color = accentColor()
        paint.alpha = 58
        val stars = arrayOf(0.08f to 0.12f, 0.22f to 0.05f, 0.78f to 0.09f, 0.93f to 0.22f, 0.13f to 0.86f, 0.88f to 0.82f)
        stars.forEachIndexed { index, point ->
            canvas.drawCircle(width * point.first, height * point.second, dp(if (index % 2 == 0) 1.4f else 1f), paint)
        }
        paint.alpha = 255
        val moonX = width - dp(18f)
        val moonY = dp(12f)
        canvas.drawCircle(moonX, moonY, dp(6f), paint)
        paint.color = backgroundTopColor()
        canvas.drawCircle(moonX + dp(3f), moonY - dp(2f), dp(5.5f), paint)
        val pad = dp(5f)
        val top = dp(7f)
        val previewHeight = if (composition.isNotEmpty()) dp(49f) else dp(5f)
        suggestionBounds.clear()
        if (composition.isNotEmpty()) {
            paint.color = accentColor()
            paint.textSize = dp(12f)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(composition + "  →  " + preview, pad * 2, top + dp(15f), paint)
            val shown = suggestions.take(3)
            if (shown.isNotEmpty()) {
                val gap = dp(5f)
                val itemWidth = (width - pad * 2 - gap * (shown.size - 1)) / shown.size
                shown.forEachIndexed { index, word ->
                    val left = pad + index * (itemWidth + gap)
                    val rect = RectF(left, top + dp(23f), left + itemWidth, top + dp(45f))
                    suggestionBounds.add(rect)
                    paint.color = keyColor(special = true)
                    canvas.drawRoundRect(rect, dp(6f), dp(6f), paint)
                    paint.color = keyTextColor()
                    paint.textAlign = Paint.Align.CENTER
                    paint.textSize = dp(13f)
                    canvas.drawText(word, rect.centerX(), rect.centerY() - (paint.ascent() + paint.descent()) / 2, paint)
                }
            }
        }
        val rowHeight = (height - top - previewHeight - pad) / rows.size
        bounds.clear()
        rows.forEachIndexed { rowIndex, row ->
            val gap = dp(4f)
            val total = row.sumOf { it.weight.toDouble() }.toFloat()
            val usable = width - pad * 2 - gap * (row.size - 1)
            var x = pad
            row.forEach { key ->
                val w = usable * key.weight / total
                val rect = RectF(x, top + previewHeight + rowIndex * rowHeight + dp(2f),
                    x + w, top + previewHeight + (rowIndex + 1) * rowHeight - dp(3f))
                bounds.add(rect)
                paint.color = keyColor(special = key.special, modeKey = key.value == "MODE")
                canvas.drawRoundRect(rect, dp(10f), dp(10f), paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp(0.7f)
                paint.color = accentColor()
                paint.alpha = if (key.special) 82 else 48
                canvas.drawRoundRect(rect, dp(10f), dp(10f), paint)
                paint.style = Paint.Style.FILL
                paint.alpha = 255
                paint.color = keyTextColor()
                paint.textAlign = Paint.Align.CENTER
                val preferredTextSize = if (key.label.length > 5) dp(11f) else dp(18f)
                // Fit labels inside short rows (especially 7-row Bangla Direct at 160dp).
                paint.textSize = minOf(preferredTextSize, rect.height() * 0.68f)
                paint.typeface = keyTypeface
                val baseline = rect.centerY() - (paint.ascent() + paint.descent()) / 2
                canvas.drawText(key.label, rect.centerX(), baseline, paint)
                x += w + gap
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downSuggestionIndex = suggestionBounds.indexOfFirst { it.contains(event.x, event.y) }
                downKeyIndex = bounds.indexOfFirst { it.contains(event.x, event.y) }
                downTime = SystemClock.elapsedRealtime()
                swipePath.setLength(0)
                lastSwipeKeyIndex = -1
                swipeTravel = 0f
                swipeDetected = false
                lastTouchX = event.x
                lastTouchY = event.y
                if (swipeEnabled && mode == Mode.ENGLISH && !symbols && !emojiMode && !clipboardMode) appendSwipeKey(event.x, event.y)
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastTouchX
                val dy = event.y - lastTouchY
                swipeTravel += Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
                if (swipeTravel >= dp(38f)) swipeDetected = true
                // Track the path before the swipe threshold is crossed too; otherwise
                // early letters can be lost when the gesture becomes a swipe later.
                if (swipeEnabled && mode == Mode.ENGLISH && !symbols && !emojiMode && !clipboardMode) {
                    appendSwipeSegment(lastTouchX, lastTouchY, event.x, event.y)
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_UP -> {
                // ACTION_UP may be the only event delivered inside the final key.
                // Capture it before matching so the last letter is not silently lost.
                if (swipeEnabled && swipeDetected && mode == Mode.ENGLISH && !symbols && !emojiMode && !clipboardMode) {
                    appendSwipeSegment(lastTouchX, lastTouchY, event.x, event.y)
                }
                if (swipeEnabled && swipeDetected && swipePath.length >= 3) {
                    listener.onKey("SWIPE:$swipePath")
                    downKeyIndex = -1
                    downSuggestionIndex = -1
                    resetSwipeTracking()
                    performClick()
                    return true
                }
                val suggestionIndex = suggestionBounds.indexOfFirst { it.contains(event.x, event.y) }
                if (suggestionIndex >= 0 && suggestionIndex == downSuggestionIndex) {
                    suggestions.getOrNull(suggestionIndex)?.let { listener.onKey("SUGGEST:$it") }
                } else {
                    val index = bounds.indexOfFirst { it.contains(event.x, event.y) }
                    if (index >= 0 && index == downKeyIndex) {
                        if (keySound) playSoundEffect(SoundEffectConstants.CLICK)
                        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        flatKeys.getOrNull(index)?.let { key ->
                            val held = SystemClock.elapsedRealtime() - downTime >= 450L
                            listener.onKey(if (held && key.longPressValue != null) "LONG:${key.longPressValue}" else key.value)
                        }
                    }
                }
                downKeyIndex = -1
                downSuggestionIndex = -1
                resetSwipeTracking()
                performClick()
            }
            MotionEvent.ACTION_CANCEL -> {
                downKeyIndex = -1
                downSuggestionIndex = -1
                resetSwipeTracking()
                return true
            }
        }
        return true
    }

    /**
     * Sample the full pointer segment so fast moves or sparse MotionEvent delivery
     * do not skip letters between two reported touch positions.
     */
    private fun appendSwipeSegment(startX: Float, startY: Float, endX: Float, endY: Float) {
        val dx = endX - startX
        val dy = endY - startY
        val distance = Math.hypot(dx.toDouble(), dy.toDouble()).toFloat()
        val stepSize = dp(8f).coerceAtLeast(1f)
        val steps = kotlin.math.ceil(distance / stepSize).toInt().coerceAtLeast(1)
        for (step in 1..steps) {
            val fraction = step.toFloat() / steps
            appendSwipeKey(startX + dx * fraction, startY + dy * fraction)
        }
    }

    private fun appendSwipeKey(x: Float, y: Float) {
        val index = bounds.indexOfFirst { it.contains(x, y) }
        if (index < 0 || index == lastSwipeKeyIndex) return
        val value = flatKeys.getOrNull(index)?.value ?: return
        if (value.length == 1 && value[0] in 'a'..'z') {
            swipePath.append(value)
            lastSwipeKeyIndex = index
        }
    }

    private fun longPressFor(ch: Char): String? = when (ch.lowercaseChar()) {
        'q' -> "1"; 'w' -> "2"; 'e' -> "3"; 'r' -> "4"; 't' -> "5"
        'y' -> "6"; 'u' -> "7"; 'i' -> "8"; 'o' -> "9"; 'p' -> "0"
        'a' -> "@"; 's' -> "#"; 'd' -> "\$"; 'f' -> "%"; 'g' -> "&"
        'h' -> "-"; 'j' -> "+"; 'k' -> "("; 'l' -> ")"
        'z' -> "!"; 'x' -> "?"; 'c' -> ":"; 'v' -> ";"; 'b' -> "\\"; 'n' -> "'"; 'm' -> "\""
        else -> null
    }

    override fun performClick(): Boolean { super.performClick(); return true }
    private fun dp(value: Float) = value * resources.displayMetrics.density
}
