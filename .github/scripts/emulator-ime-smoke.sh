#!/usr/bin/env bash
set +e
mkdir -p emulator-diagnostics
adb wait-for-device
ready=0
for attempt in $(seq 1 60); do
  api_level=$(adb shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')
  if [[ "$api_level" == "30" ]]; then
    ready=1
    break
  fi
  sleep 2
done
if [[ "$ready" -ne 1 ]]; then
  echo "Emulator did not expose Android API 30 after boot."
  adb logcat -d -v threadtime > emulator-diagnostics/logcat.txt || true
  exit 1
fi
gradle connectedDebugAndroidTest
test_status=$?
adb logcat -d -v threadtime > emulator-diagnostics/logcat.txt || true
if [[ -d app/build/outputs/androidTest-results ]]; then
  cp -R app/build/outputs/androidTest-results emulator-diagnostics/
fi
if [[ -d app/build/reports/androidTests ]]; then
  cp -R app/build/reports/androidTests emulator-diagnostics/
fi
exit "$test_status"
