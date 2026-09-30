package com.hirehop.feature.onboarding.impl.pastejd

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PasteJobDescriptionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PasteJobDescriptionViewModel

    @Before
    fun setup() {
        viewModel = PasteJobDescriptionViewModel()
    }

    @Test
    fun onEnter_default_startsEmptyWithTheActionOff() {
        val state = viewModel.uiState.value
        assertThat(state.text).isEmpty()
        assertThat(state.wordCount).isEqualTo(0)
        assertThat(state.canAnalyse).isFalse()
        assertThat(state.canClear).isFalse()
    }

    @Test
    fun onEnter_loading_namesTheRealStep() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.LOADING))

        assertThat(viewModel.uiState.first().isLoading).isTrue()
    }

    @Test
    fun onEnter_empty_saysTheShareHadNoText() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.EMPTY))

        val state = viewModel.uiState.first()
        assertThat(state.text).isEmpty()
        assertThat(state.message).isEqualTo(PasteJobDescriptionMessage.NOTHING_TO_READ)
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onEnter_offline_flagsOfflineAndKeepsTheTextEditable() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.OFFLINE))

        val state = viewModel.uiState.first()
        assertThat(state.isOffline).isTrue()
        assertThat(state.message).isNull()
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onEnter_error_reportsAFailedPaste() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.ERROR))

        assertThat(viewModel.uiState.first().message)
            .isEqualTo(PasteJobDescriptionMessage.PASTE_FAILED)
    }

    @Test
    fun onAction_retry_clearsTheFailedPaste() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.ERROR))

        viewModel.onAction(PasteJobDescriptionAction.RetryTapped)

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun onEnter_partial_saysOnlyPartOfTheShareCameThrough() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.PARTIAL))

        assertThat(viewModel.uiState.first().message)
            .isEqualTo(PasteJobDescriptionMessage.PASTE_PARTIAL)
    }

    @Test
    fun onEnter_success_readsAsAReadyScreenWithNoMessage() = runTest {
        viewModel.onEnter(
            PasteJobDescriptionNavKey(scenario = DebugScenario.SUCCESS),
            sharedText = SAMPLE_JD,
        )

        val state = viewModel.uiState.first()
        assertThat(state.message).isNull()
        assertThat(state.arrival).isEqualTo(PasteJobDescriptionArrival.SHARED_IN)
        assertThat(state.canAnalyse).isTrue()
    }

    @Test
    fun onEnter_withSharedText_prefillsAndMarksTheSharedArrival() = runTest {
        viewModel.onEnter(
            PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT),
            sharedText = SAMPLE_JD,
        )

        val state = viewModel.uiState.first()
        assertThat(state.text).isEqualTo(SAMPLE_JD)
        assertThat(state.arrival).isEqualTo(PasteJobDescriptionArrival.SHARED_IN)
        assertThat(state.canAnalyse).isTrue()
    }

    @Test
    fun onEnter_withOnlyWhitespace_sharedText_staysEmpty() = runTest {
        viewModel.onEnter(
            PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT),
            sharedText = "   \n  ",
        )

        val state = viewModel.uiState.first()
        assertThat(state.text).isEmpty()
        assertThat(state.arrival).isEqualTo(PasteJobDescriptionArrival.TYPED)
    }

    @Test
    fun onEnter_whenCalledTwice_keepsTheFirstState() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.OFFLINE))
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun onAction_textChanged_countsWordsAsYouType() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("Associate Analyst"))
        assertThat(viewModel.uiState.value.wordCount).isEqualTo(2)

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("Associate  Analyst\n"))
        assertThat(viewModel.uiState.value.wordCount).isEqualTo(2)
    }

    @Test
    fun onAction_textChanged_keepsTheActionOffUntilThereIsSomethingToRead() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("Associate"))
        assertThat(viewModel.uiState.value.canAnalyse).isFalse()
        assertThat(viewModel.uiState.value.problem)
            .isEqualTo(PasteJobDescriptionProblem.TOO_SHORT)

        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        assertThat(viewModel.uiState.value.canAnalyse).isTrue()
    }

    @Test
    fun onAction_textChanged_keepsTheActionOffForABareLink() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("https://jobs.example.com/role/1"))

        val state = viewModel.uiState.value
        assertThat(state.problem).isEqualTo(PasteJobDescriptionProblem.LINK_ONLY)
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onAction_textChanged_keepsTheActionOffWhenTheTextRunsOverTheCap() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged("word ".repeat(PASTE_JD_MAX_CHARACTERS)))

        val state = viewModel.uiState.value
        assertThat(state.problem).isEqualTo(PasteJobDescriptionProblem.TOO_LONG)
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onAction_textChanged_clearsAnEarlierMessage() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.ERROR))

        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun onAction_clear_emptiesTheDraftWhenThereIsSomethingToClear() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged("Northwind GCC"))
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged("Associate Analyst"))

        viewModel.onAction(PasteJobDescriptionAction.ClearTapped)

        val state = viewModel.uiState.value
        assertThat(state.text).isEmpty()
        assertThat(state.company).isEmpty()
        assertThat(state.role).isEmpty()
        assertThat(state.wordCount).isEqualTo(0)
        assertThat(state.canClear).isFalse()
        assertThat(state.canAnalyse).isFalse()
    }

    @Test
    fun onAction_clear_doesNothingWhenTheDraftIsAlreadyEmpty() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.EMPTY))

        viewModel.onAction(PasteJobDescriptionAction.ClearTapped)

        assertThat(viewModel.uiState.value.message)
            .isEqualTo(PasteJobDescriptionMessage.NOTHING_TO_READ)
    }

    @Test
    fun onAction_analyse_handsOverTheTrimmedDraft() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged("\n$SAMPLE_JD\n"))
        viewModel.onAction(PasteJobDescriptionAction.CompanyChanged(" Northwind GCC "))
        viewModel.onAction(PasteJobDescriptionAction.RoleChanged(" Associate Analyst "))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(viewModel.uiState.value.analysisRequest).isEqualTo(
            PasteJobDescriptionHandoff(
                text = SAMPLE_JD,
                company = "Northwind GCC",
                role = "Associate Analyst",
            ),
        )
    }

    @Test
    fun onAction_analyse_doesNothingWhileTheTextIsTooShort() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged("Associate Analyst role"))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(viewModel.uiState.value.analysisRequest).isNull()
    }

    @Test
    fun onAction_analyse_doesNothingWhileLoading() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.LOADING))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))

        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        assertThat(viewModel.uiState.value.analysisRequest).isNull()
    }

    @Test
    fun onAction_analysisRequestConsumed_clearsTheRequest() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(PasteJobDescriptionAction.TextChanged(SAMPLE_JD))
        viewModel.onAction(PasteJobDescriptionAction.AnalyseTapped)

        viewModel.onAction(PasteJobDescriptionAction.AnalysisRequestConsumed)

        assertThat(viewModel.uiState.value.analysisRequest).isNull()
    }

    @Test
    fun onAction_dismissMessage_clearsTheMessage() = runTest {
        viewModel.onEnter(PasteJobDescriptionNavKey(scenario = DebugScenario.ERROR))

        viewModel.onAction(PasteJobDescriptionAction.DismissMessageTapped)

        assertThat(viewModel.uiState.value.message).isNull()
    }

    @Test
    fun pasteJdWordCount_ignoresRunsOfWhitespace() {
        assertThat(pasteJdWordCount("")).isEqualTo(0)
        assertThat(pasteJdWordCount("   \n\t ")).isEqualTo(0)
        assertThat(pasteJdWordCount("one two   three\nfour")).isEqualTo(4)
    }

    @Test
    fun pasteJdProblem_isNothingForAnEmptyDraft() {
        assertThat(pasteJdProblem("")).isNull()
        assertThat(pasteJdProblem("   ")).isNull()
    }

    @Test
    fun pasteJdProblem_flagsAJdInsideALongSentenceAsRead() {
        val longSentence = "SQL and Power BI are required for this role in Bengaluru across three teams"

        assertThat(pasteJdProblem(longSentence)).isNull()
    }

    private companion object {
        val SAMPLE_JD: String = "Associate Analyst, Business Intelligence at Northwind Global " +
            "Capability Centre, Bengaluru. You will build weekly reports in SQL and Advanced " +
            "Excel, and model dashboards in Power BI or Tableau. The team works in Agile with " +
            "JIRA and reports to stakeholders every Friday morning."
    }
}
