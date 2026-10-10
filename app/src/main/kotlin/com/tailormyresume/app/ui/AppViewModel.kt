package com.tailormyresume.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tailormyresume.core.data.repository.CreditsRepository
import com.tailormyresume.core.data.repository.SessionRepository
import com.tailormyresume.core.domain.onboarding.ObserveStartDestinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    observeStartDestination: ObserveStartDestinationUseCase,
    private val creditsRepository: CreditsRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val rootState: StateFlow<AppRootState> = sessionRepository.observeAccount()
        .map { account -> account?.id }
        .distinctUntilChanged()
        .flatMapLatest { accountId ->
            flow {
                emit(AppRootState.Ready(observeStartDestination().first(), accountId))
                if (accountId != null) refreshWallet()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AppRootState.Loading,
        )

    private suspend fun refreshWallet() {
        runCatching { creditsRepository.refresh() }
            .onFailure { failure -> if (failure is CancellationException) throw failure }
    }
}
