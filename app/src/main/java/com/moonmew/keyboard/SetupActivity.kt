package com.moonmew.keyboard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class SetupActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(28, 36, 28, 36)
            setBackgroundColor(0xFF17151B.toInt())
        }
        val title = TextView(this).apply {
            text = "☾  MoonMew Keyboard"
            textSize = 28f
            setTextColor(0xFFFFD5E2.toInt())
            gravity = Gravity.CENTER
        }
        val intro = TextView(this).apply {
            text = "A little moonlit keyboard for English and বাংলা.\n\nEnable MoonMew, then choose it as your default keyboard."
            textSize = 16f
            setTextColor(0xFFF3EAF0.toInt())
            gravity = Gravity.CENTER
            setPadding(0, 22, 0, 30)
        }
        val enable = Button(this).apply {
            text = "1. Enable MoonMew"
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        }
        val choose = Button(this).apply {
            text = "2. Choose MoonMew keyboard"
            setOnClickListener {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.showInputMethodPicker()
            }
        }
        root.addView(title, LinearLayout.LayoutParams(-1, -2))
        root.addView(intro, LinearLayout.LayoutParams(-1, -2))
        root.addView(enable, LinearLayout.LayoutParams(-1, -2))
        root.addView(choose, LinearLayout.LayoutParams(-1, -2))
        setContentView(root)
    }
}
