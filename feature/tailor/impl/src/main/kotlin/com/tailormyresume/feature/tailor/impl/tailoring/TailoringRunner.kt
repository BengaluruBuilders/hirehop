package com.tailormyresume.feature.tailor.impl.tailoring

import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers
import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.CreditsRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.domain.TailorResumeUseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import kotlin.time.Clock

internal sealed interface TailoringResult {
    data object Success : TailoringResult

    data class Failure(val cause: Throwable) : TailoringResult
}

internal class TailoringRunner @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val creditsRepository: CreditsRepository,
    private val tailorResume: TailorResumeUseCase,
    private val clock: Clock,
    @Dispatcher(TmrDispatchers.Default) private val dispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(applicationId: String): TailoringResult =
        TailoringResult.Failure(NotImplementedError())
}
