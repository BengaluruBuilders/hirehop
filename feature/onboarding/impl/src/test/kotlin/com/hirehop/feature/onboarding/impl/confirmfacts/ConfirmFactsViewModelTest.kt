package com.hirehop.feature.onboarding.impl.confirmfacts

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ImportRemovalNotice
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.KeptJobDescription
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.SignInAccount
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

class ConfirmFactsViewModelTest {

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
    fun defaultScenario_loadsEveryImportedFactAsOpen() {
        val state = createViewModel().uiState.value

        assertThat(state.facts.map { it.id }).containsExactly("U-01", "I-01", "C-01", "P-01")
        assertThat(state.confirmedCount).isEqualTo(2)
        assertThat(state.openCount).isEqualTo(4)
        assertThat(state.isFullyConfirmed).isFalse()
        assertThat(state.skills).containsExactly("SQL", "Power BI")
    }

    @Test
    fun removedBanner_followsWhatTheImportRemoved() {
        val viewModel = createViewModel()
        assertThat(viewModel.uiState.value.showsRemovedBanner).isTrue()

        notice.record(removedDateOfBirthOrPhoto = false)

        assertThat(viewModel.uiState.value.showsRemovedBanner).isFalse()
    }

    @Test
    fun emptyScenario_keepsTheEmptyStateEvenWithAStoredProfile() {
        val state = createViewModel(DebugScenario.EMPTY).uiState.value

        assertThat(state.facts).isEmpty()
        assertThat(state.isEmpty).isTrue()
        assertThat(state.confirmedCount).isEqualTo(0)
    }

    @Test
    fun partlyConfirmedScenario_confirmsHalfTheFacts() {
        val state = createViewModel(DebugScenario.PARTLY_CONFIRMED).uiState.value

        assertThat(state.confirmedCount).isEqualTo(4)
        assertThat(state.openCount).isEqualTo(2)
        assertThat(state.isFullyConfirmed).isFalse()
    }

    @Test
    fun fullyConfirmedScenario_confirmsEveryFact() {
        val state = createViewModel(DebugScenario.FULLY_CONFIRMED).uiState.value

        assertThat(state.confirmedCount).isEqualTo(6)
        assertThat(state.openCount).isEqualTo(0)
        assertThat(state.isFullyConfirmed).isTrue()
    }

    @Test
    fun userStatedScenario_marksEveryFactConfirmed() {
        val state = createViewModel(DebugScenario.USER_STATED).uiState.value

        assertThat(state.isFullyConfirmed).isTrue()
        assertThat(state.facts.map { it.source }).containsExactly(
            FactSource.USER_STATED,
            FactSource.USER_STATED,
            FactSource.USER_STATED,
            FactSource.USER_STATED,
        )
    }

    @Test
    fun loadingScenario_staysLoadingWithoutReadingTheProfile() {
        val state = createViewModel(DebugScenario.LOADING).uiState.value

        assertThat(state.isLoading).isTrue()
        assertThat(state.facts).isEmpty()
    }

