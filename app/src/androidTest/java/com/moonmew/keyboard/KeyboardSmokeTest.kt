package com.moonmew.keyboard

import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
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

        try {
            val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?: error("MoonMew launcher activity was not found")
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(launch)

            assertTrue(
                "Setup screen did not appear",
                device.wait(Until.hasObject(By.pkg(context.packageName)), 15_000)
            )

            // The editor sits below the fold on a standard emulator. Scroll it into
            // view and tap the actual EditText, matching the normal user interaction.
            val scrollable = UiScrollable(UiSelector().scrollable(true)).apply {
                setAsVerticalList()
                setMaxSearchSwipes(8)
            }
            assertTrue(
                "Test editor was not found on the setup screen",
                scrollable.scrollIntoView(UiSelector().className("android.widget.EditText"))
            )
            val editor: UiObject = device.findObject(UiSelector().className("android.widget.EditText"))
            assertNotNull("Test editor was not available", editor)
            device.waitForIdle(5_000)
            assertTrue("Test editor was not clickable", editor.isClickable)
            editor.click()
            device.waitForIdle(2_000)

            // UIAutomator does not consistently expose the IME's custom virtual
            // accessibility nodes as part of the app window hierarchy. Confirm the
            // Android input-method manager actually selected and showed our IME
            // window instead of treating a missing accessibility node as no keyboard.
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
            runCatching { device.pressBack() }
            runCatching { device.executeShellCommand("ime reset") }
        }
    }
}
