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
        "student", "teacher", "book", "paper", "table", "chair", "water", "food", "coffee",
        "tea", "house", "room", "door", "window", "car", "bus", "train", "road", "street", "city",
        "country", "phone", "mobile", "computer", "laptop", "screen", "app", "application", "system",
        "version", "build", "error", "issue", "problem", "fixed", "fix", "working", "works", "noted",
        "sure", "right", "wrong", "true", "false", "okay", "cool", "wow", "awesome", "amazing",
        "congratulations", "welcome", "goodbye", "bye", "goodnight", "goodbye", "dear", "please",
        "kind", "kindly", "hope", "wish", "today's", "tomorrow's",
        // Frequent everyday English words that should remain English in phonetic mode.
        "go", "come", "get", "make", "take", "give", "think", "know", "see", "look", "use",
        "find", "tell", "ask", "try", "call", "keep", "let", "put", "mean", "become", "leave",
        "feel", "seem", "help", "talk", "turn", "start", "show", "hear", "run", "move", "live",
        "believe", "bring", "happen", "provide", "sit", "stand", "lose", "pay", "meet", "include",
        "continue", "set", "change", "lead", "understand", "watch", "follow", "stop", "create",
        "speak", "allow", "add", "spend", "grow", "walk", "win", "offer", "remember", "consider",
        "appear", "buy", "wait", "serve", "send", "expect", "stay", "fall", "cut", "reach",
        "remain", "suggest", "raise", "pass", "sell", "require", "decide", "pull", "return",
        "explain", "develop", "carry", "break", "agree", "support", "produce", "eat", "cover",
        "catch", "draw", "choose", "cause", "point", "listen", "realize", "place", "increase",
        "deal", "throw", "fill", "protect", "plan", "finish", "check", "answer", "question",
        "though", "although", "while", "during", "through", "without", "between", "under", "over",
        "around", "against", "since", "until", "unless", "probably", "actually", "already", "yet",
        "even", "ever", "often", "usually", "together", "almost", "enough", "quite", "rather",
        "else", "everywhere", "someone", "anyone", "everyone", "nobody", "anything", "something",
        "everything", "nothing", "which", "whose", "whom", "then", "than", "soon", "late", "early",
        "first", "last", "second", "third", "same", "different", "another", "other", "own", "such",
        "either", "neither", "few", "several", "less", "least", "lot", "lots", "one", "two", "three",
        "four", "five", "six", "seven", "eight", "nine", "ten", "young", "big", "small", "long",
        "short", "high", "low", "worst", "ready", "free", "busy", "easy", "hard", "simple",
        "important", "possible", "impossible", "pretty", "sweet", "funny", "fun", "interesting",
        "boring", "angry", "tired", "hungry", "thirsty", "sick", "healthy", "strong", "weak",
        "hot", "cold", "warm", "clean", "dirty", "dark", "bright", "soft", "quick", "careful",
        "polite", "rude", "smart", "clever", "stupid", "wonderful", "perfect", "fine", "congrats"
    )

    fun resolvePhoneticWord(roman: String): String {
        val trimmed = roman.trim()
        if (trimmed.isEmpty()) return trimmed
        val normalized = trimmed.lowercase()
        if (normalized in commonEnglish) return trimmed
        return BanglaWordSuggestions.exact(normalized)
            ?: BanglaTransliterator.convert(trimmed).ifEmpty { trimmed }
    }
}
