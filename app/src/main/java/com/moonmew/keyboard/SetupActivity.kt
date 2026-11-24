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
            this.text = "After selecting MoonMew, tap the box and try English, বাংলা, emoji, and your favourite theme."
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
        val themePreview = TextView(this).apply {
            textSize = 21f
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(16), dp(14), dp(16))
        }
        val themeNames = listOf("Pink Plush", "Moonlit Blue", "Lavender Dream", "Glass Candy", "Moon Garden", "Midnight Fur", "Lavender Kitty Plush")
        val themeColors = listOf(
            0xFFFFD5E2.toInt() to 0xFFFFF8F5.toInt(),
            0xFF416B8B.toInt() to 0xFF1D2B3E.toInt(),
            0xFF795A99.toInt() to 0xFF302541.toInt(),
            0xFFFF83BA.toInt() to 0xFFFFF8FC.toInt(),
            0xFF7866C8.toInt() to 0xFF292743.toInt(),
            0xFF17151B.toInt() to 0xFF09090F.toInt(),
            0xFFD8C2E8.toInt() to 0xFFF4D7E8.toInt()
        )
        fun updateThemePreview(id: Int) {
            val pair = themeColors[id.coerceIn(0, 6)]
            themePreview.text = "🐱   Aa   ♥   ☾   🎀"
            themePreview.setTextColor(if (id == 0 || id == 3 || id == 6) 0xFF49334E.toInt() else 0xFFF8F2FF.toInt())
            themePreview.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(pair.first, pair.second)).apply { cornerRadius = dp(18).toFloat() }
        }
        fun selectedTheme(): Int = preferences.getInt("theme_id", if (preferences.getBoolean("rose_theme", true)) 0 else 1).coerceIn(0, 6)
        updateThemePreview(selectedTheme())
        lateinit var themeButton: Button
        themeButton = actionButton("🎨  Browse all 7 themes  ·  " + themeNames[selectedTheme()]) {
            val choices = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(8)) }
            val dialog = AlertDialog.Builder(this).setTitle("Choose your MoonMew look")
                .setView(ScrollView(this).apply { addView(choices) }).setNegativeButton("Close", null).create()
            themeNames.forEachIndexed { index, name ->
                val pair = themeColors[index]
                val option = TextView(this).apply {
                    this.text = (if (selectedTheme() == index) "✓  " else "") + "🐱  " + name + "     Aa  ♥  ☾  🎀"
                    textSize = 16f; gravity = Gravity.CENTER_VERTICAL
                    setTextColor(if (index == 0 || index == 3 || index == 6) 0xFF49334E.toInt() else 0xFFF8F2FF.toInt())
                    setPadding(dp(14), dp(17), dp(14), dp(17))
                    background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(pair.first, pair.second)).apply { cornerRadius = dp(16).toFloat() }
                    setOnClickListener {
                        preferences.edit().putInt("theme_id", index).putBoolean("rose_theme", index == 0).apply()
                        updateThemePreview(index)
                        themeButton.text = "🎨  Browse all 5 themes  ·  " + name
                        dialog.dismiss()
                    }
                }
                choices.addView(option, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(9) })
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