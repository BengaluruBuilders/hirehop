package com.tailormyresume.core.domain.onboarding

import com.tailormyresume.core.data.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

enum class StartDestination { Welcome, Applications }

class ObserveStartDestinationUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    operator fun invoke(): Flow<StartDestination> =
        combine(
            sessionRepository.observeAccount(),
            sessionRepository.observeOnboardingComplete(),
            sessionRepository.observeConsent(),
        ) { account, complete, consent ->
            if (account != null && complete && consent?.isCurrent == true) StartDestination.Applications else StartDestination.Welcome
        }.distinctUntilChanged()
}
