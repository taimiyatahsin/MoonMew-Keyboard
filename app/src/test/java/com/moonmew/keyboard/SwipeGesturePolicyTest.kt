package com.moonmew.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeGesturePolicyTest {
    @Test fun letterKeysCanStartSwipe() {
        assertTrue(SwipeGesturePolicy.canStart("a"))
        assertTrue(SwipeGesturePolicy.canStart("z"))
    }

    @Test fun actionAndNonLetterKeysCannotStartSwipe() {
        listOf(null, "SHIFT", "SPACE", "DELETE", "ENTER", "MODE", "1", "ab", "A").forEach {
            assertFalse("Unexpected swipe start key: $it", SwipeGesturePolicy.canStart(it))
        }
    }
}
