package com.tailormyresume.core.domain.offline

internal data class VerbHit(val word: String, val rank: Int)

internal object OwnershipVerbs {
    private const val MAX_PHRASE_WORDS = 3

    private val ladder: Map<String, Int> = buildMap {
        putRank(
            1,
            "assisted", "assist", "assists", "assisting", "helped", "help", "helps", "helping",
            "supported", "support", "supports", "supporting", "aided", "aid", "aiding", "shadowed", "shadow",
        )
        putRank(
            2,
            "contributed", "contribute", "contributes", "contributing", "collaborated", "collaborate",
            "collaborates", "collaborating", "participated", "participate", "participates", "participating",
        )
        putRank(
            3,
            "developed", "develop", "develops", "developing", "built", "build", "builds", "building",
            "implemented", "implement", "implements", "implementing", "created", "create", "creates",
            "creating", "designed", "design", "designs", "designing", "engineered", "coded", "code",
            "wrote", "write", "writes", "writing", "written", "authored", "author", "programmed",
            "launched", "launch", "launches", "launching", "delivered", "deliver", "delivers", "delivering",
            "deployed", "deploy", "deploys", "deploying", "established", "establish", "shipped",
        )
        putRank(
            4,
            "led", "lead", "leads", "leading", "managed", "manage", "manages", "managing", "headed",
            "spearheaded", "spearhead", "spearheads", "spearheading", "directed", "directing", "directs",
            "supervised", "supervise", "supervises", "supervising", "orchestrated", "orchestrate",
            "orchestrating", "architected", "architecting", "oversaw", "oversee", "oversees", "overseeing",
            "overseen", "drove", "drive", "drives", "driving", "driven", "founded", "found", "pioneered",
            "pioneer", "pioneering", "championed", "champion", "championing", "ran", "mentored", "mentor",
            "mentoring", "chaired",
        )
        putRank(5, "owned", "owns", "owning", "ownership")
    }

    private val phrases: Map<List<String>, Int> = mapOf(
        listOf("responsible", "for") to 4,
        listOf("in", "charge", "of") to 4,
        listOf("took", "ownership") to 5,
    )

    private val weakNouns = setOf("found", "champion", "mentor", "pioneer", "code", "aid", "shadow", "drive", "driven")

    private val leadingOnly = mapOf("own" to 5)

    fun hits(text: String): List<VerbHit> {
        val words = TextTokens.words(text)
        val result = mutableListOf<VerbHit>()
        var index = 0
        while (index < words.size) {
            val phrase = matchPhrase(words, index)
            if (phrase != null) {
                result += phrase.first
                index += phrase.second
                continue
            }
            singleHit(words[index], index)?.let { result += it }
            index++
        }
        return result
    }

    fun strongest(text: String): VerbHit? = hits(text).maxByOrNull { it.rank }

    fun leading(text: String): VerbHit? = hits(text).firstOrNull()

    private fun matchPhrase(words: List<String>, index: Int): Pair<VerbHit, Int>? {
        for (length in MAX_PHRASE_WORDS downTo 2) {
            val slice = words.drop(index).take(length)
            val rank = phrases[slice] ?: continue
            return VerbHit(slice.joinToString(" "), rank) to length
        }
        return null
    }

    private fun singleHit(word: String, index: Int): VerbHit? {
        if (index == 0) leadingOnly[word]?.let { return VerbHit(word, it) }
        if (word in weakNouns && index != 0) return null
        return ladder[word]?.let { VerbHit(word, it) }
    }

    private fun MutableMap<String, Int>.putRank(rank: Int, vararg words: String) {
        words.forEach { put(it, rank) }
    }
}
