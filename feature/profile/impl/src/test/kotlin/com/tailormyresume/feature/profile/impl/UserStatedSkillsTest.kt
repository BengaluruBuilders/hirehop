package com.tailormyresume.feature.profile.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.AddUserStatedFactsUseCase
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.repository.TestSessionRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestIdGenerator
import com.tailormyresume.feature.profile.impl.common.FactStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UserStatedSkillsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val writer = UserFactWriter(repository, AddUserStatedFactsUseCase(repository, TestIdGenerator()))
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        viewModel = ProfileViewModel(repository, TestSessionRepository(), TestConnectivityMonitor())
    }

    private fun skillStatuses(profile: com.tailormyresume.core.model.CandidateProfile) =
        ProfileOverviewState.of(profile).sections.first { it.kind == ProfileSectionKind.Skills }.facts.map { it.status }

    @Test
    fun skillsTypedInTheGuidedFormAreStoredUserStated() = runTest {
        repository.sendProfile(sampleProfile.copy(skills = listOf("Kotlin")))

        writer.write(drafts = emptyList(), skills = listOf("SQL", "kotlin", " Excel "))

        val saved = repository.observeProfile().first().let(::checkNotNull)
        assertThat(saved.skills).containsExactly("Kotlin", "SQL", "Excel").inOrder()
        assertThat(saved.userStatedSkills).containsExactly("SQL", "Excel").inOrder()
    }

    @Test
    fun skillsAddedFromProfileAreStoredUserStated() = runTest {
        repository.sendProfile(sampleProfile)

        viewModel.addSkill("Power BI")

        assertThat(repository.observeProfile().first().let(::checkNotNull).userStatedSkills).containsExactly("Power BI")
    }

    @Test
    fun removingASkillAlsoRemovesItsProvenance() = runTest {
        repository.sendProfile(sampleProfile.copy(skills = listOf("SQL"), userStatedSkills = listOf("SQL")))

        viewModel.removeSkill("sql")

        val saved = repository.observeProfile().first().let(::checkNotNull)
        assertThat(saved.skills).isEmpty()
        assertThat(saved.userStatedSkills).isEmpty()
    }

    @Test
    fun overviewMarksUserStatedSkillsAndKeepsOthersConfirmed() {
        val profile = sampleProfile.copy(skills = listOf("Kotlin", "SQL"), userStatedSkills = listOf("sql"))

        assertThat(skillStatuses(profile)).containsExactly(FactStatus.Confirmed, FactStatus.UserStated).inOrder()
    }

    @Test
    fun overviewReadsSkillsWithoutProvenanceAsConfirmed() {
        val profile = sampleProfile.copy(skills = listOf("Kotlin", "SQL"))

        assertThat(skillStatuses(profile)).containsExactly(FactStatus.Confirmed, FactStatus.Confirmed)
    }

    @Test
    fun overviewCountsUserStatedSkillsAsUserStated() = runTest {
        repository.sendProfile(sampleProfile.copy(skills = listOf("Kotlin", "SQL"), entries = emptyList(), userStatedSkills = listOf("SQL")))

        viewModel.uiState.test {
            val overview = (expectMostRecentItem() as ProfileUiState.Success).overview
            assertThat(overview.userStatedCount).isEqualTo(1)
            assertThat(overview.confirmedCount).isEqualTo(1)
        }
    }
}
