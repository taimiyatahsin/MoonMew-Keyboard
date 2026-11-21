# MoonMew Keyboard

A lightweight, offline-first Android keyboard with English, Bangla phonetic, and direct Bangla layouts.

## Features

- English QWERTY keyboard and direct Bangla character layout.
- Starter Bangla phonetic typing with live composing preview.
- Emoji panel with Unicode-safe emoji characters.
- Long-press English letter keys for common number and symbol alternatives.
- Adjustable keyboard height (160–320 dp).
- Light/dark keyboard themes and optional key sounds.
- Shift, backspace, space, enter, and symbol layout.
- Optional clipboard history: off by default; explicit opt-in; reads plain text only when the 📋 key is tapped; stores up to 5 items locally for up to 24 hours; disabled in password fields.
- Compact keyboard layout in portrait and landscape (device testing still required).
- Android 6.0 (API 23) and newer.

## Download and install

**[Download the latest MoonMew test APK](https://github.com/taimiyatahsin0-png/MoonMew-Keyboard/releases/download/latest/app-debug.apk)**

If the direct link does not start, open the [Latest Test Build page](https://github.com/taimiyatahsin0-png/MoonMew-Keyboard/releases/tag/latest) and choose `app-debug.apk` under **Assets**.

1. Download `app-debug.apk` on your Android phone.
2. Open the downloaded file. If Android asks, allow installation from that browser or file manager.
3. Tap **Install**.
4. Open **MoonMew Keyboard** and follow the setup instructions.
5. In Android keyboard settings, enable MoonMew and select it as the default keyboard.

On Samsung, the setting is usually **Settings → General management → Keyboard list and default**. Menu names may vary by Android version and manufacturer.

## Privacy

- No `INTERNET` permission, network client, or analytics. Ordinary keystrokes are not stored.
- Optional clipboard history is off by default and requires explicit opt-in in settings. When enabled, tapping 📋 reads the current plain-text clipboard and stores up to 5 items locally for up to 24 hours. Turning it off clears saved items. Do not copy passwords or sensitive information; clipboard history is disabled in password fields.
- Password fields are detected from Android input types and switch to the plain English layout.
- As with any third-party keyboard, avoid using it for highly sensitive input until you have reviewed and tested the app yourself.

## Bangla typing limitations

Phonetic mode currently uses a small deterministic offline mapping. It is an early starter implementation, **not** a complete Avro-compatible engine. Spelling rules, conjuncts, vowel signs, word suggestions, and context-aware conversion still need improvement. Direct Bangla mode currently offers a compact character layout.

## Build from source

1. Open this repository in Android Studio.
2. Allow Gradle sync to finish.
3. Select **Build → Build APK(s)**.
4. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions also builds every push to `main`. Successful main-branch builds automatically update the latest test APK release.

## Test-build notice

This APK is an automatically generated **debug/test build**, not a signed Play Store release. Android may show its normal warning when installing apps from outside the Play Store. Physical-device testing is still required across different manufacturers, screen sizes, and Android versions.
