package com.moonmew.keyboard

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View

class KeyboardSurface(context: Context, private val listener: Listener) : View(context) {
    interface Listener { fun onKey(key: String) }
    enum class Mode { ENGLISH, BANGLA_PHONETIC, BANGLA_DIRECT }
    private data class Key(val label: String, val value: String, val weight: Float = 1f, val special: Boolean = false)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mode = Mode.ENGLISH
    private var shifted = false
    private var symbols = false
    private var composition = ""
    private var preview = ""
    private val bounds = mutableListOf<RectF>()
    private val rows = mutableListOf<List<Key>>()

    init { setBackgroundColor(0xFF17151B.toInt()); rebuild() }
    fun setMode(value: Mode) { mode = value; symbols = false; rebuild(); invalidate() }
    fun setShifted(value: Boolean) { shifted = value; rebuild(); invalidate() }
    fun setSymbols(value: Boolean) { symbols = value; rebuild(); invalidate() }
    fun setComposition(value: String) { composition = value; invalidate() }
    fun setPreview(value: String) { preview = value; invalidate() }

    private fun rebuild() {
        rows.clear()
        if (symbols) {
            listOf("1234567890", "!@#$%^&*()", "-_=+[]{}<>", "?/;:,.~").forEach { line ->
                rows.add(line.map { ch -> Key(ch.toString(), ch.toString()) })
            }
            rows.add(listOf(Key("ABC", "ABC", 1.2f, true), Key("☾", "MODE", 1.1f, true),
                Key("⌫", "DELETE", 1.2f, true)))
            rows.add(listOf(Key("=\\<", "SYMBOLS_MORE", 1.1f, true), Key(",", ",", .7f),
                Key("English / বাংলা", "SPACE", 4.6f, true), Key(".", "." , .7f), Key("↵", "ENTER", 1.1f, true)))
            return
        }
        val lines = if (mode == Mode.BANGLA_DIRECT)
            listOf("ািীুূৃেৈোৌংঃ", "কখগঘঙচছজঝঞ", "টঠডঢণতথদধন", "পফবভমযরলশষসহ")
        else listOf("qwertyuiop", "asdfghjkl", "zxcvbnm")
        lines.forEach { line ->
            rows.add(line.map { ch ->
                val label = if (mode == Mode.ENGLISH && shifted) ch.uppercaseChar().toString() else ch.toString()
                Key(label, ch.toString())
            })
        }
        rows.add(listOf(Key("⇧","SHIFT",1.2f,true), Key("☾","MODE",1.1f,true),
            Key(if (mode == Mode.ENGLISH) "EN" else if (mode == Mode.BANGLA_PHONETIC) "বাংলা" else "অক্ষর","MODE",1.4f,true),
            Key("⌫","DELETE",1.2f,true)))
        rows.add(listOf(Key("?123","SYMBOLS",1.1f,true), Key(",",",",.7f),
            Key("English / বাংলা","SPACE",4.6f,true), Key(".", ".",.7f), Key("↵","ENTER",1.1f,true)))
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val pad = dp(5f)
        val top = dp(7f)
        val previewHeight = if (composition.isNotEmpty()) dp(27f) else dp(5f)
        if (composition.isNotEmpty()) {
            paint.color = 0xFFE9B7C8.toInt()
            paint.textSize = dp(14f)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(composition + "   " + preview, pad * 2, top + dp(17f), paint)
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
                paint.color = if (key.value == "MODE") 0xFF563B50.toInt() else if (key.special) 0xFF39333F.toInt() else 0xFF29252E.toInt()
                canvas.drawRoundRect(rect, dp(8f), dp(8f), paint)
                paint.color = 0xFFF6EDF1.toInt()
                paint.textAlign = Paint.Align.CENTER
                paint.textSize = if (key.label.length > 5) dp(11f) else dp(18f)
                paint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                val baseline = rect.centerY() - (paint.ascent() + paint.descent()) / 2
                canvas.drawText(key.label, rect.centerX(), baseline, paint)
                x += w + gap
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val index = bounds.indexOfFirst { it.contains(event.x, event.y) }
            if (index >= 0) rows.flatten().getOrNull(index)?.let { listener.onKey(it.value) }
            performClick()
            return true
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
    private fun dp(value: Float) = value * resources.displayMetrics.density
}
