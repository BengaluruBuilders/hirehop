package com.tailormyresume.feature.profile.impl.guidedform

import android.annotation.SuppressLint
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.domain.fact.FactDraftErrorReason
import com.tailormyresume.core.domain.fact.FactDraftValidator
import com.tailormyresume.core.domain.fact.FactField
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.tailormyresume.feature.profile.impl.ContactInput
import com.tailormyresume.feature.profile.impl.FactWriteResult
import com.tailormyresume.feature.profile.impl.ProfileExitResolver
import com.tailormyresume.feature.profile.impl.UserFactWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("VisibleForTests")
@HiltViewModel
class GuidedFormViewModel @Inject internal constructor(
    private val factWriter: UserFactWriter,
    private val exitResolver: ProfileExitResolver,
    private val connectivityMonitor: ConnectivityMonitor,
    private val savedState: SavedStateHandle = SavedStateHandle(),
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
        restoreTypedInput()
        mutableState
            .map { it.toTyped() }
            .distinctUntilChanged()
            .onEach { typed -> savedState[TYPED_KEY] = typed }
            .launchIn(viewModelScope)
        connectivityMonitor.isOnline
            .onEach { online -> mutableState.update { it.copy(isOffline = forcedOffline || !online) } }
            .launchIn(viewModelScope)
        factWriter.observeEntries()
            .onEach { entries -> mutableState.update { it.syncedWith(entries) } }
            .launchIn(viewModelScope)
    }

    private fun restoreTypedInput() {
        val typed = savedState.get<Typed>(TYPED_KEY) ?: return
        mutableState.update { state ->
            state.copy(
                stepIndex = typed.stepIndex.coerceIn(0, GUIDED_STEPS.lastIndex),
                showIntro = typed.showIntro,
                skills = typed.skills,
                values = typed.values.mapNotNull { (name, value) -> GuidedField.entries.find { it.name == name }?.let { it to value } }.toMap(),
                completedSteps = typed.completedSteps.mapNotNull(::stepNamed).toSet(),
                stepEntryIds = typed.stepEntryIds.mapNotNull { (name, ids) -> stepNamed(name)?.let { it to ids.toList() } }.toMap(),
            )
        }
    }

    private data class Typed(
        val stepIndex: Int,
        val showIntro: Boolean,
        val skills: List<String>,
        val values: HashMap<String, String>,
        val completedSteps: ArrayList<String>,
        val stepEntryIds: HashMap<String, ArrayList<String>>,
    ) : java.io.Serializable

    private fun GuidedFormUiState.toTyped() = Typed(
        stepIndex = stepIndex,
        showIntro = showIntro,
        skills = skills,
        values = HashMap(values.mapKeys { (field, _) -> field.name }),
        completedSteps = ArrayList(completedSteps.map { it.name }),
        stepEntryIds = HashMap(stepEntryIds.entries.associate { (step, ids) -> step.name to ArrayList(ids) }),
    )

    private fun stepNamed(name: String): GuidedStep? = GuidedStep.entries.find { it.name == name }

    fun onAction(action: GuidedFormAction) {
        when (action) {
            is GuidedFormAction.ValueChanged -> onValueChanged(action.field, action.value)
            GuidedFormAction.AddSkill -> onAddSkill()
            is GuidedFormAction.RemoveSkill -> onRemoveSkill(action.skill)
            is GuidedFormAction.ChooseExperience -> mutableState.update { it.copy(experienceChoice = action.choice) }
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
                ).prefilledFromEntries(guidedStepAt(state.stepIndex - 1))
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
        val fieldedDrafts = draftsFor(step, state.values)
        val drafts = fieldedDrafts.map { it.second }
        val problems = problemsOf(step, drafts) + contactProblemsOf(step, state.values)
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
                        stepEntryFields = current.stepEntryFields + (step to fieldedDrafts.map { it.first }),
                        entries = current.entries?.let { known -> known.filterNot { old -> outcome.entries.any { it.id == old.id } } + outcome.entries },
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

    private fun GuidedFormUiState.syncedWith(entries: List<ProfileEntry>): GuidedFormUiState {
        return copy(entries = entries)
    }

    private fun GuidedFormUiState.prefilledFromEntries(target: GuidedStep): GuidedFormUiState {
        val ids = stepEntryIds[target].orEmpty()
        val fields = stepEntryFields[target].orEmpty()
        val byId = entries.orEmpty().associateBy { it.id }
        val restored = ids.zip(fields).mapNotNull { (id, field) -> byId[id]?.let { field to it } }
            .flatMap { (field, entry) ->
                if (field == GuidedField.COURSE) {
                    listOf(
                        GuidedField.COURSE to entry.title,
                        GuidedField.COLLEGE to entry.organization,
                        GuidedField.EDUCATION_END to entry.endDate,
                    )
                } else {
                    listOf(field to entry.title)
                }
            }
        return copy(values = values + restored)
    }

    private fun GuidedFormUiState.finishedLater(): GuidedFormUiState = copy(
        saved = GuidedSaved(
            completedSteps = completedSteps.size,
            totalSteps = GUIDED_STEPS.size,
            entryIds = createdEntryIds,
            doneSteps = completedSteps,
        ),
    )

    private fun GuidedFormUiState.contactInput() = ContactInput(
        fullName = values[GuidedField.FULL_NAME].orEmpty(),
        email = values[GuidedField.EMAIL].orEmpty(),
        phone = values[GuidedField.PHONE].orEmpty(),
    )

    private fun draftsFor(step: GuidedStep, values: Map<GuidedField, String>): List<Pair<GuidedField, FactDraft>> {
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
        return listOf(GuidedField.COURSE to degree, GuidedField.COURSEWORK to coursework)
            .filter { (_, it) -> it.title.isNotEmpty() || it.organization.isNotEmpty() || it.endDate.isNotEmpty() }
    }

    private fun contactProblemsOf(step: GuidedStep, values: Map<GuidedField, String>): Map<GuidedField, GuidedFieldProblem> {
        if (step != GuidedStep.CONTACT) return emptyMap()
        val email = values[GuidedField.EMAIL].orEmpty()
        val phone = values[GuidedField.PHONE].orEmpty()
        return buildMap {
            if (email.isNotBlank() && !ContactFieldValidator.isValidEmail(email)) {
                put(GuidedField.EMAIL, GuidedFieldProblem.INVALID_EMAIL)
            }
            if (phone.isNotBlank() && !ContactFieldValidator.isValidPhone(phone)) {
                put(GuidedField.PHONE, GuidedFieldProblem.INVALID_PHONE)
            }
        }
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
        FactDraftErrorReason.INVALID_DATE -> GuidedFieldProblem.INVALID_DATE
    }

    private companion object {
        const val EVIDENCE_HANDOFF_CATEGORY = "projects"
        const val TYPED_KEY = "guidedForm.typed"
    }
}
