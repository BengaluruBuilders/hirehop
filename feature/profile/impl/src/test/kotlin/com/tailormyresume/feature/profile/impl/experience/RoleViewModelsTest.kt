package com.tailormyresume.feature.profile.impl.experience

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.fact.FactIdAllocator
import com.tailormyresume.core.domain.profile.ProfileCompleteness
import com.tailormyresume.core.domain.profile.RequiredGaps
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.testing.data.PrototypeFixtures
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ExperienceViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val profileRepository = TestProfileRepository()

    private fun viewModel() = ExperienceViewModel(profileRepository)

    private fun TestScope.collectUiState(viewModel: ExperienceViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
    }

    private fun ExperienceViewModel.content(): ExperienceUiState.Content {
        val state = uiState.value
        assertThat(state).isInstanceOf(ExperienceUiState.Content::class.java)
        return state as ExperienceUiState.Content
    }

    @Test
    fun initialState_beforeAnyEmission_isLoading() {
        assertThat(viewModel().uiState.value).isEqualTo(ExperienceUiState.Loading)
    }

    @Test
    fun filledList_showsDatesAndNowForPresent() = runTest {
        val viewModel = viewModel()
        collectUiState(viewModel)
        profileRepository.sendProfile(PrototypeFixtures.returning().profile)

        val rows = viewModel.content().rows

        assertThat(rows.map { it.entryId }).containsExactly("exp-infosys", "exp-tata", "exp-bajaj").inOrder()
        val current = rows.first()
        assertThat(current.isCurrent).isTrue()
        assertThat(current.end).isEqualTo("Present")
        assertThat(current.start).isEqualTo("Jul 2022")
        assertThat(current.title).isEqualTo("Business Analyst")
        assertThat(current.company).isEqualTo("Infosys")
        assertThat(rows.map { it.needsEndDate }).containsExactly(false, false, false)
    }

    @Test
    fun missingEndDate_marksRequiredFix() = runTest {
        val viewModel = viewModel()
        collectUiState(viewModel)
        profileRepository.sendProfile(PrototypeFixtures.fresh().profile)

        val rows = viewModel.content().rows
        val intern = rows.first { it.entryId == "exp-tata" }

        assertThat(intern.needsEndDate).isTrue()
        assertThat(intern.end).isEqualTo("")
        assertThat(rows.filter { it.entryId != "exp-tata" }.map { it.needsEndDate }).containsExactly(false, false)
    }

    @Test
    fun empty_showsOnlyAddRole() = runTest {
        val repository = TestProfileRepository()
        val viewModel = ExperienceViewModel(repository)
        collectUiState(viewModel)

        assertThat(viewModel.content().rows).isEmpty()

        val base = PrototypeFixtures.returning().profile
        repository.sendProfile(base.copy(entries = base.entries.filter { it.category != EntryCategory.EXPERIENCE }))

        assertThat(viewModel.content().rows).isEmpty()
    }

    @Test
    fun orderFollowsStoredOrder() = runTest {
        val viewModel = viewModel()
        collectUiState(viewModel)
        val profile = PrototypeFixtures.returning().profile
        profileRepository.sendProfile(profile.copy(entries = profile.entries.reversed()))
        val stored = viewModel.content().rows.map { it.entryId }

        profileRepository.sendProfile(profile)

        assertThat(stored).containsExactly("exp-bajaj", "exp-tata", "exp-infosys").inOrder()
        assertThat(viewModel.content().rows.map { it.entryId }).containsExactly("exp-infosys", "exp-tata", "exp-bajaj").inOrder()
    }
}

class EditRoleViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val profileRepository = TestProfileRepository()

    private fun viewModel(entryId: String?) = EditRoleViewModel(profileRepository, FactIdAllocator(), entryId)

    private fun TestScope.collectUiState(viewModel: EditRoleViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
    }

    private fun EditRoleViewModel.editing(): EditRoleUiState.Editing {
        val state = uiState.value
        assertThat(state).isInstanceOf(EditRoleUiState.Editing::class.java)
        return state as EditRoleUiState.Editing
    }

    private fun EditRoleViewModel.draft(): EditRoleDraft = editing().draft

    private suspend fun savedProfile(): CandidateProfile? = profileRepository.observeProfile().first()

    private fun CandidateProfile?.entry(id: String): ProfileEntry {
        val entry = this?.entries?.firstOrNull { it.id == id }
        assertThat(entry).isNotNull()
        return checkNotNull(entry)
    }

    @Test
    fun initialState_beforeProfileIsLoaded_isLoading() {
        assertThat(viewModel("exp-tata").uiState.value).isEqualTo(EditRoleUiState.Loading)
    }

    @Test
    fun existingRole_loadsDraftFromEntry() = runTest {
        profileRepository.sendProfile(PrototypeFixtures.fresh().profile)
        val viewModel = viewModel("exp-tata")
        collectUiState(viewModel)

        val editing = viewModel.editing()

        assertThat(editing.isNew).isFalse()
        assertThat(editing.draft.title).isEqualTo("Data Analyst Intern")
        assertThat(editing.draft.company).isEqualTo("Tata Digital")
        assertThat(editing.draft.start).isEqualTo("Jun 2021")
        assertThat(editing.draft.end).isEqualTo("")
        assertThat(editing.draft.current).isFalse()
        assertThat(editing.draft.bullets).containsExactly(
            "Built forecasting models in Python for category demand.",
            "Automated weekly Excel reports for 5 business teams.",
        ).inOrder()
        assertThat(editing.canSave).isTrue()
    }

    @Test
    fun laterProfileEmissions_doNotOverwriteTheDraft() = runTest {
        val profile = PrototypeFixtures.returning().profile
        profileRepository.sendProfile(profile)
        val viewModel = viewModel("exp-infosys")
        collectUiState(viewModel)
        viewModel.onTitleChange("Business Analyst")

        profileRepository.sendProfile(profile.copy(headline = "Finance lead"))

        assertThat(viewModel.draft().title).isEqualTo("Business Analyst")
    }

    @Test
    fun addMode_startsEmptyWithOneBulletAndNoDelete() = runTest {
        val profile = PrototypeFixtures.returning().profile
        profileRepository.sendProfile(profile)
        val viewModel = viewModel(null)
        collectUiState(viewModel)

        val editing = viewModel.editing()

        assertThat(editing.isNew).isTrue()
        assertThat(editing.draft).isEqualTo(EditRoleDraft())
        assertThat(editing.canSave).isFalse()

        viewModel.events.test {
            viewModel.delete()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(savedProfile()).isEqualTo(profile)
    }

    @Test
    fun currentToggle_savesPresentAndClearsGap() = runTest {
        val fresh = PrototypeFixtures.fresh().profile
        profileRepository.sendProfile(fresh)
        val before = ProfileCompleteness.percent(fresh)
        val viewModel = viewModel("exp-tata")
        collectUiState(viewModel)
        viewModel.onEndChange("May 2022")
        viewModel.onCurrentChange(true)

        viewModel.events.test {
            viewModel.save()
            assertThat(awaitItem()).isEqualTo(EditRoleEvent.Saved)
            cancelAndIgnoreRemainingEvents()
        }

        val saved = checkNotNull(savedProfile())
        assertThat(saved.entry("exp-tata").endDate).isEqualTo("Present")
        assertThat(RequiredGaps.of(saved)).isEmpty()
        assertThat(ProfileCompleteness.percent(saved) - before).isEqualTo(6)
    }

    @Test
    fun currentToggleOff_restoresTypedEnd() = runTest {
        profileRepository.sendProfile(PrototypeFixtures.fresh().profile)
        val viewModel = viewModel("exp-tata")
        collectUiState(viewModel)
        viewModel.onEndChange("May 2022")
        viewModel.onCurrentChange(true)
        viewModel.onCurrentChange(false)

        assertThat(viewModel.draft().end).isEqualTo("May 2022")
        assertThat(viewModel.draft().current).isFalse()

        viewModel.events.test {
            viewModel.save()
            assertThat(awaitItem()).isEqualTo(EditRoleEvent.Saved)
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(checkNotNull(savedProfile()).entry("exp-tata").endDate).isEqualTo("May 2022")
    }

    @Test
    fun saveExisting_marksUserEditedAndKeepsId() = runTest {
        val profile = PrototypeFixtures.returning().profile
        profileRepository.sendProfile(profile)
        val viewModel = viewModel("exp-infosys")
        collectUiState(viewModel)
        viewModel.onTitleChange("  Senior Business Analyst  ")

        viewModel.events.test {
            viewModel.save()
            assertThat(awaitItem()).isEqualTo(EditRoleEvent.Saved)
            cancelAndIgnoreRemainingEvents()
        }

        val saved = checkNotNull(savedProfile())
        assertThat(saved.entries.indexOfFirst { it.id == "exp-infosys" })
            .isEqualTo(profile.entries.indexOfFirst { it.id == "exp-infosys" })
        assertThat(saved.entries.filter { it.id != "exp-infosys" })
            .isEqualTo(profile.entries.filter { it.id != "exp-infosys" })
        val entry = saved.entry("exp-infosys")
        assertThat(entry.title).isEqualTo("Senior Business Analyst")
        assertThat(entry.source).isEqualTo(FactSource.USER_EDITED)
        assertThat(entry.isConfirmed).isTrue()
        assertThat(entry.bullets.map { it.id }).containsExactly("exp-infosys-b1", "exp-infosys-b2").inOrder()
    }

    @Test
    fun saveNew_usesUserStatedAndAllocatedId() = runTest {
        val profile = PrototypeFixtures.returning().profile
        profileRepository.sendProfile(profile)
        val viewModel = viewModel(null)
        collectUiState(viewModel)
        viewModel.onTitleChange("Intern")
        viewModel.onCompanyChange("Acme")
        viewModel.onStartChange("Jan 2019")
        viewModel.onEndChange("Mar 2019")
        viewModel.onBulletChange(0, "Built a report")

        viewModel.events.test {
            viewModel.save()
            assertThat(awaitItem()).isEqualTo(EditRoleEvent.Saved)
            cancelAndIgnoreRemainingEvents()
        }

        val allocated = FactIdAllocator().nextId(EntryCategory.EXPERIENCE, profile.entries, "Intern")
        assertThat(allocated).isEqualTo("I-01")
        val saved = checkNotNull(savedProfile())
        val created = saved.entry(allocated)
        assertThat(saved.entries.indexOfFirst { it.id == allocated })
            .isEqualTo(profile.entries.indexOfFirst { it.id == "exp-infosys" })
        assertThat(created.category).isEqualTo(EntryCategory.EXPERIENCE)
        assertThat(created.source).isEqualTo(FactSource.USER_STATED)
        assertThat(created.isConfirmed).isTrue()
        assertThat(created.title).isEqualTo("Intern")
        assertThat(created.organization).isEqualTo("Acme")
        assertThat(created.startDate).isEqualTo("Jan 2019")
        assertThat(created.endDate).isEqualTo("Mar 2019")
        assertThat(created.bullets.map { it.id }).containsExactly("I-01-b1")
        assertThat(created.bullets.single().text).isEqualTo("Built a report")
        assertThat(saved.entries.filter { it.id != allocated }).isEqualTo(profile.entries)
    }

    @Test
    fun saveWithEndDate_removesRequiredGap() = runTest {
        val fresh = PrototypeFixtures.fresh().profile
        profileRepository.sendProfile(fresh)
        val viewModel = viewModel("exp-tata")
        collectUiState(viewModel)
        assertThat(RequiredGaps.of(fresh)).isNotEmpty()
        viewModel.onEndChange("May 2022")

        viewModel.events.test {
            viewModel.save()
            assertThat(awaitItem()).isEqualTo(EditRoleEvent.Saved)
            cancelAndIgnoreRemainingEvents()
        }

        val saved = checkNotNull(savedProfile())
        assertThat(saved.entry("exp-tata").endDate).isEqualTo("May 2022")
        assertThat(RequiredGaps.of(saved)).isEmpty()
    }

    @Test
    fun blankTitle_blocksSaveWithoutWriting() = runTest {
        val profile = PrototypeFixtures.returning().profile
        profileRepository.sendProfile(profile)
        val viewModel = viewModel(null)
        collectUiState(viewModel)
        viewModel.onCompanyChange("Acme")
        viewModel.onTitleChange("   ")

        assertThat(viewModel.editing().canSave).isFalse()

        viewModel.events.test {
            viewModel.save()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(savedProfile()).isEqualTo(profile)

        viewModel.onTitleChange("Analyst")

        assertThat(viewModel.editing().canSave).isTrue()
    }

    @Test
    fun bulletLimits_enforced() = runTest {
        profileRepository.sendProfile(PrototypeFixtures.returning().profile)
        val viewModel = viewModel(null)
        collectUiState(viewModel)
        viewModel.onTitleChange("Intern")
        viewModel.onCompanyChange("Acme")

        viewModel.onBulletChange(0, "x".repeat(401))

        assertThat(viewModel.editing().canSave).isFalse()

        viewModel.onBulletChange(0, "x".repeat(400))

        assertThat(viewModel.editing().canSave).isTrue()

        repeat(20) { viewModel.onAddBullet() }

        assertThat(viewModel.draft().bullets).hasSize(15)
        assertThat(viewModel.editing().canAddBullet).isFalse()

        viewModel.onBulletChange(0, "  spaced  ")
        viewModel.onBulletChange(1, "   ")

        viewModel.events.test {
            viewModel.save()
            assertThat(awaitItem()).isEqualTo(EditRoleEvent.Saved)
            cancelAndIgnoreRemainingEvents()
        }

        val created = checkNotNull(savedProfile()).entries.first()
        assertThat(created.bullets.map { it.text }).containsExactly("spaced")
        assertThat(created.bullets.single().id).isEqualTo("${created.id}-b1")
    }

    @Test
    fun delete_removesEntryAndEmitsDeleted() = runTest {
        val profile = PrototypeFixtures.returning().profile
        profileRepository.sendProfile(profile)
        val viewModel = viewModel("exp-bajaj")
        collectUiState(viewModel)

        viewModel.events.test {
            viewModel.delete()
            assertThat(awaitItem()).isEqualTo(EditRoleEvent.Deleted)
            cancelAndIgnoreRemainingEvents()
        }

        val saved = checkNotNull(savedProfile())
        assertThat(saved.entries.map { it.id }).doesNotContain("exp-bajaj")
        assertThat(saved.entries).isEqualTo(profile.entries.filter { it.id != "exp-bajaj" })
    }

    @Test
    fun doubleSaveInAddMode_writesOneRoleWithOneId() = runTest {
        val base = PrototypeFixtures.returning().profile
        profileRepository.sendProfile(base)
        val slowRepository = object : ProfileRepository by profileRepository {
            override fun observeProfile(): Flow<CandidateProfile?> = flow {
                delay(10)
                emit(profileRepository.observeProfile().first())
            }
        }
        val viewModel = EditRoleViewModel(slowRepository, FactIdAllocator(), null)
        collectUiState(viewModel)
        advanceUntilIdle()
        viewModel.onTitleChange("Intern")
        viewModel.onCompanyChange("Acme")

        viewModel.save()
        viewModel.save()
        advanceUntilIdle()

        val entries = checkNotNull(savedProfile()).entries
        assertThat(entries).hasSize(base.entries.size + 1)
        assertThat(entries.map { it.id }).containsNoDuplicates()
    }

    @Test
    fun deleteInAddMode_isNoop() = runTest {
        val profile = PrototypeFixtures.returning().profile
        profileRepository.sendProfile(profile)
        val viewModel = viewModel(null)
        collectUiState(viewModel)

        viewModel.events.test {
            viewModel.delete()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(savedProfile()).isEqualTo(profile)
    }

    @Test
    fun saveInAddModeAtEntryLimit_isBlocked() = runTest {
        val base = PrototypeFixtures.returning().profile
        val education = base.entries.first { it.category == EntryCategory.EDUCATION }
        profileRepository.sendProfile(
            base.copy(entries = base.entries + (2..31).map { education.copy(id = "edu-$it") }),
        )
        val viewModel = viewModel(null)
        collectUiState(viewModel)
        viewModel.onTitleChange("Intern")
        viewModel.onCompanyChange("Acme")
        viewModel.onStartChange("Jan 2019")

        assertThat(checkNotNull(savedProfile()).entries).hasSize(40)
        assertThat(viewModel.editing().canSave).isFalse()
    }
}
