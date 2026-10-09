package com.tailormyresume.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.ProfileLimits
import com.tailormyresume.core.model.fitBulletsToLimit
import com.tailormyresume.core.model.isTooLong
import org.junit.Test

class BulletLimitsTest {

    private fun sentence(length: Int): String = "S" + "a".repeat(length - 2) + "."

    @Test
    fun threeSentenceBulletOf900CharactersBecomesThreeBulletsWithTheSameText() {
        val original = listOf(sentence(299), sentence(299), sentence(300)).joinToString(" ")
        assertThat(original.length).isEqualTo(900)

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted).hasSize(3)
        assertThat(fitted.all { it.length <= ProfileLimits.MAX_BULLET_LENGTH }).isTrue()
        assertThat(fitted.joinToString(" ")).isEqualTo(original)
    }

    @Test
    fun shortSentencesShareABulletUpToTheLimit() {
        val original = List(8) { sentence(100) }.joinToString(" ")

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted.all { it.length <= ProfileLimits.MAX_BULLET_LENGTH }).isTrue()
        assertThat(fitted.joinToString(" ")).isEqualTo(original)
    }

    @Test
    fun singleSentenceOver400CharactersIsKeptWholeAndFlagged() {
        val original = sentence(450)

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted).containsExactly(original)
        assertThat(EvidenceBullet("b", fitted.single()).isTooLong).isTrue()
    }

    @Test
    fun bulletsWithinTheLimitAreUntouched() {
        val texts = listOf(sentence(400), "Short one. Another short one.")

        assertThat(fitBulletsToLimit(texts)).isEqualTo(texts)
    }

    @Test
    fun splittingNeverGoesPastFifteenBulletsInAnEntry() {
        val long = listOf(sentence(299), sentence(299), sentence(300)).joinToString(" ")
        val texts = List(ProfileLimits.MAX_BULLETS_PER_ENTRY - 2) { "Bullet $it." } + long

        val fitted = fitBulletsToLimit(texts)

        assertThat(fitted.size).isAtMost(ProfileLimits.MAX_BULLETS_PER_ENTRY)
        assertThat(fitted.joinToString(" ")).isEqualTo(texts.joinToString(" "))
    }

    @Test
    fun splitBulletsKeepTheirOrderAmongTheOthers() {
        val long = listOf(sentence(299), sentence(299), sentence(300)).joinToString(" ")

        val fitted = fitBulletsToLimit(listOf("First.", long, "Last."))

        assertThat(fitted.first()).isEqualTo("First.")
        assertThat(fitted.last()).isEqualTo("Last.")
        assertThat(fitted).hasSize(5)
    }
}
