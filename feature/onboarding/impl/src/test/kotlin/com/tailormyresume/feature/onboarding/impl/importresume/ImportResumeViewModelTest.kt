package com.tailormyresume.feature.onboarding.impl.importresume

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ResumeTextParser
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.onboarding.api.navigation.ImportResumeNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ImportResumeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val connectivity = TestConnectivityMonitor()
    private val parser = RecordingResumeTextParser()
    private val source = RecordingResumeTextSource()
    private val file = ResumeFile(
        displayName = "Resume_2026.pdf",
        mimeType = RESUME_PDF_MIME,
        byteSize = 212L * 1024L,
        uri = "content://documents/1",
    )

    @Before
    fun setup() {
        repository.sendProfile(null)
    }

    private fun createViewModel(scenario: DebugScenario = DebugScenario.DEFAULT) = ImportResumeViewModel(
        resumeTextSource = source,
        resumeTextParser = parser,
        factIdAllocator = FactIdAllocator(),
        profileRepository = repository,
        connectivityMonitor = connectivity,
    ).apply { onEnter(ImportResumeNavKey(scenario = scenario)) }

    @Test
    fun defaultScenario_isIdleAndAsksForNoStoragePermissionState() {
        val state = createViewModel().uiState.value

        assertThat(state.stage).isEqualTo(ImportStage.Idle)
        assertThat(state.isOffline).isFalse()
        assertThat(state.facts).isEmpty()
    }

    @Test
    fun everyScenario_mapsToAStage() {
        val stages = DebugScenario.entries.map { scenario ->
            scenario to createViewModel(scenario).uiState.value.stage
        }

        assertThat(stages).containsAtLeast(
            DebugScenario.DEFAULT to ImportStage.Idle,
            DebugScenario.LOADING to ImportStage.Picking,
            DebugScenario.OFFLINE to ImportStage.Idle,
            DebugScenario.ERROR to ImportStage.Failed,
            DebugScenario.PARTIAL to ImportStage.Parsing,
            DebugScenario.SUCCESS to ImportStage.Success,
            DebugScenario.SCANNED to ImportStage.ScannedNoText,
            DebugScenario.IMPORTED to ImportStage.Success,
            DebugScenario.EMPTY to ImportStage.Idle,
            DebugScenario.FULLY_CONFIRMED to ImportStage.Idle,
            DebugScenario.PARTLY_CONFIRMED to ImportStage.Idle,
            DebugScenario.USER_STATED to ImportStage.Idle,
        )
    }

    @Test
    fun picking_showsThePickerStateAndDisabledAction() {
        val viewModel = createViewModel()

        viewModel.onPickRequested()

        assertThat(viewModel.uiState.value.isPicking).isTrue()
        assertThat(viewModel.uiState.value.canPick).isFalse()
    }

    @Test
    fun dismissedPicker_returnsToIdle() {
        val viewModel = createViewModel()
        viewModel.onPickRequested()

        viewModel.onPickerDismissed()

        assertThat(viewModel.uiState.value.stage).isEqualTo(ImportStage.Idle)
    }

    @Test
    fun chosenFile_isReadAndParsedIntoFacts() = runTest {
        val viewModel = createViewModel()

        viewModel.onFileChosen(file)

        val state = viewModel.uiState.value
        assertThat(source.requestedUris).containsExactly(file.uri)
        assertThat(parser.parsedText).hasSize(1)
        assertThat(state.stage).isEqualTo(ImportStage.Success)
        assertThat(state.facts).isNotEmpty()
        assertThat(state.factCount).isAtLeast(1)
    }

    @Test
    fun successfulParse_storesUnconfirmedImportedFacts() = runTest {
        val viewModel = createViewModel()

        viewModel.onFileChosen(file)

        val stored = repository.observeProfile().first()
        assertThat(stored).isNotNull()
        assertThat(stored?.entries).isNotEmpty()
        assertThat(stored?.entries?.all { !it.isConfirmed }).isTrue()
        assertThat(stored?.entries?.all { it.source == FactSource.IMPORTED }).isTrue()
        assertThat(stored?.entries?.map { it.id }).containsExactly("E-01", "P-01")
    }

    @Test
    fun internshipEntry_getsAnInternshipFactId() = runTest {
        val viewModel = createViewModel()
        parser.profile = parser.profile.copy(
            entries = listOf(
                parsedEntry("entry-1", EntryCategory.EXPERIENCE, "Data Intern"),
                parsedEntry("entry-2", EntryCategory.EXPERIENCE, "Operations Associate"),
            ),
        )

        viewModel.onFileChosen(file)

        val stored = repository.observeProfile().first()
        assertThat(stored?.entries?.map { it.id }).containsExactly("I-01", "W-01")
    }

    @Test
    fun successfulParse_retainsNoFileText() = runTest {
        val viewModel = createViewModel()

        viewModel.onFileChosen(file)

        assertThat(viewModel.retainedText).isNull()
        assertThat(viewModel.uiState.value.facts.joinToString()).doesNotContain(RESUME_TEXT_MARKER)
    }

    @Test
    fun scannedPdf_offersTheGuidedFormAtEqualWeight() = runTest {
        val viewModel = createViewModel()
        source.result = ResumeRead.NoTextLayer("Resume_2026.pdf")

        viewModel.onFileChosen(file)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ImportStage.ScannedNoText)
        assertThat(viewModel.uiState.value.isStop).isTrue()
    }

    @Test
    fun unsupportedFile_namesTheFileAndKeepsThePickerAvailable() = runTest {
        val viewModel = createViewModel()
        source.result = ResumeRead.Unsupported("Resume.pages")

        viewModel.onFileChosen(file)

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ImportStage.Unsupported)
        assertThat(state.canPick).isTrue()
        assertThat(state.fileName).isEqualTo("Resume.pages")
    }

    @Test
    fun tooLargeFile_stopsWithoutReadingIt() = runTest {
        val viewModel = createViewModel()
        source.result = ResumeRead.TooLarge(byteSize = 40L * 1024L * 1024L, limitBytes = RESUME_READ_LIMIT_BYTES)

        viewModel.onFileChosen(file)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ImportStage.TooLarge)
        assertThat(parser.parsedText).isEmpty()
    }

    @Test
    fun emptyFile_stopsWithoutClaimingFacts() = runTest {
        val viewModel = createViewModel()
        source.result = ResumeRead.Empty(byteSize = 0L)

        viewModel.onFileChosen(file)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ImportStage.Empty)
        assertThat(viewModel.uiState.value.facts).isEmpty()
    }

    @Test
    fun parseWithoutFacts_isANormalOutcomeNotAFailure() = runTest {
        val viewModel = createViewModel()
        source.result = ResumeRead.Text("nothing that parses")
        parser.profile = CandidateProfile(
            fullName = "",
            email = "",
            phone = "",
            headline = "",
            skills = emptyList(),
            entries = emptyList(),
        )

        viewModel.onFileChosen(file)

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ImportStage.NoFactsFound)
        assertThat(state.facts).isEmpty()
    }

    @Test
    fun readFailure_surfacesTheErrorStage() = runTest {
        val viewModel = createViewModel()
        source.result = ResumeRead.Unreadable("Resume_2026.pdf")

        viewModel.onFileChosen(file)

        assertThat(viewModel.uiState.value.stage).isEqualTo(ImportStage.Failed)
    }

    @Test
    fun offline_queuesTheFileInsteadOfReadingIt() = runTest {
        val viewModel = createViewModel(DebugScenario.OFFLINE)

        viewModel.onFileChosen(file)

        val state = viewModel.uiState.value
        assertThat(state.isQueued).isTrue()
        assertThat(state.fileName).isEqualTo(file.displayName)
        assertThat(source.requestedUris).isEmpty()
        assertThat(parser.parsedText).isEmpty()
    }

    @Test
    fun offline_readsTheQueuedFileWhenTheConnectionReturns() = runTest {
        connectivity.setOnline(false)
        val viewModel = createViewModel()
        viewModel.onFileChosen(file)
        assertThat(viewModel.uiState.value.isQueued).isTrue()

        connectivity.setOnline(true)

        val state = viewModel.uiState.value
        assertThat(state.isQueued).isFalse()
        assertThat(state.stage).isEqualTo(ImportStage.Success)
        assertThat(source.requestedUris).containsExactly(file.uri)
    }

    @Test
    fun chooseAnotherFile_whileQueued_dropsTheQueuedFile() = runTest {
        val viewModel = createViewModel(DebugScenario.OFFLINE)
        viewModel.onFileChosen(file)

        viewModel.onChooseAnotherFile()

        assertThat(viewModel.uiState.value.isQueued).isFalse()
    }

    @Test
    fun chooseAnotherFile_clearsTheLiftedFacts() = runTest {
        val viewModel = createViewModel()
        viewModel.onFileChosen(file)

        viewModel.onChooseAnotherFile()

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ImportStage.Idle)
        assertThat(state.facts).isEmpty()
    }

    @Test
    fun retry_returnsToIdleAndKeepsTheFileName() = runTest {
        val viewModel = createViewModel()
        source.result = ResumeRead.Unreadable(file.displayName)
        viewModel.onFileChosen(file)

        viewModel.onRetry()

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(ImportStage.Idle)
        assertThat(state.fileName).isEqualTo(file.displayName)
    }
}

