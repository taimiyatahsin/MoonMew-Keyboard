package com.moonmew.keyboard

/**
 * Offline word-level language choice for phonetic Bangla mode.
 * Known English words remain English; known Banglish words use the curated
 * Bangla lexicon; other roman input falls back to the local transliterator.
 */
object TypingLanguagePolicy {
    private val commonEnglish = setOf(
        "a", "an", "the", "and", "or", "but", "if", "to", "of", "in", "on", "at", "for", "with",
        "is", "are", "am", "was", "were", "be", "been", "being", "i", "you", "he", "she", "it",
        "we", "they", "this", "that", "these", "those", "hello", "hi", "hey", "good", "morning",
        "night", "afternoon", "evening", "how", "what", "when", "where", "why", "who", "yes", "no",
        "not", "please", "thanks", "thank", "sorry", "okay", "ok", "love", "like", "want", "need",
        "can", "will", "would", "should", "could", "do", "does", "did", "have", "has", "had",
        "my", "your", "his", "her", "our", "their", "me", "him", "them", "us", "from", "as", "by",
        "about", "just", "really", "very", "nice", "beautiful", "cute", "keyboard", "moonmew", "app",
        "phone", "android", "github", "code", "test", "done", "next", "update", "install", "download",
        "file", "settings", "clipboard", "space", "enter", "back", "home", "work", "play", "time",
        "today", "tomorrow", "yesterday", "now", "later", "always", "never", "sometimes", "maybe",
        "because", "before", "after", "again", "also", "only", "more", "most", "some", "many", "much",
        "every", "each", "all", "any", "everything", "something", "nothing", "people", "person",
        "friend", "family", "name", "world", "life", "happy", "sad", "great", "best", "better",
        "beautifully", "design", "pink", "black", "white", "red", "blue", "green", "color", "style",
        "music", "photo", "picture", "video", "message", "email", "internet", "online", "offline",
        "website", "password", "account", "save", "share", "search", "open", "close", "copy", "paste",
        "delete", "send", "receive", "button", "number", "symbol", "language", "english", "bangla",
        "english", "keyboard", "typing", "write", "writing", "read", "reading", "learn", "learning",
        "school", "student", "teacher", "book", "paper", "table", "chair", "water", "food", "coffee",
        "tea", "house", "room", "door", "window", "car", "bus", "train", "road", "street", "city",
        "country", "phone", "mobile", "computer", "laptop", "screen", "app", "application", "system",
        "version", "build", "error", "issue", "problem", "fixed", "fix", "working", "works", "noted",
        "sure", "right", "wrong", "true", "false", "okay", "cool", "wow", "awesome", "amazing",
        "congratulations", "welcome", "goodbye", "bye", "goodnight", "goodbye", "dear", "please",
        "kind", "kindly", "hope", "wish", "today's", "tomorrow's"
    )

    fun resolvePhoneticWord(roman: String): String {
        val trimmed = roman.trim()
        if (trimmed.isEmpty()) return trimmed
        val normalized = trimmed.lowercase()
        return BanglaWordSuggestions.exact(normalized)
            ?: if (normalized in commonEnglish) trimmed
            else BanglaTransliterator.convert(trimmed).ifEmpty { trimmed }
    }
}
