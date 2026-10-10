package com.tailormyresume.feature.tailor.impl.result

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.TailoredResume
import com.tailormyresume.core.model.TailoredSkills
import com.tailormyresume.core.model.TailoredText
import com.tailormyresume.core.testing.data.PrototypeFixtures
import com.tailormyresume.feature.tailor.impl.entryFor
import com.tailormyresume.feature.tailor.impl.testBullet
import com.tailormyresume.feature.tailor.impl.testProfile
import org.junit.Test

class ChangeCardMapperTest {

    private val prototypeProfile = PrototypeFixtures.returning().profile

    @Test
    fun prototypeAnswerGivesThreeChangesAndNoInfosysAdded() {
        val bullet = testBullet(
            id = "b-infosys-1",
            entryId = "exp-infosys",
            original = "Built dashboards in Power BI for monthly reports, cut prep time 40%.",
            proposed = "Built Power BI dashboards for monthly reporting, cutting preparation time by 40%.",
            sourceIds = listOf("exp-infosys-b1"),
        )
        val resume = TailoredResume(
            bullets = listOf(bullet),
            entryIds = listOf("exp-infosys"),
            summary = TailoredText(
                text = "Finance analyst with 4 years of SQL, Power BI and Excel work, presenting insights to senior stakeholders.",
                original = "Finance analyst with 4 years of SQL, Power BI and Excel work across banking and retail.",
                sourceIds = listOf("ans-presenting-to-senior-leaders"),
            ),
            skills = TailoredSkills(
                skills = listOf("SQL", "Power BI", "Stakeholder updates"),
                original = listOf("SQL", "Power BI"),
            ),
        )

        val cards = resume.toChangeCards(prototypeProfile)

        assertThat(cards.map { it.id }).containsExactly("summary", "b-infosys-1", "skills").inOrder()
        assertThat(cards.map { it.kind })
            .containsExactly(ChangeKind.Rewritten, ChangeKind.Rewritten, ChangeKind.Added).inOrder()
        assertThat(cards.map { it.area })
            .containsExactly(ChangeArea.Summary, ChangeArea.Entry("Business Analyst · Infosys"), ChangeArea.Skills).inOrder()
        assertThat(cards[0].source).isEqualTo(ChangeSource.YourAnswer)
        assertThat(cards[1].source).isEqualTo(ChangeSource.YourResume)
        assertThat(cards[2].source).isEqualTo(ChangeSource.YourResume)
        assertThat(cards[1].before)
            .isEqualTo("Built dashboards in Power BI for monthly reports, cut prep time 40%.")
        assertThat(cards[1].after)
            .isEqualTo("Built Power BI dashboards for monthly reporting, cutting preparation time by 40%.")
        assertThat(
            cards.any {
                it.kind == ChangeKind.Added && (it.area as? ChangeArea.Entry)?.label?.contains("Infosys") == true
            },
        ).isFalse()
    }

    @Test
    fun answerBackedBulletOnNamedEmployerIsAddedFromYourAnswer() {
        val profile = testProfile(
            entries = listOf(entryFor(id = "exp-infosys")).map {
                it.copy(title = "Business Analyst", organization = "Infosys")
            },
        )
        val bullet = testBullet(
            id = "b-answer",
            entryId = "exp-infosys",
            original = "",
            proposed = "Presented monthly variance analysis to senior stakeholders.",
            sourceIds = listOf("ans-presenting-to-senior-leaders"),
        )

        val cards = TailoredResume(bullets = listOf(bullet)).toChangeCards(profile)

        assertThat(cards).hasSize(1)
        assertThat(cards[0].id).isEqualTo("b-answer")
        assertThat(cards[0].kind).isEqualTo(ChangeKind.Added)
        assertThat(cards[0].source).isEqualTo(ChangeSource.YourAnswer)
        assertThat(cards[0].area).isEqualTo(ChangeArea.Entry("Business Analyst · Infosys"))
        assertThat(cards[0].before).isNull()
        assertThat(cards[0].after).isEqualTo("Presented monthly variance analysis to senior stakeholders.")
        assertThat(cards[0].undone).isFalse()
    }

