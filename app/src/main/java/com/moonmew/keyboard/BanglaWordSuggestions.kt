package com.moonmew.keyboard

/**
 * Small on-device Bangla starter lexicon. It is intentionally stored locally:
 * no network requests and no typed text leaves the device.
 */
object BanglaWordSuggestions {
    private val words = linkedMapOf(
        "ami" to "আমি", "amar" to "আমার", "amra" to "আমরা", "amake" to "আমাকে",
        "tumi" to "তুমি", "tomar" to "তোমার", "tomake" to "তোমাকে", "apni" to "আপনি",
        "apnar" to "আপনার", "se" to "সে", "she" to "সে", "tara" to "তারা",
        "amader" to "আমাদের", "tomader" to "তোমাদের", "ki" to "কি", "keno" to "কেন",
        "kothay" to "কোথায়", "kemon" to "কেমন", "kobe" to "কবে", "ke" to "কে",
        "kirokom" to "কীরকম", "hoy" to "হয়", "hobe" to "হবে", "hoye" to "হয়ে",
        "ache" to "আছে", "achi" to "আছি", "achhi" to "আছি", "nei" to "নেই",
        "na" to "না", "hya" to "হ্যাঁ", "ha" to "হ্যাঁ", "bhalo" to "ভালো",
        "valo" to "ভালো", "kharap" to "খারাপ", "sundor" to "সুন্দর", "onek" to "অনেক",
        "khub" to "খুব", "beshi" to "বেশি", "kom" to "কম", "ekta" to "একটা",
        "ekhon" to "এখন", "aj" to "আজ", "kal" to "কাল", "gotokal" to "গতকাল",
        "agami" to "আগামী", "din" to "দিন", "raat" to "রাত", "sokal" to "সকাল",
        "dupur" to "দুপুর", "bikel" to "বিকেল", "somoy" to "সময়", "shomoy" to "সময়",
        "bangla" to "বাংলা", "bangladesh" to "বাংলাদেশ", "desh" to "দেশ", "bhasha" to "ভাষা",
        "manush" to "মানুষ", "bondhu" to "বন্ধু", "poribar" to "পরিবার", "ma" to "মা",
        "baba" to "বাবা", "bhai" to "ভাই", "bon" to "বোন", "dada" to "দাদা",
        "didi" to "দিদি", "dhonnobad" to "ধন্যবাদ", "shagotom" to "স্বাগতম",
        "please" to "প্লিজ", "sorry" to "সরি", "bujhi" to "বুঝি", "bujhte" to "বুঝতে",
        "chai" to "চাই", "chao" to "চাও", "korbo" to "করব", "koro" to "করো",
        "korchi" to "করছি", "kore" to "করে", "kora" to "করা", "korechi" to "করেছি",
        "jacchi" to "যাচ্ছি", "jabo" to "যাব", "dao" to "দাও", "dibe" to "দেবে",
        "hobe" to "হবে", "parbo" to "পারব", "pari" to "পারি", "parina" to "পারি না",
        "ekhane" to "এখানে", "okhane" to "ওখানে", "bari" to "বাড়ি", "kaj" to "কাজ",
        "school" to "স্কুল", "phone" to "ফোন", "keyboard" to "কিবোর্ড", "app" to "অ্যাপ",
        "computer" to "কম্পিউটার", "shikkha" to "শিক্ষা", "porashona" to "পড়াশোনা",
        "jibon" to "জীবন", "shopno" to "স্বপ্ন", "mon" to "মন", "bhalobasha" to "ভালোবাসা",
        "valobasha" to "ভালোবাসা", "khushi" to "খুশি", "dukkho" to "দুঃখ",
        "allah" to "আল্লাহ", "inshallah" to "ইনশাআল্লাহ", "mashallah" to "মাশাআল্লাহ",
        "shukriya" to "শুকরিয়া", "assalamu" to "আসসালামু", "alaikum" to "আলাইকুম"
    )

    fun exact(roman: String): String? = words[roman]

    fun suggest(prefix: String, limit: Int = 3): List<String> {
        val query = prefix.lowercase()
        if (query.isBlank()) return emptyList()
        val matches = words.entries
            .filter { it.key.startsWith(query) }
            .sortedWith(compareByDescending<Map.Entry<String, String>> { it.key == query }
                .thenBy { it.key.length })
            .take(limit)
            .map { it.value }
        return matches.distinct()
    }
}
