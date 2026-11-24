# MoonMew Keyboard

A lightweight offline Android keyboard with English, Bangla phonetic and direct Bangla layouts.

## Features

- English QWERTY keyboard with long-press number and symbol alternatives.
- Double-tap Shift within 350 ms to toggle Caps Lock; a normal Shift tap remains one-shot.
- Hold Backspace to repeat deletions, with a short initial delay and faster repeat cadence for longer holds.
- Drag horizontally across the spacebar to move the text cursor; a normal tap still inserts a space.
- Bangla phonetic starter mode with live conversion preview and offline word suggestions.
- Dedicated direct Bangla layout with independent vowels, vowel signs on long press, consonants and alternate characters.
- Emoji panel with categories and Unicode emoji.
- Two-page symbol keyboard, including Bengali punctuation and additional Bangla signs.
- Experimental English swipe typing with local word suggestions.
- Adjustable keyboard height: **140–280 dp** (default: **210 dp**).
- Seven themes: Pink Plush, Moonlit Blue, Lavender Dream, Glass Candy, Moon Garden, Midnight Fur and Lavender Kitty Plush.
- Soft gradients, glossy rounded keycaps, subtle moon/star details and compact toolbar.
- Optional key sounds and a separate light key-vibration toggle; Android's system haptic setting is still respected.
- Optional **Reduce animations & visual effects** setting to skip decorative details and plush fur overlays on lower-end devices.
- Releases optional rendering caches and plush textures when Android reports memory pressure.
- Optional clipboard history: off by default; explicit opt-in; reads plain text only when the clipboard button is tapped; stores up to 5 items locally for up to 24 hours; unavailable in password fields.
- Password fields switch to the English layout and disable swipe and clipboard history.
- Android 6.0 (API 23) and newer.

## Download and install

**[Download the latest MoonMew test APK](https://github.com/taimiyatahsin0-png/MoonMew-Keyboard/releases/download/latest/app-debug.apk)**

If the direct link does not start, open the [Latest Test Build page](https://github.com/taimiyatahsin0-png/MoonMew-Keyboard/releases/tag/latest) and choose `app-debug.apk` under **Assets**.

1. Download `app-debug.apk` on your Android phone.
2. Open the downloaded file. If Android asks, allow installation from that browser or file manager.
3. Tap **Install**.
4. Open **MoonMew Keyboard** and follow the setup instructions.
5. In Android keyboard settings, enable MoonMew and select it as the default keyboard.

Open your device's **Settings** and look for **System**, **Language & input**, or **Keyboard** settings. Enable MoonMew in the available on-screen keyboard list, then select it as the default keyboard. Menu names and locations vary across Android versions and device interfaces.

MoonMew does not need background-running or battery-optimization exemptions to function as an input method.

### Play Protect and sideloading

Install only an APK downloaded from this project's official GitHub Releases page. Android may ask you to allow **Install unknown apps** for the browser or file manager you used; grant that permission only to the app you trust and turn it off again after installation. If Play Protect displays a warning, read the app name and warning carefully. Do not bypass a block or disable Play Protect just to install an APK; stop and verify the release source and file integrity first.

## Privacy

- The app requests no `INTERNET` permission and contains no network client or analytics. Ordinary keystrokes are not stored.
- Optional clipboard history is off by default and requires explicit opt-in in settings. When enabled, tapping 📋 reads the current plain-text clipboard and stores up to 5 items locally for up to 24 hours. Turning it off clears saved items. Avoid copying passwords or private information; clipboard history is disabled in password fields.
- Password fields are detected from Android input types and switch to the English layout.
- As with any third-party keyboard, review and test the app before using it for highly sensitive input.

## Known limitations

### Bangla typing

Phonetic mode is a small deterministic offline converter with a growing local dictionary. It is **not** a complete Avro-compatible engine; spelling rules, conjunct handling, vowel signs and context-aware conversion still need further improvement. Direct Bangla mode is a compact character layout, not a full Gboard-style composition engine.

### Swipe typing

Swipe typing is an experimental English-only prototype using a small on-device word list and edit-distance matching. It asks you to choose a suggested word rather than silently inserting a guess. It is not a Gboard-level swipe engine and needs accuracy and performance testing on physical devices.

### Device compatibility

The project is built for Android 6.0+ and the keyboard limits its height instead of using the full screen. However, compatibility, touch accuracy, layout and performance still need hands-on testing across manufacturers, Android versions, screen sizes and apps. Automated CI tests cannot replace physical-device testing.

Use the [physical-device test checklist](docs/PHYSICAL_DEVICE_TEST_CHECKLIST.md) to record device details, setup, layout, typing, password/privacy, performance and experimental swipe results. Do not mark a device or Android version as tested until it has been exercised on physical hardware.

## Build from source

1. Open this repository in Android Studio.
2. Allow Gradle sync to finish.
3. Select **Build → Build APK(s)**.
4. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions runs unit tests and builds a debug APK for pull requests and pushes to `main`. Successful main-branch builds update the latest test APK release.

## Test-build notice

This APK is an automatically generated **debug/test build**, not a signed Play Store release. Android may show its normal warning when installing apps from outside the Play Store.
