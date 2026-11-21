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
    private var roseTheme = preferences.getBoolean("rose_theme", true)
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
        setBackgroundColor(if (roseTheme) 0xFF17151B.toInt() else 0xFF101820.toInt())
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
        roseTheme = preferences.getBoolean("rose_theme", true)
        keySound = preferences.getBoolean("key_sound", false)
        setBackgroundColor(if (roseTheme) 0xFF17151B.toInt() else 0xFF101820.toInt())
        applyHeight()
        invalidate()
    }

    fun setSwipeEnabled(value: Boolean) { swipeEnabled = value; if (!value) { swipePath.setLength(0); swipeDetected = false } }
    fun setMode(value: Mode) { mode = value; symbols = false; emojiMode = false; clipboardMode = false; symbolsPage = 0; rebuild(); invalidate() }
    fun setShifted(value: Boolean) { shifted = value; rebuild(); invalidate() }
    fun setSymbols(value: Boolean) { symbols = value; emojiMode = false; clipboardMode = false; symbolsPage = 0; rebuild(); invalidate() }
    fun setEmoji(value: Boolean) { emojiMode = value; clipboardMode = false; symbols = false; rebuild(); invalidate() }
    fun setClipboard(value: Boolean) { clipboardMode = value; emojiMode = false; symbols = false; rebuild(); invalidate() }
    fun setClipboardItems(value: List<String>) { clipboardItems = value.take(5); invalidate() }
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
                listOf("ািীুূৃেৈোৌংঃ", "কখগঘঙচছজঝঞ", "টঠডঢণতথদধন", "পফবভমযরলশষসহ")
            else listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
            lines.forEach { line ->
                rows.add(line.map { ch ->
                    val label = if (mode == Mode.ENGLISH && shifted) ch.uppercaseChar().toString() else ch.toString()
                    Key(label, ch.toString(), longPressValue = if (mode == Mode.ENGLISH) longPressFor(ch) else null)
                })
            }
            rows.add(listOf(Key("⇧", "SHIFT", 1.2f, true), Key("☾", "MODE", 1.1f, true),
                Key(if (mode == Mode.ENGLISH) "EN" else if (mode == Mode.BANGLA_PHONETIC) "বাংলা" else "অক্ষর", "MODE", 1.4f, true),
                Key("⌫", "DELETE", 1.2f, true)))
            rows.add(listOf(Key("?123", "SYMBOLS", 1.1f, true), Key(",", ",", .7f),
                Key("English / বাংলা", "SPACE", 3.5f, true), Key(".", ".", .7f), Key("😊", "EMOJI", .8f, true), Key("📋", "CLIP", .8f, true), Key("↵", "ENTER", 1.1f, true)))
        }
        rows.forEach { flatKeys.addAll(it) }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rows.isEmpty()) return
        val pad = dp(5f)
        val top = dp(7f)
        val previewHeight = if (composition.isNotEmpty()) dp(49f) else dp(5f)
        suggestionBounds.clear()
        if (composition.isNotEmpty()) {
            paint.color = 0xFFE9B7C8.toInt()
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
                    paint.color = if (roseTheme) 0xFF49303E.toInt() else 0xFF293D4B.toInt()
                    canvas.drawRoundRect(rect, dp(6f), dp(6f), paint)
                    paint.color = if (roseTheme) 0xFFFFF0F5.toInt() else 0xFFEAF4FA.toInt()
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
                paint.color = if (roseTheme) {
                    if (key.value == "MODE") 0xFF704356.toInt() else if (key.special) 0xFF49303E.toInt() else 0xFF30232E.toInt()
                } else {
                    if (key.value == "MODE") 0xFF36566A.toInt() else if (key.special) 0xFF293D4B.toInt() else 0xFF202D38.toInt()
                }
                canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)
                paint.color = if (roseTheme) 0xFFFFF0F5.toInt() else 0xFFEAF4FA.toInt()
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = if (key.label.length > 5) dp(11f) else dp(18f)
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
                lastTouchX = event.x
                lastTouchY = event.y
                if (swipeTravel >= dp(38f)) swipeDetected = true
                if (swipeEnabled && swipeDetected && mode == Mode.ENGLISH && !symbols && !emojiMode && !clipboardMode) appendSwipeKey(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                if (swipeEnabled && swipeDetected && swipePath.length >= 3) {
                    listener.onKey("SWIPE:$swipePath")
                    downKeyIndex = -1
                    downSuggestionIndex = -1
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
                swipePath.setLength(0)
                swipeDetected = false
                performClick()
            }
            MotionEvent.ACTION_CANCEL -> { downKeyIndex = -1; downSuggestionIndex = -1; swipePath.setLength(0); swipeDetected = false; return true }
        }
        return true
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
