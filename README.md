# MoonMew Keyboard

A lightweight, offline-first Android keyboard with English, Bangla phonetic, and direct Bangla layouts.

## Features

- English QWERTY keyboard with long-press number and symbol alternatives.
- Hold Backspace to repeat deletions, with a short initial delay and faster repeat cadence for longer holds.
- Bangla phonetic starter mode with live conversion preview and offline word suggestions.
- Dedicated direct Bangla layout with independent vowels, vowel signs on long press, consonants, and alternate characters.
- Emoji panel with categories and Unicode emoji.
- Two-page symbol keyboard, including Bengali punctuation and additional Bangla signs.
- Experimental English swipe typing with local word suggestions.
- Adjustable keyboard height: **140–280 dp** (default: **210 dp**).
- Seven themes: Pink Plush, Moonlit Blue, Lavender Dream, Glass Candy, Moon Garden, Midnight Fur, and Lavender Kitty Plush.
- Midnight Fur and Lavender Kitty Plush use one tiny pre-rendered tiled bitmap texture and a bounded static keyboard-layer cache (up to 1.5 MiB). Dynamic key labels and suggestions stay sharply rendered above the cached layer. Android memory-pressure callbacks release the cache and texture. Reduce Animations disables the fur overlay, bitmap cache, and pressed-key scale effect as a low-memory fallback.
- Soft gradients, glossy rounded keycaps, subtle moon/star details, and compact toolbar.
- Optional key sounds; haptics follow device settings.
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

On Samsung, the setting is usually **Settings → General management → Keyboard list and default**. Menu names may vary by Android version and manufacturer.

## Privacy

- The app requests no `INTERNET` permission and contains no network client or analytics. Ordinary keystrokes are not stored.
- Optional clipboard history is off by default and requires explicit opt-in in settings. When enabled, tapping 📋 reads the current plain-text clipboard and stores up to 5 items locally for up to 24 hours. Turning it off clears saved items. Avoid copying passwords or private information; clipboard history is disabled in password fields.
- Password fields are detected from Android input types and switch to the English layout.
- As with any third-party keyboard, review and test the app before using it for highly sensitive input.

## Known limitations

### Bangla typing

Phonetic mode is a small deterministic offline converter with a growing local dictionary. It is **not** a complete Avro-compatible engine; spelling rules, conjunct handling, vowel signs, and context-aware conversion still need further improvement. Direct Bangla mode is a compact character layout, not a full Gboard-style composition engine.

### Swipe typing

Swipe typing is an experimental English-only prototype using a small on-device word list and edit-distance matching. It asks you to choose a suggested word rather than silently inserting a guess. It is not a Gboard-level swipe engine and needs accuracy and performance testing on physical devices.

### Device compatibility

The project is built for Android 6.0+ and the keyboard limits its height instead of using the full screen. However, compatibility, touch accuracy, layout, and performance still need hands-on testing across manufacturers, Android versions, screen sizes, and apps. Automated CI tests cannot replace physical-device testing.

## Build from source

1. Open this repository in Android Studio.
2. Allow Gradle sync to finish.
3. Select **Build → Build APK(s)**.
4. Find the debug APK at `app/build/outputs/apk/debug/app-debug.apk`.

GitHub Actions runs unit tests and builds a debug APK for pull requests and pushes to `main`. Successful main-branch builds update the latest test APK release.

## Test-build notice

This APK is an automatically generated **debug/test build**, not a signed Play Store release. Android may show its normal warning when installing apps from outside the Play Store.


## Rendering and touch responsiveness

- Key press feedback is shown immediately on touch-down and cleared on release or cancelled gestures.
- Press feedback invalidates the affected key region and uses a short 65 ms, 0.95-scale inset effect; Reduce Animations disables it.
- Key gradients and drawing rectangles are reused to reduce repeated allocations.
- The keycap look uses a small faux bottom lip and a thin highlight instead of real-time blur or shadow layers.
- A full keyboard bitmap cache is intentionally avoided because suggestions, editor actions, and key layouts change dynamically; retaining a large bitmap would add memory pressure on low-RAM devices.
- A stable 60 FPS target must be confirmed with frame-timing/profiling on physical devices; it is not guaranteed by source-level changes alone.
