package com.hirehop.feature.settings.impl.yourdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobApplication
import com.hirehop.feature.settings.api.navigation.YourDataNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YourDataViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val paymentGateway: PaymentGateway,
) : ViewModel() {

    private val mutableState = MutableStateFlow(YourDataUiState())

    private var hasEntered = false

    private var isOffline = false

    private var exportFileName: String = yourDataExportFileName(fullName = null)

    private var entitlement: PurchaseEntitlement? = null

    private var applicationsSnapshot: List<JobApplication> = emptyList()

    private var profileSnapshot: CandidateProfile? = null

    private var applicationCount: Int = 0

    val uiState: StateFlow<YourDataUiState> = mutableState.asStateFlow()

    fun onEnter(key: YourDataNavKey) {
        if (hasEntered) return
        hasEntered = true
        isOffline = yourDataIsOffline(key.scenario)
        mutableState.update { it.copy(isOffline = isOffline) }
        loadEntitlement()
        observeLedger()
    }

    fun onAction(action: YourDataAction) {
        when (action) {
            YourDataAction.DownloadTapped -> onDownloadTapped()
            YourDataAction.ShareTapped -> onShareTapped()
            is YourDataAction.LedgerActionTapped -> onLedgerActionTapped(action)
            is YourDataAction.DeleteRequested -> onDeleteRequested(action.applicationId)
            YourDataAction.DeleteConfirmed -> onDeleteConfirmed()
            YourDataAction.DeleteDismissed -> onDeleteDismissed()
            is YourDataAction.DestinationSelected -> onDestinationSelected(action.destination)
            YourDataAction.DestinationConsumed -> mutableState.update { it.copy(destination = null) }
        }
    }

    private fun loadEntitlement() {
        viewModelScope.launch {
            entitlement = runCatching { paymentGateway.entitlement() }.getOrNull()
            refreshLedger(applications = applicationsSnapshot, profile = profileSnapshot)
        }
    }

    private fun observeLedger() {
        viewModelScope.launch {
            combine(
                applicationRepository.observeApplications(),
                profileRepository.observeProfile(),
            ) { applications, profile -> applications to profile }
                .collect { (applications, profile) ->
                    applicationsSnapshot = applications
                    profileSnapshot = profile
                    refreshLedger(applications = applications, profile = profile)
                }
        }
    }

    private fun refreshLedger(
        applications: List<JobApplication>,
        profile: CandidateProfile?,
    ) {
        exportFileName = yourDataExportFileName(fullName = profile?.fullName)
        applicationCount = applications.size
        mutableState.update { state ->
            state.copy(
                ledger = yourDataLedger(
                    profile = profile,
                    applications = applications,
                    entitlement = entitlement,
                ),
                profileFactCount = profile?.entries?.size ?: 0,
            )
        }
    }

    private fun onDownloadTapped() {
        if (isOffline || mutableState.value.isExporting) return
        mutableState.update { state ->
            state.copy(
                stage = YourDataStage.PREPARING,
                steps = yourDataExportSteps(
                    currentIndex = 0,
                    applicationCount = applicationCount,
                ),
            )
        }
        viewModelScope.launch {
            for (index in 1 until YourDataExportStepKind.entries.size) {
                delay(EXPORT_STEP_DELAY_MS)
                mutableState.update { state ->
                    state.copy(
                        steps = yourDataExportSteps(
                            currentIndex = index,
                            applicationCount = applicationCount,
                        ),
                    )
                }
            }
            mutableState.update { state ->
                state.copy(
                    stage = YourDataStage.READY,
                    exportFileName = exportFileName,
                )
            }
        }
    }

    private fun onShareTapped() {
        mutableState.update { state ->
            if (state.stage == YourDataStage.READY && state.exportFileName != null) {
                state.copy(destination = YourDataDestination.SHARE_SHEET)
            } else {
                state
            }
        }
    }

    private fun onLedgerActionTapped(action: YourDataAction.LedgerActionTapped) {
        val destination = when (action.kind) {
            YourDataLedgerKind.PROFILE -> if (action.action == YourDataLedgerAction.CORRECT) {
                YourDataDestination.PROFILE_CORRECT
            } else {
                YourDataDestination.PROFILE_VIEW
            }

            YourDataLedgerKind.APPLICATIONS -> YourDataDestination.APPLICATIONS_VIEW
            YourDataLedgerKind.PURCHASES -> YourDataDestination.PURCHASES_VIEW
            YourDataLedgerKind.UPLOADED_RESUME -> null
        }
        mutableState.update { state ->
            if (destination == null) state else state.copy(destination = destination)
        }
    }

    private fun onDeleteRequested(applicationId: String) {
        mutableState.update { state ->
            val item = state.ledger
                .flatMap { row -> row.items }
                .firstOrNull { candidate -> candidate.applicationId == applicationId }
            if (item == null) {
                state
            } else {
                state.copy(
                    deleteTarget = YourDataDeleteTarget(
                        applicationId = item.applicationId,
                        title = item.title,
                        company = item.company,
                    ),
                )
            }
        }
    }

    private fun onDeleteConfirmed() {
        val target = mutableState.value.deleteTarget
        if (target == null) return
        mutableState.update { it.copy(deleteTarget = null) }
        viewModelScope.launch {
            applicationRepository.deleteApplication(target.applicationId)
        }
    }

    private fun onDeleteDismissed() {
        mutableState.update { it.copy(deleteTarget = null) }
    }

    private fun onDestinationSelected(destination: YourDataDestination) {
        mutableState.update { it.copy(destination = destination) }
    }
}

internal const val EXPORT_STEP_DELAY_MS: Long = 900L
