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
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ImportedContinuationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun role(id: String, title: String, start: String) = ProfileEntry(
        id, EntryCategory.EXPERIENCE, title, "Acme", start, "2025",
        listOf(EvidenceBullet("$id-b", "Did $title work.")), FactSource.IMPORTED, isConfirmed = false,
    )

    private suspend fun importedFacts(vararg entries: ProfileEntry): List<ImportedFactUi> {
        val parser = object : ResumeTextParser {
            override suspend fun parse(rawText: String): CandidateProfile =
                CandidateProfile("P", "", "", "", emptyList(), entries.toList())
        }
        val viewModel = ImportResumeViewModel(
            resumeTextSource = object : ResumeTextSource {
                override suspend fun read(file: ResumeFile): ResumeRead = ResumeRead.Text("text")
            },
            resumeTextParser = parser,
            factIdAllocator = FactIdAllocator(),
            profileRepository = TestProfileRepository().apply { sendProfile(null) },
            connectivityMonitor = TestConnectivityMonitor(),
        ).apply { onEnter(ImportResumeNavKey(scenario = DebugScenario.DEFAULT)) }
        viewModel.onFileChosen(ResumeFile("r.pdf", RESUME_PDF_MIME, 1L, "content://r"))
        return viewModel.uiState.value.facts
    }

    @Test
    fun secondEntryOfTheSameRoleIsMarkedContinued() = runTest {
        val facts = importedFacts(role("a", "Intern", "2024"), role("b", "Intern", "2024"), role("c", "Analyst", "2023"))

        assertThat(facts.map { it.continued }).containsExactly(false, true, false).inOrder()
    }
}
