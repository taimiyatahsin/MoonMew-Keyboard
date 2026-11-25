package com.moonmew.keyboard

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositionPolicyTest {
    @Test fun allowsNormalLengthWords() {
        assertFalse(CompositionPolicy.shouldCommitBeforeAppend(0))
        assertFalse(CompositionPolicy.shouldCommitBeforeAppend(CompositionPolicy.MAX_COMPOSING_CHARACTERS - 1))
    }

    @Test fun commitsBeforeTheBufferCanGrowPastItsLimit() {
        assertTrue(CompositionPolicy.shouldCommitBeforeAppend(CompositionPolicy.MAX_COMPOSING_CHARACTERS))
        assertTrue(CompositionPolicy.shouldCommitBeforeAppend(CompositionPolicy.MAX_COMPOSING_CHARACTERS + 1))
    }
}
