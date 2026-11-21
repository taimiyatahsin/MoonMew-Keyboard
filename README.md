# MoonMew Keyboard

A native Android `InputMethodService` keyboard with English, English-letter Bangla phonetic, and direct Bangla modes. The crescent Moon Switch cycles through all three.

## Privacy

- No `INTERNET` permission, network client, analytics, or keystroke storage.
- Password text fields are detected using Android's input type. Secure fields switch to the plain English layout.
- Android controls secure-field display and may hide or alter keyboard behavior. Verify with the device's password fields before relying on any third-party IME.

## Download the Android APK from GitHub

**Easy download:** open [MoonMew Latest Test APK](https://github.com/taimiyatahsin0-png/MoonMew-Keyboard/releases/tag/latest), scroll to **Assets**, and tap `app-debug.apk`. No ZIP extraction is needed. The release is updated automatically after each successful development-branch build.

If the release is not available yet, open the [MoonMew Actions page](https://github.com/taimiyatahsin0-png/MoonMew-Keyboard/actions), select the latest successful run, then download `moonmew-debug-apk` under **Artifacts** and extract `app-debug.apk` from the ZIP.

This is a debug/test APK, not a signed Play Store release. Android may show its normal warning for apps installed outside the Play Store.

## Build locally with Android Studio

1. Open this repository folder in Android Studio and allow Gradle sync.
2. Choose **Build > Build APK(s)**.
3. The debug APK appears at `app/build/outputs/apk/debug/app-debug.apk`.

## Enable the keyboard

On Samsung, open **Settings > General management > Keyboard list and default**, enable MoonMew Keyboard, then select it as the default keyboard. Menu labels may vary by Android/One UI version.

## Current typing behavior

Phonetic mode converts typed Latin letter sequences using an embedded, deterministic offline transliteration table. It is an initial implementation, not a complete Avro-compatible engine; spellings, conjuncts, and context-sensitive vowel signs need further refinement. Direct Bangla mode currently provides a compact character layout.
