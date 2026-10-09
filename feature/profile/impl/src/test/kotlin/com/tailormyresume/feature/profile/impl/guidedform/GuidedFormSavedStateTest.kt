package com.tailormyresume.feature.profile.impl.guidedform

import androidx.lifecycle.SavedStateHandle
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
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class GuidedFormSavedStateTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()

    private fun viewModel(handle: SavedStateHandle) = GuidedFormViewModel(
        factWriter = UserFactWriter(repository, AddUserStatedFactsUseCase(repository, TestIdGenerator())),
        exitResolver = ProfileExitResolver(NextOnboardingStepUseCase(session, repository), session),
        connectivityMonitor = TestConnectivityMonitor(),
        savedState = handle,
    )

    private fun restarted(handle: SavedStateHandle) =
        SavedStateHandle(handle.keys().associateWith { handle.get<Any>(it) })

    @Test
    fun theTypedValuesAndStepSurviveProcessDeath() {
        val handle = SavedStateHandle()
        val before = viewModel(handle)
        before.onEnter(GuidedProfileFormNavKey(startStep = "education"))
        before.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        before.onAction(GuidedFormAction.ValueChanged(GuidedField.COLLEGE, "Pune University"))

        val after = viewModel(restarted(handle))
        after.onEnter(GuidedProfileFormNavKey())

        val state = after.uiState.value
        assertThat(state.step).isEqualTo(GuidedStep.EDUCATION)
        assertThat(state.values[GuidedField.COURSE]).isEqualTo("B.Tech Computer Science")
        assertThat(state.values[GuidedField.COLLEGE]).isEqualTo("Pune University")
    }

    @Test
    fun addedSkillsSurviveProcessDeath() {
        val handle = SavedStateHandle()
        val before = viewModel(handle)
        before.onEnter(GuidedProfileFormNavKey(startStep = "skills"))
        before.onAction(GuidedFormAction.ValueChanged(GuidedField.SKILL, "SQL"))
        before.onAction(GuidedFormAction.AddSkill)

        val after = viewModel(restarted(handle))
        after.onEnter(GuidedProfileFormNavKey())

        assertThat(after.uiState.value.skills).containsExactly("SQL")
    }

    @Test
    fun backThenNextAfterRestoreRewritesTheFinishedStepInsteadOfDuplicatingIt() = runBlocking {
        val handle = SavedStateHandle()
        val before = viewModel(handle)
        before.onEnter(GuidedProfileFormNavKey(startStep = "education"))
        before.onAction(GuidedFormAction.ValueChanged(GuidedField.COURSE, "B.Tech Computer Science"))
        before.onAction(GuidedFormAction.Next)

        val after = viewModel(restarted(handle))
        after.onEnter(GuidedProfileFormNavKey())
        after.onAction(GuidedFormAction.Back)
        after.onAction(GuidedFormAction.Next)

        val education = repository.observeProfile().first().let(::checkNotNull).entries
            .filter { it.category == EntryCategory.EDUCATION }
        assertThat(education).hasSize(1)
        assertThat(after.uiState.value.completedSteps).contains(GuidedStep.EDUCATION)
    }
}
