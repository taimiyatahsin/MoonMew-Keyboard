package com.moonmew.keyboard

internal data class ClipboardHistoryEntry(
    val timestampMillis: Long,
    val text: String
)

/** Keeps each clipboard item on its own 24-hour retention window. */
internal object ClipboardHistoryPolicy {
    const val MAX_ITEMS = 5
    const val MAX_TEXT_LENGTH = 1000
    const val RETENTION_MILLIS = 24L * 60L * 60L * 1000L

    fun prune(entries: List<ClipboardHistoryEntry>, nowMillis: Long): List<ClipboardHistoryEntry> {
        val cutoff = nowMillis - RETENTION_MILLIS
        return entries
            .filter { it.timestampMillis in cutoff..nowMillis && it.text.isNotBlank() && it.text.length <= MAX_TEXT_LENGTH }
            .sortedByDescending { it.timestampMillis }
            .distinctBy { it.text }
            .take(MAX_ITEMS)
    }

    fun add(
        text: String,
        existing: List<ClipboardHistoryEntry>,
        nowMillis: Long
    ): List<ClipboardHistoryEntry> {
        val retained = prune(existing, nowMillis)
        if (text.isBlank() || text.length > MAX_TEXT_LENGTH) return retained
        return (listOf(ClipboardHistoryEntry(nowMillis, text)) + retained.filterNot { it.text == text })
            .take(MAX_ITEMS)
    }
}
