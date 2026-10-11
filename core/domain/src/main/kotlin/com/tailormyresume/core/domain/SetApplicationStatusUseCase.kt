package com.tailormyresume.core.domain

import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.model.ApplicationStatus
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

data class StatusChange(val status: ApplicationStatus, val appliedOn: Instant?)

class SetApplicationStatusUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(applicationId: String, status: ApplicationStatus): StatusChange? = TODO()

    suspend fun restore(applicationId: String, previous: StatusChange) { TODO() }
}
