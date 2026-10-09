package com.tailormyresume.feature.profile.impl.guidedform

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestIdGenerator
import com.tailormyresume.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.tailormyresume.feature.profile.impl.ProfileExitResolver
import com.tailormyresume.feature.profile.impl.UserFactWriter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GuidedFormFiledRefreshTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()
    private val viewModel = GuidedFormViewModel(
        factWriter = UserFactWriter(repository, AddUserStatedFactsUseCase(repository, TestIdGenerator())),
        exitResolver = ProfileExitResolver(NextOnboardingStepUseCase(session, repository), session),
        connectivityMonitor = TestConnectivityMonitor(),
    )

    private fun fileEducation() {
        viewModel.onEnter(GuidedProfileFormNavKey(startStep = "education"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COLLEGE, "Pune University"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.EDUCATION_END, "2024"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSEWORK, "DBMS"))
        viewModel.onAction(GuidedFormAction.Next)
    }

    @Test
    fun anEditedFactShowsItsNewTextOnTheFiledCard() = runTest {
        fileEducation()
        val profile = repository.observeProfile().first().let(::checkNotNull)
        val degreeId = viewModel.uiState.value.shownFiledEntries.first().id

        repository.sendProfile(
            profile.copy(entries = profile.entries.map { if (it.id == degreeId) it.copy(title = "M.Tech Data Science") else it }),
        )

        assertThat(viewModel.uiState.value.shownFiledEntries.first { it.id == degreeId }.title).isEqualTo("M.Tech Data Science")
    }

    @Test
    fun aDeletedFactDisappearsFromTheFiledCards() = runTest {
        fileEducation()
        val profile = repository.observeProfile().first().let(::checkNotNull)
        val deletedId = viewModel.uiState.value.shownFiledEntries.first().id

        repository.sendProfile(profile.copy(entries = profile.entries.filterNot { it.id == deletedId }))

        assertThat(viewModel.uiState.value.shownFiledEntries.map { it.id }).doesNotContain(deletedId)
        assertThat(viewModel.uiState.value.shownFiledEntries).hasSize(1)
        assertThat(viewModel.uiState.value.createdEntryIds).doesNotContain(deletedId)
    }

    private fun fileDegreeOnly() {
        viewModel.onEnter(GuidedProfileFormNavKey(startStep = "education"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.COLLEGE, "Pune University"))
        viewModel.onAction(GuidedFormAction.ValueChanged(GuidedField.EDUCATION_END, "2024"))
        viewModel.onAction(GuidedFormAction.Next)
    }

    private fun educationEntries() =
        repository.let { kotlinx.coroutines.runBlocking { it.observeProfile().first() } }.let(::checkNotNull).entries
            .filter { it.category == EntryCategory.EDUCATION }

    @Test
    fun aStaleEmissionDoesNotMakeBackThenNextDuplicateTheFact() = runTest {
        fileDegreeOnly()
        val fresh = repository.observeProfile().first().let(::checkNotNull)

        repository.sendProfile(fresh.copy(entries = emptyList()))
        repository.sendProfile(fresh)
        viewModel.onAction(GuidedFormAction.Back)
        viewModel.onAction(GuidedFormAction.Next)

        assertThat(educationEntries()).hasSize(1)
        assertThat(viewModel.uiState.value.shownFiledEntries).hasSize(1)
    }

    @Test
    fun backThenNextKeepsAnEditMadeFromAFiledCard() = runTest {
        fileDegreeOnly()
        val profile = repository.observeProfile().first().let(::checkNotNull)
        val degreeId = viewModel.uiState.value.shownFiledEntries.first().id
        repository.sendProfile(
            profile.copy(entries = profile.entries.map { if (it.id == degreeId) it.copy(title = "M.Tech") else it }),
        )

        viewModel.onAction(GuidedFormAction.Back)
        assertThat(viewModel.uiState.value.values[GuidedField.COURSE]).isEqualTo("M.Tech")
        viewModel.onAction(GuidedFormAction.Next)

        assertThat(educationEntries().map { it.title }).containsExactly("M.Tech")
    }
}
