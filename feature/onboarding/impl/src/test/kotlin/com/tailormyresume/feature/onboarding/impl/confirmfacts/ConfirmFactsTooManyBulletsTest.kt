package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.ImportRemovalNotice
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.onboarding.api.navigation.ConfirmFactsNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h2400dp-xxhdpi")
class ConfirmFactsTooManyBulletsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()

    private fun entry(id: String, count: Int, isConfirmed: Boolean) = ProfileEntry(
        id = id,
        category = EntryCategory.EXPERIENCE,
        title = "Data intern",
        organization = "Nashik",
        startDate = "May 2025",
        endDate = "Jul 2025",
        bullets = (1..count).map { EvidenceBullet("$id-b$it", "Did task $it.") },
        source = FactSource.IMPORTED,
        isConfirmed = isConfirmed,
    )

    private fun createViewModel(): ConfirmFactsViewModel {
        repository.sendProfile(
            sampleProfile.copy(entries = listOf(entry("MANY", 18, false), entry("OK", 15, false))),
        )
        return ConfirmFactsViewModel(
            profileRepository = repository,
            nextOnboardingStep = NextOnboardingStepUseCase(TestSessionRepository(), repository),
            connectivityMonitor = TestConnectivityMonitor(),
            importRemovalNotice = ImportRemovalNotice(),
        ).apply { onEnter(ConfirmFactsNavKey(scenario = DebugScenario.DEFAULT)) }
    }

    private fun show(isConfirmed: Boolean) {
        val scenario = if (isConfirmed) DebugScenario.FULLY_CONFIRMED else DebugScenario.DEFAULT
        val state = ConfirmFactsScenarioMapper.withProfile(
            state = ConfirmFactsScenarioMapper.seed(scenario),
            profile = sampleProfile.copy(entries = listOf(entry("MANY", 18, isConfirmed))),
            scenario = scenario,
        )
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ConfirmFactsScreen(
                    uiState = state,
                    actions = ConfirmFactsActions(
                        onBack = {},
                        onConfirm = {},
                        onEdit = { _, _ -> },
                        onAddOne = {},
                        onSkip = {},
                        onContinue = {},
                        onImportResume = {},
                    ),
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun factWithMoreThanFifteenBulletsIsFlaggedOnTheBoard() {
        val state = createViewModel().uiState.value

        assertThat(state.facts.first { it.id == "MANY" }.hasTooManyBullets).isTrue()
        assertThat(state.facts.first { it.id == "OK" }.hasTooManyBullets).isFalse()
    }

    @Test
    fun oneTapConfirmDoesNothingForAFactWithTooManyBullets() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Confirm("MANY"))

        assertThat(repository.observeProfile().first()?.entries?.first { it.id == "MANY" }?.isConfirmed).isFalse()
    }

    @Test
    fun oneTapConfirmStillWorksForFifteenBullets() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Confirm("OK"))

        assertThat(repository.observeProfile().first()?.entries?.first { it.id == "OK" }?.isConfirmed).isTrue()
    }

    @Test
    fun confirmedFactWithTooManyBulletsShowsTheStatusAndTheNote() {
        show(isConfirmed = true)

        composeRule.onNodeWithText("Too many lines").assertExists()
        composeRule.onNodeWithText("Lines after the first 15 are left out", substring = true).assertExists()
    }

    @Test
    fun pendingFactWithTooManyBulletsShowsTheStatusAndTheNote() {
        show(isConfirmed = false)

        composeRule.onNodeWithText("Too many lines").assertExists()
        composeRule.onNodeWithText("Lines after the first 15 are left out", substring = true).assertExists()
    }
}
