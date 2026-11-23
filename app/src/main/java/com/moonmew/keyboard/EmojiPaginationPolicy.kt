package com.moonmew.keyboard

/** Small deterministic pagination policy for the emoji picker. */
internal object EmojiPaginationPolicy {
    const val ITEMS_PER_ROW = 8
    const val ROWS_PER_PAGE = 3
    private const val ITEMS_PER_PAGE = ITEMS_PER_ROW * ROWS_PER_PAGE

    fun pageCount(itemCount: Int): Int =
        if (itemCount <= 0) 1 else (itemCount + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE

    fun clampPage(page: Int, pageCount: Int): Int =
        page.coerceIn(0, (pageCount.coerceAtLeast(1)) - 1)
}
