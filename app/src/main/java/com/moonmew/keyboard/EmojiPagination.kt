package com.moonmew.keyboard

internal data class EmojiPageWindow(
    val pageIndex: Int,
    val pageCount: Int,
    val startRow: Int,
    val endRowExclusive: Int
)

internal object EmojiPagination {
    fun window(totalRows: Int, requestedPage: Int, rowsPerPage: Int = 3): EmojiPageWindow {
        val safeRows = totalRows.coerceAtLeast(0)
        val safeRowsPerPage = rowsPerPage.coerceAtLeast(1)
        val count = if (safeRows == 0) 1 else 1 + (safeRows - 1) / safeRowsPerPage
        val page = requestedPage.coerceIn(0, count - 1)
        val start = page * safeRowsPerPage
        return EmojiPageWindow(
            pageIndex = page,
            pageCount = count,
            startRow = start,
            endRowExclusive = start + safeRowsPerPage.coerceAtMost(safeRows - start)
        )
    }
}
