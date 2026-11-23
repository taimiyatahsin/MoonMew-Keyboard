package com.moonmew.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import android.os.Bundle
import android.graphics.Typeface
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.os.SystemClock
import android.view.SoundEffectConstants
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo

class KeyboardSurface(context: Context, private val listener: Listener) : View(context) {
    interface Listener { fun onKey(key: String) }
    enum class Mode { ENGLISH, BANGLA_PHONETIC, BANGLA_DIRECT }
    private data class Key(val label: String, val value: String, val weight: Float = 1f, val special: Boolean = false, val longPressValue: String? = null)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundGradient: android.graphics.LinearGradient? = null
    private var backgroundGradientWidth = -1
    private var backgroundGradientHeight = -1
    private var backgroundGradientTop = 0
    private var backgroundGradientBottom = 0
    private data class CachedKeyGradient(val bounds: RectF, val topColor: Int, val bottomColor: Int, val shader: android.graphics.LinearGradient)
    private val keyGradientCache = mutableListOf<CachedKeyGradient>()
    // Reused draw rectangles avoid allocating temporary RectF objects for every key on every frame.
    private val shadowRect = RectF()
    private val highlightRect = RectF()
    private val pawOvalRect = RectF()
    // Immutable normalized decoration points are created once, not on every redraw.
    private val starPoints = arrayOf(0.08f to 0.12f, 0.22f to 0.05f, 0.78f to 0.09f, 0.93f to 0.22f, 0.13f to 0.86f, 0.88f to 0.82f)
    private val candyAccentPoints = arrayOf(0.08f to 0.08f, 0.27f to 0.07f, 0.73f to 0.07f, 0.92f to 0.08f, 0.05f to 0.92f, 0.95f to 0.92f)
    private val pawPrintPoints = arrayOf(0.16f to 0.18f, 0.84f to 0.18f, 0.10f to 0.78f, 0.90f to 0.78f)
    private val keyTypeface = Typeface.create("sans-serif-rounded", Typeface.NORMAL)
    private val preferences = context.getSharedPreferences("moonmew_settings", Context.MODE_PRIVATE)
    private var themeId = preferences.getInt("theme_id", if (preferences.getBoolean("rose_theme", true)) 0 else 1)
    private var keySound = preferences.getBoolean("key_sound", false)
    private var reduceAnimations = preferences.getBoolean("reduce_animations", false)
    private var mode = Mode.ENGLISH
    private var shifted = false
    private var symbols = false
    private var emojiMode = false
    private var emojiCategory = 0
    private var clipboardMode = false
    private var clipboardItems = emptyList<String>()
    private var downKeyIndex = -1
    private var pressedKeyIndex = -1
    private var downSuggestionIndex = -1
    private var downTime = 0L
    private val swipePath = StringBuilder()
    private var lastSwipeKeyIndex = -1
    private var swipeTravel = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var swipeDetected = false
    private var multiTouchCancelled = false
    private var swipeEnabled = true
    private var passwordField = false
    private var numericInput = false
    private var numericDecimal = false
    private var numericSigned = false
    private var editorAction = EditorInfo.IME_ACTION_NONE
    private var symbolsPage = 0
    private var composition = ""
    private var preview = ""
    private var compositionPreviewText = ""
    private var suggestions = emptyList<String>()
    private val bounds = mutableListOf<RectF>()
    private val suggestionBounds = mutableListOf<RectF>()
    private val rows = mutableListOf<List<Key>>()
    private val flatKeys = mutableListOf<Key>()

    // Keep hit-test rectangles across frames; rebuild() clears them when key structure changes.
    private fun backgroundShader(): android.graphics.LinearGradient {
        val topColor = backgroundTopColor()
        val bottomColor = backgroundBottomColor()
        val cached = backgroundGradient
        if (cached == null || backgroundGradientWidth != width || backgroundGradientHeight != height ||
            backgroundGradientTop != topColor || backgroundGradientBottom != bottomColor) {
            return android.graphics.LinearGradient(0f, 0f, 0f, height.toFloat(), topColor, bottomColor,
                android.graphics.Shader.TileMode.CLAMP).also {
                backgroundGradient = it
                backgroundGradientWidth = width
                backgroundGradientHeight = height
                backgroundGradientTop = topColor
                backgroundGradientBottom = bottomColor
            }
        }
        return cached
    }

