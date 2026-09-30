package com.hirehop.core.domain.offline

internal data class VerbHit(val word: String, val rank: Int)

internal object OwnershipVerbs {
    private val ladder: Map<String, Int> = buildMap {
        putRank(1, "assisted", "assisting", "helped", "helping", "supported", "supporting", "aided", "aiding", "shadowed")
        putRank(2, "contributed", "contributing", "collaborated", "collaborating", "participated", "participating")
        putRank(
            3,
            "developed", "developing", "built", "building", "implemented", "implementing", "created", "creating",
            "designed", "designing", "engineered", "coded", "wrote", "authored", "programmed",
        )
        putRank(
            4,
            "led", "leading", "managed", "managing", "headed", "spearheaded", "directed", "directing",
            "supervised", "supervising", "orchestrated",
        )
        putRank(5, "owned", "owning", "ownership")
    }

    fun hits(text: String): List<VerbHit> =
        TextTokens.words(text).mapNotNull { word -> ladder[word]?.let { VerbHit(word, it) } }

    fun strongest(text: String): VerbHit? = hits(text).maxByOrNull { it.rank }

    fun leading(text: String): VerbHit? = hits(text).firstOrNull()

    private fun MutableMap<String, Int>.putRank(rank: Int, vararg words: String) {
        words.forEach { put(it, rank) }
    }
}
