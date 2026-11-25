#!/usr/bin/env bash
set -euo pipefail

mkdir -p emulator-diagnostics
capture_diagnostics() {
  adb logcat -d -v threadtime > emulator-diagnostics/logcat.txt 2>/dev/null || true
}
trap capture_diagnostics EXIT

adb wait-for-device
expected_api_level="${ANDROID_API_LEVEL:-30}"
ready=0
for attempt in $(seq 1 90); do
  api_level=$(adb shell getprop ro.build.version.sdk 2>/dev/null | tr -d '\r')
  if [[ "$api_level" == "$expected_api_level" ]]; then
    ready=1
    break
  fi
  sleep 2
done
if [[ "$ready" -ne 1 ]]; then
  echo "Emulator did not expose Android API $expected_api_level after boot (last reported API: ${api_level:-unknown})."
  exit 1
fi

# A boot-complete property alone is not sufficient on a cold API 35 image:
# system_server may still be initializing PackageManager. Probe the actual
# package service before spending time compiling the instrumentation APKs.
package_manager_ready=0
for attempt in $(seq 1 90); do
  if timeout 15s adb shell cmd package list packages >/dev/null 2>&1; then
    package_manager_ready=1
    break
  fi
  echo "Waiting for Android PackageManager to become responsive (attempt $attempt/90)."
  sleep 2
done
if [[ "$package_manager_ready" -ne 1 ]]; then
  echo "Android PackageManager did not become responsive after emulator boot."
  exit 1
fi

# Install the APKs directly with adb and invoke AndroidJUnitRunner. This avoids
# a ddmlib/UTP split-APK installation failure observed on the API 30 emulator.
gradle assembleDebug assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk

set +e
adb shell am instrument -w -r \
  com.moonmew.keyboard.test/androidx.test.runner.AndroidJUnitRunner 2>&1 \
  | tee emulator-diagnostics/instrumentation.txt
instrument_status=${PIPESTATUS[0]}
set -e

# AndroidJUnitRunner may print INSTRUMENTATION_CODE: -1 after a successful
# run on some platform versions. Use the process exit status and test summary
# to decide success; explicit failure markers still fail the smoke test.
if [[ "$instrument_status" -ne 0 ]] || \
   grep -Eq 'FAILURES!!!|INSTRUMENTATION_FAILED' emulator-diagnostics/instrumentation.txt || \
   ! grep -q 'OK (' emulator-diagnostics/instrumentation.txt; then
  echo "Android instrumentation tests failed. See emulator-diagnostics/instrumentation.txt."
  exit 1
fi