    private fun keyShader(index: Int, rect: RectF, topColor: Int, bottomColor: Int): android.graphics.LinearGradient {
        while (keyGradientCache.size <= index) {
            keyGradientCache.add(CachedKeyGradient(RectF(), 0, 0,
                android.graphics.LinearGradient(0f, 0f, 0f, 1f, 0, 0, android.graphics.Shader.TileMode.CLAMP)))
        }
        val cached = keyGradientCache[index]
        if (cached.topColor == topColor && cached.bottomColor == bottomColor && cached.bounds == rect) return cached.shader
        val boundsCopy = RectF(rect)
        val shader = android.graphics.LinearGradient(rect.left, rect.top, rect.left, rect.bottom,
            topColor, bottomColor, android.graphics.Shader.TileMode.CLAMP)
        keyGradientCache[index] = CachedKeyGradient(boundsCopy, topColor, bottomColor, shader)
        return shader
    }

    private fun obtainRect(pool: MutableList<RectF>, index: Int): RectF {
        while (pool.size <= index) pool.add(RectF())
        return pool[index]
    }

    private fun trimRectPool(pool: MutableList<RectF>, used: Int) {
        while (pool.size > used) pool.removeAt(pool.lastIndex)
    }

    private val keyboardAccessibilityProvider = object : AccessibilityNodeProvider() {
        override fun createAccessibilityNodeInfo(virtualViewId: Int): AccessibilityNodeInfo? {
            if (virtualViewId == HOST_VIEW_ID) {
                return AccessibilityNodeInfo.obtain(this@KeyboardSurface).apply {
                    className = KeyboardSurface::class.java.name
                    packageName = context.packageName
                    contentDescription = "MoonMew keyboard"
                    setSource(this@KeyboardSurface)
                    val parentView = this@KeyboardSurface.parent
                    if (parentView is View) setParent(parentView)
                    for (index in bounds.indices) addChild(this@KeyboardSurface, index)
                    for (index in suggestionBounds.indices) addChild(this@KeyboardSurface, flatKeys.size + index)
                }
            }
            val suggestionIndex = virtualViewId - flatKeys.size
            val isSuggestion = suggestionIndex >= 0
            val rect = if (isSuggestion) suggestionBounds.getOrNull(suggestionIndex) else bounds.getOrNull(virtualViewId)
            if (rect == null) return null
            val label = if (isSuggestion) suggestions.getOrNull(suggestionIndex) else flatKeys.getOrNull(virtualViewId)?.label
            return AccessibilityNodeInfo.obtain().apply {
                setSource(this@KeyboardSurface, virtualViewId)
                setParent(this@KeyboardSurface)
                packageName = context.packageName
                className = "android.widget.Button"
                text = label ?: "Keyboard key"
                contentDescription = label ?: "Keyboard key"
                isEnabled = true
                isClickable = true
        importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
                addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK)
                val nodeBounds = Rect()
                rect.roundOut(nodeBounds)
                setBoundsInParent(nodeBounds)
            }
        }

