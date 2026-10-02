package com.hirehop.feature.profile.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.SignInAccount
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.profile.impl.common.FactStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()
    private val connectivity = TestConnectivityMonitor()
    private lateinit var viewModel: ProfileViewModel

    private val profile = CandidateProfile(
        fullName = "Priya Deshmukh",
        email = "priya.d@example.com",
        phone = "+91 90000 00000",
        headline = "Data Operations Associate",
        skills = listOf("SQL", "Excel"),
        entries = listOf(
            fact("U-01", EntryCategory.EDUCATION, FactSource.IMPORTED, isConfirmed = true),
            fact("U-02", EntryCategory.EDUCATION, FactSource.USER_STATED, isConfirmed = true),
            fact("I-01", EntryCategory.EXPERIENCE, FactSource.IMPORTED, isConfirmed = false),
            fact("C-01", EntryCategory.PROJECT, FactSource.IMPORTED, isConfirmed = true),
            fact("X-01", EntryCategory.CERTIFICATION, FactSource.IMPORTED, isConfirmed = false),
        ),
    )

    @Before
    fun setup() {
        viewModel = ProfileViewModel(
            profileRepository = repository,
            sessionRepository = session,
            connectivityMonitor = connectivity,
        )
    }

    @Test
    fun overview_showsDesignIdsForOldIdFormatsAndKeepsTheStoredIdForNavigation() {
        val legacy = profile.copy(
            entries = listOf(fact("entry-9", EntryCategory.PROJECT, FactSource.IMPORTED, isConfirmed = true)),
        )

        val fact = ProfileOverviewState.of(legacy).sections.first { it.kind == ProfileSectionKind.Projects }.facts.single()

        assertThat(fact.id).isEqualTo("entry-9")
        assertThat(fact.displayId).isEqualTo("P-01")
    }

    private fun fact(
        id: String,
        category: EntryCategory,
        source: FactSource,
        isConfirmed: Boolean,
    ) = ProfileEntry(
        id = id,
        category = category,
        title = "Fact $id",
        organization = "",
        startDate = "",
        endDate = "",
        bullets = emptyList(),
        source = source,
        isConfirmed = isConfirmed,
    )

    private suspend fun successState(): ProfileUiState.Success {
        var latest: ProfileUiState? = null
        viewModel.uiState.test { latest = expectMostRecentItem() }
        return latest as ProfileUiState.Success
    }

    @Test
    fun uiState_startsLoadingThenShowsEmptyWithTheAccountName() = runTest {
        session.sendAccount(SignInAccount(id = "a", displayName = "Priya Deshmukh", email = "p@example.com"))
        assertThat(viewModel.uiState.value).isEqualTo(ProfileUiState.Loading)

        viewModel.uiState.test {
            assertThat(expectMostRecentItem()).isEqualTo(ProfileUiState.Empty(headerLine = "Priya Deshmukh"))
        }
    }

    @Test
    fun overview_countsFactsTheWayTheHeaderShowsThem() = runTest {
        repository.sendProfile(profile)

        val overview = successState().overview

        assertThat(overview.factCount).isEqualTo(7)
        assertThat(overview.confirmedCount).isEqualTo(4)
        assertThat(overview.userStatedCount).isEqualTo(1)
        assertThat(overview.unconfirmedCount).isEqualTo(2)
        assertThat(overview.firstUnconfirmedId).isEqualTo("I-01")
        assertThat(overview.headlineLine).isEqualTo("Priya Deshmukh · Data Operations Associate")
    }

    @Test
    fun overview_groupsFactsIntoSectionsAndNumbersSkills() = runTest {
        repository.sendProfile(profile)

        val sections = successState().overview.sections

        assertThat(sections.map { it.kind }).containsExactly(
            ProfileSectionKind.Education,
            ProfileSectionKind.Experience,
            ProfileSectionKind.Projects,
            ProfileSectionKind.Skills,
            ProfileSectionKind.Certifications,
        ).inOrder()
        assertThat(sections.first { it.kind == ProfileSectionKind.Skills }.facts.map { it.id })
            .containsExactly("S-01", "S-02").inOrder()
        assertThat(sections.first { it.kind == ProfileSectionKind.Experience }.facts.single().status)
            .isEqualTo(FactStatus.ToConfirm)
        assertThat(sections.first { it.kind == ProfileSectionKind.Education }.facts.map { it.status })
            .containsExactly(FactStatus.Confirmed, FactStatus.UserStated).inOrder()
    }

    @Test
    fun uiState_followsTheConnectivityMonitor() = runTest {
        repository.sendProfile(profile)
        viewModel.uiState.test {
            assertThat((expectMostRecentItem() as ProfileUiState.Success).isOffline).isFalse()

            connectivity.setOnline(false)

            assertThat((awaitItem() as ProfileUiState.Success).isOffline).isTrue()
        }
    }

    @Test
    fun scenarios_forceTheMatchingState() = runTest {
        repository.sendProfile(profile)
        viewModel.uiState.test {
            expectMostRecentItem()

            viewModel.selectScenario(DebugScenario.OFFLINE)
            assertThat((awaitItem() as ProfileUiState.Success).isOffline).isTrue()

            viewModel.selectScenario(DebugScenario.EMPTY)
            assertThat(awaitItem()).isInstanceOf(ProfileUiState.Empty::class.java)

            viewModel.selectScenario(DebugScenario.ERROR)
            assertThat(awaitItem()).isEqualTo(ProfileUiState.Failure)

            viewModel.selectScenario(DebugScenario.LOADING)
            assertThat(awaitItem()).isEqualTo(ProfileUiState.Loading)
        }
    }

    @Test
    fun confirmEntry_confirmsOnlyThatEntry() = runTest {
        repository.sendProfile(profile)

        viewModel.confirmEntry("I-01")

        val saved = repository.observeProfile().first().let(::checkNotNull)
        assertThat(saved.entries.first { it.id == "I-01" }.isConfirmed).isTrue()
        assertThat(saved.entries.first { it.id == "X-01" }.isConfirmed).isFalse()
    }

    @Test
    fun updateContact_trimsAndSavesTheContactFields() = runTest {
        repository.sendProfile(profile)

        viewModel.updateContact(
            ContactDraft(fullName = " Priya D ", email = "p@example.com", phone = "", headline = " Analyst "),
        )

        val saved = repository.observeProfile().first().let(::checkNotNull)
        assertThat(saved.fullName).isEqualTo("Priya D")
        assertThat(saved.headline).isEqualTo("Analyst")
        assertThat(saved.phone).isEmpty()
        assertThat(saved.entries).isEqualTo(profile.entries)
    }

    @Test
    fun skills_addIgnoresBlankAndDuplicatesAndRemoveIsCaseInsensitive() = runTest {
        repository.sendProfile(profile)

        viewModel.addSkill("  ")
        viewModel.addSkill("sql")
        viewModel.addSkill(" Power BI ")
        viewModel.removeSkill("EXCEL")

        assertThat(repository.observeProfile().first().let(::checkNotNull).skills).containsExactly("SQL", "Power BI").inOrder()
    }

    @Test
    fun edits_withoutAProfile_changeNothing() = runTest {
        viewModel.confirmEntry("I-01")
        viewModel.addSkill("SQL")

        assertThat(repository.observeProfile().first()).isNull()
    }
}
