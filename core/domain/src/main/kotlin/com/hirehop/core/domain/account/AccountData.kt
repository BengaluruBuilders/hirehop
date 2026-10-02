package com.hirehop.core.domain.account

import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseRecord
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.ExportRecord
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.SignInAccount
import java.io.File
import kotlin.time.Instant

data class AccountData(
    val generatedAt: Instant,
    val account: SignInAccount?,
    val consent: ConsentRecord?,
    val profile: CandidateProfile?,
    val applications: List<JobApplication>,
    val entitlement: PurchaseEntitlement,
    val purchases: List<PurchaseRecord>,
    val exports: List<ExportRecord>,
)

data class AccountDataArchive(val fileName: String, val file: File)

interface AccountDataExporter {
    suspend fun export(data: AccountData): AccountDataArchive
}
