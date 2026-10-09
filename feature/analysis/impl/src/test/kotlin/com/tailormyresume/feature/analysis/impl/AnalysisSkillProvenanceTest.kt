package com.tailormyresume.feature.analysis.impl

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class AnalysisSkillProvenanceTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val requirement = JobRequirement(
        id = "req-1",
        text = "SQL for reporting",
        type = RequirementType.SKILL,
        priority = RequirementPriority.MUST_HAVE,
        keywords = listOf("sql"),
    )

    private fun profile(userStated: List<String>) = CandidateProfile(
        fullName = "Test Candidate",
        email = "candidate@example.test",
        phone = "",
        headline = "",
        skills = listOf("SQL", "Excel"),
        entries = emptyList(),
        userStatedSkills = userStated,
    )

    private fun sectionsFor(userStated: List<String>): RequirementItem =
        listOf(RequirementMatch(requirement, MatchStatus.MET, listOf("skill:SQL", "skill:Excel")))
            .toSections(profile(userStated), emptySet(), emptySet())
            .single().items.single()

    private fun item(userStatedSkills: List<String>) = RequirementItem(
        requirement = requirement,
        status = MatchStatus.MET,
        skills = listOf("SQL"),
        isInPrepPlan = false,
        userStatedSkills = userStatedSkills,
    )

    private fun showRow(item: RequirementItem) {
        val state = AnalysisUiState.Result(
            job = JobLabel("Analyst", "Contoso Labs"),
            keywordCoverage = KeywordCoverage(1, 1),
            sections = listOf(RequirementSection(RequirementGroup.Met, listOf(item))),
            totalCredits = 1,
        )
        composeRule.setContent { TmrTheme { AnalysisScreen(uiState = state, actions = AnalysisActions()) } }
    }

    private fun showSheet(item: RequirementItem) {
        composeRule.setContent { TmrTheme { SourceSheetContent(item, AnalysisActions()) } }
    }

    @Test
    fun mapperMarksOnlyUserStatedSkills() {
        assertThat(sectionsFor(listOf("sql")).userStatedSkills).containsExactly("SQL")
    }

    @Test
    fun mapperMarksNothingWhenNoSkillIsUserStated() {
        assertThat(sectionsFor(emptyList()).userStatedSkills).isEmpty()
    }

    @Test
    fun rowShowsUserStatedForAUserStatedSkill() {
        showRow(item(listOf("SQL")))

        composeRule.onNodeWithText("User-stated").assertExists()
    }

    @Test
    fun rowShowsNoProvenanceForAConfirmedSkill() {
        showRow(item(emptyList()))

        composeRule.onNodeWithText("User-stated").assertDoesNotExist()
    }

    @Test
    fun sheetShowsUserStatedForAUserStatedSkill() {
        showSheet(item(listOf("SQL")))

        composeRule.onNodeWithText("User-stated").assertExists()
    }

    @Test
    fun sheetShowsNoProvenanceForAConfirmedSkill() {
        showSheet(item(emptyList()))

        composeRule.onNodeWithText("User-stated").assertDoesNotExist()
    }
}
