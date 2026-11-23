package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class SpacebarCursorPolicyTest {
    @Test fun requiresOneFullStep() {
        assertEquals(0, SpacebarCursorPolicy.stepsForDistance(17f, 18f))
        assertEquals(1, SpacebarCursorPolicy.stepsForDistance(18f, 18f))
    }

    @Test fun supportsBothDirectionsAndMultipleSteps() {
        assertEquals(2, SpacebarCursorPolicy.stepsForDistance(40f, 18f))
        assertEquals(-2, SpacebarCursorPolicy.stepsForDistance(-40f, 18f))
    }

    @Test fun boundsWorkPerMotionEvent() {
        assertEquals(20, SpacebarCursorPolicy.stepsForDistance(10_000f, 1f))
        assertEquals(-20, SpacebarCursorPolicy.stepsForDistance(-10_000f, 1f))
    }

    @Test fun rejectsInvalidThresholdsAndDistances() {
        assertEquals(0, SpacebarCursorPolicy.stepsForDistance(20f, 0f))
        assertEquals(0, SpacebarCursorPolicy.stepsForDistance(Float.NaN, 18f))
    }
}
