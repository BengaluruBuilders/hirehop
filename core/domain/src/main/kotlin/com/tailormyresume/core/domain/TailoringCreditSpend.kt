package com.tailormyresume.core.domain

import com.tailormyresume.core.model.CreditLedgerEntry
import kotlin.time.Clock

object TailoringCreditSpend {
    suspend fun <T> forSuccess(
        applicationId: String,
        clock: Clock,
        record: suspend (CreditLedgerEntry) -> Unit,
        tailoring: suspend () -> T,
    ): T = tailoring()
}
