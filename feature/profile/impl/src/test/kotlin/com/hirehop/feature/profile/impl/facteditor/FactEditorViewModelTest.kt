package com.hirehop.feature.profile.impl.facteditor

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.domain.fact.FactDraftErrorReason
import com.hirehop.core.domain.fact.FactDraftValidator
import com.hirehop.core.domain.fact.FactField
import com.hirehop.core.domain.fact.FactIdAllocator
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.FactSource
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.data.sampleProfile
import com.hirehop.core.testing.data.sampleProjectEntry
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.profile.api.navigation.FactEditorNavKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class FactEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val connectivity = TestConnectivityMonitor()
    private val allocator = FactIdAllocator()
    private var nextBulletId = 0
    private val idGenerator = IdGenerator { "bullet-${nextBulletId++}" }

    private val editedEntry = sampleProjectEntry.copy(
        id = "C-02",
        title = "Placement Stats Dashboard",
        organization = "Power BI, Excel",
        startDate = "Jan 2024",
        endDate = "Apr 2024",
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )

    private val profileWithEditedEntry = sampleProfile.copy(
        entries = listOf(sampleProjectEntry, editedEntry),
    )

    @Before
    fun setup() {
        repository.sendProfile(profileWithEditedEntry)
    }

    private fun createViewModel(
        entryId: String? = null,
        entryType: String = "project",
        scenario: DebugScenario = DebugScenario.DEFAULT,
        profileRepository: ProfileRepository = repository,
    ): FactEditorViewModel {
        return FactEditorViewModel(
            profileRepository = profileRepository,
            factIdAllocator = allocator,
            idGenerator = idGenerator,
            connectivityMonitor = connectivity,
            key = FactEditorNavKey(entryId = entryId, entryType = entryType, scenario = scenario),
        )
    }

    @Test
    fun newFact_withoutEntryId_isNewModeWithTheNextFreeId() {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertThat(state.mode).isEqualTo(FactEditorMode.New)
        assertThat(state.factId).isEqualTo(allocator.nextId(state.draft.category, profileWithEditedEntry.entries))
        assertThat(state.draft.title).isEmpty()
        assertThat(state.provenance).isEqualTo(FactSource.USER_STATED)
        assertThat(state.canDelete).isFalse()
    }

    @Test
    fun newExperienceFact_whoseTitleNamesAnInternship_getsAnInternshipId() {
        val viewModel = createViewModel(entryType = "experience")
        assertThat(viewModel.uiState.value.factId).startsWith("W-")

        viewModel.onTitleChange("Data analyst intern")

        assertThat(viewModel.uiState.value.factId).startsWith("I-")
        assertThat(viewModel.uiState.value.displayId).isEqualTo(viewModel.uiState.value.factId)
    }

    @Test
    fun editingFact_withAnOldIdFormat_showsTheDesignIdButKeepsTheStoredId() {
        repository.sendProfile(sampleProfile.copy(entries = listOf(editedEntry.copy(id = "entry-7"))))

        val state = createViewModel(entryId = "entry-7").uiState.value

        assertThat(state.factId).isEqualTo("entry-7")
        assertThat(state.displayId).isEqualTo("P-01")
    }

    @Test
    fun newFact_withNoProfile_stillAllocatesAnId() {
        repository.sendProfile(null)

        val state = createViewModel().uiState.value

        assertThat(state.factId).isEqualTo(allocator.nextId(state.draft.category, emptyList()))
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun editingFact_withEntryId_loadsThatEntryIntoTheDraft() {
        val viewModel = createViewModel(entryId = editedEntry.id)

        val state = viewModel.uiState.value
        assertThat(state.mode).isEqualTo(FactEditorMode.Editing)
        assertThat(state.factId).isEqualTo(editedEntry.id)
        assertThat(state.draft.title).isEqualTo("Placement Stats Dashboard")
        assertThat(state.draft.organization).isEqualTo("Power BI, Excel")
        assertThat(state.draft.startDate).isEqualTo("Jan 2024")
        assertThat(state.draft.endDate).isEqualTo("Apr 2024")
        assertThat(state.draft.detail).isEqualTo(editedEntry.bullets.joinToString(" ") { it.text })
        assertThat(state.provenance).isEqualTo(FactSource.IMPORTED)
        assertThat(state.canDelete).isTrue()
    }

    @Test
    fun editingFact_withUnknownEntryId_fallsBackToNewMode() {
        val state = createViewModel(entryId = "does-not-exist").uiState.value

        assertThat(state.mode).isEqualTo(FactEditorMode.New)
        assertThat(state.draft.title).isEmpty()
    }

    @Test
    fun entryType_drivesTheDraftCategory() {
        val state = createViewModel(entryType = "experience").uiState.value

        assertThat(state.draft.category.name).isEqualTo("EXPERIENCE")
    }

    @Test
    fun validation_blankTitle_mapsRequiredToTheTitleField() {
        val viewModel = createViewModel(entryId = editedEntry.id)

        viewModel.onTitleChange("  ")

        val state = viewModel.uiState.value
        assertThat(state.fieldErrors[FactField.TITLE]).isEqualTo(FactDraftErrorReason.REQUIRED)
        assertThat(state.visibleReasonFor(FactField.TITLE)).isEqualTo(FactDraftErrorReason.REQUIRED)
        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun validation_endBeforeStart_mapsToTheEndDateField() {
        val viewModel = createViewModel(entryId = editedEntry.id)

        viewModel.onEndDateChange("Dec 2023")

        val state = viewModel.uiState.value
        assertThat(state.fieldErrors[FactField.END_DATE]).isEqualTo(FactDraftErrorReason.END_BEFORE_START)
        assertThat(state.visibleReasonFor(FactField.END_DATE)).isEqualTo(FactDraftErrorReason.END_BEFORE_START)
        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun validation_tooLongDetail_mapsToTheDetailField() {
        val viewModel = createViewModel(entryId = editedEntry.id)

        viewModel.onDetailChange("x".repeat(FactDraftValidator.DETAIL_LIMIT + 1))

        val state = viewModel.uiState.value
        assertThat(state.fieldErrors[FactField.DETAIL]).isEqualTo(FactDraftErrorReason.TOO_LONG)
        assertThat(state.visibleReasonFor(FactField.DETAIL)).isEqualTo(FactDraftErrorReason.TOO_LONG)
    }

    @Test
    fun validation_aFreshFormShowsNoFieldErrorAndLetsTheUserTapSave() {
        val state = createViewModel().uiState.value

        assertThat(state.fieldErrors).isEmpty()
        assertThat(state.visibleReasonFor(FactField.TITLE)).isNull()
        assertThat(state.saveBlockReason).isEqualTo(FactDraftErrorReason.REQUIRED)
        assertThat(state.isSaveEnabled).isTrue()
    }

    @Test
    fun save_onAFreshForm_flagsTheTitleAndKeepsTheEditorOpen() {
        val viewModel = createViewModel()

        viewModel.save()

        val state = viewModel.uiState.value
        assertThat(state.outcome).isEqualTo(FactEditorOutcome.Editing)
        assertThat(state.visibleReasonFor(FactField.TITLE)).isEqualTo(FactDraftErrorReason.REQUIRED)
    }

    @Test
    fun validation_anUntouchedFieldStaysCleanWhileAnotherFieldCarriesTheError() {
        val viewModel = createViewModel(entryId = editedEntry.id)

        viewModel.onEndDateChange("Dec 2023")

        val state = viewModel.uiState.value
        assertThat(state.fieldErrors[FactField.END_DATE]).isEqualTo(FactDraftErrorReason.END_BEFORE_START)
        assertThat(state.visibleReasonFor(FactField.END_DATE)).isEqualTo(FactDraftErrorReason.END_BEFORE_START)
        assertThat(state.visibleReasonFor(FactField.TITLE)).isNull()
        assertThat(state.saveBlockReason).isEqualTo(FactDraftErrorReason.END_BEFORE_START)
    }

    @Test
    fun validation_fixingTheDates_clearsTheError() {
        val viewModel = createViewModel(entryId = editedEntry.id)
        viewModel.onEndDateChange("Dec 2023")

        viewModel.onEndDateChange("Apr 2024")

        assertThat(viewModel.uiState.value.fieldErrors).isEmpty()
    }

    @Test
    fun save_newFact_addsAUserStatedEntry() = runTest {
        val viewModel = createViewModel()
        val newId = viewModel.uiState.value.factId

        viewModel.onTitleChange("Placement Stats Dashboard v2")
        viewModel.onToolsChange("Power BI, Excel")
        viewModel.onStartDateChange("Jan 2024")
        viewModel.onEndDateChange("Apr 2024")
        viewModel.onDetailChange("Built a dashboard of 3 batches of placement data.")
        viewModel.save()

        val state = viewModel.uiState.value
        val added = savedProfile().entries.last()
        assertThat(state.outcome).isEqualTo(FactEditorOutcome.Saved)
        assertThat(state.provenance).isEqualTo(FactSource.USER_STATED)
        assertThat(added.id).isEqualTo(newId)
        assertThat(added.title).isEqualTo("Placement Stats Dashboard v2")
        assertThat(added.organization).isEqualTo("Power BI, Excel")
        assertThat(added.source).isEqualTo(FactSource.USER_STATED)
        assertThat(added.isConfirmed).isTrue()
        assertThat(added.bullets.map { it.text })
            .containsExactly("Built a dashboard of 3 batches of placement data.")
    }

    @Test
    fun save_editedFact_marksTheEntryUserEditedAndKeepsItsBulletId() = runTest {
        val viewModel = createViewModel(entryId = editedEntry.id)

        viewModel.onTitleChange("Placement Stats Dashboard")
        viewModel.save()

        val saved = savedProfile().entries.first { it.id == editedEntry.id }
        val state = viewModel.uiState.value
        assertThat(state.outcome).isEqualTo(FactEditorOutcome.Saved)
        assertThat(state.provenance).isEqualTo(FactSource.USER_EDITED)
        assertThat(saved.source).isEqualTo(FactSource.USER_EDITED)
        assertThat(saved.isConfirmed).isTrue()
        assertThat(saved.bullets.single().id).isEqualTo(editedEntry.bullets.first().id)
        assertThat(savedProfile().entries).hasSize(2)
    }

    @Test
    fun save_rejected_marksEveryFieldTouchedAndWritesNothing() = runTest {
        val viewModel = createViewModel(entryId = editedEntry.id)

        viewModel.onTitleChange("")
        viewModel.onEndDateChange("Dec 2023")
        viewModel.save()

        val state = viewModel.uiState.value
        assertThat(state.outcome).isEqualTo(FactEditorOutcome.Editing)
        assertThat(state.touchedFields).containsExactlyElementsIn(FactField.entries)
        assertThat(state.fieldErrors).hasSize(2)
        assertThat(state.isSaveEnabled).isFalse()
        assertThat(savedProfile()).isEqualTo(profileWithEditedEntry)
    }

    @Test
    fun save_whenTheRepositoryFails_reportsTheFailureAndKeepsTheDraft() = runTest {
        val failing = FailingProfileRepository(profileWithEditedEntry)
        val viewModel = createViewModel(entryId = editedEntry.id, profileRepository = failing)

        viewModel.onTitleChange("Placement Stats Dashboard")
        viewModel.save()

        val state = viewModel.uiState.value
        assertThat(state.isSaveFailed).isTrue()
        assertThat(state.outcome).isEqualTo(FactEditorOutcome.Editing)
        assertThat(state.draft.title).isEqualTo("Placement Stats Dashboard")
    }

    @Test
    fun save_whileOffline_queuesTheSaveAndStillWrites() = runTest {
        val viewModel = createViewModel(
            entryId = editedEntry.id,
            scenario = DebugScenario.OFFLINE,
        )

        viewModel.onTitleChange("Placement Stats Dashboard")
        viewModel.save()

        val state = viewModel.uiState.value
        assertThat(state.outcome).isEqualTo(FactEditorOutcome.Saved)
        assertThat(state.wasQueued).isTrue()
        assertThat(savedProfile().entries.first { it.id == editedEntry.id }).isNotNull()
    }

    @Test
    fun cancel_discardsTheDraftAndWritesNothing() = runTest {
        val viewModel = createViewModel(entryId = editedEntry.id)

        viewModel.onTitleChange("Something else entirely")
        viewModel.cancel()

        assertThat(viewModel.uiState.value.outcome).isEqualTo(FactEditorOutcome.Cancelled)
        assertThat(savedProfile()).isEqualTo(profileWithEditedEntry)
    }

    @Test
    fun delete_confirmed_removesTheEntry() = runTest {
        val viewModel = createViewModel(entryId = editedEntry.id)

        viewModel.requestDelete()
        assertThat(viewModel.uiState.value.isDeleteDialogVisible).isTrue()

        viewModel.confirmDelete()

        val state = viewModel.uiState.value
        assertThat(state.outcome).isEqualTo(FactEditorOutcome.Deleted)
        assertThat(state.isDeleteDialogVisible).isFalse()
        assertThat(savedProfile().entries).containsExactly(sampleProjectEntry)
    }

    @Test
    fun delete_cancelled_keepsTheEntry() = runTest {
        val viewModel = createViewModel(entryId = editedEntry.id)

        viewModel.requestDelete()
        viewModel.dismissDelete()

        assertThat(viewModel.uiState.value.isDeleteDialogVisible).isFalse()
        assertThat(viewModel.uiState.value.outcome).isEqualTo(FactEditorOutcome.Editing)
        assertThat(savedProfile().entries).containsExactly(
            sampleProjectEntry,
            editedEntry,
        ).inOrder()
    }

    @Test
    fun delete_isNotOfferedForAFactThatIsNotStoredYet() {
        val state = createViewModel().uiState.value

        assertThat(state.canDelete).isFalse()
    }

    @Test
    fun scenario_loading_staysLoadingAndBlocksSaving() {
        val state = createViewModel(
            entryId = editedEntry.id,
            scenario = DebugScenario.LOADING,
        ).uiState.value

        assertThat(state.isLoading).isTrue()
        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun connectivity_losingTheNetworkMarksTheScreenOffline() {
        val viewModel = createViewModel()
        assertThat(viewModel.uiState.value.isOffline).isFalse()

        connectivity.setOnline(false)

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun scenario_offline_marksTheScreenOffline() {
        val state = createViewModel(scenario = DebugScenario.OFFLINE).uiState.value

        assertThat(state.isOffline).isTrue()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun scenario_empty_neverLoadsAnExistingEntry() {
        val state = createViewModel(
            entryId = editedEntry.id,
            scenario = DebugScenario.EMPTY,
        ).uiState.value

        assertThat(state.mode).isEqualTo(FactEditorMode.New)
        assertThat(state.draft.title).isEmpty()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun scenario_error_seedsASaveFailure() {
        val state = createViewModel(scenario = DebugScenario.ERROR).uiState.value

        assertThat(state.isSaveFailed).isTrue()
    }

    @Test
    fun scenario_partial_marksTheLoadedEntryUnconfirmed() {
        val state = createViewModel(
            entryId = editedEntry.id,
            scenario = DebugScenario.PARTIAL,
        ).uiState.value

        assertThat(state.mode).isEqualTo(FactEditorMode.Editing)
        assertThat(state.isConfirmed).isFalse()
    }

    @Test
    fun scenario_success_seedsTheSavedOutcome() {
        val state = createViewModel(scenario = DebugScenario.SUCCESS).uiState.value

        assertThat(state.outcome).isEqualTo(FactEditorOutcome.Saved)
    }

    @Test
    fun liveLine_tracksTheDraftAsItIsTyped() {
        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value.hasLiveLine).isFalse()

        viewModel.onTitleChange("Placement Stats Dashboard")
        viewModel.onDetailChange("Built a dashboard of 3 batches of placement data.")
        viewModel.onToolsChange("Power BI, Excel")
        viewModel.onStartDateChange("Jan 2024")
        viewModel.onEndDateChange("Apr 2024")

        val state = viewModel.uiState.value
        assertThat(state.hasLiveLine).isTrue()
        assertThat(state.liveLine).isEqualTo(
            "Placement Stats Dashboard · Built a dashboard of 3 batches of placement data. " +
                "· Power BI, Excel · Jan 2024 to Apr 2024",
        )
    }

    @Test
    fun toolTokens_splitTheToolsFieldOnCommas() {
        val viewModel = createViewModel()

        viewModel.onToolsChange(" Power BI , , Excel ")

        assertThat(viewModel.uiState.value.toolTokens).containsExactly("Power BI", "Excel").inOrder()
    }

    private suspend fun savedProfile(): CandidateProfile =
        checkNotNull(repository.observeProfile().first()) { "Expected a saved profile" }
}

private class FailingProfileRepository(
    profile: CandidateProfile?,
) : ProfileRepository {
    private val delegate = TestProfileRepository().apply { sendProfile(profile) }

    override fun observeProfile(): Flow<CandidateProfile?> = delegate.observeProfile()

    override suspend fun saveProfile(profile: CandidateProfile): Unit =
        throw IllegalStateException("Disk is full")

    override suspend fun clearProfile() {
        delegate.clearProfile()
    }
}
