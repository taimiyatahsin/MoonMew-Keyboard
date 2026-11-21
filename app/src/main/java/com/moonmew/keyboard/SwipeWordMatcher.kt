package com.moonmew.keyboard

/** Small offline English word matcher for the first swipe-typing prototype. */
object SwipeWordMatcher {
    private val words = listOf(
        "the","there","their","then","them","this","that","these","those","think",
        "hello","help","hey","how","home","good","great","going","go","get",
        "you","your","yes","yeah","we","well","will","with","what","when","where",
        "who","why","which","would","could","should","can","come","cool","care",
        "love","like","life","live","look","let","little","long","last","later",
        "and","are","all","also","about","after","again","any","anyone","always",
        "for","from","friend","free","fun","fine","feel","find","first","follow",
        "have","has","had","here","her","him","his","happy","hope","house",
        "in","is","it","its","if","into","important","im","i","me","my","mine",
        "not","now","no","new","need","nice","night","name","never","next",
        "on","one","only","our","out","over","okay","ok","of","off",
        "please","people","person","phone","play","put","pretty","problem",
        "really","right","read","run","remember","room","ready","rain",
        "so","some","see","she","say","said","sorry","something","someone","soon",
        "to","today","tomorrow","time","take","talk","thank","thanks","than","they",
        "up","us","use","very","want","was","way","work","world","write"
    )
    fun suggest(path: String, limit: Int = 3): List<String> {
        val query = path.lowercase().filter { it in 'a'..'z' }
        if (query.length < 3 || limit <= 0) return emptyList()
        return words.asSequence().map { it to distance(query, it) }
            .filter { (word, score) -> score <= maxOf(1, word.length / 3) }
            .sortedBy { it.second }
            .take(limit).map { it.first }.toList()
    }
    private fun distance(a: String, b: String): Int {
        var previous = IntArray(b.length + 1) { it }
        for (i in 1..a.length) {
            val current = IntArray(b.length + 1)
            current[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                current[j] = minOf(current[j - 1] + 1, previous[j] + 1, previous[j - 1] + cost)
            }
            previous = current
        }
        return previous[b.length]
    }
}