    @Test
    fun confirmOne_writesIsConfirmedThroughTheRepository() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Confirm("C-01"))

        val stored = repository.observeProfile().first()
        assertThat(stored?.entries?.first { it.id == "C-01" }?.isConfirmed).isTrue()
        assertThat(stored?.entries?.first { it.id == "C-01" }?.source).isEqualTo(FactSource.IMPORTED)
        assertThat(viewModel.uiState.value.confirmedCount).isEqualTo(3)
        assertThat(viewModel.uiState.value.openCount).isEqualTo(3)
    }

    @Test
    fun confirmingEveryFact_reachesTheFullyConfirmedState() = runTest {
        val viewModel = createViewModel()

        listOf("U-01", "I-01", "C-01", "P-01").forEach { factId ->
            viewModel.onAction(ConfirmFactsAction.Confirm(factId))
        }

        val state = viewModel.uiState.value
        assertThat(state.confirmedCount).isEqualTo(6)
        assertThat(state.openCount).isEqualTo(0)
        assertThat(state.isFullyConfirmed).isTrue()
    }

    @Test
    fun editOne_handsTheFactToTheMergedFactEditor() {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Edit(factId = "C-01", category = EntryCategory.PROJECT))

        val pendingEdit = viewModel.uiState.value.pendingEdit
        assertThat(pendingEdit?.factId).isEqualTo("C-01")
        assertThat(pendingEdit?.category).isEqualTo(EntryCategory.PROJECT)
    }

    @Test
    fun consumedEdit_handsBackToTheScreen() {
        val viewModel = createViewModel()
        viewModel.onAction(ConfirmFactsAction.Edit(factId = "C-01", category = EntryCategory.PROJECT))

        viewModel.onAction(ConfirmFactsAction.EditConsumed)

        assertThat(viewModel.uiState.value.pendingEdit).isNull()
    }

    @Test
    fun editedFactSavedByTheEditor_showsUpAsUserEditedAndConfirmed() = runTest {
        repository.sendProfile(
            importedProfile.copy(
                entries = importedProfile.entries.map { entry ->
                    if (entry.id == "C-01") {
                        entry.copy(source = FactSource.USER_EDITED, isConfirmed = true, title = "Placement Stats Dashboard")
                    } else {
                        entry
                    }
                },
            ),
        )
        val viewModel = createViewModel()

        val state = viewModel.uiState.value

        val edited = state.facts.first { it.id == "C-01" }
        assertThat(edited.source).isEqualTo(FactSource.USER_EDITED)
        assertThat(edited.isConfirmed).isTrue()
        assertThat(state.confirmedCount).isEqualTo(3)
    }

    @Test
    fun offline_saysSoAndKeepsWorking() = runTest {
        val viewModel = createViewModel(DebugScenario.OFFLINE)

        viewModel.onAction(ConfirmFactsAction.Confirm("C-01"))

        assertThat(viewModel.uiState.value.isOffline).isTrue()
        assertThat(viewModel.uiState.value.confirmedCount).isEqualTo(3)
    }

    @Test
    fun skipOne_hidesTheEmptySection() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Skip(ConfirmFactsSection.Certifications))

        val visible = viewModel.uiState.value.visibleSections.map { it.section }
        assertThat(visible).doesNotContain(ConfirmFactsSection.Certifications)
        assertThat(visible).contains(ConfirmFactsSection.Education)
    }

    @Test
    fun addOne_handsTheCategoryToTheMergedFactEditor() {
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.AddOne(ConfirmFactsSection.Certifications))

        val pendingEdit = viewModel.uiState.value.pendingEdit
        assertThat(pendingEdit?.factId).isNull()
        assertThat(pendingEdit?.category).isEqualTo(EntryCategory.CERTIFICATION)
    }

    @Test
    fun sections_groupFactsByDesignSection() {
        val state = createViewModel().uiState.value

        val grouped = state.sections.associate { section ->
            section.section to section.facts.map { it.id }
        }
        assertThat(grouped[ConfirmFactsSection.Education]).containsExactly("U-01")
        assertThat(grouped[ConfirmFactsSection.Experience]).containsExactly("I-01")
        assertThat(grouped[ConfirmFactsSection.Projects]).containsExactly("C-01")
        assertThat(grouped[ConfirmFactsSection.Extras]).containsExactly("P-01")
        assertThat(grouped[ConfirmFactsSection.Certifications]).isEmpty()
    }

    @Test
    fun noFactCarriesASensitiveAttribute() {
        val state = createViewModel().uiState.value

        val text = state.facts.joinToString(" ") { "${it.title} ${it.detail}" }.lowercase()
        listOf("date of birth", "dob", "photo", "religion", "caste", "marital").forEach { attribute ->
            assertThat(text).doesNotContain(attribute)
        }
    }

    @Test
    fun continue_withAJobKept_asksForTheGapAnalysis() = runTest {
        val job = KeptJobDescription(text = "SQL and Power BI", company = "Northwind GCC", role = "Associate Analyst")
        session.sendAccount(SignInAccount.localAccount)
        session.sendConsent(consentRecord())
        repository.sendProfile(
            importedProfile.copy(entries = importedProfile.entries.map { it.copy(isConfirmed = true) }),
        )
        session.keepJobDescription(job)
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Continue)

        assertThat(viewModel.uiState.value.nextStep).isEqualTo(OnboardingStep.GapAnalysis(job))
    }

    @Test
    fun continue_withNoJobKept_asksForThePasteStep() = runTest {
        session.sendAccount(SignInAccount.localAccount)
        session.sendConsent(consentRecord())
        repository.sendProfile(
            importedProfile.copy(entries = importedProfile.entries.map { it.copy(isConfirmed = true) }),
        )
        val viewModel = createViewModel()

        viewModel.onAction(ConfirmFactsAction.Continue)

        assertThat(viewModel.uiState.value.nextStep).isEqualTo(OnboardingStep.PasteJobDescription)
    }

    @Test
    fun nextStepConsumed_clearsTheStep() = runTest {
        session.sendAccount(SignInAccount.localAccount)
        session.sendConsent(consentRecord())
        repository.sendProfile(
            importedProfile.copy(entries = importedProfile.entries.map { it.copy(isConfirmed = true) }),
        )
        val viewModel = createViewModel()
        viewModel.onAction(ConfirmFactsAction.Continue)

        viewModel.onAction(ConfirmFactsAction.NextStepConsumed)

        assertThat(viewModel.uiState.value.nextStep).isNull()
    }

    @Test
    fun whenTheDeviceGoesOffline_flagsOffline() {
        val viewModel = createViewModel()

        connectivity.setOnline(false)

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun anEditMadeElsewhere_reachesTheScreenWhenItComesBack() {
        val viewModel = createViewModel()

        repository.sendProfile(importedProfile.copy(entries = importedProfile.entries.map { it.copy(isConfirmed = true) }))

        assertThat(viewModel.uiState.value.isFullyConfirmed).isTrue()
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
