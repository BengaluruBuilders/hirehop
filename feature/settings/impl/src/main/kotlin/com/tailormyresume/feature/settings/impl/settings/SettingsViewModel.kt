package com.tailormyresume.feature.settings.impl.settings

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.CreditsRepository
import com.tailormyresume.core.data.repository.ResumeSettingsRepository
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.SignInGateway
import com.tailormyresume.core.domain.account.ExportAccountDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    creditsRepository: CreditsRepository,
    resumeSettingsRepository: ResumeSettingsRepository,
    exportAccountData: ExportAccountDataUseCase,
    signInGateway: SignInGateway,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = MutableStateFlow(SettingsUiState.Loading)

    val events: Flow<SettingsEvent> = emptyFlow()

    fun onPageSizeClicked() = Unit

    fun onFileNameClicked() = Unit

    fun onProductUpdatesToggled() = Unit

    fun onDownloadMyData() = Unit

    fun onSignOut() = Unit
}
