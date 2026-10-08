package com.tailormyresume.feature.onboarding.impl.confirmfacts

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ImportRemovalNotice
import com.tailormyresume.core.domain.onboarding.NextOnboardingStepUseCase
import com.tailormyresume.core.model.ConsentPurpose
import com.tailormyresume.core.model.ConsentRecord
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.model.factCounts
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.sampleEducationEntry
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.core.testing.data.sampleProjectEntry
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.onboarding.api.navigation.ConfirmFactsNavKey
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

    private val notice = ImportRemovalNotice()

    private fun createViewModel(scenario: DebugScenario = DebugScenario.DEFAULT) = ConfirmFactsViewModel(
        profileRepository = repository,
        nextOnboardingStep = NextOnboardingStepUseCase(session, repository),
        connectivityMonitor = connectivity,
        importRemovalNotice = notice,
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
