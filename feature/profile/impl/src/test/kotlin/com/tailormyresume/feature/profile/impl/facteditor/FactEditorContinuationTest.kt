package com.tailormyresume.feature.profile.impl.facteditor

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.IdGenerator
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.continues
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.profile.api.navigation.FactEditorNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class FactEditorContinuationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()

    private fun role(
        id: String,
        title: String = "Intern",
        organization: String = "Acme",
        start: String = "Jan 2024",
        end: String = "Jun 2024",
        source: FactSource = FactSource.IMPORTED,
    ) = ProfileEntry(
        id, EntryCategory.EXPERIENCE, title, organization, start, end,
        listOf(EvidenceBullet("$id-b", "Task of $id done.")), source, isConfirmed = true,
    )

    private fun editorFor(entryId: String, vararg entries: ProfileEntry): FactEditorViewModel {
        repository.sendProfile(CandidateProfile("P", "", "", "", emptyList(), entries.toList()))
        return FactEditorViewModel(
            profileRepository = repository,
            factIdAllocator = FactIdAllocator(),
            idGenerator = IdGenerator { "new-bullet" },
            connectivityMonitor = TestConnectivityMonitor(),
            key = FactEditorNavKey(entryId = entryId, entryType = "experience", scenario = DebugScenario.DEFAULT),
        )
    }

    private suspend fun saved(): List<ProfileEntry> = checkNotNull(repository.observeProfile().first()).entries

    @Test
    fun editingTheDatesOfARoleMovesItsContinuationsToo() = runTest {
        val viewModel = editorFor("W-01", role("W-01"), role("W-02"), role("W-03", title = "Analyst", start = "Jan 2023"))

        viewModel.onStartDateChange("Feb 2024")
        viewModel.onEndDateChange("Jul 2024")
        viewModel.save()

        val entries = saved()
        assertThat(entries.map { it.startDate }).containsExactly("Feb 2024", "Feb 2024", "Jan 2023").inOrder()
        assertThat(entries.map { it.endDate }).containsExactly("Jul 2024", "Jul 2024", "Jun 2024").inOrder()
        assertThat(entries[1].continues(entries[0])).isTrue()
        assertThat(entries[1].bullets.map { it.text }).containsExactly("Task of W-02 done.")
    }

    @Test
    fun editingTheTitleAndOrganizationAlsoKeepsTheRoleTogether() = runTest {
        val viewModel = editorFor("W-01", role("W-01"), role("W-02"))

        viewModel.onTitleChange("Software Intern")
        viewModel.onToolsChange("Acme Labs")
        viewModel.save()

        val entries = saved()
        assertThat(entries.map { it.title }).containsExactly("Software Intern", "Software Intern")
        assertThat(entries.map { it.organization }).containsExactly("Acme Labs", "Acme Labs")
        assertThat(entries[1].continues(entries[0])).isTrue()
    }

    @Test
    fun editingALaterPartOfTheRoleKeepsTheWholeRunTogether() = runTest {
        val viewModel = editorFor("W-02", role("W-01"), role("W-02"), role("W-03"))

        viewModel.onEndDateChange("Aug 2024")
        viewModel.save()

        assertThat(saved().map { it.endDate }).containsExactly("Aug 2024", "Aug 2024", "Aug 2024")
    }

    @Test
    fun anUnrelatedRoleWithTheSameCompanyIsNotTouched() = runTest {
        val viewModel = editorFor("W-01", role("W-01"), role("W-02", start = "Jan 2025", end = "Jun 2025"))

        viewModel.onEndDateChange("Jul 2024")
        viewModel.save()

        val entries = saved()
        assertThat(entries[0].endDate).isEqualTo("Jul 2024")
        assertThat(entries[1].startDate).isEqualTo("Jan 2025")
        assertThat(entries[1].endDate).isEqualTo("Jun 2025")
    }

    @Test
    fun aRoleWithoutContinuationsSavesAsBefore() = runTest {
        val viewModel = editorFor("W-02", role("W-01"), role("W-02", title = "Analyst", start = "Jan 2025", end = "Jun 2025"))

        viewModel.onEndDateChange("Jul 2025")
        viewModel.save()

        val entries = saved()
        assertThat(entries[0]).isEqualTo(role("W-01"))
        assertThat(entries[1].endDate).isEqualTo("Jul 2025")
    }
}
