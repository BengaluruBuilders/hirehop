package com.hirehop.feature.profile.impl.guidedform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.domain.fact.FactDraft
import com.hirehop.core.domain.fact.FactDraftErrorReason
import com.hirehop.core.domain.fact.FactDraftValidator
import com.hirehop.core.domain.fact.FactField
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry
import com.hirehop.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.hirehop.feature.profile.impl.ContactInput
import com.hirehop.feature.profile.impl.FactWriteResult
import com.hirehop.feature.profile.impl.ProfileExitResolver
import com.hirehop.feature.profile.impl.UserFactWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GuidedFormViewModel @Inject internal constructor(
    private val factWriter: UserFactWriter,
    private val exitResolver: ProfileExitResolver,
    private val connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

    private val mutableState = MutableStateFlow(GuidedFormUiState())

    private var hasEntered = false
    private var forcedOffline = false

    val uiState: StateFlow<GuidedFormUiState> = mutableState.asStateFlow()

    fun onEnter(key: GuidedProfileFormNavKey) {
        if (hasEntered) return
        hasEntered = true
        forcedOffline = key.scenario == DebugScenario.OFFLINE
        mutableState.value = guidedFormStateFor(
            scenario = key.scenario,
            startStep = key.startStep,
            resumedFromScan = key.resumedFromScan,
        )
        connectivityMonitor.isOnline
            .onEach { online -> mutableState.update { it.copy(isOffline = forcedOffline || !online) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: GuidedFormAction) {
        when (action) {
            is GuidedFormAction.ValueChanged -> onValueChanged(action.field, action.value)
            GuidedFormAction.AddSkill -> onAddSkill()
            is GuidedFormAction.RemoveSkill -> onRemoveSkill(action.skill)
            GuidedFormAction.StartForm -> mutableState.update { it.copy(showIntro = false) }
            GuidedFormAction.Next -> onNext()
            GuidedFormAction.Back -> onBack()
            GuidedFormAction.SaveAndFinishLater -> onSaveAndFinishLater()
            GuidedFormAction.FinishSaved -> onFinish()
            GuidedFormAction.GoToProjects -> onGoToProjects()
            GuidedFormAction.NavigationConsumed -> mutableState.update { it.copy(navigation = null) }
            GuidedFormAction.DismissMessage -> mutableState.update { it.copy(message = null) }
        }
    }

    private fun onValueChanged(field: GuidedField, value: String) {
        mutableState.update { state ->
            state.copy(
                values = state.values + (field to value),
                fieldProblems = state.fieldProblems - field,
                message = null,
            )
        }
    }

    private fun onAddSkill() {
        mutableState.update { state -> state.withPendingSkill() }
    }

    private fun onRemoveSkill(skill: String) {
        mutableState.update { state -> state.copy(skills = state.skills.filterNot { it == skill }) }
    }

    private fun onBack() {
        mutableState.update { state ->
            if (state.isFirstStep) {
                state
            } else {
                state.copy(
                    stepIndex = state.stepIndex - 1,
                    fieldProblems = emptyMap(),
                    filedEntries = emptyList(),
                    message = null,
                )
            }
        }
    }

    private fun onNext() {
        val state = mutableState.value
        if (state.isSaving) return
        if (state.isLastStep) {
            onGoToProjects()
            return
        }
        persistStep(state) { written ->
            mutableState.update { current ->
                current.copy(
                    stepIndex = (current.stepIndex + 1).coerceAtMost(GUIDED_STEPS.lastIndex),
                    filedEntries = written,
                )
            }
        }
    }

    private fun onGoToProjects() {
        mutableState.update { it.copy(navigation = GuidedNavigation.Evidence(EVIDENCE_HANDOFF_CATEGORY)) }
    }

    private fun onSaveAndFinishLater() {
        val state = mutableState.value
        if (state.isSaving) return
        if (state.showIntro) {
            mutableState.update { it.finishedLater() }
            return
        }
        persistStep(state) { mutableState.update { it.finishedLater() } }
    }

    private fun onFinish() {
        viewModelScope.launch {
            val exit = exitResolver.resolve()
            mutableState.update { it.copy(navigation = GuidedNavigation.Exit(exit)) }
        }
    }

    private fun persistStep(
        initial: GuidedFormUiState,
        onDone: (written: List<ProfileEntry>) -> Unit,
    ) {
        val state = initial.withPendingSkill()
        val step = state.step
        val drafts = draftsFor(step, state.values)
        val problems = problemsOf(step, drafts)
        if (problems.isNotEmpty()) {
            mutableState.value = state.copy(fieldProblems = problems)
            return
        }
        mutableState.value = state.copy(isSaving = true, message = null, fieldProblems = emptyMap())
        viewModelScope.launch {
            val result = runCatching {
                factWriter.write(
                    drafts = drafts,
                    replacing = state.stepEntryIds[step].orEmpty().toSet(),
                    contact = if (step == GuidedStep.CONTACT) state.contactInput() else ContactInput(),
                    skills = if (step == GuidedStep.SKILLS) state.skills else emptyList(),
                )
            }
            val outcome = result.getOrNull()
            if (outcome is FactWriteResult.Written) {
                mutableState.update { current ->
                    current.copy(
                        isSaving = false,
                        completedSteps = current.completedSteps + step,
                        stepEntryIds = current.stepEntryIds + (step to outcome.entries.map { it.id }),
                    )
                }
                onDone(outcome.entries)
            } else if (outcome == FactWriteResult.NothingToWrite) {
                mutableState.update { it.copy(isSaving = false) }
                onDone(emptyList())
            } else {
                mutableState.update { it.copy(isSaving = false, message = GuidedMessage.SAVE_FAILED) }
            }
        }
    }

    private fun GuidedFormUiState.withPendingSkill(): GuidedFormUiState {
        val typed = values[GuidedField.SKILL].orEmpty().trim()
        if (typed.isEmpty()) return this
        val known = skills.any { it.equals(typed, ignoreCase = true) }
        return copy(
            skills = if (known) skills else skills + typed,
            values = values + (GuidedField.SKILL to ""),
        )
    }

    private fun GuidedFormUiState.finishedLater(): GuidedFormUiState = copy(
        saved = GuidedSaved(
            completedSteps = completedSteps.size,
            totalSteps = GUIDED_STEPS.size,
            entryIds = createdEntryIds,
        ),
    )

    private fun GuidedFormUiState.contactInput() = ContactInput(
        fullName = values[GuidedField.FULL_NAME].orEmpty(),
        email = values[GuidedField.EMAIL].orEmpty(),
        phone = values[GuidedField.PHONE].orEmpty(),
    )

    private fun draftsFor(step: GuidedStep, values: Map<GuidedField, String>): List<FactDraft> {
        if (step != GuidedStep.EDUCATION) return emptyList()
        val degree = FactDraft(
            category = EntryCategory.EDUCATION,
            title = values[GuidedField.COURSE].orEmpty().trim(),
            organization = values[GuidedField.COLLEGE].orEmpty().trim(),
            startDate = "",
            endDate = values[GuidedField.EDUCATION_END].orEmpty().trim(),
            detail = "",
        )
        val coursework = FactDraft(
            category = EntryCategory.EDUCATION,
            title = values[GuidedField.COURSEWORK].orEmpty().trim(),
            organization = "",
            startDate = "",
            endDate = "",
            detail = "",
        )
        return listOf(degree, coursework).filter { it.title.isNotEmpty() || it.organization.isNotEmpty() || it.endDate.isNotEmpty() }
    }

    private fun problemsOf(step: GuidedStep, drafts: List<FactDraft>): Map<GuidedField, GuidedFieldProblem> =
        drafts.flatMap { FactDraftValidator.validate(it) }
            .mapNotNull { error -> error.field.toGuidedField(step)?.let { it to error.reason.toProblem() } }
            .toMap()

    private fun FactField.toGuidedField(step: GuidedStep): GuidedField? =
        if (step != GuidedStep.EDUCATION) {
            null
        } else {
            when (this) {
                FactField.TITLE -> GuidedField.COURSE
                FactField.ORGANIZATION -> GuidedField.COLLEGE
                FactField.END_DATE -> GuidedField.EDUCATION_END
                FactField.START_DATE, FactField.DETAIL -> null
            }
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