        override fun performAction(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
            if (action != AccessibilityNodeInfo.ACTION_CLICK) return false
            if (virtualViewId == HOST_VIEW_ID) return this@KeyboardSurface.performAccessibilityAction(action, arguments)
            if (virtualViewId >= flatKeys.size) {
                val word = suggestions.getOrNull(virtualViewId - flatKeys.size) ?: return false
                listener.onKey("SUGGEST:$word")
            } else {
                val key = flatKeys.getOrNull(virtualViewId) ?: return false
                listener.onKey(key.value)
            }
            return true
        }
    }

    init {
        isClickable = true
        setBackgroundColor(backgroundBottomColor())
        applyHeight()
        rebuild()
    }

    private fun configuredHeightDp(): Int =
        preferences.getInt("keyboard_height_dp", 210).coerceIn(140, 280)

    private fun applyHeight() {
        val params = layoutParams ?: ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, dp(configuredHeightDp().toFloat()).toInt()
        )
        params.height = dp(configuredHeightDp().toFloat()).toInt()
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams = params
        requestLayout()
    }

    // IME parents on some Android builds measure their input view at almost the
    // full display height. Do not let that turn the keyboard into a full-screen view.
    // A resize changes every key's hitbox. Clear old geometry before another touch
    // can be dispatched; onDraw will repopulate the pools using the new dimensions.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w != oldw || h != oldh) {
            // A finger may still be down while Android resizes the IME. Cancel that
            // gesture too: onDraw can repopulate the same indices before ACTION_UP.
            multiTouchCancelled = true
            downKeyIndex = -1
            downSuggestionIndex = -1
            setPressedKey(-1)
            resetSwipeTracking()
            bounds.clear()
            suggestionBounds.clear()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val displayHeightPx = resources.displayMetrics.heightPixels
        val screenCapPx = (displayHeightPx * 0.44f).toInt().coerceAtLeast(dp(120f).toInt())
        val parentCapPx = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            screenCapPx
        } else {
            MeasureSpec.getSize(heightMeasureSpec).coerceAtLeast(1)
        }
        val desiredPx = dp(configuredHeightDp().toFloat()).toInt()
        val measuredHeight = minOf(desiredPx, screenCapPx, parentCapPx).coerceAtLeast(1)
        val measuredWidth = if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            resources.displayMetrics.widthPixels.coerceAtLeast(suggestedMinimumWidth)
        } else {
            MeasureSpec.getSize(widthMeasureSpec)
        }
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    fun refreshPreferences() {
        themeId = preferences.getInt("theme_id", if (preferences.getBoolean("rose_theme", true)) 0 else 1).coerceIn(0, 4)
        keySound = preferences.getBoolean("key_sound", false)
        reduceAnimations = preferences.getBoolean("reduce_animations", false)
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
        3 -> 0xFFFFD9EC.toInt()
        4 -> 0xFF24213D.toInt()
        else -> 0xFFFFEAF1.toInt()
    }
    private fun backgroundBottomColor() = when (themeId) {
        1 -> 0xFF0B1422.toInt()
        2 -> 0xFF171020.toInt()
        3 -> 0xFFFFB9D9.toInt()
        4 -> 0xFF111329.toInt()
        else -> 0xFFF7C9DA.toInt()
    }
    private fun accentColor() = when (themeId) {
        1 -> 0xFFB9DFFF.toInt()
        2 -> 0xFFE0C8FF.toInt()
        3 -> 0xFFFFFFFF.toInt()
        4 -> 0xFFB9A7FF.toInt()
        else -> 0xFFFF8FB9.toInt()
    }
    private fun keyColor(special: Boolean, modeKey: Boolean = false) = when (themeId) {
        1 -> if (modeKey) 0xFF416B8B.toInt() else if (special) 0xFF2B405B.toInt() else 0xFF1D2B3E.toInt()
        2 -> if (modeKey) 0xFF795A99.toInt() else if (special) 0xFF49365F.toInt() else 0xFF302541.toInt()
        3 -> if (modeKey) 0xFFFF83BA.toInt() else if (special) 0xFFFFC4E0.toInt() else 0xFFFFF8FC.toInt()
        4 -> if (modeKey) 0xFF7866C8.toInt() else if (special) 0xFF39365D.toInt() else 0xFF292743.toInt()
        else -> if (modeKey) 0xFFFFA9C8.toInt() else if (special) 0xFFFFD5E3.toInt() else 0xFFFFF8F5.toInt()
    }
    private fun keyTextColor() = when (themeId) {
        1 -> 0xFFEAF5FF.toInt()
        2 -> 0xFFF4EAFF.toInt()
        3 -> 0xFF713B60.toInt()
        4 -> 0xFFF0ECFF.toInt()
        else -> 0xFF805365.toInt()
    }

    fun setSwipeEnabled(value: Boolean) { swipeEnabled = value; if (!value) resetSwipeTracking() }
    fun setNumericInput(value: Boolean, decimal: Boolean = false, signed: Boolean = false) {
        numericInput = value
        numericDecimal = decimal
        numericSigned = signed
        rebuild()
        invalidate()
    }
    fun setEditorAction(value: Int) { editorAction = EditorActionPolicy.keyboardAction(value); rebuild(); invalidate() }
    private fun editorActionLabel(): String = when (editorAction) {
        EditorInfo.IME_ACTION_GO -> "Go"
        EditorInfo.IME_ACTION_SEARCH -> "Search"
        EditorInfo.IME_ACTION_SEND -> "Send"
        EditorInfo.IME_ACTION_NEXT -> "Next"
        EditorInfo.IME_ACTION_DONE -> "Done"
        EditorInfo.IME_ACTION_PREVIOUS -> "Prev"
        else -> "↵"
    }
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
    fun setEmoji(value: Boolean) { emojiMode = value; if (value) emojiCategory = 0; clipboardMode = false; symbols = false; rebuild(); invalidate() }
    fun setEmojiCategory(value: Int) { emojiCategory = value.coerceIn(0, 7); if (emojiMode) { rebuild(); invalidate() } }
    fun setClipboard(value: Boolean) { clipboardMode = value; emojiMode = false; symbols = false; rebuild(); invalidate() }
    fun setClipboardItems(value: List<String>) { clipboardItems = value.take(5); if (clipboardMode) rebuild(); invalidate() }
    fun nextSymbolsPage() { if (symbols) { symbolsPage = 1 - symbolsPage; rebuild(); invalidate() } }
    private fun updateCompositionPreviewText() {
        compositionPreviewText = if (composition.isEmpty()) "" else "$composition  →  $preview"
    }
    fun setComposition(value: String) {
        composition = value
        updateCompositionPreviewText()
        suggestionBounds.clear()
        invalidate()
    }
    fun setPreview(value: String) {
        preview = value
        updateCompositionPreviewText()
        invalidate()
    }
    fun setSuggestions(value: List<String>) { suggestions = value.take(3); suggestionBounds.clear(); invalidate() }

    private fun rebuild() {
        // A layout rebuild invalidates the geometry captured by an in-flight touch.
        // Cancel it so ACTION_UP cannot activate a different key at the same index.
        multiTouchCancelled = true
        downKeyIndex = -1
        downSuggestionIndex = -1
        setPressedKey(-1)
        resetSwipeTracking()
        rows.clear()
        flatKeys.clear()
        // Old hitboxes must not point at a newly rebuilt key list before the next draw.
        bounds.clear()
        suggestionBounds.clear()
        keyGradientCache.clear()
        if (clipboardMode) {
            if (clipboardItems.isEmpty()) {
                val enabled = preferences.getBoolean("clipboard_history_enabled", false)
                rows.add(listOf(Key(
                    if (enabled) "No saved clips" else "Enable in MoonMew settings",
                    if (enabled) "CLIP_EMPTY" else "CLIP_NOTICE",
                    1f, true
                )))
                rows.add(listOf(Key("ABC", "CLIP_CLOSE", 1f, true)))
            } else {
                clipboardItems.forEachIndexed { index, item ->
                    rows.add(listOf(Key(item.replace("\n", " ").take(32), "CLIP_ITEM:$index", 1f, true)))
                }
                rows.add(listOf(Key("Clear history", "CLIP_CLEAR", 1.4f, true), Key("ABC", "CLIP_CLOSE", 1f, true)))
            }
        } else if (emojiMode) {
            // Expanded offline emoji groups: faces, animals, nature, food,
            // activities, travel, objects and hearts/symbols.
            val categories = listOf(
                "😀 😃 😄 😁 😆 😅 😂 🤣 🥲 ☺️ 😊 😇 🙂 🙃 😉 😌 😍 🥰 😘 😗 😙 😚 😋 😛 😝 😜 🤪 🤨 🧐 🤓 😎 🥸 🤩 🥳 😏 😒 😞 😔 😟 😕 🙁 ☹️ 😣 😖 😫 😩 🥺 😢 😭 😤 😠 😡 🤯".split(" "),
                "🐱 🐈 🐈‍⬛ 🐶 🐕 🐩 🐺 🦊 🦝 🐻 🐻‍❄️ 🐼 🐨 🐯 🦁 🐮 🐷 🐸 🐵 🙈 🙉 🙊 🐔 🐧 🐦 🐤 🐣 🦆 🦉 🦇 🐴 🦄 🐝 🦋 🐌 🐞 🐢 🐍 🦎 🐙 🦀 🐠 🐬 🐳 🦈".split(" "),
                "🌸 💐 🌷 🌹 🥀 🌺 🌻 🌼 🌵 🌲 🌳 🌴 🌱 🌿 ☘️ 🍀 🍁 🍂 🍃 🍄 🌾 💧 💦 🌊 🌈 ☀️ 🌤️ ⛅ 🌥️ ☁️ 🌦️ 🌧️ ⛈️ 🌩️ ❄️ ☃️ ⛄ 🌙 🌛 🌜 ⭐ 🌟 ✨ ⚡ 🔥 💥".split(" "),
                "🍏 🍎 🍐 🍊 🍋 🍌 🍉 🍇 🍓 🫐 🍈 🍒 🍑 🥭 🍍 🥥 🥝 🍅 🍆 🥑 🥦 🥬 🥒 🌶️ 🫑 🌽 🥕 🫒 🧄 🧅 🥔 🍠 🥐 🥯 🍞 🥖 🧀 🥚 🍳 🧈 🥞 🧇 🥓 🥩 🍗 🍖 🌭 🍔 🍟 🍕".split(" "),
                "⚽ 🏀 🏈 ⚾ 🥎 🎾 🏐 🏉 🎱 🏓 🏸 🥅 🏒 🏑 🥍 🏏 🪃 🥊 🥋 🎣 🤿 🎽 🛹 🛼 🛷 ⛸️ 🥌 🎿 ⛷️ 🏂 🪂 🏋️ 🤼 🤸 ⛹️ 🤺 🏇 🧘 🏄 🏊 🚴".split(" "),
                "🚗 🚕 🚙 🚌 🚎 🏎️ 🚓 🚑 🚒 🚐 🛻 🚚 🚛 🚜 🛵 🏍️ 🚲 🛴 🚏 🛣️ 🛤️ 🚨 🚥 🚦 🛑 🚧 ⚓ ⛵ 🚤 🛳️ ⛴️ 🛥️ 🚢 ✈️ 🛩️ 🛫 🛬 🪂 💺 🚁 🚟 🚠 🚡 🚀 🛸".split(" "),
                "⌚ 📱 📲 💻 ⌨️ 🖥️ 🖨️ 🖱️ 🖲️ 💽 💾 💿 📀 📷 📸 📹 🎥 📞 ☎️ 📺 📻 🎙️ 🎚️ 🎛️ 🧭 ⏱️ ⏰ ⌛ 🔋 🪫 💡 🔦 🕯️ 🧯 🛒 🎁 🎀 🪄 🧸 🪅 🎈 🎉 🎊 🪩".split(" "),
                "❤️ 🩷 🧡 💛 💚 🩵 💙 💜 🤎 🖤 🩶 🤍 💔 ❤️‍🔥 ❤️‍🩹 💕 💞 💓 💗 💖 💘 💝 ❣️ 💟 ☮️ ✝️ ☪️ 🕉️ ☸️ ✡️ 🔯 🕎 ☯️ ☦️ 🛐 ⛎ ♈ ♉ ♊ ♋ ♌ ♍ ♎ ♏ ♐ ♑ ♒ ♓".split(" ")
            )
            rows.add(listOf("😀", "🐱", "🌿", "🍓", "⚽", "🚗", "💡", "♡").mapIndexed { index, label ->
                Key(label, "EMOJI_CAT:$index", 1f, true)
            })
            categories[emojiCategory].chunked(8).forEach { line ->
                rows.add(line.map { emoji -> Key(emoji, emoji) })
            }
            rows.add(listOf(Key("ABC", "EMOJI_BACK", 1.2f, true), Key("⌫", "DELETE", 1.2f, true), Key("Space", "SPACE", 3.6f, true)))
        } else if (numericInput && !symbols) {
            // Dedicated number-pad layout for numeric editors; avoids showing a
            // full QWERTY board where only numeric input is accepted.
            listOf("123", "456", "789").forEach { line ->
                rows.add(line.map { ch -> Key(ch.toString(), ch.toString()) })
            }
            val bottom = mutableListOf<Key>()
            if (numericSigned) bottom.add(Key("−", "-", 1f, true))
            bottom.add(Key("0", "0"))
            if (numericDecimal) bottom.add(Key(".", "."))
            bottom.add(Key("⌫", "DELETE", 1.2f, true))
            bottom.add(Key("🐾 ${editorActionLabel()}", "ENTER", 1.2f, true))
            rows.add(bottom)
        } else if (symbols) {
            val lines = if (symbolsPage == 0)
                listOf("1234567890", "!@#$%^&*()", "-_=+[]{}<>", "?/;:,.।")
            else
                listOf("1234567890", "`£€¥₹¢©®", "±×÷√∞≠≤≥", "ঁংঃ্৽ৎড়ঢ়য়")
            lines.forEach { line -> rows.add(line.map { ch ->
                    val alternate = when (ch) {
                        'ড' -> "ড়"; 'ঢ' -> "ঢ়"; 'য' -> "য়"; 'ত' -> "ৎ"
                        else -> null
                    }
                    Key(ch.toString(), ch.toString(), longPressValue = alternate)
                }) }
            rows.add(listOf(Key("ABC", "ABC", 1.2f, true), Key("☾", "MODE", 1.1f, true),
                Key("⌫", "DELETE", 1.2f, true)))
            rows.add(listOf(Key(if (symbolsPage == 0) "=\\<" else "123", "SYMBOLS_MORE", 1.1f, true),
                Key(",", ",", .7f), Key("English / বাংলা", "SPACE", 4.6f, true),
                Key(".", ".", .7f), Key("🐾 ${editorActionLabel()}", "ENTER", 1.1f, true)))
        } else if (mode == Mode.BANGLA_DIRECT) {
            // Dedicated compact Direct Bangla layout: four character rows and one action row.
            // Independent vowels stay on tap; their matching vowel signs are available on long press.
            val vowels = listOf(
                "অ" to null, "আ" to "া", "ই" to "ি", "ঈ" to "ী", "উ" to "ু",
                "ঊ" to "ূ", "ঋ" to "ৃ", "এ" to "ে", "ঐ" to "ৈ", "ও" to "ো", "ঔ" to "ৌ"
            )
            rows.add(vowels.map { (label, alternate) ->
                Key(label, label, longPressValue = alternate)
            })
            listOf("কখগঘঙচছজঝঞ", "টঠডঢণতথদধন", "পফবভমযরলশষসহ").forEach { line ->
                rows.add(line.map { ch ->
                    val alternate = when (ch) {
                        'ড' -> "ড়"; 'ঢ' -> "ঢ়"; 'য' -> "য়"; 'ত' -> "ৎ"
                        else -> null
                    }
                    Key(ch.toString(), ch.toString(), longPressValue = alternate)
                })
            }
            rows.add(listOf(
                Key("?123", "SYMBOLS", 1.05f, true),
                Key("EN", "MODE", 0.9f, true),
                Key("ং", "ং", 0.8f),
                Key("ঃ", "ঃ", 0.8f),
                Key("⌫", "DELETE", 1.0f, true),
                Key("Space", "SPACE", 2.8f, true),
                Key("🐾 ${editorActionLabel()}", "ENTER", 0.9f, true)
            ))
        } else {
            // A compact action strip keeps common tools reachable without adding
            // another full-height row of oversized controls.
            rows.add(buildList {
                add(Key("🐱 MoonMew", "TOOLBAR_BRAND", 2.1f, true))
                add(Key("😊", "EMOJI", 0.9f, true))
                if (!passwordField) add(Key("📋", "CLIP", 0.9f, true))
                add(Key("বাংলা", "MODE", 1.1f, true))
            })
            listOf("qwertyuiop", "asdfghjkl", "zxcvbnm").forEach { line ->
                rows.add(line.map { ch ->
                    val label = if (shifted) ch.uppercaseChar().toString() else ch.toString()
                    Key(label, ch.toString(), longPressValue = longPressFor(ch))
                })
            }
            rows.add(listOf(Key("⇧", "SHIFT", 1.2f, true),
                Key("EN", "MODE", 1.4f, true), Key("⌫", "DELETE", 1.2f, true)))
            rows.add(listOf(
                Key("?123", "SYMBOLS", 1.1f, true), Key(",", ",", .7f),
                Key("☾  English / বাংলা", "SPACE", 4.2f, true),
                Key(".", ".", .7f), Key(editorActionLabel(), "ENTER", 1.1f, true)
            ))
        }
        rows.forEach { flatKeys.addAll(it) }
    }

    override fun getAccessibilityNodeProvider(): AccessibilityNodeProvider = keyboardAccessibilityProvider

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (rows.isEmpty()) return
        paint.shader = backgroundShader()
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        paint.color = accentColor()
        paint.alpha = 58
        starPoints.forEachIndexed { index, point ->
            canvas.drawCircle(width * point.first, height * point.second, dp(if (index % 2 == 0) 1.4f else 1f), paint)
        }
        paint.alpha = 255
        // Decorative candy dots and tiny bow-like accents for the kawaii palettes.
        if (themeId == 0 || themeId == 3) {
            candyAccentPoints.forEachIndexed { index, point ->
                val cx = width * point.first
                val cy = height * point.second
                paint.color = if (index % 2 == 0) 0xFFFF8FB9.toInt() else 0xFFFFFFFF.toInt()
                paint.alpha = 185
                canvas.drawCircle(cx - dp(2f), cy, dp(2.4f), paint)
                canvas.drawCircle(cx + dp(2f), cy, dp(2.4f), paint)
                canvas.drawCircle(cx, cy, dp(1.5f), paint)
            }
            // Tiny paw-print motifs add a recognisable kitty detail without
            // covering keys or competing with the labels.
            pawPrintPoints.forEachIndexed { index, point ->
                val cx = width * point.first
                val cy = height * point.second
                val paw = if (index % 2 == 0) 0xFFFF8FB9.toInt() else 0xFFFFFFFF.toInt()
                paint.color = paw
                paint.alpha = 115
                val r = dp(2.2f)
                canvas.drawCircle(cx - r * 1.35f, cy - r * 1.15f, r, paint)
                canvas.drawCircle(cx, cy - r * 1.65f, r, paint)
                canvas.drawCircle(cx + r * 1.35f, cy - r * 1.15f, r, paint)
                pawOvalRect.set(cx - r * 1.5f, cy - r * 0.4f, cx + r * 1.5f, cy + r * 1.65f)
                canvas.drawOval(pawOvalRect, paint)
            }
            paint.alpha = 255
        }
        val moonX = width - dp(18f)
        val moonY = dp(12f)
        canvas.drawCircle(moonX, moonY, dp(6f), paint)
        paint.color = backgroundTopColor()
        canvas.drawCircle(moonX + dp(3f), moonY - dp(2f), dp(5.5f), paint)
        val pad = dp(5f)
        val top = dp(7f)
        val previewHeight = if (composition.isNotEmpty()) dp(49f) else dp(5f)
        var suggestionRectCount = 0
        if (composition.isNotEmpty()) {
            paint.color = accentColor()
            paint.textSize = dp(12f)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(compositionPreviewText, pad * 2, top + dp(15f), paint)
            val shownCount = minOf(suggestions.size, 3)
            if (shownCount > 0) {
                val gap = dp(5f)
                val itemWidth = (width - pad * 2 - gap * (shownCount - 1)) / shownCount
                for (index in 0 until shownCount) {
                    val word = suggestions[index]
                    val left = pad + index * (itemWidth + gap)
                    val rect = obtainRect(suggestionBounds, index).apply {
                        set(left, top + dp(23f), left + itemWidth, top + dp(45f))
                    }
                    suggestionRectCount++
                    paint.color = keyColor(special = true)
                    canvas.drawRoundRect(rect, dp(6f), dp(6f), paint)
                    paint.color = keyTextColor()
                    paint.textAlign = Paint.Align.CENTER
                    paint.textSize = dp(13f)
                    canvas.drawText(word, rect.centerX(), rect.centerY() - (paint.ascent() + paint.descent()) / 2, paint)
                }
            }
        }
        trimRectPool(suggestionBounds, suggestionRectCount)
        val rowHeight = (height - top - previewHeight - pad) / rows.size
        var keyRectCount = 0
        rows.forEachIndexed { rowIndex, row ->
            val gap = dp(4f)
            val total = row.sumOf { it.weight.toDouble() }.toFloat()
            val usable = width - pad * 2 - gap * (row.size - 1)
            var x = pad
            row.forEach { key ->
                val w = usable * key.weight / total
                val rect = obtainRect(bounds, keyRectCount++).apply {
                    set(x, top + previewHeight + rowIndex * rowHeight + dp(2f),
                        x + w, top + previewHeight + (rowIndex + 1) * rowHeight - dp(3f))
                }
                val radius = dp(if (themeId == 3) 13f else 10f)
                // Soft offset edge gives the keycap a tactile, raised appearance.
                paint.color = if (themeId == 1 || themeId == 2) 0x66000000 else 0x55B65F83
                shadowRect.set(rect.left, rect.top + dp(2f), rect.right, rect.bottom + dp(2f))
                canvas.drawRoundRect(shadowRect, radius, radius, paint)
                val base = keyColor(special = key.special, modeKey = key.value == "MODE")
                val topTint = if (themeId == 1 || themeId == 2) 0xFF64718A.toInt() else 0xFFFFFFFF.toInt()
                paint.shader = keyShader(keyRectCount - 1, rect, topTint, base)
                canvas.drawRoundRect(rect, radius, radius, paint)
                paint.shader = null
                if (keyRectCount - 1 == pressedKeyIndex) {
                    // Instant tactile feedback without animation, blur, or shadow layers.
                    paint.color = if (themeId == 1 || themeId == 2) 0x22FFFFFF else 0x18000000
                    canvas.drawRoundRect(rect, radius, radius, paint)
                }
                // A small glossy highlight makes pink and glass keys feel like crafted keycaps.
                paint.color = if (themeId == 1 || themeId == 2) 0x33FFFFFF else 0x99FFFFFF.toInt()
                highlightRect.set(rect.left + dp(3f), rect.top + dp(2f), rect.right - dp(3f), rect.top + dp(5f))
                canvas.drawRoundRect(highlightRect, dp(3f), dp(3f), paint)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = dp(if (themeId == 3) 1.2f else 0.8f)
                paint.color = accentColor()
                paint.alpha = if (key.special) 190 else 125
                canvas.drawRoundRect(rect, radius, radius, paint)
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
        trimRectPool(bounds, keyRectCount)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                multiTouchCancelled = false
                downSuggestionIndex = suggestionBounds.indexOfFirst { it.contains(event.x, event.y) }
                downKeyIndex = bounds.indexOfFirst { it.contains(event.x, event.y) }
                setPressedKey(downKeyIndex)
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
            MotionEvent.ACTION_POINTER_DOWN -> {
                // A second finger can otherwise turn one gesture into an accidental key press.
                // Cancel the entire gesture until every pointer is lifted.
                multiTouchCancelled = true
                downKeyIndex = -1
                downSuggestionIndex = -1
                setPressedKey(-1)
                resetSwipeTracking()
            }
            MotionEvent.ACTION_UP -> {
                if (multiTouchCancelled) {
                    multiTouchCancelled = false
                    downKeyIndex = -1
                    downSuggestionIndex = -1
                    setPressedKey(-1)
                    resetSwipeTracking()
                    return true
                }
                // Include the final segment in distance detection too. Some devices may
                // deliver DOWN and UP without an intermediate MOVE for a very fast swipe.
                val finalDx = event.x - lastTouchX
                val finalDy = event.y - lastTouchY
                swipeTravel += Math.hypot(finalDx.toDouble(), finalDy.toDouble()).toFloat()
                if (swipeTravel >= dp(38f)) swipeDetected = true
                // ACTION_UP may be the only event delivered inside the final key.
                // Capture it before matching so the last letter is not silently lost.
                if (swipeEnabled && swipeDetected && mode == Mode.ENGLISH && !symbols && !emojiMode && !clipboardMode) {
                    appendSwipeSegment(lastTouchX, lastTouchY, event.x, event.y)
                }
                if (swipeEnabled && swipeDetected && swipePath.length >= 3) {
                    listener.onKey("SWIPE:$swipePath")
                    downKeyIndex = -1
                    downSuggestionIndex = -1
                    setPressedKey(-1)
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
                setPressedKey(-1)
                resetSwipeTracking()
                performClick()
            }
            MotionEvent.ACTION_CANCEL -> {
                multiTouchCancelled = false
                downKeyIndex = -1
                downSuggestionIndex = -1
                setPressedKey(-1)
                resetSwipeTracking()
                return true
            }
        }
        return true
    }

    private fun setPressedKey(index: Int) {
        // Low-end devices can disable the pressed-key visual overlay entirely.
        val visibleIndex = if (reduceAnimations) -1 else index
        if (pressedKeyIndex == visibleIndex) return
        bounds.getOrNull(pressedKeyIndex)?.let { old ->
            invalidate(old.left.toInt(), old.top.toInt(), old.right.toInt() + 1, old.bottom.toInt() + 1)
        }
        pressedKeyIndex = visibleIndex
        bounds.getOrNull(pressedKeyIndex)?.let { current ->
            invalidate(current.left.toInt(), current.top.toInt(), current.right.toInt() + 1, current.bottom.toInt() + 1)
        }
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
        // Bound work for unusually large MotionEvent jumps on slow devices.
        val steps = kotlin.math.ceil(distance / stepSize).toInt().coerceIn(1, 64)
        for (step in 1..steps) {
            if (swipePath.length >= 32) return
            val fraction = step.toFloat() / steps
            appendSwipeKey(startX + dx * fraction, startY + dy * fraction)
        }
    }

    private fun appendSwipeKey(x: Float, y: Float) {
        val index = bounds.indexOfFirst { it.contains(x, y) }
        if (index < 0 || index == lastSwipeKeyIndex) return
        val value = flatKeys.getOrNull(index)?.value ?: return
        if (value.length == 1 && value[0] in 'a'..'z') {
            if (swipePath.length < 32) swipePath.append(value)
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
