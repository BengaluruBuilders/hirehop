package com.tailormyresume.core.domain.account

import com.tailormyresume.core.data.repository.PendingAccountWipe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinishPendingAccountWipeUseCase @Inject constructor(
    private val pendingWipe: PendingAccountWipe,
    private val finisher: AccountWipeFinisher,
    private val serverAccountDeleter: ServerAccountDeleter,
) {
    suspend operator fun invoke(): PendingWipeOutcome = PendingWipeOutcome.NOTHING_PENDING
}
