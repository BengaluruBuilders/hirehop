package com.hirehop.feature.onboarding.impl.consent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hirehop.core.data.repository.SessionRepository
import com.hirehop.core.domain.ConsentUploader
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val consentUploader: ConsentUploader,
    private val nextOnboardingStep: NextOnboardingStepUseCase,
    private val clock: Clock,
) : ViewModel() {

    private val mutableState = MutableStateFlow(ConsentUiState())

    private var hasEntered = false

    val uiState: StateFlow<ConsentUiState> = mutableState.asStateFlow()

    fun onEnter(key: ConsentNavKey) {
        if (hasEntered) return
        hasEntered = true
        mutableState.value = consentStateFor(scenario = key.scenario, readOnly = key.readOnly)
        if (key.readOnly) {
            viewModelScope.launch {
                sessionRepository.observeConsent().collect { record -> mutableState.update { it.showing(record) } }
            }
        }
    }

    fun onAction(action: ConsentAction) {
        when (action) {
            is ConsentAction.PurposeToggled -> onPurposeToggled(action.purpose)
            ConsentAction.Agree -> onAgree()
            ConsentAction.NotNow -> mutableState.update { it.copy(isDeclined = true) }
            ConsentAction.ReadAgain -> mutableState.update { it.copy(isDeclined = false) }
            ConsentAction.NextStepConsumed -> mutableState.update { it.copy(nextStep = null) }
        }
    }

    private fun onPurposeToggled(purpose: ConsentPurpose) {
        mutableState.update { state ->
            if (state.isReadOnly) {
                state
            } else {
                state.copy(
                    uploadFailed = false,
                    entries = state.entries.map { entry ->
                        if (entry.purpose == purpose) entry.copy(isAcknowledged = !entry.isAcknowledged) else entry
                    },
                )
            }
        }
    }

    private fun onAgree() {
        val state = mutableState.value
        if (!state.canAgree) return
        mutableState.value = state.copy(isSaving = true, uploadFailed = false)
        viewModelScope.launch {
            val record = ConsentRecord(
                purposes = state.entries.map { it.purpose }.toSet(),
                acceptedAt = clock.now(),
                noticeVersion = ConsentRecord.CURRENT_NOTICE_VERSION,
            )
            if (consentUploader.upload(record).isFailure) {
                mutableState.update { it.copy(isSaving = false, uploadFailed = true) }
                return@launch
            }
            sessionRepository.recordConsent(record)
            val step = nextOnboardingStep()
            mutableState.update { it.copy(isSaving = false, nextStep = step) }
        }
    }

    private fun ConsentUiState.showing(record: ConsentRecord?): ConsentUiState = copy(
        agreedAt = record?.acceptedAt,
        entries = entries.map { entry ->
            entry.copy(isAcknowledged = record?.purposes?.contains(entry.purpose) == true)
        },
    )
}
