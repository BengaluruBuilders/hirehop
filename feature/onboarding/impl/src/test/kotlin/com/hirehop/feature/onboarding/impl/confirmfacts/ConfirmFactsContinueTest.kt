package com.hirehop.feature.onboarding.impl.confirmfacts

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.SignInAccount
import com.hirehop.core.model.factCounts
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.data.sampleEducationEntry
import com.hirehop.core.testing.data.sampleProfile
import com.hirehop.core.testing.data.sampleProjectEntry
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.onboarding.api.navigation.ConfirmFactsNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class ConfirmFactsContinueTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()

    private val importedProfile = sampleProfile.copy(
        entries = listOf(
            sampleEducationEntry.copy(id = "U-01", isConfirmed = false, source = FactSource.IMPORTED),
            sampleProjectEntry.copy(id = "C-01", isConfirmed = false, source = FactSource.IMPORTED),
            experienceEntry("I-01"),
            achievementEntry("P-01"),
        ),
        skills = listOf("SQL", "Power BI"),
    )

    @Before
    fun setup() {
        repository.sendProfile(importedProfile)
        session.sendAccount(SignInAccount.localAccount)
        session.sendConsent(consentRecord())
    }

    private val session = TestSessionRepository()

    private val connectivity = TestConnectivityMonitor()

    private fun createViewModel(scenario: DebugScenario = DebugScenario.DEFAULT) = ConfirmFactsViewModel(
        profileRepository = repository,
        nextOnboardingStep = NextOnboardingStepUseCase(session, repository),
        connectivityMonitor = connectivity,
    ).apply { onEnter(ConfirmFactsNavKey(scenario = scenario)) }

    @Test
    fun continue_withNoFactConfirmed_doesNothing() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Continue)

        assertThat(viewModel.uiState.value.canContinue).isFalse()
        assertThat(viewModel.uiState.value.nextStep).isNull()
    }

    @Test
    fun continue_afterOneFactConfirmed_asksForTheNextStep() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Confirm("U-01"))

        assertThat(viewModel.uiState.value.canContinue).isTrue()

        viewModel.onAction(ConfirmFactsAction.Continue)

        assertThat(viewModel.uiState.value.nextStep).isNotNull()
    }

    @Test
    fun counts_followTheProfileFactCountsRule() = runTest {
        val viewModel = createViewModel()

        val before = viewModel.uiState.value
        val beforeCounts = importedProfile.factCounts()
        assertThat(before.confirmedCount + before.openCount).isEqualTo(beforeCounts.total)
        assertThat(before.confirmedCount).isEqualTo(beforeCounts.confirmed + beforeCounts.userStated)

        viewModel.onAction(ConfirmFactsAction.Confirm("U-01"))

        val stored = repository.observeProfile().first()
        val after = viewModel.uiState.value
        val afterCounts = requireNotNull(stored).factCounts()
        assertThat(after.confirmedCount + after.openCount).isEqualTo(afterCounts.total)
        assertThat(after.confirmedCount).isEqualTo(afterCounts.confirmed + afterCounts.userStated)
    }

    private fun consentRecord() = ConsentRecord(
        purposes = ConsentPurpose.entries.toSet(),
        acceptedAt = Instant.fromEpochSeconds(0),
        noticeVersion = ConsentRecord.CURRENT_NOTICE_VERSION,
    )
}

private fun experienceEntry(id: String): ProfileEntry = ProfileEntry(
    id = id,
    category = EntryCategory.EXPERIENCE,
    title = "Data intern, Kiran Agro Exports",
    organization = "Nashik",
    startDate = "May 2025",
    endDate = "Jul 2025",
    bullets = listOf(EvidenceBullet(id = "$id-b1", text = "Cleaned 12,000 rows of sales data in Excel.")),
    source = FactSource.IMPORTED,
    isConfirmed = false,
)

private fun achievementEntry(id: String): ProfileEntry = ProfileEntry(
    id = id,
    category = EntryCategory.ACHIEVEMENT,
    title = "Smart India Hackathon 2024",
    organization = "",
    startDate = "2024",
    endDate = "",
    bullets = listOf(EvidenceBullet(id = "$id-b1", text = "Internal-round finalist.")),
    source = FactSource.IMPORTED,
    isConfirmed = false,
)
