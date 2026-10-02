package com.hirehop.feature.applications.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.KeywordCoverage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    sessionRepository: SessionRepository,
    paymentGateway: PaymentGateway,
    connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

    private val scenario = MutableStateFlow(DebugScenario.defaultValue)

    private val presentation = MutableStateFlow(ApplicationsPresentation())

    private val applications = MutableStateFlow<List<JobApplication>>(emptyList())

    private val header = combine(
        sessionRepository.observeAccount(),
        paymentGateway.observeEntitlement(),
    ) { account, entitlement ->
        ApplicationsHeader(
            firstName = account?.displayName?.trim()?.substringBefore(' ')?.takeIf { name -> name.isNotEmpty() },
            credits = entitlement.totalCredits,
        )
    }

    private val isOffline = combine(scenario, connectivityMonitor.isOnline) { activeScenario, isOnline ->
        activeScenario == DebugScenario.OFFLINE || !isOnline
    }

    val uiState: StateFlow<ApplicationsUiState> = combine(
        applicationRepository.observeApplications().onEach { latest -> applications.value = latest },
        header,
        isOffline,
        scenario,
        presentation,
    ) { latest, headerState, offline, activeScenario, presentationState ->
        toUiState(
            applications = latest,
            header = headerState,
            isOffline = offline,
            scenario = activeScenario,
            presentation = presentationState,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApplicationsUiState.Loading(),
    )

    fun onEnter(key: DebugScenario) {
        scenario.value = key
    }

    fun onAction(action: ApplicationsAction) {
        when (action) {
            is ApplicationsAction.ApplicationChosen -> Unit
            ApplicationsAction.PasteJobChosen -> Unit
            ApplicationsAction.CreditsChosen -> Unit
            is ApplicationsAction.StatusChipChosen -> openStatusSheet(action.id)
            ApplicationsAction.StatusSheetDismissed -> closeStatusSheet()
            is ApplicationsAction.StatusChosen -> confirmStatus(action.status)
            ApplicationsAction.StatusUndoChosen -> undoStatus()
            ApplicationsAction.MessageDismissed -> dismissMessage()
        }
    }

    private fun openStatusSheet(id: String) {
        val current = applications.value.firstOrNull { application -> application.id == id } ?: return
        presentation.value = presentation.value.copy(
            statusSheet = ApplicationStatusSheetState(rowId = current.id, current = current.status),
            message = null,
        )
    }

    private fun closeStatusSheet() {
        presentation.value = presentation.value.copy(statusSheet = null)
    }

    private fun confirmStatus(status: ApplicationStatus) {
        val sheet = presentation.value.statusSheet ?: return
        presentation.value = presentation.value.copy(
            statusSheet = null,
            message = ApplicationStatusMessage(status = status, canUndo = true),
            undoTarget = UndoTarget(applicationId = sheet.rowId, status = sheet.current),
        )
        viewModelScope.launch { applicationRepository.updateStatus(sheet.rowId, status) }
    }

    private fun undoStatus() {
        val undo = presentation.value.undoTarget ?: return
        presentation.value = presentation.value.copy(message = null, undoTarget = null)
        viewModelScope.launch { applicationRepository.updateStatus(undo.applicationId, undo.status) }
    }

    private fun dismissMessage() {
        presentation.value = presentation.value.copy(message = null, undoTarget = null)
    }

    private fun toUiState(
        applications: List<JobApplication>,
        header: ApplicationsHeader,
        isOffline: Boolean,
        scenario: DebugScenario,
        presentation: ApplicationsPresentation,
    ): ApplicationsUiState {
        if (applications.isEmpty()) return ApplicationsUiState.Empty(header)
        val pendingId = applications
            .filter { application -> scenario == DebugScenario.PENDING }
            .maxByOrNull { application -> application.updatedAt }
            ?.id
        val rows = applications
            .map { application ->
                application.toListRow(isSyncPending = application.id == pendingId)
            }
            .sortedWith(
                compareByDescending<ApplicationListRow> { row -> row.isSyncPending }
                    .thenByDescending { row -> row.updatedAt },
            )
        return ApplicationsUiState.Applications(
            header = header,
            rows = rows,
            isOffline = isOffline,
            statusSheet = presentation.statusSheet,
            message = presentation.message,
        )
    }

    private fun JobApplication.toListRow(isSyncPending: Boolean): ApplicationListRow = ApplicationListRow(
        id = id,
        role = job.title,
        company = job.company,
        status = status,
        coverage = gapAnalysis?.keywordCoverage ?: EMPTY_COVERAGE,
        updatedAt = updatedAt,
        isSyncPending = isSyncPending,
    )

    private companion object {
        val EMPTY_COVERAGE = KeywordCoverage(covered = 0, total = 0)
    }
}

private data class ApplicationsPresentation(
    val statusSheet: ApplicationStatusSheetState? = null,
    val message: ApplicationStatusMessage? = null,
    val undoTarget: UndoTarget? = null,
)

private data class UndoTarget(
    val applicationId: String,
    val status: ApplicationStatus,
)
