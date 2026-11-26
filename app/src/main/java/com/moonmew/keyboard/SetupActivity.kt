package com.moonmew.keyboard

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.graphics.drawable.GradientDrawable

class SetupActivity : Activity() {
    private lateinit var status: TextView
    private val preferences by lazy { getSharedPreferences("moonmew_settings", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val rose = 0xFFFFD5E2.toInt()
        val text = 0xFFF3EAF0.toInt()
        val muted = 0xFFBBAEB8.toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(32), dp(24), dp(28))
            setBackgroundColor(0xFF17151B.toInt())
        }
        val title = TextView(this).apply {
            this.text = "☾  MoonMew Keyboard"
            textSize = 28f
            setTextColor(rose)
            gravity = Gravity.CENTER
        }
        val intro = TextView(this).apply {
            this.text = "A little moonlit keyboard for English and বাংলা.\n\nEnable MoonMew, then choose it as your default keyboard."
            textSize = 15f
            setTextColor(text)
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, dp(20))
        }
        status = TextView(this).apply {
            textSize = 14f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(18))
        }
        val enable = actionButton("1. Enable MoonMew") {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }
        val choose = actionButton("2. Choose MoonMew keyboard") {
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showInputMethodPicker()
        }
        val testTitle = TextView(this).apply {
            this.text = "3. Test MoonMew"
            textSize = 16f
            setTextColor(rose)
            gravity = Gravity.START
            setPadding(0, dp(24), 0, dp(8))
        }
        val testHint = TextView(this).apply {
            this.text = "After selecting MoonMew, tap the box and try English, বাংলা, emoji and your favourite theme."
            textSize = 13f
            setTextColor(muted)
            setPadding(0, 0, 0, dp(8))
        }
        val testBox = EditText(this).apply {
            hint = "Type here to test MoonMew…"
            textSize = 16f
            minLines = 2
            maxLines = 4
            setTextColor(0xFF302541.toInt())
            setHintTextColor(0xFF806F7C.toInt())
            setPadding(dp(14), dp(12), dp(14), dp(12))
            background = GradientDrawable().apply {
                setColor(0xFFFFF8FC.toInt())
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), 0xFFFFB8D0.toInt())
            }
        }
        val testButton = actionButton("Try typing in the test box") {
            testBox.requestFocus()
            val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            manager.showSoftInput(testBox, InputMethodManager.SHOW_IMPLICIT)
        }
        val settingsTitle = TextView(this).apply {
            this.text = "PERSONALISE YOUR KEYBOARD"
            textSize = 12f
            setTextColor(rose)
            setPadding(0, dp(28), 0, dp(8))
        }
        val heightLabel = TextView(this).apply {
            textSize = 14f
            setTextColor(text)
            setPadding(0, dp(10), 0, dp(2))
        }
        val heightControl = SeekBar(this).apply {
            max = 140
            progress = (preferences.getInt("keyboard_height_dp", 210).coerceIn(140, 280) - 140)
            heightLabel.text = "Keyboard height: ${progress + 140} dp"
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    heightLabel.text = "Keyboard height: ${progress + 140} dp"
                    if (fromUser) preferences.edit().putInt("keyboard_height_dp", progress + 140).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        val themeNames = listOf("Pink Plush", "Moonlit Blue", "Lavender Dream", "Glass Candy", "Moon Garden", "Midnight Fur", "Lavender Kitty Plush")
        val themePalettes = listOf(
            intArrayOf(0xFFFFEAF1.toInt(), 0xFFF7C9DA.toInt(), 0xFFFFF8F5.toInt(), 0xFFFFD5E3.toInt(), 0xFF805365.toInt(), 0xFFFF8FB9.toInt()),
            intArrayOf(0xFF172B43.toInt(), 0xFF0B1422.toInt(), 0xFF1D2B3E.toInt(), 0xFF2B405B.toInt(), 0xFFEAF5FF.toInt(), 0xFFB9DFFF.toInt()),
            intArrayOf(0xFF30213F.toInt(), 0xFF171020.toInt(), 0xFF302541.toInt(), 0xFF49365F.toInt(), 0xFFF4EAFF.toInt(), 0xFFE0C8FF.toInt()),
            intArrayOf(0xFFFFD9EC.toInt(), 0xFFFFB9D9.toInt(), 0xFFFFF8FC.toInt(), 0xFFFFC4E0.toInt(), 0xFF713B60.toInt(), 0xFFFFFFFF.toInt()),
            intArrayOf(0xFF24213D.toInt(), 0xFF111329.toInt(), 0xFF292743.toInt(), 0xFF39365D.toInt(), 0xFFF0ECFF.toInt(), 0xFFB9A7FF.toInt()),
            intArrayOf(0xFF17151B.toInt(), 0xFF09090F.toInt(), 0xFF211F29.toInt(), 0xFF39313D.toInt(), 0xFFF7EAF0.toInt(), 0xFFF4DCE6.toInt()),
            intArrayOf(0xFFD8C2E8.toInt(), 0xFFF4D7E8.toInt(), 0xFFFFF8FC.toInt(), 0xFFDCC8EC.toInt(), 0xFF49334E.toInt(), 0xFFC7A9E2.toInt())
        )
        fun selectedTheme(): Int = preferences.getInt("theme_id", if (preferences.getBoolean("rose_theme", true)) 0 else 1).coerceIn(themeNames.indices)
        fun previewKey(label: String, palette: IntArray, special: Boolean = false): TextView {
            val fill = if (special) palette[3] else palette[2]
            return TextView(this).apply {
                text = label
                textSize = 10f
                gravity = Gravity.CENTER
                setTextColor(palette[4])
                setPadding(dp(2), dp(5), dp(2), dp(5))
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                    if (special) palette[5] else ColorMath.blend(palette[2], 0xFFFFFFFF.toInt(), 0.28f),
                    fill
                )).apply {
                    cornerRadius = dp(7).toFloat()
                    setStroke(dp(1), palette[5])
                }
                elevation = dp(2).toFloat()
            }
        }
        fun keyboardPreview(id: Int, compact: Boolean = false): LinearLayout {
            val p = themePalettes[id.coerceIn(themePalettes.indices)]
            val sample = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(if (compact) 7 else 10), dp(if (compact) 7 else 10), dp(if (compact) 7 else 10), dp(if (compact) 7 else 10))
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(p[0], p[1])).apply { cornerRadius = dp(14).toFloat() }
            }
            val top = LinearLayout(this).apply { gravity = Gravity.CENTER; orientation = LinearLayout.HORIZONTAL }
            listOf("☾ MoonMew", "✦", "🎀").forEach { label ->
                val v = TextView(this).apply { text = label; textSize = if (compact) 9f else 11f; setTextColor(p[4]); gravity = Gravity.CENTER; setPadding(dp(4), dp(2), dp(4), dp(4)) }
                top.addView(v, LinearLayout.LayoutParams(0, -2, if (label.startsWith("☾")) 2f else 0.6f))
            }
            sample.addView(top)
            val lines = listOf("1234567890", "QWERTYUIOP", "ASDFGHJKL", "⇧ZXCVBNM⌫", "☻  ,      English     ↵")
            lines.forEachIndexed { rowIndex, line ->
                val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER }
                val chars = when (rowIndex) {
                    0 -> line.map { it.toString() }
                    1 -> line.map { it.toString() }
                    2 -> line.map { it.toString() }
                    3 -> listOf("⇧") + "ZXCVBNM".map { it.toString() } + listOf("⌫")
                    else -> listOf("☻", ",", "English", "↵")
                }
                chars.forEachIndexed { index, label ->
                    val special = rowIndex == 4 || (rowIndex == 3 && (index == 0 || index == chars.lastIndex))
                    row.addView(previewKey(label, p, special), LinearLayout.LayoutParams(0, -2, when {
                        rowIndex == 4 && label == "English" -> 3f
                        special -> 1.25f
                        else -> 1f
                    }).apply { setMargins(dp(2), dp(2), dp(2), dp(2)) })
                }
                sample.addView(row)
            }
            return sample
        }
        val themePreview = keyboardPreview(selectedTheme())
        fun updateThemePreview(id: Int) {
            val parent = themePreview.parent
            if (parent is LinearLayout) {
                val index = parent.indexOfChild(themePreview)
                parent.removeViewAt(index)
                parent.addView(keyboardPreview(id), index, matchWithTop(dp(8)))
            }
        }
        fun themeButtonLabel(): String = "🎨  Explore & preview all ${themeNames.size} themes"
        lateinit var themeButton: Button
        themeButton = actionButton(themeButtonLabel()) {
            val choices = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(14), dp(8), dp(14), dp(8)) }
            val dialog = AlertDialog.Builder(this).setTitle("Explore MoonMew themes")
                .setView(ScrollView(this).apply { addView(choices) }).setNegativeButton("Close", null).create()
            themeNames.forEachIndexed { index, name ->
                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(dp(10), dp(10), dp(10), dp(10))
                    background = GradientDrawable().apply {
                        setColor(0x18FFFFFF)
                        cornerRadius = dp(16).toFloat()
                        setStroke(dp(1), if (selectedTheme() == index) 0xFFFF8FB9.toInt() else 0x33777777)
                    }
                }
                val heading = TextView(this).apply {
                    text = (if (selectedTheme() == index) "✓  " else "") + name
                    textSize = 15f
                    setTextColor(0xFFF7EAF0.toInt())
                    setPadding(dp(4), dp(2), dp(4), dp(7))
                }
                card.addView(heading)
                card.addView(keyboardPreview(index, true), LinearLayout.LayoutParams(-1, -2))
                card.setOnClickListener {
                    preferences.edit().putInt("theme_id", index).putBoolean("rose_theme", index == 0).apply()
                    updateThemePreview(index)
                    themeButton.text = themeButtonLabel() + " · " + name
                    dialog.dismiss()
                }
                choices.addView(card, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
            }
            dialog.show()
        }
        val soundSwitch = Switch(this).apply {
            this.text = "Key sounds"
            textSize = 15f
            setTextColor(text)
            isChecked = preferences.getBoolean("key_sound", false)
            setOnCheckedChangeListener { _, checked ->
                preferences.edit().putBoolean("key_sound", checked).apply()
            }
        }
        val hapticSwitch = Switch(this).apply {
            this.text = "Light key vibration"
            textSize = 15f
            setTextColor(text)
            isChecked = preferences.getBoolean("key_haptics", true)
            setOnCheckedChangeListener { _, checked ->
                preferences.edit().putBoolean("key_haptics", checked).apply()
            }
        }

        val reduceEffectsSwitch = Switch(this).apply {
            this.text = "Reduce animations & visual effects"
            textSize = 14f
            setTextColor(text)
            isChecked = preferences.getBoolean("reduce_animations", false)
            setOnCheckedChangeListener { _, checked ->
                preferences.edit().putBoolean("reduce_animations", checked).apply()
            }
        }
        val clipboardSwitch = Switch(this).apply {
            this.text = "Optional clipboard history (local only)"
            textSize = 14f
            setTextColor(text)
            isChecked = preferences.getBoolean("clipboard_history_enabled", false)
            setOnCheckedChangeListener { _, checked ->
                preferences.edit().putBoolean("clipboard_history_enabled", checked).apply()
                if (!checked) preferences.edit().remove("clipboard_history").apply()
            }
        }
        val clipboardWarning = TextView(this).apply {
            this.text = "Off by default. When enabled, tapping 📋 reads plain text from the clipboard and keeps up to 5 items on this device for up to 24 hours. Avoid copying passwords or private information. Disabled in password fields."
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(6), 0, dp(4))
        }
        val note = TextView(this).apply {
            this.text = "Sound is off by default. Haptic feedback follows your device settings."
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(8), 0, 0)
        }
        root.addView(title, match())
        root.addView(intro, match())
        root.addView(status, match())
        root.addView(enable, matchWithTop(dp(8)))
        root.addView(choose, matchWithTop(dp(10)))
        root.addView(testTitle, matchWithTop(dp(4)))
        root.addView(testHint, match())
        root.addView(testBox, match())
        root.addView(testButton, matchWithTop(dp(8)))
        root.addView(settingsTitle, matchWithTop(dp(8)))
        root.addView(heightLabel, matchWithTop(dp(4)))
        root.addView(heightControl, match())
        root.addView(themeButton, matchWithTop(dp(6)))
        root.addView(themePreview, matchWithTop(dp(8)))
        root.addView(soundSwitch, matchWithTop(dp(6)))
        root.addView(reduceEffectsSwitch, matchWithTop(dp(8)))
        root.addView(hapticSwitch, matchWithTop(dp(4)))
        root.addView(clipboardSwitch, matchWithTop(dp(10)))
        root.addView(clipboardWarning, matchWithTop(dp(2)))
        root.addView(note, matchWithTop(dp(4)))
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(root)
        })
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshStatus()
    }

    private fun refreshStatus() {
        // InputMethodInfo.id and DEFAULT_INPUT_METHOD use the short component form.
        val component = ComponentName(this, MoonMewImeService::class.java).flattenToShortString()
        val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = manager.enabledInputMethodList.any { it.id == component }
        val selected = Settings.Secure.getString(contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD) == component
        status.text = "Keyboard: " + if (selected) "Selected as default ✓" else if (enabled) "Enabled — choose it as default" else "Not enabled yet"
    }

    private fun actionButton(label: String, action: () -> Unit) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 14f
        setTextColor(0xFF2B2028.toInt())
        backgroundTintList = ColorStateList.valueOf(0xFFFFD5E2.toInt())
        setOnClickListener { action() }
    }

    private fun match() = LinearLayout.LayoutParams(-1, -2)
    private fun matchWithTop(top: Int) = LinearLayout.LayoutParams(-1, -2).apply { topMargin = top }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}