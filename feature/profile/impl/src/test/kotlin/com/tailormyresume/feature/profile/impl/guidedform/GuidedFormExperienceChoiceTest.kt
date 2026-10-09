package com.tailormyresume.feature.profile.impl.guidedform

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestIdGenerator
import com.tailormyresume.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.tailormyresume.feature.profile.impl.ProfileExitResolver
import com.tailormyresume.feature.profile.impl.UserFactWriter
import org.junit.Rule
import org.junit.Test

class GuidedFormExperienceChoiceTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()
    private val viewModel = GuidedFormViewModel(
        factWriter = UserFactWriter(repository, AddUserStatedFactsUseCase(repository, TestIdGenerator())),
        exitResolver = ProfileExitResolver(NextOnboardingStepUseCase(session, repository), session),
        connectivityMonitor = TestConnectivityMonitor(),
    )

    @Test
    fun choosingStoresTheChoiceAndANewChoiceReplacesIt() {
        viewModel.onEnter(GuidedProfileFormNavKey(startStep = "experience"))
        assertThat(viewModel.uiState.value.experienceChoice).isNull()

        viewModel.onAction(GuidedFormAction.ChooseExperience(ExperienceChoice.NO))
        assertThat(viewModel.uiState.value.experienceChoice).isEqualTo(ExperienceChoice.NO)

        viewModel.onAction(GuidedFormAction.ChooseExperience(ExperienceChoice.YES))
        assertThat(viewModel.uiState.value.experienceChoice).isEqualTo(ExperienceChoice.YES)
    }
}
