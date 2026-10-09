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

        assertThat(fitted).isEqualTo(texts)
        assertThat(fitted.last().isTooLong()).isTrue()
    }

    private fun String.isTooLong() = EvidenceBullet("b", this).isTooLong

    private fun unpunctuated(length: Int, lead: String = "Led") = lead + " " + "a".repeat(length - lead.length - 1)

    @Test
    fun rupeeAbbreviationIsNotASentenceBoundary() {
        val original = unpunctuated(392) + " Rs. 5 lakh in seed funding for the club."

        assertThat(fitBulletsToLimit(listOf(original))).containsExactly(original)
    }

    @Test
    fun abbreviationsNeverEndOrStartASplitPiece() {
        val first = "Raised Rs. 5 lakh e.g. via B.Tech. CSE alumni " + "a".repeat(250) + "."
        val second = "Cut Pvt. Ltd. report time by 30% " + "b".repeat(250) + "."
        val original = "$first $second"

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted).containsExactly(first, second).inOrder()
    }

    @Test
    fun otherCommonAbbreviationsAndInitialsAreNotBoundaries() {
        val abbreviations = listOf("i.e.", "etc.", "M.Tech.", "B.E.", "M.B.A.", "Inc.", "Dr.", "Mr.", "Ms.", "No.", "vs.", "U.S.", "A.", "Jan.")
        abbreviations.forEach { abbreviation ->
            val original = unpunctuated(392) + " $abbreviation Next words after it."

            assertThat(fitBulletsToLimit(listOf(original))).containsExactly(original)
        }
    }

    @Test
    fun newlineIsASentenceBoundary() {
        val first = unpunctuated(250, "Led")
        val second = unpunctuated(250, "Built")

        val fitted = fitBulletsToLimit(listOf("$first\n$second"))

        assertThat(fitted).containsExactly(first, second).inOrder()
    }

    @Test
    fun longBulletWithOnlyAbbreviationDotsStaysWholeAndFlagged() {
        val original = "Raised Rs. 5 lakh e.g. from Dr. Rao at Pvt. Ltd. " + "a".repeat(400)

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted).containsExactly(original)
        assertThat(fitted.single().isTooLong()).isTrue()
    }

    @Test
    fun trailingWhitespaceNeverCreatesABlankOrPaddedBullet() {
        val head = sentence(51)
        val body = sentence(400)
        val original = "$head $body "
        assertThat(original.length).isEqualTo(453)

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted).containsExactly(head, body).inOrder()
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
