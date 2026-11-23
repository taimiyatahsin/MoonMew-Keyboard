package com.moonmew.keyboard

/**
 * Pure, deterministic rules for the opt-in local clipboard history.
 * Keeps only recent, bounded, non-empty plain-text entries and newest-first ordering.
 */
internal object ClipboardHistoryPolicy {
    const val MAX_ITEMS = 5
    const val MAX_TEXT_LENGTH = 1000
    const val MAX_AGE_MS = 24L * 60L * 60L * 1000L

    data class Entry(val timestampMs: Long, val text: String)

    fun prune(entries: List<Entry>, nowMs: Long): List<Entry> {
        val cutoff = nowMs - MAX_AGE_MS
        return entries.asSequence()
            .filter { it.timestampMs in cutoff..nowMs }
            .filter { it.text.isNotBlank() && it.text.length <= MAX_TEXT_LENGTH }
            .sortedByDescending { it.timestampMs }
            .distinctBy { it.text }
            .take(MAX_ITEMS)
            .toList()
    }

    fun add(entries: List<Entry>, text: String, nowMs: Long): List<Entry> {
        if (text.isBlank() || text.length > MAX_TEXT_LENGTH) return prune(entries, nowMs)
        return prune(listOf(Entry(nowMs, text)) + entries, nowMs)
    }
}