private const val RESUME_TEXT_MARKER = "PRIVATE-UNIQUE-LINE"

private class RecordingResumeTextSource : ResumeTextSource {
    var result: ResumeRead = ResumeRead.Text(text = "PRIVATE-UNIQUE-LINE\nEXPERIENCE\nData intern")
    val requestedUris = mutableListOf<String>()

    override suspend fun read(file: ResumeFile): ResumeRead {
        requestedUris += file.uri
        return result
    }
}

private class RecordingResumeTextParser : ResumeTextParser {
    var profile: CandidateProfile = CandidateProfile(
        fullName = "Asha Rao",
        email = "asha.rao@example.com",
        phone = "+91 90000 00000",
        headline = "",
        skills = listOf("SQL"),
        entries = listOf(
            parsedEntry(
                id = "entry-1",
                category = EntryCategory.EDUCATION,
                title = "B.Tech Computer Science",
            ),
            parsedEntry(
                id = "entry-2",
                category = EntryCategory.PROJECT,
                title = "Placement Stats Dashboard",
            ),
        ),
    )
    val parsedText = mutableListOf<String>()

    override suspend fun parse(rawText: String): CandidateProfile {
        parsedText += rawText
        return profile
    }
}

private fun parsedEntry(
    id: String,
    category: EntryCategory,
    title: String,
): ProfileEntry = ProfileEntry(
    id = id,
    category = category,
    title = title,
    organization = "",
    startDate = "",
    endDate = "",
    bullets = listOf(EvidenceBullet(id = "$id-b1", text = "detail")),
    source = FactSource.IMPORTED,
    isConfirmed = false,
)
