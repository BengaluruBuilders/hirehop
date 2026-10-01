package com.hirehop.feature.profile.impl.guidedform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.domain.AddUserStatedFactsUseCase
import com.hirehop.core.domain.fact.AddFactsOutcome
import com.hirehop.core.domain.fact.FactDraft
import com.hirehop.core.domain.fact.FactDraftError
import com.hirehop.core.domain.fact.FactDraftErrorReason
import com.hirehop.core.domain.fact.FactDraftValidator
import com.hirehop.core.domain.fact.FactField
import com.hirehop.core.domain.fact.FactLineRenderer
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry
import com.hirehop.feature.profile.api.navigation.GuidedProfileFormNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuidedFormViewModel @Inject constructor(
    private val addUserStatedFacts: AddUserStatedFactsUseCase,
) : ViewModel() {

    private val mutableState = MutableStateFlow(GuidedFormUiState())

    private var hasEntered = false

    val uiState: StateFlow<GuidedFormUiState> = mutableState.asStateFlow()

    fun onEnter(key: GuidedProfileFormNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = guidedFormStateFor(
            scenario = key.scenario,
            startStep = key.startStep,
            resumedFromScan = key.resumedFromScan,
        )
    }

    fun onAction(action: GuidedFormAction) {
        when (action) {
            is GuidedFormAction.ValueChanged -> onValueChanged(action.field, action.value)
            GuidedFormAction.Next -> onNext()
            GuidedFormAction.Back -> onBack()
            GuidedFormAction.SaveAndFinishLater -> onSaveAndFinishLater()
            GuidedFormAction.ContinueNow -> onContinueNow()
            GuidedFormAction.StartHandoff -> onStartHandoff()
            GuidedFormAction.HandoffConsumed -> onHandoffConsumed()
            GuidedFormAction.DismissMessage -> onDismissMessage()
        }
    }

    private fun onValueChanged(
        field: GuidedField,
        value: String,
    ) {
        mutableState.update { state ->
            state.copy(
                values = state.values + (field to value),
                fieldProblems = state.fieldProblems - field,
                isSaveRejected = false,
                message = null,
            )
        }
    }

    private fun onNext() {
        val state = mutableState.value
        val draft = draftForStep(state.step, state.values)
        if (draft != null) {
            val problems = problemsOf(state.step, FactDraftValidator.validate(draft))
            if (problems.isNotEmpty()) {
                mutableState.value = state.copy(
                    fieldProblems = problems,
                    isSaveRejected = true,
                    message = GuidedMessage.SAVE_REJECTED,
                )
                return
            }
            mutableState.value = state.advanced(
                previews = state.previews.withoutCategory(draft.category) + draft.toPreview(),
            )
            return
        }
        mutableState.value = state.advanced(previews = state.previews)
    }

    private fun onBack() {
        val state = mutableState.value
        if (state.isFirstStep) return
        mutableState.value = state.copy(
            stepIndex = (state.stepIndex - 1).coerceAtLeast(0),
            fieldProblems = emptyMap(),
            isSaveRejected = false,
            message = null,
            saved = null,
        )
    }

    private fun onSaveAndFinishLater() {
        val state = mutableState.value
        if (state.isSaving) return
        val drafts = state.completedSteps.mapNotNull { draftForStep(it, state.values) }
        if (drafts.isEmpty()) {
            mutableState.value = state.finishedSaving(emptyList())
            return
        }
        mutableState.value = state.copy(isSaving = true, message = null)
        viewModelScope.launch {
            when (val outcome = addUserStatedFacts(drafts)) {
                is AddFactsOutcome.Added -> mutableState.update { it.finishedSaving(outcome.entries) }
                is AddFactsOutcome.Rejected -> mutableState.update {
                    it.copy(
                        isSaving = false,
                        isSaveRejected = true,
                        fieldProblems = it.problemsFor(outcome.errors),
                        message = GuidedMessage.SAVE_REJECTED,
                    )
                }

                AddFactsOutcome.NothingToAdd -> mutableState.update { it.finishedSaving(emptyList()) }
            }
        }
    }

    private fun onContinueNow() {
        mutableState.update { it.copy(saved = null, message = null) }
    }

    private fun onStartHandoff() {
        mutableState.update { state ->
            if (state.isLastStep) state.copy(handoff = GuidedHandoff(EVIDENCE_HANDOFF_CATEGORY)) else state
        }
    }

    private fun onHandoffConsumed() {
        mutableState.update { it.copy(handoff = null) }
    }

    private fun onDismissMessage() {
        mutableState.update { it.copy(message = null, isSaveRejected = false) }
    }

    private fun GuidedFormUiState.advanced(previews: List<GuidedFactPreview>): GuidedFormUiState = copy(
        previews = previews,
        stepIndex = (stepIndex + 1).coerceAtMost(GUIDED_STEPS.lastIndex),
        completedSteps = (completedSteps + step).distinct(),
        fieldProblems = emptyMap(),
        isSaveRejected = false,
        message = null,
        saved = null,
    )

    private fun GuidedFormUiState.finishedSaving(entries: List<ProfileEntry>): GuidedFormUiState = copy(
        isSaving = false,
        previews = previews.withEntries(entries),
        saved = GuidedSaved(completedSteps = completedSteps.size, totalSteps = GUIDED_STEPS.size),
        isSaveRejected = false,
        message = if (isOffline) GuidedMessage.OFFLINE_QUEUED else GuidedMessage.SAVED,
    )

    private fun draftForStep(
        step: GuidedStep,
        values: Map<GuidedField, String>,
    ): FactDraft? {
        val category = step.entryCategory() ?: return null
        val title = when (step) {
            GuidedStep.EDUCATION -> values[GuidedField.COURSE].orEmpty()
            GuidedStep.EXPERIENCE -> values[GuidedField.ROLE].orEmpty()
            GuidedStep.CONTACT, GuidedStep.SKILLS -> return null
        }
        val organization = when (step) {
            GuidedStep.EDUCATION -> values[GuidedField.COLLEGE].orEmpty()
            GuidedStep.EXPERIENCE -> values[GuidedField.EMPLOYER].orEmpty()
            GuidedStep.CONTACT, GuidedStep.SKILLS -> ""
        }
        val start = when (step) {
            GuidedStep.EDUCATION -> values[GuidedField.EDUCATION_START].orEmpty()
            GuidedStep.EXPERIENCE -> values[GuidedField.EXPERIENCE_START].orEmpty()
            GuidedStep.CONTACT, GuidedStep.SKILLS -> ""
        }
        val end = when (step) {
            GuidedStep.EDUCATION -> values[GuidedField.EDUCATION_END].orEmpty()
            GuidedStep.EXPERIENCE -> values[GuidedField.EXPERIENCE_END].orEmpty()
            GuidedStep.CONTACT, GuidedStep.SKILLS -> ""
        }
        return FactDraft(
            category = category,
            title = title.trim(),
            organization = organization.trim(),
            startDate = start.trim(),
            endDate = end.trim(),
            detail = "",
        )
    }

    private fun FactDraft.toPreview(): GuidedFactPreview = GuidedFactPreview(
        category = category,
        line = FactLineRenderer.render(this),
    )

    private fun List<GuidedFactPreview>.withoutCategory(category: EntryCategory): List<GuidedFactPreview> =
        filterNot { it.category == category }

    private fun List<GuidedFactPreview>.withEntries(entries: List<ProfileEntry>): List<GuidedFactPreview> =
        map { preview ->
            val entry = entries.firstOrNull { it.category == preview.category } ?: return@map preview
            preview.copy(entry = entry, line = FactLineRenderer.render(entry))
        }

    private fun problemsOf(
        step: GuidedStep,
        errors: List<FactDraftError>,
    ): Map<GuidedField, GuidedFieldProblem> = errors
        .mapNotNull { error -> error.field.toGuidedField(step)?.let { it to error.reason.toProblem() } }
        .toMap()

    private fun GuidedFormUiState.problemsFor(
        errors: List<FactDraftError>,
    ): Map<GuidedField, GuidedFieldProblem> = completedSteps
        .fold(emptyMap<GuidedField, GuidedFieldProblem>()) { found, step ->
            found + problemsOf(step, errors)
        }

    private fun FactField.toGuidedField(step: GuidedStep): GuidedField? = when (step) {
        GuidedStep.EDUCATION -> when (this) {
            FactField.TITLE -> GuidedField.COURSE
            FactField.ORGANIZATION -> GuidedField.COLLEGE
            FactField.START_DATE -> GuidedField.EDUCATION_START
            FactField.END_DATE -> GuidedField.EDUCATION_END
            FactField.DETAIL -> null
        }

        GuidedStep.EXPERIENCE -> when (this) {
            FactField.TITLE -> GuidedField.ROLE
            FactField.ORGANIZATION -> GuidedField.EMPLOYER
            FactField.START_DATE -> GuidedField.EXPERIENCE_START
            FactField.END_DATE -> GuidedField.EXPERIENCE_END
            FactField.DETAIL -> null
        }

        GuidedStep.CONTACT, GuidedStep.SKILLS -> null
    }

    private fun FactDraftErrorReason.toProblem(): GuidedFieldProblem = when (this) {
        FactDraftErrorReason.REQUIRED -> GuidedFieldProblem.REQUIRED
        FactDraftErrorReason.END_BEFORE_START -> GuidedFieldProblem.END_BEFORE_START
        FactDraftErrorReason.TOO_LONG -> GuidedFieldProblem.TOO_LONG
    }

    private companion object {
        const val EVIDENCE_HANDOFF_CATEGORY = "projects"
    }
}
