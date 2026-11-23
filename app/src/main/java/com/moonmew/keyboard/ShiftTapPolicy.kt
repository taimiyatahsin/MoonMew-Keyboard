package com.moonmew.keyboard

/** Timing policy for distinguishing a one-shot Shift tap from a double-tap Caps Lock gesture. */
internal object ShiftTapPolicy {
    const val DOUBLE_TAP_WINDOW_MS = 350L

    fun isDoubleTap(previousTapAt: Long, currentTapAt: Long): Boolean {
        if (previousTapAt <= 0L || currentTapAt < previousTapAt) return false
        return currentTapAt - previousTapAt <= DOUBLE_TAP_WINDOW_MS
    }
}
