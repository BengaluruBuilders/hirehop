package com.tailormyresume.app.credits

import com.tailormyresume.core.data.repository.CreditSnapshot
import com.tailormyresume.core.data.repository.RemoteLedgerSource
import com.tailormyresume.core.domain.FirebaseUidProvider
import com.tailormyresume.core.network.TailorMyResumeApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteCreditsLedgerSource @Inject constructor(
    private val api: TailorMyResumeApi,
    private val uids: FirebaseUidProvider,
) : RemoteLedgerSource {
    override val ownsLedger = true

    override fun owner(): String? = uids.uid()

    override suspend fun fetch(): CreditSnapshot = TODO()
}
