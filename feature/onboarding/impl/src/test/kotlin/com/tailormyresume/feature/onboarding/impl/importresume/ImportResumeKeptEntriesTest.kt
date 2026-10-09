package com.tailormyresume.feature.onboarding.impl.importresume

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ResumeTextParser
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.feature.onboarding.api.navigation.ImportResumeNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ImportResumeKeptEntriesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val parser = KeptEntriesRecordingParser()

    private fun entry(id: String, source: FactSource) = ProfileEntry(
        id, EntryCategory.EXPERIENCE, "Role $id", "Acme", "2024", "2025", emptyList(), source, isConfirmed = true,
    )

    @Test
    fun userStatedEntriesAreCountedAndImportedOnesAreNotWhenParsing() = runTest {
        val profile = CandidateProfile(
            "P", "", "", "", emptyList(),
            listOf(
                entry("W-01", FactSource.USER_STATED),
                entry("W-02", FactSource.USER_EDITED),
                entry("W-03", FactSource.USER_STATED),
                entry("W-04", FactSource.IMPORTED),
            ),
        )
        repository.sendProfile(profile)
        val viewModel = ImportResumeViewModel(
            resumeTextSource = object : ResumeTextSource {
                override suspend fun read(file: ResumeFile): ResumeRead = ResumeRead.Text("text")
            },
            resumeTextParser = parser,
            factIdAllocator = FactIdAllocator(),
            profileRepository = repository,
            connectivityMonitor = TestConnectivityMonitor(),
        ).apply { onEnter(ImportResumeNavKey(scenario = DebugScenario.DEFAULT)) }

        viewModel.onFileChosen(ResumeFile("r.pdf", RESUME_PDF_MIME, 1L, "content://r"))

        assertThat(parser.keptEntries).isEqualTo(3)
    }
}

private class KeptEntriesRecordingParser : ResumeTextParser {
    var keptEntries: Int? = null

    override suspend fun parse(rawText: String): CandidateProfile = error("kept entries must be passed")

    override suspend fun parse(rawText: String, keptEntries: Int): CandidateProfile {
        this.keptEntries = keptEntries
        return CandidateProfile("P", "", "", "", emptyList(), emptyList())
    }
}
