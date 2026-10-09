package com.tailormyresume.feature.tailor.impl.document

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.TailoredResume
import com.tailormyresume.feature.tailor.impl.testEntry
import com.tailormyresume.feature.tailor.impl.testProfile
import org.junit.Test

class ResumeDocumentContinuationTest {

    private val assembler = ResumeDocumentAssembler(TestResumeHeadings)

    private fun role(
        id: String,
        count: Int,
        firstLine: Int = 1,
        startDate: String = "2024",
        endDate: String = "2025",
    ): ProfileEntry = testEntry(
        id = id,
        title = "Intern",
        organization = "Acme",
        startDate = startDate,
        endDate = endDate,
        bullets = (firstLine until firstLine + count).map { EvidenceBullet("$id-b$it", "Task $it done.") },
    )

    private fun entriesOf(vararg entries: ProfileEntry, entryIds: List<String>? = null): List<ResumeEntry> =
        assembler.assemble(testProfile(entries.toList()), TailoredResume(emptyList(), entryIds))
            .sections.flatMap { it.entries }

    @Test
    fun continuationEntriesShareOneHeaderAndKeepBulletOrder() {
        val entries = entriesOf(role("W-01", 15), role("W-02", 5, firstLine = 16))

        assertThat(entries).hasSize(1)
        assertThat(entries.single().title).isEqualTo("Intern")
        assertThat(entries.single().bullets).containsExactlyElementsIn((1..20).map { "Task $it done." }).inOrder()
    }

    @Test
    fun sameCompanyWithDifferentDatesStaysTwoRoles() {
        val entries = entriesOf(role("W-01", 2), role("W-02", 2, firstLine = 3, startDate = "2025", endDate = "2026"))

        assertThat(entries.map { it.dateRange }).containsExactly("2024 - 2025", "2025 - 2026").inOrder()
        assertThat(entries.map { it.bullets.size }).containsExactly(2, 2)
    }

    @Test
    fun recordedContinuationsWithoutCitedBulletsStillShowOneHeader() {
        val entries = entriesOf(role("W-01", 2), role("W-02", 2, firstLine = 3), entryIds = listOf("W-01", "W-02"))

        assertThat(entries).hasSize(1)
    }

    @Test
    fun entryWithNoBulletsAndNotRecordedIsStillDropped() {
        val empty = testEntry("W-03", title = "Analyst", organization = "Acme", startDate = "2024", endDate = "2025")

        val entries = entriesOf(role("W-01", 2), empty, entryIds = listOf("W-01"))

        assertThat(entries.map { it.title }).containsExactly("Intern")
    }
}
