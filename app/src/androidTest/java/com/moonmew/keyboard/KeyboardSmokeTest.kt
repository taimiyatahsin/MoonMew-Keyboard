package com.moonmew.keyboard

import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
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

        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: error("MoonMew launcher activity was not found")
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(launch)

        assertTrue(
            "Setup screen did not appear",
            device.wait(Until.hasObject(By.text("☾  MoonMew Keyboard")), 10_000)
        )
        val testButton = device.findObject(By.text("Try typing in the test box"))
        assertNotNull("Keyboard test button was not shown", testButton)
        testButton.click()

        assertTrue(
            "MoonMew input view did not appear",
            device.wait(Until.hasObject(By.desc("MoonMew keyboard")), 10_000)
        )
        assertTrue(
            "Keyboard accessibility keys were not exposed",
            device.wait(Until.hasObject(By.desc("Backspace")), 5_000)
        )

        // Leave the emulator in a clean state for subsequent tests.
        device.pressBack()
        device.executeShellCommand("ime reset")
        device.executeShellCommand("settings delete secure show_ime_with_hard_keyboard")
    }
}
