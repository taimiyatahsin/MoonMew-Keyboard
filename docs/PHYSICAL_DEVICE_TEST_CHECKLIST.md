# Physical-device test checklist

Automated unit tests and APK builds do not verify real keyboard behavior. Use this checklist on physical devices before treating compatibility or swipe typing as complete.

## Record each test session

- Device model:
- Android version / security patch:
- Screen resolution and display size setting:
- MoonMew APK version or GitHub release date:
- Test app and field type:
- Keyboard height setting:
- Theme and Reduce animations setting:
- Result: Pass / Fail / Not tested
- Notes (describe behavior only; do not record passwords or private text):

## Priority device matrix

| Priority | Device / Android | Focus |
|---|---|---|
| P0 | One lower-end Android phone, using its installed Android version | Baseline performance, touch accuracy and memory use |
| P1 | One small-screen Android phone (Android 6–10 if available) | Minimum supported OS, compact layout |
| P1 | One Android 11–13 phone | IME compatibility and permission/settings flow |
| P1 | One Android 14–16 phone, where available | Current platform compatibility |
| P2 | One larger-screen phone or tablet | Width, height, orientation and layout |

Do not mark a version as tested unless it was exercised on a physical device.

## Setup and layout

- [ ] Install the APK from the official GitHub Releases page.
- [ ] Enable MoonMew in system keyboard settings and select it as default.
- [ ] Open the keyboard in portrait and landscape.
- [ ] Confirm it does not cover the entire screen and the editor remains visible.
- [ ] Test minimum (140 dp), default (210 dp) and maximum (280 dp) heights.
- [ ] Rotate the device and reopen the keyboard; confirm it remains correctly sized.
- [ ] Switch away from and back to MoonMew; confirm no stale pressed key or panel remains.

## Typing and editing

Test in a notes app, messaging app, browser search field and a multiline text field.

- [ ] English letters, uppercase, one-shot Shift and double-tap Caps Lock.
- [ ] Bangla phonetic conversion, suggestions, punctuation, digits and repeated spaces.
- [ ] Direct Bangla letters, vowel signs, conjuncts and Bengali punctuation.
- [ ] Space inserts one space; horizontal spacebar drag moves the cursor both directions.
- [ ] Backspace tap, hold-repeat, selection deletion, Bangla conjunct deletion and emoji deletion.
- [ ] Long-press alternate characters; move the pointer off a held key and confirm repeat/highlight stops.
- [ ] Emoji categories and every page's navigation.
- [ ] Symbols pages, Enter/newline and app-specific Go/Search/Send/Next/Done actions.
- [ ] Suggestions can be selected without inserting unintended neighboring text.

## Password and privacy checks

Use dummy values only.

- [ ] Password, visible-password, web-password and numeric-password fields switch to English.
- [ ] Swipe suggestions and clipboard history are unavailable in password fields.
- [ ] Ordinary email, phone and numeric fields are not incorrectly treated as password fields.
- [ ] Clipboard history is off by default.
- [ ] If explicitly enabled, clipboard history reads only after the clipboard button is tapped and can be cleared by turning it off.
- [ ] Confirm no unexpected network permission or network activity is introduced in the tested build.

Never enter real passwords, payment details, recovery codes, or other secrets during testing.

## Performance and visual checks

On the lowest-spec device available:

- [ ] Type continuously for 2 minutes in English and Bangla.
- [ ] Hold Backspace for 5 seconds; confirm repeat is responsive and stops immediately on release.
- [ ] Open/close emoji and clipboard panels repeatedly.
- [ ] Switch through all themes, including both plush themes.
- [ ] Compare Reduce animations off and on.
- [ ] Watch for keyboard freezes, missed taps, delayed key release, flicker, excessive heat, or app crashes.
- [ ] Repeat after opening several other apps to create memory pressure.

## Experimental swipe typing

Swipe typing is English-only and experimental. Use a fixed set of at least 20 common words and short phrases.

- [ ] Swipe each item 3 times at a normal speed and 3 times quickly.
- [ ] Record the suggested word, whether the path was captured and perceived delay.
- [ ] Include short words, repeated letters, diagonal paths and paths that cross between rows.
- [ ] Confirm no swipe text is inserted into password fields.
- [ ] Record false suggestions and missed paths; do not expand the dictionary based on a single device/session.

## Reporting a failure

Include device model, Android version, app/field type, keyboard height, steps to reproduce, expected behavior, actual behavior and whether it repeats. Attach a screenshot or screen recording only after removing personal text and notifications. Never include typed private content.
