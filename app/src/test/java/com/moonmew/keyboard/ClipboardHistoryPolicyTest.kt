package com.moonmew.keyboard

import org.junit.Assert.assertEquals
import org.junit.Test

class ClipboardHistoryPolicyTest {
    private val now = 10_000_000L

    @Test fun expiresEachItemAfterItsOwnTwentyFourHourWindow() {
        val entries = listOf(
            ClipboardHistoryEntry(now - ClipboardHistoryPolicy.RETENTION_MILLIS - 1, "expired"),
            ClipboardHistoryEntry(now - ClipboardHistoryPolicy.RETENTION_MILLIS, "boundary"),
            ClipboardHistoryEntry(now - 1000, "recent")
        )

        assertEquals(listOf("recent", "boundary"), ClipboardHistoryPolicy.prune(entries, now).map { it.text })
    }

    @Test fun addingAnItemPreservesExistingTimestamps() {
        val older = ClipboardHistoryEntry(now - 60_000, "older")
        val result = ClipboardHistoryPolicy.add("new", listOf(older), now)

        assertEquals(listOf("new", "older"), result.map { it.text })
        assertEquals(now - 60_000, result[1].timestampMillis)
        assertEquals(now, result[0].timestampMillis)
    }

    @Test fun addingDuplicateRefreshesOnlyThatItemsTimestamp() {
        val existing = listOf(
            ClipboardHistoryEntry(now - 60_000, "same"),
            ClipboardHistoryEntry(now - 30_000, "other")
        )

        val result = ClipboardHistoryPolicy.add("same", existing, now)

        assertEquals(listOf("same", "other"), result.map { it.text })
        assertEquals(now, result[0].timestampMillis)
        assertEquals(now - 30_000, result[1].timestampMillis)
    }

    @Test fun rejectsBlankAndOversizedEntriesAndKeepsOnlyFive() {
        val existing = (1..6).map { ClipboardHistoryEntry(now - it.toLong(), "item$it") }
        val pruned = ClipboardHistoryPolicy.prune(existing, now)

        assertEquals(5, pruned.size)
        assertEquals(pruned, ClipboardHistoryPolicy.add("  ", existing, now))
        assertEquals(pruned, ClipboardHistoryPolicy.add("x".repeat(1001), existing, now))
    }

    @Test fun pruneRejectsFutureDatedEntries() {
        val entries = listOf(
            ClipboardHistoryEntry(now + 1, "future"),
            ClipboardHistoryEntry(now, "current"),
            ClipboardHistoryEntry(now - 1, "past")
        )

        assertEquals(listOf("current", "past"), ClipboardHistoryPolicy.prune(entries, now).map { it.text })
    }

    @Test fun pruneKeepsNewestDuplicateAndSortsByTimestamp() {
        val entries = listOf(
            ClipboardHistoryEntry(now - 10, "duplicate"),
            ClipboardHistoryEntry(now - 30, "older"),
            ClipboardHistoryEntry(now - 5, "duplicate")
        )

        val result = ClipboardHistoryPolicy.prune(entries, now)

        assertEquals(listOf("duplicate", "older"), result.map { it.text })
        assertEquals(now - 5, result[0].timestampMillis)
    }

    @Test fun addNeverExceedsFiveItems() {
        val existing = (1..5).map { ClipboardHistoryEntry(now - it.toLong(), "item$it") }

        val result = ClipboardHistoryPolicy.add("new", existing, now)

        assertEquals(5, result.size)
        assertEquals("new", result.first().text)
        assertEquals(listOf("item1", "item2", "item3", "item4"), result.drop(1).map { it.text })
    }

}
