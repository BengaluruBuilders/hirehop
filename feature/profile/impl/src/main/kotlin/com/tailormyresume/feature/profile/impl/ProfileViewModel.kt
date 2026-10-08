package com.tailormyresume.feature.profile.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    sessionRepository: SessionRepository,
    connectivityMonitor: ConnectivityMonitor,
) : ViewModel() {

    private val profileMutex = Mutex()
    private val mutableScenario = MutableStateFlow(DebugScenario.defaultValue)

    val uiState: StateFlow<ProfileUiState> = combine(
        profileRepository.observeProfile(),
        sessionRepository.observeAccount(),
        connectivityMonitor.isOnline,
        mutableScenario,
    ) { profile, account, isOnline, scenario ->
        scenario.toUiState(
            profile = profile,
            accountName = account?.displayName.orEmpty(),
            isOffline = !isOnline || scenario == DebugScenario.OFFLINE,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = ProfileUiState.Loading,
    )

    fun selectScenario(debugScenario: DebugScenario) {
        mutableScenario.value = debugScenario
    }

    fun confirmEntry(entryId: String) = updateProfile { it.confirmEntry(entryId) }

    fun updateContact(contact: ContactDraft) = updateProfile { it.withContact(contact) }

    fun addSkill(skill: String) = updateProfile { it.withSkill(skill) }

    fun removeSkill(skill: String) = updateProfile { it.withoutSkill(skill) }

    private fun updateProfile(transform: (CandidateProfile) -> CandidateProfile) {
        viewModelScope.launch {
            profileMutex.withLock {
                val current = profileRepository.observeProfile().first() ?: return@withLock
                profileRepository.saveProfile(transform(current))
            }
        }
    }

    private fun DebugScenario.toUiState(
        profile: CandidateProfile?,
        accountName: String,
        isOffline: Boolean,
    ): ProfileUiState = when (this) {
        DebugScenario.LOADING -> ProfileUiState.Loading
        DebugScenario.EMPTY -> ProfileUiState.Empty(headerLine = accountName)
        DebugScenario.ERROR -> ProfileUiState.Failure
        else -> if (profile == null) {
            ProfileUiState.Empty(headerLine = accountName)
        } else {
            ProfileUiState.Success(profile = profile, isOffline = isOffline)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