    @Test
    fun skillsWithSameItemsAreReordered() {
        val resume = TailoredResume(
            bullets = emptyList(),
            skills = TailoredSkills(
                skills = listOf("SQL", "Excel", "Power BI"),
                original = listOf("Power BI", "excel", "SQL"),
            ),
        )

        val cards = resume.toChangeCards(testProfile(entries = emptyList()))

        assertThat(cards).hasSize(1)
        assertThat(cards[0].id).isEqualTo("skills")
        assertThat(cards[0].kind).isEqualTo(ChangeKind.Reordered)
        assertThat(cards[0].source).isEqualTo(ChangeSource.YourResume)
        assertThat(cards[0].before).isEqualTo("Power BI, excel, SQL")
        assertThat(cards[0].after).isEqualTo("SQL, Excel, Power BI")
        assertThat(cards[0].undone).isFalse()
    }

    @Test
    fun summaryWithBlankOriginalIsNew() {
        val resume = TailoredResume(
            bullets = emptyList(),
            summary = TailoredText(
                text = "Finance analyst with 4 years of SQL, Power BI and Excel work.",
                original = "",
                sourceIds = listOf("ans-presenting-to-senior-leaders"),
            ),
        )

        val cards = resume.toChangeCards(testProfile(entries = emptyList()))

        assertThat(cards).hasSize(1)
        assertThat(cards[0].id).isEqualTo("summary")
        assertThat(cards[0].kind).isEqualTo(ChangeKind.New)
        assertThat(cards[0].source).isEqualTo(ChangeSource.YourAnswer)
        assertThat(cards[0].area).isEqualTo(ChangeArea.Summary)
        assertThat(cards[0].before).isNull()
        assertThat(cards[0].after).isEqualTo("Finance analyst with 4 years of SQL, Power BI and Excel work.")
    }

    @Test
    fun undoneChangeShowsOriginalAndNoBefore() {
        val bullet = testBullet(
            id = "b-undone",
            entryId = "exp-1",
            original = "Built an internal tool",
            proposed = "Developed an internal tool",
            decision = BulletDecision.REJECTED,
        )
        val resume = TailoredResume(
            bullets = listOf(bullet),
            summary = TailoredText(
                text = "Rewritten summary.",
                original = "Original summary.",
                decision = BulletDecision.REJECTED,
            ),
            skills = TailoredSkills(
                skills = listOf("Kotlin", "SQL"),
                original = listOf("SQL"),
                decision = BulletDecision.REJECTED,
            ),
        )

        val cards = resume.toChangeCards(testProfile(entries = emptyList()))

        assertThat(cards.map { it.id }).containsExactly("summary", "b-undone", "skills").inOrder()
        assertThat(cards.map { it.undone }).containsExactly(true, true, true)
        assertThat(cards.map { it.before }).containsExactly(null, null, null)
        assertThat(cards.map { it.after })
            .containsExactly("Original summary.", "Built an internal tool", "SQL").inOrder()
    }

    @Test
    fun unchangedBulletsAreNotListed() {
        val unchanged = listOf(
            testBullet(
                id = "b-same-1",
                entryId = "exp-1",
                original = "Wrote SQL pipelines over 20M+ rows of transaction data.",
                proposed = "Wrote SQL pipelines over 20M+ rows of transaction data.",
            ),
            testBullet(
                id = "b-same-2",
                entryId = "exp-2",
                original = "Reconciled ledgers and prepared month-end close packs.",
                proposed = "Reconciled ledgers and prepared month-end close packs.",
            ),
        )

        val resume = TailoredResume(
            bullets = unchanged,
            summary = TailoredText(text = "Same summary.", original = "Same summary."),
            skills = TailoredSkills(skills = listOf("SQL"), original = listOf("SQL")),
        )

        val cards = resume.toChangeCards(testProfile(entries = emptyList()))

        assertThat(cards).isEmpty()
    }
}
