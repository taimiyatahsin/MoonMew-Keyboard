package com.moonmew.keyboard

/** Converts horizontal spacebar travel into a bounded number of cursor steps. */
internal object SpacebarCursorPolicy {
    const val STEP_DP = 18f
    private const val MAX_STEPS_PER_EVENT = 20

    fun stepsForDistance(distancePx: Float, stepPx: Float): Int {
        if (!distancePx.isFinite() || !stepPx.isFinite() || stepPx <= 0f) return 0
        return (distancePx / stepPx).toInt().coerceIn(-MAX_STEPS_PER_EVENT, MAX_STEPS_PER_EVENT)
    }
}
