package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class EmojiPaginationTest {
    @Test fun emptyCategoryStillHasOneSafePage() {
        assertEquals(EmojiPageWindow(0, 1, 0, 0), EmojiPagination.window(0, 0))
    }

    @Test fun rowsAreSplitIntoThreeRowPages() {
        assertEquals(EmojiPageWindow(0, 3, 0, 3), EmojiPagination.window(7, 0))
        assertEquals(EmojiPageWindow(1, 3, 3, 6), EmojiPagination.window(7, 1))
        assertEquals(EmojiPageWindow(2, 3, 6, 7), EmojiPagination.window(7, 2))
    }

    @Test fun requestedPageIsClampedToValidRange() {
        assertEquals(0, EmojiPagination.window(7, -5).pageIndex)
        assertEquals(2, EmojiPagination.window(7, 99).pageIndex)
    }

    @Test fun invalidRowsPerPageFallsBackToOne() {
        assertEquals(EmojiPageWindow(1, 3, 1, 2), EmojiPagination.window(3, 1, 0))
    }

    @Test fun veryLargeRowCountsDoNotOverflowPageMath() {
        val window = EmojiPagination.window(Int.MAX_VALUE, Int.MAX_VALUE)
        assertEquals(715827882, window.pageIndex)
        assertEquals(715827883, window.pageCount)
        assertEquals(Int.MAX_VALUE, window.endRowExclusive)
    }

    @Test fun negativeRowCountIsTreatedAsEmpty() {
        assertEquals(EmojiPageWindow(0, 1, 0, 0), EmojiPagination.window(-4, 2))
    }
}
