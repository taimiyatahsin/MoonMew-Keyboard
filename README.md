# MoonMew Keyboard

A native Android `InputMethodService` keyboard with English, English-letter Bangla phonetic, and direct Bangla modes. The crescent Moon Switch cycles through all three.

## Privacy

- No `INTERNET` permission, network client, analytics, or keystroke storage.
- Password text fields are detected using Android's input type. Phonetic composition is disabled there; the keyboard still inserts only the key the user taps.
- Android controls secure-field display and may hide or alter keyboard behavior. Verify with the device's password fields before relying on any third-party IME.

## Build and install

1. Install Android Studio (with Android SDK Platform 35 and a compatible JDK) on Windows.
2. Open this folder in Android Studio and allow Gradle sync.
3. Choose **Build > Build APK(s)**. The debug APK appears at `app/build/outputs/apk/debug/app-debug.apk`.
4. Install it on the Samsung M02s, open MoonMew, enable it in **Settings > General management > Keyboard list and default**, then select **MoonMew Keyboard**.

The exact Samsung menu labels vary by One UI version. USB installation also requires enabling Developer options and USB debugging.

## Signed APK / GitHub Release

For a release APK, create a private upload key in Android Studio's **Build > Generate Signed Bundle / APK** wizard. Keep the keystore and passwords outside Git and share them with nobody. Select APK, create a release build, then upload the resulting APK to a GitHub Release. Do not commit signing secrets. No Git remote is configured in this project yet, so a GitHub Release cannot be published from this checkout.

## Current typing behavior

Phonetic mode converts typed Latin letter sequences using an embedded, deterministic offline transliteration table. It is an initial keyboard implementation, not a complete Avro-compatible dictionary engine; spellings, conjuncts, and context-sensitive vowel signs need further refinement. Direct Bangla mode currently provides a compact character layout.
