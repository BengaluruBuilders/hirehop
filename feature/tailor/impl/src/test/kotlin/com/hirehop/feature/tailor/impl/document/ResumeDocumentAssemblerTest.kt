package com.hirehop.feature.tailor.impl.document

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.TailoredResume
import com.hirehop.feature.tailor.impl.sourceBullets
import com.hirehop.feature.tailor.impl.testBullet
import com.hirehop.feature.tailor.impl.testEntry
import com.hirehop.feature.tailor.impl.testProfile
import org.junit.Test

class ResumeDocumentAssemblerTest {

    private val assembler = ResumeDocumentAssembler()

    private fun bulletsOf(document: ResumeDocument): List<String> =
        document.sections.flatMap { it.entries }.flatMap { it.bullets }

    @Test
    fun acceptedBullet_usesProposedText() {
        val bullet = testBullet("b1", original = "Built a tool", proposed = "Developed a tool", decision = BulletDecision.ACCEPTED)

        val document = assembler.assemble(
            testProfile(listOf(testEntry("exp-1"))),
            TailoredResume(listOf(bullet)),
        )

        assertThat(bulletsOf(document)).containsExactly("Developed a tool")
    }

    @Test
    fun rejectedBullet_usesOriginalText() {
        val bullet = testBullet("b1", original = "Built a tool", proposed = "Developed a tool", decision = BulletDecision.REJECTED)

        val document = assembler.assemble(
            testProfile(listOf(testEntry("exp-1"))),
            TailoredResume(listOf(bullet)),
        )

        assertThat(bulletsOf(document)).containsExactly("Built a tool")
    }

    @Test
    fun pendingBullet_usesOriginalText() {
        val bullet = testBullet("b1", original = "Built a tool", proposed = "Developed a tool")

        val document = assembler.assemble(
            testProfile(listOf(testEntry("exp-1"))),
            TailoredResume(listOf(bullet)),
        )

        assertThat(bulletsOf(document)).containsExactly("Built a tool")
    }

    @Test
    fun acceptedBulletWithViolation_stillUsesOriginalText() {
        val bullet = testBullet(
            id = "b1",
            original = "Helped a team",
            proposed = "Led a team",
            violations = listOf(GuardrailViolation.VerbEscalation("Helped", "Led")),
            decision = BulletDecision.ACCEPTED,
        )

        val document = assembler.assemble(
            testProfile(listOf(testEntry("exp-1"))),
            TailoredResume(listOf(bullet)),
        )

        assertThat(bulletsOf(document)).containsExactly("Helped a team")
    }

    @Test
    fun unchangedBullet_keepsItsText() {
        val bullet = testBullet("b1", original = "Wrote SQL", proposed = "Wrote SQL")

        val document = assembler.assemble(
            testProfile(listOf(testEntry("exp-1"))),
            TailoredResume(listOf(bullet)),
        )

        assertThat(bulletsOf(document)).containsExactly("Wrote SQL")
    }

    @Test
    fun unconfirmedEntry_isLeftOut() {
        val profile = testProfile(
            listOf(
                testEntry("exp-1", isConfirmed = true),
                testEntry("exp-2", isConfirmed = false),
            ),
        )
        val resume = TailoredResume(
            listOf(
                testBullet("b1", entryId = "exp-1", original = "Kept", proposed = "Kept"),
                testBullet("b2", entryId = "exp-2", original = "Hidden", proposed = "Hidden"),
            ),
        )

        val document = assembler.assemble(profile, resume)

        assertThat(document.sections.flatMap { it.entries }.map { it.title }).containsExactly("Title exp-1")
        assertThat(bulletsOf(document)).containsExactly("Kept")
    }

    @Test
    fun sections_followStandardOrderAndSkipEmptyOnes() {
        val profile = testProfile(
            listOf(
                testEntry("ach", EntryCategory.ACHIEVEMENT),
                testEntry("edu", EntryCategory.EDUCATION),
                testEntry("proj", EntryCategory.PROJECT),
                testEntry("exp", EntryCategory.EXPERIENCE),
                testEntry("cert", EntryCategory.CERTIFICATION),
            ),
        )

        val document = assembler.assemble(profile, TailoredResume(emptyList()))

        assertThat(document.sections.map { it.heading })
            .containsExactly("Education", "Experience", "Projects", "Certifications", "Achievements")
            .inOrder()
    }

    @Test
    fun categoryWithoutEntries_hasNoSection() {
        val profile = testProfile(listOf(testEntry("edu", EntryCategory.EDUCATION)))

        val document = assembler.assemble(profile, TailoredResume(emptyList()))

        assertThat(document.sections.map { it.category }).containsExactly(EntryCategory.EDUCATION)
    }

    @Test
    fun entryWithoutTailoredBullets_fallsBackToProfileBullets() {
        val profile = testProfile(listOf(testEntry("edu", EntryCategory.EDUCATION, bullets = sourceBullets("a", "b"))))

        val document = assembler.assemble(profile, TailoredResume(emptyList()))

        assertThat(bulletsOf(document)).containsExactly("Source text of a", "Source text of b").inOrder()
    }

    @Test
    fun header_carriesNameContactHeadlineAndSkills() {
        val profile = testProfile(listOf(testEntry("exp-1")), skills = listOf("Kotlin", " kotlin ", "", "SQL", "SQL"))

        val document = assembler.assemble(profile, TailoredResume(emptyList()))

        assertThat(document.name).isEqualTo("Priya Sharma")
        assertThat(document.contactLine).isEqualTo("priya@example.com | +91 98765 43210")
        assertThat(document.headline).isEqualTo("Backend developer")
        assertThat(document.skills).containsExactly("Kotlin", "SQL").inOrder()
    }

    @Test
    fun dateRange_skipsBlankParts() {
        val profile = testProfile(
            listOf(
                testEntry("edu", EntryCategory.EDUCATION, startDate = "2021", endDate = "2025"),
                testEntry("proj", EntryCategory.PROJECT, startDate = "", endDate = "Apr 2025"),
                testEntry("cert", EntryCategory.CERTIFICATION, startDate = "", endDate = ""),
            ),
        )

        val document = assembler.assemble(profile, TailoredResume(emptyList()))

        assertThat(document.sections.map { it.entries.single().dateRange })
            .containsExactly("2021 - 2025", "Apr 2025", "")
            .inOrder()
    }

    @Test
    fun blankBulletText_isDropped() {
        val bullet = testBullet("b1", original = "  ", proposed = "  ")

        val document = assembler.assemble(
            testProfile(listOf(testEntry("exp-1"))),
            TailoredResume(listOf(bullet)),
        )

        assertThat(bulletsOf(document)).isEmpty()
    }
}
