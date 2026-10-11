package com.tailormyresume.feature.onboarding.impl.manual

import androidx.lifecycle.ViewModel
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.onboarding.SaveManualProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

internal enum class ManualField { FullName, Phone, City, JobTitle, Company }

internal data class ManualProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val jobTitle: String = "",
    val company: String = "",
)

internal enum class ManualProfileEvent { Saved }

@HiltViewModel
internal class ManualProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val saveManualProfile: SaveManualProfileUseCase,
) : ViewModel() {

    val uiState: StateFlow<ManualProfileUiState> = MutableStateFlow(ManualProfileUiState())

    val events: Flow<ManualProfileEvent> = emptyFlow()

    fun onFieldChange(field: ManualField, value: String) = Unit

    fun onContinue() = Unit
}
