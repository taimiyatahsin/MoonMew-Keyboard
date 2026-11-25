package com.moonmew.keyboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityNodeInfo
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeyboardSmokeTest {
    @Test
    fun setupScreenAndInputMethodWindowShowsOnEmulator() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        val component = "${context.packageName}/.MoonMewImeService"

        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        assertFalse(
            "MoonMew must not request internet access",
            packageInfo.requestedPermissions.orEmpty().contains("android.permission.INTERNET")
        )

        device.executeShellCommand("ime enable $component")
        device.executeShellCommand("ime set $component")

        val scenario = ActivityScenario.launch(SetupActivity::class.java)
        try {
            // Read the actual activity view tree directly. UIAutomator's package
            // selector can miss this app window on API 35 even after ActivityTaskManager
            // reports SetupActivity as displayed, causing a false-negative smoke test.
            scenario.onActivity { activity ->
                val contentView = activity.findViewById<View>(android.R.id.content)
                val editor = findFirstEditText(contentView)
                assertNotNull("Test editor was not present in SetupActivity", editor)
                assertTrue("Test editor was not clickable", editor!!.isClickable)
                editor.requestFocus()
                val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.showSoftInput(editor, InputMethodManager.SHOW_IMPLICIT)
            }
            device.waitForIdle(5_000)
            SystemClock.sleep(500)

            // Confirm the Android input-method manager actually selected and showed
            // MoonMew's IME window rather than relying on UIAutomator's window lookup.
            var imeState = ""
            var keyboardWindowShown = false
            for (attempt in 0 until 30) {
                imeState = runCatching {
                    device.executeShellCommand("dumpsys input_method")
                }.getOrDefault("")
                val currentMethod = Regex("""mCurMethodId=([^;\s]+)""")
                    .find(imeState)?.groupValues?.getOrNull(1)
                val inputShown = Regex("""mInputShown=(true|false)""")
                    .find(imeState)?.groupValues?.getOrNull(1) == "true"
                if (currentMethod == component && inputShown) {
                    keyboardWindowShown = true
                    break
                }
                SystemClock.sleep(500)
            }
            val activeIme = runCatching {
                device.executeShellCommand("settings get secure default_input_method").trim()
            }.getOrDefault("unavailable")
            assertTrue(
                "MoonMew IME window was not shown. Active IME: $activeIme. Input method state: " +
                    imeState.lineSequence()
                        .filter { it.contains("mCurMethodId") || it.contains("mInputShown") || it.contains("mShowRequested") }
                        .take(12)
                        .joinToString("; "),
                keyboardWindowShown
            )
        } finally {
            scenario.close()
            runCatching { device.pressBack() }
            runCatching { device.executeShellCommand("ime reset") }
        }
    }

    private fun findFirstEditText(view: View): EditText? {
        if (view is EditText) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findFirstEditText(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    @Test
    fun virtualAccessibilityKeyCanBeReadAndActivated() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activatedKeys = mutableListOf<String>()
        val surface = KeyboardSurface(context, object : KeyboardSurface.Listener {
            override fun onKey(key: String) {
                activatedKeys.add(key)
            }
        })
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels.coerceAtLeast(1)
        val height = (210f * metrics.density).toInt().coerceAtLeast(1)

        surface.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        surface.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            surface.draw(Canvas(bitmap))
            val provider = surface.accessibilityNodeProvider
            // Toolbar actions precede letter keys in the virtual-node list, so
            // locate the q key by its accessible label instead of assuming ID 0.
            val qKeyId = (0..40).firstOrNull { virtualId ->
                provider.createAccessibilityNodeInfo(virtualId)?.text?.toString() == "q"
            }

            assertNotNull("Q key should be exposed as a virtual accessibility node", qKeyId)
            assertTrue(
                "Virtual keyboard key should support accessibility activation",
                provider.performAction(qKeyId!!, AccessibilityNodeInfo.ACTION_CLICK, null)
            )
            assertEquals(listOf("q"), activatedKeys)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun virtualAccessibilitySuggestionCanBeReadAndActivated() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activatedKeys = mutableListOf<String>()
        val surface = KeyboardSurface(context, object : KeyboardSurface.Listener {
            override fun onKey(key: String) {
                activatedKeys.add(key)
            }
        })
        // Suggestions are rendered only while a composition is active.
        surface.setComposition("he")
        surface.setSuggestions(listOf("hello", "help"))
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels.coerceAtLeast(1)
        val height = (210f * metrics.density).toInt().coerceAtLeast(1)

        surface.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        surface.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            surface.draw(Canvas(bitmap))
            val provider = surface.accessibilityNodeProvider
            val suggestionId = (0..80).firstOrNull { virtualId ->
                provider.createAccessibilityNodeInfo(virtualId)?.contentDescription
                    ?.toString() == "Suggestion: hello"
            }

            assertNotNull("Suggestion should be exposed with a spoken label", suggestionId)
            assertTrue(
                "Suggestion should support accessibility activation",
                provider.performAction(suggestionId!!, AccessibilityNodeInfo.ACTION_CLICK, null)
            )
            assertEquals(listOf("SUGGEST:hello"), activatedKeys)
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun alternateCharacterCanBeActivatedWithAccessibilityLongClick() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val activatedKeys = mutableListOf<String>()
        val surface = KeyboardSurface(context, object : KeyboardSurface.Listener {
            override fun onKey(key: String) {
                activatedKeys.add(key)
            }
        })
        val metrics = context.resources.displayMetrics
        val width = metrics.widthPixels.coerceAtLeast(1)
        val height = (210f * metrics.density).toInt().coerceAtLeast(1)

        surface.measure(
            View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
        )
        surface.layout(0, 0, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        try {
            surface.draw(Canvas(bitmap))
            val provider = surface.accessibilityNodeProvider
            val alternateKeyId = (0..80).firstOrNull { virtualId ->
                provider.createAccessibilityNodeInfo(virtualId)?.isLongClickable == true
            }

            assertNotNull("At least one alternate-character key should expose long-click", alternateKeyId)
            assertTrue(
                "Alternate character should support accessibility long-click",
                provider.performAction(alternateKeyId!!, AccessibilityNodeInfo.ACTION_LONG_CLICK, null)
            )
            assertTrue(
                "Long-click should dispatch the alternate character",
                activatedKeys.singleOrNull()?.startsWith("LONG:") == true
            )
        } finally {
            bitmap.recycle()
        }
    }

}
