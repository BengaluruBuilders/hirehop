package com.tailormyresume.core.testing.account

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.PurchaseEntitlement
import com.tailormyresume.core.domain.account.AccountData
import com.tailormyresume.core.domain.account.AccountDataExporter
import com.tailormyresume.core.model.SignInAccount
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.sampleApplication
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

abstract class AccountDataExporterContractTest {

    protected abstract fun createExporter(): AccountDataExporter

    private val data = AccountData(
        generatedAt = Instant.fromEpochMilliseconds(1_790_000_000_000),
        account = SignInAccount.localAccount,
        consent = null,
        profile = canonicalCandidateProfile,
        applications = listOf(sampleApplication),
        entitlement = PurchaseEntitlement(freeCredits = 1, purchasedCredits = 0, pendingPackIds = emptyList()),
        purchases = emptyList(),
        exports = emptyList(),
    )

    @Test
    fun theArchiveHasANameAndAFileThatExists() = runTest {
        val archive = createExporter().export(data)

        assertThat(archive.fileName).isNotEmpty()
        assertThat(archive.file.isFile).isTrue()
    }

    @Test
    fun anEmptyAccountStillProducesAnArchive() = runTest {
        val empty = data.copy(account = null, profile = null, applications = emptyList())

        val archive = createExporter().export(empty)

        assertThat(archive.file.isFile).isTrue()
    }
}
