package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SwipeWordMatcherTest {
    @Test fun exactAndNearMatchesAreSuggestedOffline() {
        assertEquals("the", SwipeWordMatcher.suggest("the").first())
        assertEquals("hello", SwipeWordMatcher.suggest("helo").first())
    }
    @Test fun shortPathsDoNotSuggest() {
        assertTrue(SwipeWordMatcher.suggest("ab").isEmpty())
        assertTrue(SwipeWordMatcher.suggest("").isEmpty())
    }
}
