package com.tailormyresume.feature.onboarding.impl.consent

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.data.sampleEducationEntry
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.onboarding.api.navigation.ConsentNavKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h891dp-xxhdpi")
class ConsentFirstRunTest {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val noOpActions = ConsentActions(
        onPurposeToggle = {},
        onAgree = {},
        onNotNow = {},
        onReadAgain = {},
        onBackToStart = {},
        onBack = {},
    )

    @Test
    fun firstRun_saysNothingIsUploadedYet() {
        show(ConsentUiState())

        composeRule.onNodeWithContentDescription("Nothing is uploaded yet").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Choose what we may do").assertDoesNotExist()
    }

    @Test
    fun reConsent_keepsChooseWhatWeMayDo() {
        show(ConsentUiState(isReconsent = true))

        composeRule.onNodeWithContentDescription("Choose what we may do").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Nothing is uploaded yet").assertDoesNotExist()
    }

    @Test
    fun theDeleteTheUploadedFileCardIsShown() {
        show(ConsentUiState())

        composeRule.onNodeWithText("Delete the uploaded file").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun theDeleteCardOnlyClaimsWhatTailorMyResumeServersDo() {
        show(ConsentUiState())

        composeRule.onNodeWithText("TailorMyResume servers keep none of it", substring = true)
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun anOlderStoredNoticeMarksTheViewModelAsReconsent() = runTest {
        val session = TestSessionRepository()
        session.recordConsent(
            ConsentRecord(
                purposes = ConsentPurpose.entries.toSet(),
                acceptedAt = Instant.fromEpochMilliseconds(0),
                noticeVersion = "2026-01-a",
            ),
        )
        val viewModel = viewModelOver(session)

        viewModel.onEnter(ConsentNavKey(DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.isReconsent).isTrue()
    }

    @Test
    fun noStoredConsentLeavesTheViewModelOnFirstRun() = runTest {
        val viewModel = viewModelOver(TestSessionRepository())

        viewModel.onEnter(ConsentNavKey(DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.isReconsent).isFalse()
    }

    @Test
    fun aReturningUserWithStoredFactsAfterSignOutSeesTheReConsentHeading() = runTest {
        val session = TestSessionRepository()
        val profile = TestProfileRepository()
        profile.saveProfile(profileWithOneEntry())
        val viewModel = viewModelOver(session, profile)

        viewModel.onEnter(ConsentNavKey(DebugScenario.DEFAULT))
        show(viewModel.uiState.value)

        assertThat(viewModel.uiState.value.isReconsent).isTrue()
        composeRule.onNodeWithContentDescription("Choose what we may do").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Nothing is uploaded yet").assertDoesNotExist()
    }

    @Test
    fun anEarlierSignedInAccountWithoutAStoredRecordIsNotFirstRun() = runTest {
        val session = TestSessionRepository()
        session.saveLastAccountId("uid-1")
        val viewModel = viewModelOver(session)

        viewModel.onEnter(ConsentNavKey(DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.isReconsent).isTrue()
    }

    @Test
    fun theFirstRunHeadingWaitsUntilTheHistoryCheckResolves() = runTest {
        val profile = GatedProfileRepository()
        val viewModel = viewModelOver(TestSessionRepository(), profile)

        viewModel.onEnter(ConsentNavKey(DebugScenario.DEFAULT))
        assertThat(viewModel.uiState.value.isReconsent).isTrue()

        profile.resolve(null)
        assertThat(viewModel.uiState.value.isReconsent).isFalse()
    }

    private class GatedProfileRepository : ProfileRepository {
        private val gate = MutableSharedFlow<CandidateProfile?>(replay = 1)

        suspend fun resolve(profile: CandidateProfile?) = gate.emit(profile)

        override fun observeProfile(): Flow<CandidateProfile?> = gate

        override suspend fun saveProfile(profile: CandidateProfile) = Unit

        override suspend fun clearProfile() = Unit
    }

    private fun profileWithOneEntry() = CandidateProfile(
        fullName = "Asha Rao",
        email = "asha@example.com",
        phone = "",
        headline = "",
        skills = emptyList(),
        entries = listOf(sampleEducationEntry),
    )

    private fun viewModelOver(
        session: TestSessionRepository,
        profile: ProfileRepository = TestProfileRepository(),
    ) = ConsentViewModel(
        sessionRepository = session,
        profileRepository = profile,
        consentUploader = { Result.success(Unit) },
        nextOnboardingStep = NextOnboardingStepUseCase(session, profile),
        clock = TestClock(),
    )

    private fun show(uiState: ConsentUiState) {
        composeRule.setContent {
            TmrTheme(darkTheme = false) { ConsentScreen(uiState = uiState, actions = noOpActions) }
        }
        composeRule.waitForIdle()
    }
}
