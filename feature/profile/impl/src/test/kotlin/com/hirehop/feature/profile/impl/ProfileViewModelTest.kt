package com.hirehop.feature.profile.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.testing.data.sampleEducationEntry
import com.hirehop.core.testing.data.sampleProfile
import com.hirehop.core.testing.data.sampleProjectEntry
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val parser = FakeResumeTextParser()
    private var nextId = 0
    private val idGenerator = IdGenerator { "generated-${nextId++}" }
    private lateinit var viewModel: ProfileViewModel

    private val importedEntry = sampleProjectEntry.copy(
        source = FactSource.IMPORTED,
        isConfirmed = false,
    )
    private val profileWithImportedEntry = sampleProfile.copy(
        entries = listOf(sampleEducationEntry, importedEntry),
    )

    @Before
    fun setup() {
        viewModel = ProfileViewModel(
            profileRepository = repository,
            resumeTextParser = parser,
            idGenerator = idGenerator,
            defaultDispatcher = UnconfinedTestDispatcher(),
        )
    }

    @Test
    fun uiState_whenNoProfile_startsLoadingThenShowsEmpty() = runTest {
        assertThat(viewModel.uiState.value).isEqualTo(ProfileUiState.Loading)

        viewModel.uiState.test {
            assertThat(expectMostRecentItem()).isEqualTo(ProfileUiState.Empty)
        }
    }

    @Test
    fun uiState_whenProfileHasUnconfirmedEntries_countsThem() = runTest {
        repository.sendProfile(profileWithImportedEntry)

        viewModel.uiState.test {
            assertThat(expectMostRecentItem()).isEqualTo(
                ProfileUiState.Success(profile = profileWithImportedEntry, unconfirmedCount = 1),
            )
        }
    }

    @Test
    fun confirmEntry_marksOnlyThatEntryConfirmed() = runTest {
        repository.sendProfile(profileWithImportedEntry)

        viewModel.confirmEntry(importedEntry.id)

        val saved = savedProfile()
        assertThat(saved.entries.first { it.id == importedEntry.id }.isConfirmed).isTrue()
        assertThat(saved.entries.first { it.id == sampleEducationEntry.id }.isConfirmed).isTrue()
    }

    @Test
    fun confirmEntry_keepsOtherUnconfirmedEntriesUnconfirmed() = runTest {
        val other = importedEntry.copy(id = "other")
        repository.sendProfile(sampleProfile.copy(entries = listOf(importedEntry, other)))

        viewModel.confirmEntry(importedEntry.id)

        val saved = savedProfile()
        assertThat(saved.entries.first { it.id == importedEntry.id }.isConfirmed).isTrue()
        assertThat(saved.entries.first { it.id == other.id }.isConfirmed).isFalse()
    }

    @Test
    fun confirmAll_marksEveryEntryConfirmed() = runTest {
        val other = importedEntry.copy(id = "other")
        repository.sendProfile(sampleProfile.copy(entries = listOf(importedEntry, other)))

        viewModel.confirmAll()

        assertThat(savedProfile().entries.all { it.isConfirmed }).isTrue()
        viewModel.uiState.test {
            val state = expectMostRecentItem() as ProfileUiState.Success
            assertThat(state.unconfirmedCount).isEqualTo(0)
        }
    }

    @Test
    fun deleteEntry_removesTheEntry() = runTest {
        repository.sendProfile(profileWithImportedEntry)

        viewModel.deleteEntry(importedEntry.id)

        assertThat(savedProfile().entries).containsExactly(sampleEducationEntry)
    }

    @Test
    fun saveEntry_whenImportedTextChanges_marksSourceUserEdited() = runTest {
        repository.sendProfile(profileWithImportedEntry)
        val draft = importedEntry.toDraft().copy(title = "Campus Events App v2")

        viewModel.saveEntry(importedEntry.id, draft)

        val saved = savedProfile().entries.first { it.id == importedEntry.id }
        assertThat(saved.title).isEqualTo("Campus Events App v2")
        assertThat(saved.source).isEqualTo(FactSource.USER_EDITED)
        assertThat(saved.isConfirmed).isTrue()
    }

    @Test
    fun saveEntry_whenBulletTextChanges_marksSourceUserEdited() = runTest {
        repository.sendProfile(profileWithImportedEntry)
        val draft = importedEntry.toDraft().withBulletText(index = 0, text = "Built an Android app in Kotlin.")

        viewModel.saveEntry(importedEntry.id, draft)

        val saved = savedProfile().entries.first { it.id == importedEntry.id }
        assertThat(saved.bullets.first().text).isEqualTo("Built an Android app in Kotlin.")
        assertThat(saved.bullets.first().id).isEqualTo(importedEntry.bullets.first().id)
        assertThat(saved.source).isEqualTo(FactSource.USER_EDITED)
    }

    @Test
    fun saveEntry_whenNothingChanges_keepsImportedSource() = runTest {
        repository.sendProfile(profileWithImportedEntry)

        viewModel.saveEntry(importedEntry.id, importedEntry.toDraft())

        val saved = savedProfile().entries.first { it.id == importedEntry.id }
        assertThat(saved.source).isEqualTo(FactSource.IMPORTED)
        assertThat(saved.isConfirmed).isTrue()
    }

    @Test
    fun saveEntry_whenUserStatedEntryChanges_keepsUserStatedSource() = runTest {
        val userStated = importedEntry.copy(source = FactSource.USER_STATED)
        repository.sendProfile(sampleProfile.copy(entries = listOf(userStated)))

        viewModel.saveEntry(userStated.id, userStated.toDraft().copy(organization = "Hackathon"))

        val saved = savedProfile().entries.first { it.id == userStated.id }
        assertThat(saved.organization).isEqualTo("Hackathon")
        assertThat(saved.source).isEqualTo(FactSource.USER_STATED)
    }

    @Test
    fun saveEntry_withoutEntryId_addsConfirmedUserStatedEntry() = runTest {
        repository.sendProfile(sampleProfile.copy(entries = emptyList()))
        val draft = EntryDraft(
            category = EntryCategory.ACHIEVEMENT,
            title = "Smart India Hackathon",
            organization = "Ministry of Education",
            startDate = "2025",
            endDate = "2025",
            bullets = listOf(
                BulletDraft(id = null, text = "Reached the national finals."),
                BulletDraft(id = null, text = "  "),
            ),
        )

        viewModel.saveEntry(entryId = null, draft = draft)

        val added: ProfileEntry = savedProfile().entries.single()
        assertThat(added.id).isEqualTo("generated-0")
        assertThat(added.category).isEqualTo(EntryCategory.ACHIEVEMENT)
        assertThat(added.source).isEqualTo(FactSource.USER_STATED)
        assertThat(added.isConfirmed).isTrue()
        assertThat(added.bullets.map { it.text }).containsExactly("Reached the national finals.")
        assertThat(added.bullets.single().id).isEqualTo("generated-1")
    }

    @Test
    fun updateContact_trimsAndSavesFields() = runTest {
        repository.sendProfile(sampleProfile)

        viewModel.updateContact(
            ContactDraft(
                fullName = " Asha R ",
                email = "asha@example.com ",
                phone = " 12345",
                headline = " Android developer ",
            ),
        )

        val saved = savedProfile()
        assertThat(saved.fullName).isEqualTo("Asha R")
        assertThat(saved.email).isEqualTo("asha@example.com")
        assertThat(saved.phone).isEqualTo("12345")
        assertThat(saved.headline).isEqualTo("Android developer")
    }

    @Test
    fun addSkill_ignoresBlankAndCaseInsensitiveDuplicates() = runTest {
        repository.sendProfile(sampleProfile.copy(skills = listOf("Kotlin")))

        viewModel.addSkill("kotlin")
        viewModel.addSkill("   ")
        viewModel.addSkill(" Compose ")

        assertThat(savedProfile().skills).containsExactly("Kotlin", "Compose").inOrder()
    }

    @Test
    fun removeSkill_removesSkillIgnoringCase() = runTest {
        repository.sendProfile(sampleProfile.copy(skills = listOf("Kotlin", "SQL")))

        viewModel.removeSkill("sql")

        assertThat(savedProfile().skills).containsExactly("Kotlin")
    }

    @Test
    fun loadDemoProfile_savesProfileWithAllEntriesConfirmed() = runTest {
        viewModel.loadDemoProfile()

        val saved = savedProfile()
        assertThat(saved.entries).isNotEmpty()
        assertThat(saved.entries.all { it.isConfirmed }).isTrue()
        assertThat(saved.entries.count { it.category == EntryCategory.PROJECT }).isEqualTo(2)
        assertThat(saved.entries.count { it.category == EntryCategory.EXPERIENCE }).isEqualTo(1)
        assertThat(saved.entries.count { it.category == EntryCategory.EDUCATION }).isEqualTo(1)
        viewModel.uiState.test {
            val state = expectMostRecentItem() as ProfileUiState.Success
            assertThat(state.unconfirmedCount).isEqualTo(0)
        }
    }

    @Test
    fun startManualProfile_savesBlankProfile() = runTest {
        viewModel.startManualProfile()

        val saved = savedProfile()
        assertThat(saved.fullName).isEmpty()
        assertThat(saved.entries).isEmpty()
        assertThat(saved.skills).isEmpty()
    }

    @Test
    fun parseResume_publishesPreviewWithoutSavingProfile() = runTest {
        parser.result = parsedProfile()

        viewModel.onResumeTextChange("Ravi Kumar resume text")
        viewModel.parseResume()

        val state = viewModel.importState.value
        assertThat(parser.receivedText).isEqualTo("Ravi Kumar resume text")
        assertThat(state.preview).isEqualTo(parsedProfile())
        assertThat(state.isParsing).isFalse()
        assertThat(repository.observeProfile().first()).isNull()
    }

    @Test
    fun parseResume_whenTextBlank_doesNotCallParser() = runTest {
        viewModel.onResumeTextChange("   ")
        viewModel.parseResume()

        assertThat(parser.receivedText).isNull()
        assertThat(viewModel.importState.value.preview).isNull()
    }

    @Test
    fun onResumeTextChange_clearsStalePreview() = runTest {
        parser.result = parsedProfile()
        viewModel.onResumeTextChange("first")
        viewModel.parseResume()

        viewModel.onResumeTextChange("second")

        assertThat(viewModel.importState.value.preview).isNull()
        assertThat(viewModel.importState.value.rawText).isEqualTo("second")
    }

    @Test
    fun savePreview_replacesProfileKeepsEntriesUnconfirmedAndResetsImport() = runTest {
        repository.sendProfile(sampleProfile)
        parser.result = parsedProfile()
        viewModel.onResumeTextChange("Ravi Kumar resume text")
        viewModel.parseResume()

        viewModel.savePreview()

        val saved = savedProfile()
        assertThat(saved.fullName).isEqualTo("Ravi Kumar")
        assertThat(saved.entries.map { it.id }).containsExactly("parsed-entry")
        assertThat(saved.entries.single().isConfirmed).isFalse()
        assertThat(saved.entries.single().source).isEqualTo(FactSource.IMPORTED)
        assertThat(viewModel.importState.value).isEqualTo(ResumeImportState())
        viewModel.uiState.test {
            val state = expectMostRecentItem() as ProfileUiState.Success
            assertThat(state.unconfirmedCount).isEqualTo(1)
        }
    }

    @Test
    fun savePreview_withoutPreview_leavesProfileUntouched() = runTest {
        repository.sendProfile(sampleProfile)

        viewModel.savePreview()

        assertThat(savedProfile()).isEqualTo(sampleProfile)
    }

    @Test
    fun resetImport_clearsImportState() = runTest {
        viewModel.onResumeTextChange("text")

        viewModel.resetImport()

        assertThat(viewModel.importState.value).isEqualTo(ResumeImportState())
    }

    private suspend fun savedProfile(): CandidateProfile =
        checkNotNull(repository.observeProfile().first()) { "Expected a saved profile" }

    private fun parsedProfile() = CandidateProfile(
        fullName = "Ravi Kumar",
        email = "ravi@example.com",
        phone = "+91 90000 11111",
        headline = "Backend developer",
        skills = listOf("Java"),
        entries = listOf(
            ProfileEntry(
                id = "parsed-entry",
                category = EntryCategory.PROJECT,
                title = "Library Portal",
                organization = "College",
                startDate = "2024",
                endDate = "2024",
                bullets = emptyList(),
                source = FactSource.USER_STATED,
                isConfirmed = true,
            ),
        ),
    )
}

private class FakeResumeTextParser : ResumeTextParser {
    var result: CandidateProfile = sampleProfile
    var receivedText: String? = null

    override fun parse(rawText: String): CandidateProfile {
        receivedText = rawText
        return result
    }
}
