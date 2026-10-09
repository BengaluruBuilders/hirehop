package com.tailormyresume.feature.profile.impl.evidencepath

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestIdGenerator
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.impl.ProfileExitResolver
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class EvidencePathCategoryRaceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val inner = TestProfileRepository()
    private val repository = GatedProfileRepository(inner)
    private val session = TestSessionRepository()
    private val viewModel = EvidencePathViewModel(
        addUserStatedFacts = AddUserStatedFactsUseCase(repository, TestIdGenerator()),
        exitResolver = ProfileExitResolver(NextOnboardingStepUseCase(session, inner), session),
        connectivityMonitor = TestConnectivityMonitor(),
        sessionRepository = session,
    )

    private fun act(action: EvidencePathAction) = viewModel.onAction(action)

    private fun saveAnswer(text: String) {
        act(EvidencePathAction.AnswerChanged(text))
        act(EvidencePathAction.Save)
    }

    @Test
    fun categoryChosenWhileTheQuestionOneWriteIsPending_doesNotInheritTheOldAnchor() = runTest {
        viewModel.onEnter(FactEvidenceNavKey(category = "projects"))
        val gate = repository.holdNextSave()
        saveAnswer("Library database project")

        act(EvidencePathAction.CategoryChosen(EvidenceCategory.WORK))
        gate.complete(Unit)

        val state = viewModel.uiState.value
        assertThat(state.category).isEqualTo(EvidenceCategory.WORK)
        assertThat(state.anchor).isNull()
        assertThat(state.stamped).isNull()
        assertThat(state.isSaving).isFalse()
    }

    @Test
    fun aWorkAnswerAfterTheRace_neverAttachesToTheProjectsEntry() = runTest {
        viewModel.onEnter(FactEvidenceNavKey(category = "projects"))
        val gate = repository.holdNextSave()
        saveAnswer("Library database project")
        act(EvidencePathAction.CategoryChosen(EvidenceCategory.WORK))
        gate.complete(Unit)

        act(EvidencePathAction.Skip)
        saveAnswer("Reports reached 40 stores")

        val entries = checkNotNull(inner.observeProfile().first()).entries
        assertThat(entries.single().category).isEqualTo(EntryCategory.PROJECT)
        assertThat(entries.single().bullets.map { it.text }).doesNotContain("Reports reached 40 stores")
        assertThat(viewModel.uiState.value.needsFirstAnswer).isTrue()
    }
}

private class GatedProfileRepository(private val delegate: TestProfileRepository) : ProfileRepository {

    private var gate: CompletableDeferred<Unit>? = null

    fun holdNextSave(): CompletableDeferred<Unit> = CompletableDeferred<Unit>().also { gate = it }

    override fun observeProfile(): Flow<CandidateProfile?> = delegate.observeProfile()

    override suspend fun saveProfile(profile: CandidateProfile) {
        gate?.await()
        gate = null
        delegate.saveProfile(profile)
    }

    override suspend fun clearProfile() = delegate.clearProfile()
}
