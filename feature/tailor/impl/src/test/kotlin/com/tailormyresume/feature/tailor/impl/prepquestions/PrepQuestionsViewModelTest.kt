package com.tailormyresume.feature.tailor.impl.prepquestions

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.prep.GeneratePrepQuestionsUseCase
import com.tailormyresume.core.domain.prep.PrepQuestionGenerator
import com.tailormyresume.core.domain.prep.PrepQuestionKind
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.ReportedItemKind
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import com.tailormyresume.core.testing.connectivity.TestConnectivityMonitor
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.canonicalProfileWithoutEntries
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestContentReportRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.api.navigation.PrepQuestionsNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PrepQuestionsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val connectivity = TestConnectivityMonitor()
    private val reports = TestContentReportRepository()
    private val clock = TestClock()

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
        assertThat(strengths.map { card -> card.fact?.factId }).containsExactly("P-03-b1", "P-03-b1")
    }

    @Test
    fun gapQuestion_carriesNoBackingFactAndSaysSo() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val gaps = viewModel.uiState.value.groups.single { group -> group.kind == PrepQuestionKind.GAP }
        assertThat(gaps.cards.map { card -> card.fact }).containsExactly(null, null)
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
        assertThat(state.stage).isEqualTo(PrepQuestionsStage.READY)
        assertThat(state.isOffline).isTrue()
        assertThat(state.totalCount).isEqualTo(6)
    }

    @Test
    fun connectivityLoss_marksTheSavedListOffline() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        assertThat(viewModel.uiState.value.isOffline).isFalse()

        connectivity.setOnline(false)

        assertThat(viewModel.uiState.value.isOffline).isTrue()
        assertThat(viewModel.uiState.value.totalCount).isEqualTo(6)
    }

    @Test
    fun readyList_numbersTheFactQuestionsAndKeepsGapsApart() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.factCards.map { it.ordinal }).containsExactly(1, 2, 3, 4).inOrder()
        assertThat(state.questionCount).isEqualTo(4)
        assertThat(state.gapCards).hasSize(2)
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
    fun reportInaccurate_savesTheReportAndThanksThePerson() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        val id = viewModel.uiState.value.groups.first().cards.first().id

        viewModel.onAction(PrepQuestionsAction.ReportInaccurate(id))

        assertThat(viewModel.uiState.value.message).isEqualTo(PrepQuestionsMessage.REPORTED)
        val saved = reports.observeReports(APPLICATION_ID).first().single()
        assertThat(saved.itemKind).isEqualTo(ReportedItemKind.PREP_QUESTION)
        assertThat(saved.itemId).isEqualTo(id)
        assertThat(viewModel.uiState.value.reportedIds).containsExactly(id)
    }

    @Test
    fun reportedQuestion_staysReportedWhenTheScreenOpensAgain() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        val id = viewModel.uiState.value.groups.first().cards.first().id
        viewModel.onAction(PrepQuestionsAction.ReportInaccurate(id))

        val reopened = newViewModel()
        reopened.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(reopened.uiState.value.reportedIds).containsExactly(id)
    }

    @Test
    fun reportInaccurate_onAnUnknownQuestion_doesNothing() = runTest {
        given()
        viewModel.onEnter(PrepQuestionsNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        viewModel.onAction(PrepQuestionsAction.ReportInaccurate("not-a-real-id"))

        assertThat(viewModel.uiState.value.message).isNull()
        assertThat(reports.observeReports(APPLICATION_ID).first()).isEmpty()
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
        connectivityMonitor = connectivity,
        contentReportRepository = reports,
        clock = clock,
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
}

private const val APPLICATION_ID = "application-northwind-1"
