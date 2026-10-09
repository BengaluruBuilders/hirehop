package com.tailormyresume.feature.profile.impl.evidencepath

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestIdGenerator
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.impl.ProfileExitResolver
import org.junit.Rule
import org.junit.Test

class EvidencePathDoubleFireTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()
    private val viewModel = EvidencePathViewModel(
        addUserStatedFacts = AddUserStatedFactsUseCase(repository, TestIdGenerator()),
        exitResolver = ProfileExitResolver(NextOnboardingStepUseCase(session, repository), session),
        connectivityMonitor = TestConnectivityMonitor(),
        sessionRepository = session,
    )

    private fun act(action: EvidencePathAction) = viewModel.onAction(action)

    @Test
    fun skipTappedTwiceFromTheSameQuestionAdvancesOnce() {
        viewModel.onEnter(FactEvidenceNavKey(category = "projects"))

        act(EvidencePathAction.SkipFrom(0))
        act(EvidencePathAction.SkipFrom(0))

        assertThat(viewModel.uiState.value.questionNumber).isEqualTo(2)
    }

    @Test
    fun skipFromTheCurrentQuestionStillAdvances() {
        viewModel.onEnter(FactEvidenceNavKey(category = "projects"))

        act(EvidencePathAction.SkipFrom(0))
        act(EvidencePathAction.SkipFrom(1))

        assertThat(viewModel.uiState.value.questionNumber).isEqualTo(3)
    }

    @Test
    fun finishTappedAgainAfterTheExitWasConsumedDoesNotExitTwice() {
        viewModel.onEnter(FactEvidenceNavKey(category = "projects"))
        repeat(4) { act(EvidencePathAction.Skip) }

        act(EvidencePathAction.Finish)
        assertThat(viewModel.uiState.value.navigation).isNotNull()
        act(EvidencePathAction.NavigationConsumed)
        act(EvidencePathAction.Finish)

        assertThat(viewModel.uiState.value.navigation).isNull()
    }
}
