package com.hirehop.feature.tailor.impl.prepquestions

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.prep.GeneratePrepQuestionsUseCase
import com.hirehop.core.domain.prep.PrepQuestionGenerator
import com.hirehop.core.domain.prep.PrepQuestionKind
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.canonicalProfileWithoutEntries
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.tailor.api.navigation.PrepQuestionsNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PrepQuestionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()

    private lateinit var viewModel: PrepQuestionsViewModel

    @Before
    fun setup() {
        viewModel = newViewModel()
    }

    @Test
    fun loadingScenario_staysOnGenerating() {
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.LOADING))

        assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.GENERATING)
        assertThat(viewModel.uiState.value.groups).isEmpty()
    }

    @Test
    fun errorScenario_reportsAFailureWithoutAskingTheDomain() {
        applicationRepository.sendApplications(emptyList())
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.ERROR)
    }

    @Test
    fun defaultScenario_groupsAllThreeKindsTheDomainProduced() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(PrepQuestionsStage.READY)
        assertThat(state.groups.map { group -> group.kind }).containsExactly(
            PrepQuestionKind.STRENGTH,
            PrepQuestionKind.CLARIFY,
            PrepQuestionKind.GAP,
        ).inOrder()
        assertThat(state.totalCount).isEqualTo(6)
    }

    @Test
    fun readyList_neverExceedsTheDomainLimit() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.totalCount)
            .isAtMost(PrepQuestionGenerator.MAX_QUESTIONS)
    }

    @Test
    fun readyList_carriesTheStableIdsTheDomainAssigned() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.groups.flatMap { group -> group.cards }.map { card -> card.id })
            .containsExactly(
                "prep-strength-strong-kotlin-for-android-app-developmen",
                "prep-strength-jetpack-compose-for-modern-android-user",
                "prep-clarify-retrofit-or-ktor-for-network-calls",
                "prep-clarify-unit-tests-written-with-junit",
                "prep-gap-agile-delivery-with-jira",
                "prep-gap-clear-written-communication-in-english",
            ).inOrder()
    }

    @Test
    fun readyList_showsTheRequirementAndTheBackingFactOfEveryQuestion() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val cards = viewModel.uiState.value.groups.flatMap { group -> group.cards }
        assertThat(cards.all { card -> card.requirementText.isNotBlank() }).isTrue()
        val strengths = viewModel.uiState.value.groups
            .single { group -> group.kind == PrepQuestionKind.STRENGTH }
            .cards
        assertThat(strengths.map { card -> card.backingFactId }).containsExactly("P-03-b1", "P-03-b1")
    }

    @Test
    fun gapQuestion_carriesNoBackingFactAndSaysSo() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val gaps = viewModel.uiState.value.groups.single { group -> group.kind == PrepQuestionKind.GAP }
        assertThat(gaps.cards.map { card -> card.backingFactId }).containsExactly(null, null)
        assertThat(gaps.cards.first().requirementText).isEqualTo("Agile delivery with JIRA")
    }

    @Test
    fun gapQuestion_neverAccusesTheCandidate() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val gaps = viewModel.uiState.value.groups.single { group -> group.kind == PrepQuestionKind.GAP }
        gaps.cards.forEach { card ->
            val prompt = card.prompt.lowercase()
            assertThat(prompt).doesNotContain("you failed")
            assertThat(prompt).doesNotContain("you lack")
            assertThat(prompt).doesNotContain("weakness")
            assertThat(prompt).doesNotContain("problem")
            assertThat(prompt).doesNotContain("you cannot")
        }
    }

    @Test
    fun gapQuestion_offersAnHonestAnswer() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val gap = viewModel.uiState.value.groups
            .single { group -> group.kind == PrepQuestionKind.GAP }
            .cards
            .first()
        assertThat(gap.prompt).contains("You have no record of it yet")
    }

    @Test
    fun gapIdsFallBackToTheRequirementSlugContract() = runTest {
        given(
            gapAllGaps(
                text = "&&&",
                id = "req-symbols",
            ),
        )
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.groups.single().cards.map { card -> card.id })
            .containsExactly("prep-gap-requirement")
    }

    @Test
    fun repeatedRequirementTextKeepsTheIndexedSuffix() = runTest {
        given(
            gapAllGaps(
                text = "Agile delivery with JIRA",
                id = "req-agile-a",
                duplicate = true,
            ),
        )
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.groups.single().cards.map { card -> card.id })
            .containsExactly("prep-gap-agile-delivery-with-jira", "prep-gap-agile-delivery-with-jira-1")
            .inOrder()
    }

    @Test
    fun partialScenario_showsTheGapsGroupFirstWithoutHidingTheRest() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.PARTIAL))

        val state = viewModel.uiState.value
        assertThat(state.filter).isEqualTo(PrepQuestionFilter.GAP)
        assertThat(state.visibleGroups.map { group -> group.kind }).containsExactly(PrepQuestionKind.GAP)
        assertThat(state.totalCount).isEqualTo(6)
        assertThat(state.countOf(PrepQuestionFilter.STRENGTH)).isEqualTo(2)
    }

    @Test
    fun filterChosen_switchesTheGroupWithoutLosingAnyQuestion() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        viewModel.onAction(PrepQuestionsAction.FilterChosen(PrepQuestionFilter.STRENGTH))
        assertThat(viewModel.uiState.value.visibleGroups).hasSize(1)

        viewModel.onAction(PrepQuestionsAction.FilterChosen(PrepQuestionFilter.ALL))
        assertThat(viewModel.uiState.value.visibleGroups).hasSize(3)
        assertThat(viewModel.uiState.value.totalCount).isEqualTo(6)
    }

    @Test
    fun emptyAnalysis_saysSoInsteadOfInventingAQuestion() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(gapAnalysis = noMatches())))
        profileRepository.sendProfile(canonicalCandidateProfile)
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(PrepQuestionsStage.EMPTY_ANALYSIS)
        assertThat(state.groups).isEmpty()
        assertThat(state.totalCount).isEqualTo(0)
    }

    @Test
    fun applicationWithoutAnalysis_saysThereIsNothingToPrepare() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(gapAnalysis = null)))
        profileRepository.sendProfile(canonicalCandidateProfile)
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.EMPTY_ANALYSIS)
    }

    @Test
    fun emptyProfile_asksForAFactInsteadOfQuestioningThem() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalProfileWithoutEntries)
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(PrepQuestionsStage.EMPTY_PROFILE)
        assertThat(state.groups).isEmpty()
    }

    @Test
    fun missingProfile_asksForAFact() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(null)
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.EMPTY_PROFILE)
    }

    @Test
    fun unconfirmedFactsProduceNoStrengthQuestions() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(
            canonicalCandidateProfile.copy(
                entries = canonicalCandidateProfile.entries.map { entry -> entry.copy(isConfirmed = false) },
            ),
        )
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.EMPTY_PROFILE)
    }

    @Test
    fun offlineScenario_keepsTheSavedListReadable() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.OFFLINE))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(PrepQuestionsStage.OFFLINE)
        assertThat(state.isOffline).isTrue()
        assertThat(state.totalCount).isEqualTo(6)
    }

    @Test
    fun missingApplication_isAFailure() = runTest {
        profileRepository.sendProfile(canonicalCandidateProfile)
        applicationRepository.sendApplications(emptyList())
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.ERROR)
    }

    @Test
    fun everyScenarioMapsToAKnownStage() = runTest {
        val stages = listOf(
            PrepQuestionsStage.GENERATING,
            PrepQuestionsStage.READY,
            PrepQuestionsStage.EMPTY_ANALYSIS,
            PrepQuestionsStage.EMPTY_PROFILE,
            PrepQuestionsStage.OFFLINE,
            PrepQuestionsStage.ERROR,
        )
        for (scenario in DebugScenario.entries) {
            given()
            val fresh = newViewModel()
            fresh.onEnter(PrepQuestionsNavKey(APPLICATION_ID, scenario))

            assertThat(fresh.uiState.value.stage).isIn(stages)
        }
    }

    @Test
    fun onEnter_ignoresASecondKey() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        applicationRepository.sendApplications(emptyList())

        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.READY)
    }

    @Test
    fun practiseToggled_marksAndUnmarksTheQuestion() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        val id = viewModel.uiState.value.groups.first().cards.first().id

        viewModel.onAction(PrepQuestionsAction.PractiseToggled(id))

        assertThat(viewModel.uiState.value.cardOf(id)?.isPractised).isTrue()
        assertThat(viewModel.uiState.value.practisedCount).isEqualTo(1)
        assertThat(viewModel.uiState.value.message).isEqualTo(PrepQuestionsMessage.PRACTISED)

        viewModel.onAction(PrepQuestionsAction.PractiseToggled(id))

        assertThat(viewModel.uiState.value.cardOf(id)?.isPractised).isFalse()
        assertThat(viewModel.uiState.value.practisedCount).isEqualTo(0)
        assertThat(viewModel.uiState.value.message).isEqualTo(PrepQuestionsMessage.UNPRACTISED)
    }

    @Test
    fun practiseToggled_leavesTheOtherQuestionsAlone() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        val before = viewModel.uiState.value.practiseFlags()
        val target = viewModel.uiState.value.groups.first().cards.first().id

        viewModel.onAction(PrepQuestionsAction.PractiseToggled(target))

        val after = viewModel.uiState.value.practiseFlags()
        assertThat(after.size).isEqualTo(before.size)
        assertThat(after.filter { pair -> pair.second }.map { pair -> pair.first })
            .containsExactly(target)
        assertThat(before.all { pair -> !pair.second }).isTrue()
    }

    @Test
    fun practiseToggled_onAnUnknownQuestion_doesNothing() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        viewModel.onAction(PrepQuestionsAction.PractiseToggled("not-a-real-id"))

        assertThat(viewModel.uiState.value.practisedCount).isEqualTo(0)
        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun reportInaccurate_doesNotClaimTheReportWasSent() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        val id = viewModel.uiState.value.groups.first().cards.first().id

        viewModel.onAction(PrepQuestionsAction.ReportInaccurate(id))

        assertThat(viewModel.uiState.value.message).isEqualTo(PrepQuestionsMessage.REPORT_UNAVAILABLE)
    }

    @Test
    fun reportInaccurate_onAnUnknownQuestion_doesNothing() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        viewModel.onAction(PrepQuestionsAction.ReportInaccurate("not-a-real-id"))

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun dismissMessage_clearsTheNote() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        viewModel.onAction(PrepQuestionsAction.ReportInaccurate(viewModel.uiState.value.groups.first().cards.first().id))

        viewModel.onAction(PrepQuestionsAction.DismissMessage)

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun retry_reloadsTheQuestions() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalProfileWithoutEntries)
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.ERROR))
        assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.ERROR)

        profileRepository.sendProfile(canonicalCandidateProfile)
        viewModel.onAction(PrepQuestionsAction.Retry)

        assertThat(viewModel.uiState.value.stage).isEqualTo(PrepQuestionsStage.READY)
    }

    @Test
    fun noQuestionCarriesAScaleOrTimeSavingClaim() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        viewModel.uiState.value.groups
            .flatMap { group -> group.cards }
            .forEach { card ->
                val prompt = card.prompt.lowercase()
                assertThat(prompt).doesNotContain("ats")
                assertThat(prompt).doesNotContain("guarantee")
                assertThat(prompt).doesNotContain("hours saved")
                assertThat(prompt).doesNotContain("score")
            }
    }

    private fun newViewModel(): PrepQuestionsViewModel = PrepQuestionsViewModel(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        generatePrepQuestions = GeneratePrepQuestionsUseCase(),
    )

    private fun given(gap: GapAnalysis = requireNotNull(canonicalApplication.gapAnalysis)) {
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(gapAnalysis = gap)))
        profileRepository.sendProfile(canonicalCandidateProfile)
    }

    private fun noMatches(): GapAnalysis = GapAnalysis(
        matches = emptyList(),
        keywordCoverage = KeywordCoverage(covered = 0, total = 0),
    )

    private fun gapAllGaps(
        text: String,
        id: String,
        duplicate: Boolean = false,
    ): GapAnalysis {
        val requirement = JobRequirement(
            id = id,
            text = text,
            type = RequirementType.SKILL,
            priority = RequirementPriority.NICE_TO_HAVE,
            keywords = emptyList(),
        )
        val matches = buildList {
            add(
                RequirementMatch(
                    requirement = requirement,
                    status = MatchStatus.GAP,
                    evidenceIds = emptyList(),
                ),
            )
            if (duplicate) {
                add(
                    RequirementMatch(
                        requirement = requirement.copy(id = "$id-b"),
                        status = MatchStatus.GAP,
                        evidenceIds = emptyList(),
                    ),
                )
            }
        }
        return GapAnalysis(
            matches = matches,
            keywordCoverage = KeywordCoverage(covered = 0, total = matches.size),
        )
    }

    private fun PrepQuestionsUiState.practiseFlags(): List<Pair<String, Boolean>> = groups
        .flatMap { group -> group.cards }
        .map { card -> card.id to card.isPractised }
}

private const val APPLICATION_ID = "application-northwind-1"
