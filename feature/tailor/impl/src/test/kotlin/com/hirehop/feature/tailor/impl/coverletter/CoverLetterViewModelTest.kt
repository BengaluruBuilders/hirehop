package com.hirehop.feature.tailor.impl.coverletter

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.coverletter.CoverLetterComposer
import com.hirehop.core.domain.coverletter.GenerateCoverLetterUseCase
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.canonicalProfileWithoutEntries
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.tailor.api.navigation.CoverLetterNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CoverLetterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()

    private lateinit var viewModel: CoverLetterViewModel

    @Before
    fun setup() {
        viewModel = CoverLetterViewModel(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            generateCoverLetter = GenerateCoverLetterUseCase(),
        )
    }

    @Test
    fun loadingScenario_staysOnGenerating() {
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.LOADING))

        assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.GENERATING)
        assertThat(viewModel.uiState.value.paragraphs).isEmpty()
    }

    @Test
    fun errorScenario_reportsAFailureWithoutAskingTheDomain() {
        applicationRepository.sendApplications(emptyList())
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.ERROR)
    }

    @Test
    fun defaultScenario_rendersTheLetterTheDomainComposed() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(CoverLetterStage.READY)
        assertThat(state.jobTitle).isEqualTo("Associate Android Engineer")
        assertThat(state.jobCompany).isEqualTo("Northwind GCC")
        assertThat(state.paragraphs).hasSize(4)
        assertThat(state.paragraphs.map { paragraph -> paragraph.basis }).containsExactly(
            CoverLetterBasis.PLAIN,
            CoverLetterBasis.JOB_DESCRIPTION,
            CoverLetterBasis.CONFIRMED_FACT,
            CoverLetterBasis.PROFILE_NAME,
        ).inOrder()
    }

    @Test
    fun readyLetter_quotesTheConfirmedFactAndShowsItsId() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val evidence = viewModel.uiState.value.paragraphs.single { paragraph ->
            paragraph.basis == CoverLetterBasis.CONFIRMED_FACT
        }
        assertThat(evidence.facts.map { fact -> fact.factId }).contains("P-03-b1")
        assertThat(evidence.facts.single { fact -> fact.factId == "P-03-b1" }.entryTitle)
            .isEqualTo("Skills")
        val quoted = evidence.sentences.single { sentence -> sentence.factId == "P-03-b1" }
        assertThat(quoted.text).contains("Reviewed pull requests from two other interns in Git")
    }

    @Test
    fun readyLetter_showsEverySentenceItRenders() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.paragraphs.flatMap { paragraph -> paragraph.sentences }.map { sentence -> sentence.text })
            .contains("Dear hiring team,")
        assertThat(state.letterText).contains(CoverLetterComposer.GREETING)
    }

    @Test
    fun readyLetter_neverNamesAnEmployerContact() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val letter = viewModel.uiState.value.letterText.lowercase()
        assertThat(letter).doesNotContain("dear hiring team at")
        assertThat(letter).doesNotContain("mr.")
        assertThat(letter).doesNotContain("ms.")
        assertThat(letter).doesNotContain("mrs.")
    }

    @Test
    fun readyLetter_carriesNoScaleOrTimeSavingClaim() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val letter = viewModel.uiState.value.letterText.lowercase()
        assertThat(letter).doesNotContain("ats")
        assertThat(letter).doesNotContain("guarantee")
        assertThat(letter).doesNotContain("hours saved")
        assertThat(letter).doesNotContain("shortlist")
    }

    @Test
    fun wordCount_countsOnlyWhatIsRendered() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.wordCount).isEqualTo(70)
        assertThat(isWithinWordTarget(state.wordCount)).isFalse()
    }

    @Test
    fun noMatchingEvidence_surfacesTheComposerConstantAsAnExplanation() = runTest {
        given(gapAllGaps())
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.EMPTY))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(CoverLetterStage.NO_MATCHING_EVIDENCE)
        val explanation = state.paragraphs.single { paragraph ->
            paragraph.basis == CoverLetterBasis.NO_MATCHING_EVIDENCE
        }
        assertThat(explanation.text).isEqualTo(CoverLetterComposer.NO_MATCHING_EVIDENCE)
        assertThat(state.letterText).contains(CoverLetterComposer.NO_MATCHING_EVIDENCE)
        assertThat(state.paragraphs.any { paragraph -> paragraph.facts.isNotEmpty() }).isFalse()
    }

    @Test
    fun noMatchingEvidence_doesNotUseTheErrorColourOrWording() = runTest {
        given(gapAllGaps())
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.EMPTY))

        assertThat(viewModel.uiState.value.stage).isNotEqualTo(CoverLetterStage.ERROR)
    }

    @Test
    fun emptyProfile_asksForAFactInsteadOfWritingALetter() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalProfileWithoutEntries)
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(CoverLetterStage.EMPTY_PROFILE)
        assertThat(state.paragraphs).isEmpty()
        assertThat(state.letterText).isEmpty()
    }

    @Test
    fun missingProfile_asksForAFactInsteadOfWritingALetter() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(null)
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.EMPTY_PROFILE)
    }

    @Test
    fun offlineScenario_keepsTheLetterReadableUnderTheBanner() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.OFFLINE))

        val state = viewModel.uiState.value
        assertThat(state.isOffline).isTrue()
        assertThat(state.stage).isEqualTo(CoverLetterStage.OFFLINE)
        assertThat(state.paragraphs).hasSize(4)
    }

    @Test
    fun missingApplication_isAFailure() = runTest {
        profileRepository.sendProfile(canonicalCandidateProfile)
        applicationRepository.sendApplications(emptyList())
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.ERROR)
    }

    @Test
    fun applicationWithoutAnalysis_writesTheHonestNoEvidenceLetter() = runTest {
        profileRepository.sendProfile(canonicalCandidateProfile)
        applicationRepository.sendApplications(
            listOf(canonicalApplication.copy(gapAnalysis = null)),
        )
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(CoverLetterStage.NO_MATCHING_EVIDENCE)
        assertThat(state.letterText).contains(CoverLetterComposer.NO_MATCHING_EVIDENCE)
    }

    @Test
    fun everyScenarioMapsToAKnownStage() = runTest {
        for (scenario in DebugScenario.entries) {
            given()
            val fresh = CoverLetterViewModel(
                applicationRepository = applicationRepository,
                profileRepository = profileRepository,
                generateCoverLetter = GenerateCoverLetterUseCase(),
            )
            fresh.onEnter(CoverLetterNavKey(APPLICATION_ID, scenario))

            val state = fresh.uiState.value
            assertThat(state.stage).isIn(
                listOf(
                    CoverLetterStage.GENERATING,
                    CoverLetterStage.READY,
                    CoverLetterStage.NO_MATCHING_EVIDENCE,
                    CoverLetterStage.EMPTY_PROFILE,
                    CoverLetterStage.OFFLINE,
                    CoverLetterStage.ERROR,
                ),
            )
        }
    }

    @Test
    fun successAndProvenanceScenarios_renderTheSameReadyLetter() = runTest {
        given()
        val expected = coverLetterStageFor(DebugScenario.DEFAULT)
        for (scenario in listOf(DebugScenario.SUCCESS, DebugScenario.PARTIAL, DebugScenario.USER_STATED)) {
            given()
            val fresh = CoverLetterViewModel(
                applicationRepository = applicationRepository,
                profileRepository = profileRepository,
                generateCoverLetter = GenerateCoverLetterUseCase(),
            )
            fresh.onEnter(CoverLetterNavKey(APPLICATION_ID, scenario))

            assertThat(fresh.uiState.value.stage).isEqualTo(expected)
        }
    }

    @Test
    fun onEnter_ignoresASecondKey() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        applicationRepository.sendApplications(emptyList())

        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.READY)
    }

    @Test
    fun editParagraph_keepsTheFactCitationAndMarksTheTextAsYours() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        val ordinal = viewModel.uiState.value.paragraphs
            .single { paragraph -> paragraph.basis == CoverLetterBasis.CONFIRMED_FACT }
            .ordinal

        viewModel.onAction(CoverLetterAction.BeginEdit(ordinal))
        assertThat(viewModel.uiState.value.isEditing).isTrue()
        assertThat(viewModel.uiState.value.editingText).isNotEmpty()

        viewModel.onAction(CoverLetterAction.EditTextChanged("I wrote this line myself."))
        viewModel.onAction(CoverLetterAction.SaveEdit)

        val edited = viewModel.uiState.value.paragraphs.single { paragraph -> paragraph.ordinal == ordinal }
        assertThat(edited.text).isEqualTo("I wrote this line myself.")
        assertThat(edited.isUserEdited).isTrue()
        assertThat(edited.facts.map { fact -> fact.factId }).contains("P-03-b1")
        assertThat(edited.sentences.all { sentence -> sentence.factId == null }).isTrue()
        assertThat(viewModel.uiState.value.message).isEqualTo(CoverLetterMessage.SAVED)
        assertThat(viewModel.uiState.value.isEditing).isFalse()
    }

    @Test
    fun cancelEdit_keepsTheComposedParagraph() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        val before = viewModel.uiState.value.letterText

        viewModel.onAction(CoverLetterAction.BeginEdit(2))
        viewModel.onAction(CoverLetterAction.EditTextChanged("throw this away"))
        viewModel.onAction(CoverLetterAction.CancelEdit)

        assertThat(viewModel.uiState.value.letterText).isEqualTo(before)
        assertThat(viewModel.uiState.value.isEditing).isFalse()
    }

    @Test
    fun saveEdit_withNothingTyped_keepsTheComposedParagraph() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        val before = viewModel.uiState.value.letterText

        viewModel.onAction(CoverLetterAction.BeginEdit(2))
        viewModel.onAction(CoverLetterAction.EditTextChanged("   "))
        viewModel.onAction(CoverLetterAction.SaveEdit)

        assertThat(viewModel.uiState.value.letterText).isEqualTo(before)
    }

    @Test
    fun beginEdit_onAnUnknownParagraph_doesNothing() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        viewModel.onAction(CoverLetterAction.BeginEdit(99))

        assertThat(viewModel.uiState.value.isEditing).isFalse()
    }

    @Test
    fun copyLetter_handsBackExactlyWhatIsOnScreen() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        val letter = viewModel.uiState.value.letterText

        viewModel.onAction(CoverLetterAction.CopyLetter(letter))

        assertThat(viewModel.uiState.value.copiedText).isEqualTo(letter)
        assertThat(viewModel.uiState.value.message).isEqualTo(CoverLetterMessage.COPIED)
    }

    @Test
    fun copyLetter_afterAnEdit_copiesTheEditedLetter() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        viewModel.onAction(CoverLetterAction.BeginEdit(2))
        viewModel.onAction(CoverLetterAction.EditTextChanged("My own opening line."))
        viewModel.onAction(CoverLetterAction.SaveEdit)

        viewModel.onAction(CoverLetterAction.CopyLetter(viewModel.uiState.value.letterText))

        assertThat(viewModel.uiState.value.copiedText).contains("My own opening line.")
        assertThat(viewModel.uiState.value.copiedText).doesNotContain("I am applying for")
    }

    @Test
    fun reportInaccurate_doesNotClaimTheReportWasSent() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        viewModel.onAction(CoverLetterAction.ReportInaccurate(1))

        assertThat(viewModel.uiState.value.message).isEqualTo(CoverLetterMessage.REPORT_UNAVAILABLE)
    }

    @Test
    fun reportInaccurate_onAnUnknownParagraph_doesNothing() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        viewModel.onAction(CoverLetterAction.ReportInaccurate(99))

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun dismissMessage_clearsTheNote() = runTest {
        given()
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))
        viewModel.onAction(CoverLetterAction.CopyLetter("x"))

        viewModel.onAction(CoverLetterAction.DismissMessage)

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun retry_reloadsTheLetter() = runTest {
        profileRepository.sendProfile(canonicalProfileWithoutEntries)
        applicationRepository.sendApplications(listOf(canonicalApplication))
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.ERROR))
        assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.ERROR)

        profileRepository.sendProfile(canonicalCandidateProfile)
        viewModel.onAction(CoverLetterAction.Retry)

        assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.READY)
    }

    @Test
    fun unconfirmedFactsAreNeverQuoted() = runTest {
        val withoutConfirmed = canonicalCandidateProfile.copy(
            entries = canonicalCandidateProfile.entries.map { entry -> entry.copy(isConfirmed = false) },
        )
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(withoutConfirmed)
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.EMPTY_PROFILE)
    }

    @Test
    fun userStatedFactsAreNotQuotedAsConfirmed() = runTest {
        val onlyUserStated = CandidateProfile(
            fullName = canonicalCandidateProfile.fullName,
            email = canonicalCandidateProfile.email,
            phone = canonicalCandidateProfile.phone,
            headline = canonicalCandidateProfile.headline,
            skills = canonicalCandidateProfile.skills,
            entries = canonicalCandidateProfile.entries
                .filter { entry -> entry.source.name == "USER_STATED" }
                .map { entry -> entry.copy(isConfirmed = false) },
        )
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(onlyUserStated)
        viewModel.onEnter(CoverLetterNavKey(APPLICATION_ID, DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.stage).isEqualTo(CoverLetterStage.EMPTY_PROFILE)
    }

    private fun given(gap: GapAnalysis = requireNotNull(canonicalApplication.gapAnalysis)) {
        applicationRepository.sendApplications(
            listOf(canonicalApplication.copy(gapAnalysis = gap)),
        )
        profileRepository.sendProfile(canonicalCandidateProfile)
    }

    private fun gapAllGaps(): GapAnalysis {
        val gap = requireNotNull(canonicalApplication.gapAnalysis)
        return gap.copy(
            matches = gap.matches.map { match -> match.copy(status = MatchStatus.GAP, evidenceIds = emptyList()) },
        )
    }
}

private const val APPLICATION_ID = "application-northwind-1"
