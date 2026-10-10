package com.tailormyresume.core.domain

import com.tailormyresume.core.data.repository.ApplicationRepository
import javax.inject.Inject
import kotlin.time.Clock

class AcceptChangesUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(applicationId: String) = Unit
}
