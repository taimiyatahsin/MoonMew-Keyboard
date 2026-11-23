package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class EmojiPaginationPolicyTest {
    @Test fun keepsEachPageWithinThreeRows() {
        assertEquals(1, EmojiPaginationPolicy.pageCount(24))
        assertEquals(2, EmojiPaginationPolicy.pageCount(25))
        assertEquals(3, EmojiPaginationPolicy.pageCount(49))
    }

    @Test fun emptyCategoryStillHasOneSafePage() {
        assertEquals(1, EmojiPaginationPolicy.pageCount(0))
        assertEquals(0, EmojiPaginationPolicy.clampPage(7, 0))
    }

    @Test fun clampsNavigationAtBothEnds() {
        assertEquals(0, EmojiPaginationPolicy.clampPage(-1, 3))
        assertEquals(1, EmojiPaginationPolicy.clampPage(1, 3))
        assertEquals(2, EmojiPaginationPolicy.clampPage(99, 3))
    }
}
