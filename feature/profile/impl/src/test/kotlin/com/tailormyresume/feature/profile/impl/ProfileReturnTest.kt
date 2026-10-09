package com.tailormyresume.feature.profile.impl

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestIdGenerator
import com.tailormyresume.feature.profile.api.navigation.FactEvidenceNavKey
import com.tailormyresume.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.tailormyresume.feature.profile.impl.evidencepath.EvidenceNavigation
import com.tailormyresume.feature.profile.impl.evidencepath.EvidencePathAction
import com.tailormyresume.feature.profile.impl.evidencepath.EvidencePathViewModel
import com.tailormyresume.feature.profile.impl.guidedform.GuidedFormAction
import com.tailormyresume.feature.profile.impl.guidedform.GuidedFormViewModel
import com.tailormyresume.feature.profile.impl.guidedform.GuidedNavigation
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ProfileReturnTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()
    private val resolver = ProfileExitResolver(NextOnboardingStepUseCase(session, repository), session)
    private val guided = GuidedFormViewModel(
        factWriter = UserFactWriter(repository, AddUserStatedFactsUseCase(repository, TestIdGenerator())),
        exitResolver = resolver,
        connectivityMonitor = TestConnectivityMonitor(),
    )
    private val evidence = EvidencePathViewModel(
        addUserStatedFacts = AddUserStatedFactsUseCase(repository, TestIdGenerator()),
        exitResolver = resolver,
        connectivityMonitor = TestConnectivityMonitor(),
        sessionRepository = session,
    )

    @Test
    fun goToProfileFromTheSavedGuidedFormOpensProfileWhenTheFormStartedThere() = runTest {
        guided.onEnter(GuidedProfileFormNavKey(returnsToProfile = true))

        guided.onAction(GuidedFormAction.FinishSaved)

        assertThat(guided.uiState.value.navigation).isEqualTo(GuidedNavigation.Exit(ProfileExit.Profile))
    }

    @Test
    fun goToProfileFromAllDoneOpensProfileWhenThePathStartedThere() = runTest {
        evidence.onEnter(FactEvidenceNavKey(returnsToProfile = true))

        evidence.onAction(EvidencePathAction.Finish)

        assertThat(evidence.uiState.value.navigation).isEqualTo(EvidenceNavigation.Exit(ProfileExit.Profile))
    }
}
