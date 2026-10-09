package com.tailormyresume.feature.onboarding.impl.confirmfacts

import com.google.common.truth.Truth.assertThat
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

class ConfirmFactsLongBulletTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()

    private fun entry(id: String, bulletText: String) = ProfileEntry(
        id = id,
        category = EntryCategory.EXPERIENCE,
        title = "Data intern",
        organization = "Nashik",
        startDate = "May 2025",
        endDate = "Jul 2025",
        bullets = listOf(EvidenceBullet("$id-b1", bulletText)),
        source = FactSource.IMPORTED,
        isConfirmed = false,
    )

    private fun createViewModel(): ConfirmFactsViewModel {
        repository.sendProfile(
            sampleProfile.copy(
                entries = listOf(
                    entry("LONG", "S" + "a".repeat(448) + "."),
                    entry("OK", "Cleaned the sales data."),
                ),
            ),
        )
        return ConfirmFactsViewModel(
            profileRepository = repository,
            nextOnboardingStep = NextOnboardingStepUseCase(TestSessionRepository(), repository),
            connectivityMonitor = TestConnectivityMonitor(),
            importRemovalNotice = ImportRemovalNotice(),
        ).apply { onEnter(ConfirmFactsNavKey(scenario = DebugScenario.DEFAULT)) }
    }

    @Test
    fun factWithATooLongBulletIsFlaggedOnTheBoard() {
        val state = createViewModel().uiState.value

        assertThat(state.facts.first { it.id == "LONG" }.hasTooLongBullet).isTrue()
        assertThat(state.facts.first { it.id == "OK" }.hasTooLongBullet).isFalse()
    }

    @Test
    fun oneTapConfirmDoesNothingForATooLongBullet() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Confirm("LONG"))

        val stored = repository.observeProfile().first()
        assertThat(stored?.entries?.first { it.id == "LONG" }?.isConfirmed).isFalse()
        assertThat(viewModel.uiState.value.confirmedCount).isEqualTo(0)
    }

    @Test
    fun oneTapConfirmStillWorksForAnEntryWithinTheLimit() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Confirm("OK"))

        assertThat(repository.observeProfile().first()?.entries?.first { it.id == "OK" }?.isConfirmed).isTrue()
    }
}
