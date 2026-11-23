package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class ClipboardHistoryPolicyTest {
    private val now = 2_000_000_000L

    @Test fun expiresEntriesOlderThanTwentyFourHours() {
        val entries = listOf(
            ClipboardHistoryPolicy.Entry(now - ClipboardHistoryPolicy.MAX_AGE_MS - 1, "old"),
            ClipboardHistoryPolicy.Entry(now - ClipboardHistoryPolicy.MAX_AGE_MS, "boundary"),
            ClipboardHistoryPolicy.Entry(now, "new")
        )
        assertEquals(listOf("new", "boundary"),
            ClipboardHistoryPolicy.prune(entries, now).map { it.text })
    }

    @Test fun rejectsFutureBlankAndOversizedEntries() {
        val entries = listOf(
            ClipboardHistoryPolicy.Entry(now + 1, "future"),
            ClipboardHistoryPolicy.Entry(now, "  "),
            ClipboardHistoryPolicy.Entry(now, "x".repeat(ClipboardHistoryPolicy.MAX_TEXT_LENGTH + 1)),
            ClipboardHistoryPolicy.Entry(now, "valid")
        )
        assertEquals(listOf("valid"), ClipboardHistoryPolicy.prune(entries, now).map { it.text })
    }

    @Test fun newestDuplicateWinsAndEntriesAreNewestFirst() {
        val entries = listOf(
            ClipboardHistoryPolicy.Entry(now - 10, "same"),
            ClipboardHistoryPolicy.Entry(now - 20, "older"),
            ClipboardHistoryPolicy.Entry(now, "same")
        )
        assertEquals(listOf("same", "older"),
            ClipboardHistoryPolicy.prune(entries, now).map { it.text })
        assertEquals(now, ClipboardHistoryPolicy.prune(entries, now).first().timestampMs)
    }

    @Test fun capsHistoryAtFiveItems() {
        val entries = (0L..7L).map { ClipboardHistoryPolicy.Entry(now - it, "item-$it") }
        assertEquals(5, ClipboardHistoryPolicy.prune(entries, now).size)
    }

    @Test fun addingAnExistingItemMovesItToTheFront() {
        val entries = listOf(
            ClipboardHistoryPolicy.Entry(now - 10, "older"),
            ClipboardHistoryPolicy.Entry(now - 20, "recently copied")
        )
        val result = ClipboardHistoryPolicy.add(entries, "recently copied", now)
        assertEquals(listOf("recently copied", "older"), result.map { it.text })
        assertEquals(now, result.first().timestampMs)
    }

    @Test fun invalidNewItemDoesNotPolluteHistory() {
        val entries = listOf(ClipboardHistoryPolicy.Entry(now - 1, "keep"))
        assertEquals(listOf("keep"),
            ClipboardHistoryPolicy.add(entries, " ".repeat(2), now).map { it.text })
        assertEquals(listOf("keep"),
            ClipboardHistoryPolicy.add(entries, "x".repeat(1001), now).map { it.text })
    }
}
