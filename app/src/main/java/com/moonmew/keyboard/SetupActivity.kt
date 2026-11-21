package com.moonmew.keyboard

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

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
            max = 160
            progress = (preferences.getInt("keyboard_height_dp", 240).coerceIn(160, 320) - 160)
            heightLabel.text = "Keyboard height: ${progress + 160} dp"
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    heightLabel.text = "Keyboard height: ${progress + 160} dp"
                    if (fromUser) preferences.edit().putInt("keyboard_height_dp", progress + 160).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        val roseSwitch = Switch(this).apply {
            this.text = "Moonlit rose theme"
            textSize = 15f
            setTextColor(text)
            isChecked = preferences.getBoolean("rose_theme", true)
            setOnCheckedChangeListener { _, checked ->
                preferences.edit().putBoolean("rose_theme", checked).apply()
            }
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
        root.addView(settingsTitle, matchWithTop(dp(8)))
        root.addView(heightLabel, matchWithTop(dp(4)))
        root.addView(heightControl, match())
        root.addView(roseSwitch, matchWithTop(dp(6)))
        root.addView(soundSwitch, matchWithTop(dp(6)))
        root.addView(clipboardSwitch, matchWithTop(dp(10)))
        root.addView(clipboardWarning, matchWithTop(dp(2)))
        root.addView(note, matchWithTop(dp(4)))
        setContentView(root)
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        if (::status.isInitialized) refreshStatus()
    }

    private fun refreshStatus() {
        val component = ComponentName(this, MoonMewImeService::class.java).flattenToString()
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