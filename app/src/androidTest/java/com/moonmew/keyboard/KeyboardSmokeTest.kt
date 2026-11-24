package com.moonmew.keyboard

import android.content.Intent
import android.content.pm.PackageManager
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
    fun setupScreenAndInputMethodRenderOnEmulator() {
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
        device.executeShellCommand("settings put secure show_ime_with_hard_keyboard 1")

        try {
            val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?: error("MoonMew launcher activity was not found")
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(launch)

            assertTrue(
                "Setup screen did not appear",
                device.wait(Until.hasObject(By.text("☾  MoonMew Keyboard")), 10_000)
            )

            // The test editor sits below the fold on a standard emulator. Scroll it
            // into view and focus it directly rather than tapping a helper button
            // whose position may be outside the visible viewport.
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
            editor.click()

            assertTrue(
                "MoonMew keyboard keys did not appear after focusing the test editor",
                device.wait(Until.hasObject(By.desc("Backspace")), 15_000)
            )
        } finally {
            runCatching { device.pressBack() }
            runCatching { device.executeShellCommand("ime reset") }
            runCatching { device.executeShellCommand("settings delete secure show_ime_with_hard_keyboard") }
        }
    }
}
