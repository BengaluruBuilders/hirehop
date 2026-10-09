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
import org.junit.Rule
import org.junit.Test

class ImportResumeIdCollisionTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()

    private fun work(id: String, title: String, source: FactSource, dates: Pair<String, String> = "2024" to "2025") = ProfileEntry(
        id, EntryCategory.EXPERIENCE, title, "Acme", dates.first, dates.second,
        listOf(EvidenceBullet("$id-b", "Did $title work.")), source, isConfirmed = source != FactSource.IMPORTED,
    )

    private fun importWith(kept: List<ProfileEntry>, imported: List<ProfileEntry>): ImportResumeViewModel {
        repository.sendProfile(CandidateProfile("P", "", "", "", emptyList(), kept))
        val parser = object : ResumeTextParser {
            override suspend fun parse(rawText: String): CandidateProfile =
                CandidateProfile("P", "", "", "", emptyList(), imported)
        }
        return ImportResumeViewModel(
            resumeTextSource = object : ResumeTextSource {
                override suspend fun read(file: ResumeFile): ResumeRead = ResumeRead.Text("text")
            },
            resumeTextParser = parser,
            factIdAllocator = FactIdAllocator(),
            profileRepository = repository,
            connectivityMonitor = TestConnectivityMonitor(),
        ).apply { onEnter(ImportResumeNavKey(scenario = DebugScenario.DEFAULT)) }
    }

    private val file = ResumeFile("r.pdf", RESUME_PDF_MIME, 1L, "content://r")

    @Test
    fun importedWorkEntryGetsTheNextIdAfterAKeptUserStatedOne() = runTest {
        val viewModel = importWith(
            kept = listOf(work("W-01", "Kept role", FactSource.USER_STATED)),
            imported = listOf(work("x", "Imported role", FactSource.IMPORTED, "2020" to "2021")),
        )

        viewModel.onFileChosen(file)

        val saved = repository.observeProfile().first().orEmpty()
        assertThat(saved.map { it.id }).containsExactly("W-01", "W-02").inOrder()
        assertThat(saved.single { it.title == "Imported role" }.id).isEqualTo("W-02")
        assertThat(viewModel.uiState.value.facts.map { it.id }).containsExactly("W-02")
    }

    @Test
    fun idsAreUniqueAcrossTheSavedProfile() = runTest {
        val viewModel = importWith(
            kept = listOf(
                work("W-01", "Kept one", FactSource.USER_STATED),
                work("W-02", "Kept two", FactSource.USER_EDITED, "2022" to "2023"),
            ),
            imported = listOf(
                work("a", "Imported one", FactSource.IMPORTED, "2018" to "2019"),
                work("b", "Imported two", FactSource.IMPORTED, "2019" to "2020"),
            ),
        )

        viewModel.onFileChosen(file)

        val ids = repository.observeProfile().first().orEmpty().map { it.id }
        assertThat(ids).containsNoDuplicates()
        assertThat(ids).containsExactly("W-01", "W-02", "W-03", "W-04")
    }

    private fun CandidateProfile?.orEmpty(): List<ProfileEntry> = this?.entries.orEmpty()
}
