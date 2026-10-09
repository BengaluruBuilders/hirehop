package com.tailormyresume.core.domain.account

import com.tailormyresume.core.data.repository.PendingAccountWipe
import com.tailormyresume.core.data.repository.PendingWipeState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinishPendingAccountWipeUseCase @Inject constructor(
    private val pendingWipe: PendingAccountWipe,
    private val finisher: AccountWipeFinisher,
    private val serverAccountDeleter: ServerAccountDeleter,
) {
    private val mutex = Mutex()

    suspend operator fun invoke(): PendingWipeOutcome = mutex.withLock {
        try {
            when (pendingWipe.state()) {
                PendingWipeState.NONE -> PendingWipeOutcome.NOTHING_PENDING
                PendingWipeState.SERVER_CLOSED -> wipe()
                PendingWipeState.REQUESTED -> resolveRequested()
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (failure: Exception) {
            PendingWipeOutcome.STILL_PENDING
        }
    }

    private suspend fun resolveRequested(): PendingWipeOutcome {
        val closed = serverAccountDeleter.isClosed().getOrNull() ?: return PendingWipeOutcome.STILL_PENDING
        if (!closed) {
            pendingWipe.clear()
            return PendingWipeOutcome.ACCOUNT_KEPT
        }
        pendingWipe.markServerClosed()
        return wipe()
    }

    private suspend fun wipe(): PendingWipeOutcome {
        withContext(NonCancellable) {
            finisher.finish()
            pendingWipe.clear()
        }
        return PendingWipeOutcome.FINISHED
    }
}
