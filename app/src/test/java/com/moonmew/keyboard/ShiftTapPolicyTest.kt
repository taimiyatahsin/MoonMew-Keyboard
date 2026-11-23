package com.moonmew.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShiftTapPolicyTest {
    @Test fun acceptsSecondTapInsideWindow() {
        assertTrue(ShiftTapPolicy.isDoubleTap(1_000L, 1_350L))
    }

    @Test fun rejectsSecondTapOutsideWindow() {
        assertFalse(ShiftTapPolicy.isDoubleTap(1_000L, 1_351L))
    }

    @Test fun rejectsMissingOrReversedFirstTap() {
        assertFalse(ShiftTapPolicy.isDoubleTap(0L, 100L))
        assertFalse(ShiftTapPolicy.isDoubleTap(200L, 100L))
    }
}
