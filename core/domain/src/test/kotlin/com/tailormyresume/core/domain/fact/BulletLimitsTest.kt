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
    fun newlineAfterASentenceEndingIsABoundary() {
        val first = sentence(250)
        val second = "B" + "a".repeat(248) + "."

        assertThat(fitBulletsToLimit(listOf("$first\n$second"))).containsExactly(first, second).inOrder()
    }

    @Test
    fun newlineAfterAFullStopSplitsWhenTheTextIsTooLong() {
        val first = unpunctuated(385) + " the team."
        val second = "Built X in 2023 too."

        assertThat(fitBulletsToLimit(listOf("$first\n$second"))).containsExactly(first, second).inOrder()
    }

    @Test
    fun blankLineIsAParagraphBreak() {
        val first = unpunctuated(250, "Para")
        val second = unpunctuated(250, "Next")

        assertThat(fitBulletsToLimit(listOf("$first\n\n$second"))).containsExactly(first, second).inOrder()
        assertThat(fitBulletsToLimit(listOf("$first\n \n$second"))).containsExactly(first, second).inOrder()
    }

    @Test
    fun hardWrappedLineIsNotSplitMidSentence() {
        val original = unpunctuated(380, "Led") + " engineers to build\nthe payments platform that cut costs by thirty percent."

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted).containsExactly(original)
    }

    @Test
    fun newlineAfterAnUnpunctuatedLineIsAddedAsASpace() {
        val first = unpunctuated(200, "Led")
        val second = unpunctuated(190, "Built")

        assertThat(fitBulletsToLimit(listOf("$first\n" + " ".repeat(20) + second))).containsExactly("$first $second")
    }

    @Test
    fun newlineAfterAnAbbreviationIsNotABoundary() {
        val original = unpunctuated(392) + " Asst.\nProfessor of CSE at the institute."

        assertThat(fitBulletsToLimit(listOf(original))).containsExactly(original)
    }

    @Test
    fun seniorityAndInstitutionAbbreviationsAreNotBoundaries() {
        val words = listOf(
            "Sr.", "Jr.", "Asst.", "Assoc.", "Dy.", "Addl.", "Govt.", "Engg.", "Dept.", "Univ.", "Inst.", "Mgr.", "Mgmt.",
            "Exec.", "Admin.", "Tech.", "Intl.", "Natl.", "Est.", "Ref.", "Fig.", "Vol.", "Ed.", "Hons.", "Sec.", "Div.",
            "Corp.", "Bros.", "asst.", "ASST.",
        )
        words.forEach { word ->
            val original = unpunctuated(380) + " as $word Professor of CSE at the institute."

            assertThat(fitBulletsToLimit(listOf(original))).containsExactly(original)
        }
    }

    @Test
    fun professorTitleStaysInOnePiece() {
        val original = unpunctuated(380) + " as Asst. Professor of CSE at the institute."

        assertThat(fitBulletsToLimit(listOf(original))).containsExactly(original)
    }

    @Test
    fun bulletThatFitsAfterTrimmingIsNotFlagged() {
        val body = sentence(398)
        val original = "$body   \n  "
        assertThat(original.length).isEqualTo(404)

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted).containsExactly(body)
        assertThat(fitted.single().isTooLong()).isFalse()
    }

    @Test
    fun bulletThatFitsAfterCollapsingWhitespaceIsNotFlagged() {
        val original = sentence(380) + "   " + sentence(18) + "     "
        assertThat(original.length).isGreaterThan(400)

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted).containsExactly(sentence(380) + " " + sentence(18))
    }

    @Test
    fun fourHundredAndFiveCharactersWithTrailingWhitespaceIsSentTrimmed() {
        val body = sentence(398)
        val original = "$body       "
        assertThat(original.length).isEqualTo(405)

        val fitted = fitBulletsToLimit(listOf(original))

        assertThat(fitted).containsExactly(body)
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
