package com.moonmew.keyboard

/**
 * Decides whether a second space should become a period followed by one space.
 * The editor context and elapsed time are supplied by the IME service so this
 * policy stays deterministic and easy to test.
 */
internal object DoubleSpacePeriodPolicy {
    const val WINDOW_MS = 500L

    fun shouldInsertPeriod(
        beforeCursor: String,
        elapsedSinceLastSpaceMs: Long,
        enabled: Boolean
    ): Boolean {
        if (!enabled || elapsedSinceLastSpaceMs !in 1..WINDOW_MS || beforeCursor.length != 2) return false
        return beforeCursor[0].isLetterOrDigit() && beforeCursor[1] == ' '
    }
}
