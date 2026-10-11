package com.tailormyresume.feature.tailor.impl.export

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.PageSize
import com.tailormyresume.core.model.TailoredBullet
import com.tailormyresume.core.model.TailoredResume
import com.tailormyresume.core.model.TailoredSkills
import com.tailormyresume.core.model.TailoredText
import com.tailormyresume.core.testing.data.PrototypeFixtures
import com.tailormyresume.feature.tailor.impl.document.ResumeDocument
import com.tailormyresume.feature.tailor.impl.document.ResumeDocumentAssembler
import com.tailormyresume.feature.tailor.impl.document.ResumeEntry
import com.tailormyresume.feature.tailor.impl.document.ResumeSection
import com.tailormyresume.feature.tailor.impl.document.TestResumeHeadings
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ResumePdfFitTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val profile = PrototypeFixtures.returning().profile

    private val document: ResumeDocument =
        ResumeDocumentAssembler(TestResumeHeadings).assemble(profile, tailoredResume())

    private fun renderer(): ResumePdfRenderer = AndroidPdfResumeRenderer(
        ApplicationProvider.getApplicationContext(),
        UnconfinedTestDispatcher(),
        UnconfinedTestDispatcher(),
    )

    private fun tailoredResume(): TailoredResume = TailoredResume(
        bullets = profile.entries
            .filter { it.id in TAILORED_ENTRY_IDS }
            .flatMap { entry ->
                entry.bullets.map { bullet -> tailoredBullet(entry.id, bullet.id, bullet.text) }
            },
        entryIds = null,
        summary = TailoredText(
            text = "Finance analyst with 4 years of SQL, Power BI and Excel work, presenting insights to senior stakeholders.",
            original = profile.summary,
            decision = BulletDecision.ACCEPTED,
        ),
        skills = TailoredSkills(
            skills = profile.skills,
            original = profile.skills,
            decision = BulletDecision.ACCEPTED,
        ),
    )

    private fun tailoredBullet(entryId: String, bulletId: String, original: String): TailoredBullet =
        TailoredBullet(
            id = bulletId,
            entryId = entryId,
            originalText = original,
            proposedText = when (bulletId) {
                INFOSYS_B1 -> INFOSYS_B1_PROPOSED
                TATA_B1 -> TATA_B1_PROPOSED
                else -> original
            },
            sourceIds = listOf(bulletId),
            editTypes = emptyList(),
            keywordsUsed = emptyList(),
            violations = emptyList(),
            decision = if (bulletId == TATA_B1) BulletDecision.REJECTED else BulletDecision.ACCEPTED,
        )

    private fun expectedLines(document: ResumeDocument): List<String> = buildList {
        listOf(document.name, document.contactLine, document.headline, document.summary)
            .filter { it.isNotEmpty() }
            .forEach(::add)
        document.sections.forEach { section ->
            add(section.heading)
            section.entries.forEach { entry ->
                add(listOf(entry.title, entry.organization).filter { it.isNotEmpty() }.joinToString(", "))
                if (entry.dateRange.isNotEmpty()) add(entry.dateRange)
                entry.bullets.forEach(::add)
            }
        }
        if (document.skills.isNotEmpty()) {
            add(document.skillsHeading)
            add(document.skills.joinToString(", "))
        }
    }

    @Test
    fun prototypeDataIsOnePageInA4AndLetter() = runTest {
        val renderer = renderer()

        listOf(PageSize.A4, PageSize.LETTER).forEach { size ->
            val rendered = renderer.render(document, "resume-${size.name.lowercase()}.pdf", size)
            val copied = File(folder.root, rendered.file.name)
            copied.writeBytes(rendered.file.readBytes())

            assertThat(rendered.pageCount).isEqualTo(1)
            assertThat(copied.exists()).isTrue()
            assertThat(copied.length()).isGreaterThan(0L)
            assertThat(copied.length()).isAtMost(200_000L)
            assertThat(renderer.pageCount(document, size)).isEqualTo(1)
        }
    }

    @Test
    fun renderedLinesEqualAcceptedDocument() = runTest {
        val rendered = renderer().render(document, "accepted.pdf", PageSize.A4)

        assertThat(rendered.lines).containsExactlyElementsIn(expectedLines(document)).inOrder()
        assertThat(rendered.lines).contains(INFOSYS_B1_PROPOSED)
        assertThat(rendered.lines).contains(TATA_B1_ORIGINAL)
        assertThat(rendered.lines).doesNotContain(TATA_B1_PROPOSED)
    }

    @Test
    fun oversizedResumeSpillsToMoreThanOnePage() = runTest {
        val oversized = ResumeDocument(
            name = "Priya Deshmukh",
            contactLine = "priya.deshmukh@gmail.com | +91 98200 41736",
            headline = "Finance analyst",
            skills = listOf("SQL", "Power BI"),
            sections = listOf(
                ResumeSection(
                    category = EntryCategory.EXPERIENCE,
                    heading = "Experience",
                    entries = (1..60).map { index ->
                        ResumeEntry(
                            title = "Role $index",
                            organization = "Company $index",
                            dateRange = "Jan 2020 - Dec 2021",
                            bullets = List(3) { "$LONG_BULLET entry $index point ${it + 1}" },
                        )
                    },
                ),
            ),
            skillsHeading = "Skills",
        )

        val rendered = renderer().render(oversized, "oversized.pdf", PageSize.A4)

        assertThat(rendered.pageCount).isGreaterThan(1)
    }

    @Test
    fun fileNameIsKept() = runTest {
        val fileName = File(folder.root, "Priya-Deshmukh_Northwind-GCC_Associate-Analyst.pdf").name

        val rendered = renderer().render(document, fileName, PageSize.A4)

        assertThat(rendered.file.name).isEqualTo(fileName)
    }

    private companion object {
        val TAILORED_ENTRY_IDS = setOf("exp-infosys", "exp-tata")
        const val INFOSYS_B1 = "exp-infosys-b1"
        const val TATA_B1 = "exp-tata-b1"
        const val INFOSYS_B1_PROPOSED = "Built Power BI dashboards for monthly financial reporting, cutting prep time by 40%."
        const val TATA_B1_ORIGINAL = "Built forecasting models in Python for category demand."
        const val TATA_B1_PROPOSED = "Trained forecasting models in Python for category demand across 5 business teams."
        const val LONG_BULLET = "Owned the end to end delivery of a cross functional reporting programme, aligned finance, " +
            "sales and operations leaders on one set of numbers, and cut the monthly close by two working days."
    }
}
