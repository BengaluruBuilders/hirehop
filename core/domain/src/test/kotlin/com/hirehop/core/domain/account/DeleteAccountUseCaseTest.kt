package com.hirehop.core.domain.account

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.CreditSpend
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseResult
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.JobApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.sampleApplication
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteAccountUseCaseTest {

    private val applications = MutableStateFlow<List<JobApplication>>(emptyList())
    private val profile = MutableStateFlow<CandidateProfile?>(null)
    private val calls = mutableListOf<String>()
    private var failingApplicationIds: Set<String> = emptySet()
    private var failingProfileClear = false

    @Test
    fun previewCountsTheRealProfileApplicationsAndCredits() = runTest {
        applications.value = fourApplications()
        profile.value = canonicalCandidateProfile
        val useCase = useCase(gateway = gatewayWith(credits = 4))

        val counts = useCase.preview()

        assertThat(counts.profileFacts).isEqualTo(18)
        assertThat(counts.applications).isEqualTo(4)
        assertThat(counts.unusedCredits).isEqualTo(4)
    }

    @Test
    fun previewWithAnEmptyAccountCountsZero() = runTest {
        val useCase = useCase(gateway = gatewayWith(credits = 0))

        val counts = useCase.preview()

        assertThat(counts.profileFacts).isEqualTo(0)
        assertThat(counts.applications).isEqualTo(0)
        assertThat(counts.unusedCredits).isEqualTo(0)
    }

    @Test
    fun deletingRemovesEveryApplicationTheProfileAndTheCredits() = runTest {
        applications.value = fourApplications()
        profile.value = canonicalCandidateProfile
        val gateway = gatewayWith(credits = 4)
        val useCase = useCase(gateway = gateway)

        val result = useCase()

        assertThat(result).isEqualTo(
            AccountDeletionResult.Deleted(
                AccountDeletionCounts(
                    profileFacts = 18,
                    applications = 4,
                    unusedCredits = 4,
                ),
            ),
        )
        assertThat(applications.value).isEmpty()
        assertThat(profile.value).isNull()
        assertThat(gateway.entitlement().totalCredits).isEqualTo(0)
    }

    @Test
    fun deletingReportsTheNamedStepsInOrderBehindTheCounts() = runTest {
        applications.value = fourApplications()
        profile.value = canonicalCandidateProfile
        val seen = mutableListOf<AccountDeletionStep>()
        val useCase = useCase(gateway = gatewayWith(credits = 2))

        useCase(onStep = { step -> seen += step })

        assertThat(seen).containsExactly(
            AccountDeletionStep.DELETING_APPLICATIONS,
            AccountDeletionStep.DELETING_PROFILE_FACTS,
            AccountDeletionStep.CLOSING_ACCOUNT,
        ).inOrder()
    }

    @Test
    fun theRepositoriesAreCalledInTheOrderTheStepsPromise() = runTest {
        applications.value = fourApplications()
        profile.value = canonicalCandidateProfile
        val useCase = useCase(gateway = gatewayWith(credits = 1))

        useCase()

        assertThat(calls).containsExactly(
            "delete:application-1",
            "delete:application-2",
            "delete:application-3",
            "delete:application-4",
            "clearProfile",
        ).inOrder()
    }

    @Test
    fun aFailureWhileDeletingAnApplicationLeavesTheDataIntact() = runTest {
        applications.value = fourApplications()
        profile.value = canonicalCandidateProfile
        failingApplicationIds = setOf("application-3")
        val useCase = useCase(gateway = gatewayWith(credits = 4))

        val result = useCase()

        assertThat(result).isEqualTo(AccountDeletionResult.Failed(dataIntact = true))
        assertThat(applications.value.map { it.id }).containsExactly(
            "application-1",
            "application-2",
            "application-3",
            "application-4",
        )
        assertThat(profile.value).isEqualTo(canonicalCandidateProfile)
    }

    @Test
    fun aFailureWhileClearingTheProfileRestoresTheDeletedApplications() = runTest {
        applications.value = fourApplications()
        profile.value = canonicalCandidateProfile
        failingProfileClear = true
        val useCase = useCase(gateway = gatewayWith(credits = 4))

        val result = useCase()

        assertThat(result).isEqualTo(AccountDeletionResult.Failed(dataIntact = true))
        assertThat(applications.value.map { it.id }).containsExactly(
            "application-1",
            "application-2",
            "application-3",
            "application-4",
        )
        assertThat(profile.value).isEqualTo(canonicalCandidateProfile)
    }

    @Test
    fun aFailureWhileClosingTheAccountNeverClaimsTheDataIsIntact() = runTest {
        applications.value = fourApplications()
        profile.value = canonicalCandidateProfile
        val useCase = useCase(gateway = ThrowingPaymentGateway())

        val result = useCase()

        assertThat(result).isEqualTo(AccountDeletionResult.Failed(dataIntact = false))
    }

    @Test
    fun cancellationIsNotSwallowedAsAFailedDeletion() = runTest {
        applications.value = fourApplications()
        profile.value = canonicalCandidateProfile
        val useCase = useCase(gateway = gatewayWith(credits = 4))

        val thrown = runCatching {
            useCase(onStep = { step ->
                if (step == AccountDeletionStep.CLOSING_ACCOUNT) throw CancellationException("stop")
            })
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(CancellationException::class.java)
    }

    private fun useCase(gateway: PaymentGateway): DeleteAccountUseCase = DeleteAccountUseCase(
        applicationRepository = RecordingApplicationRepository(
            applications = applications,
            calls = calls,
            failingIds = { failingApplicationIds },
        ),
        profileRepository = RecordingProfileRepository(
            profile = profile,
            calls = calls,
            shouldFailOnClear = { failingProfileClear },
        ),
        creditBalance = AccountCreditBalance(paymentGateway = gateway),
    )

    private fun fourApplications(): List<JobApplication> = listOf("1", "2", "3", "4").map { index ->
        sampleApplication.copy(id = "application-$index", status = ApplicationStatus.SAVED)
    }

    private fun gatewayWith(credits: Int): PaymentGateway = BalancePaymentGateway(
        PurchaseEntitlement(
            freeCredits = credits,
            purchasedCredits = 0,
            pendingPackIds = emptyList(),
        ),
    )
}

private class RecordingApplicationRepository(
    private val applications: MutableStateFlow<List<JobApplication>>,
    private val calls: MutableList<String>,
    private val failingIds: () -> Set<String>,
) : ApplicationRepository {

    override fun observeApplications(): Flow<List<JobApplication>> = applications

    override fun observeApplication(id: String): Flow<JobApplication?> = MutableStateFlow(null)

    override suspend fun upsertApplication(application: JobApplication) {
        calls += "upsert:${application.id}"
        applications.update { current ->
            if (current.any { it.id == application.id }) {
                current.map { if (it.id == application.id) application else it }
            } else {
                current + application
            }
        }
    }

    override suspend fun updateStatus(id: String, status: ApplicationStatus) = Unit

    override suspend fun updateNotes(id: String, notes: String) = Unit

    override suspend fun deleteApplication(id: String) {
        if (id in failingIds()) throw IllegalStateException("delete failed for $id")
        calls += "delete:$id"
        applications.update { current -> current.filterNot { it.id == id } }
    }
}

private class RecordingProfileRepository(
    private val profile: MutableStateFlow<CandidateProfile?>,
    private val calls: MutableList<String>,
    private val shouldFailOnClear: () -> Boolean,
) : ProfileRepository {

    override fun observeProfile(): Flow<CandidateProfile?> = profile

    override suspend fun saveProfile(profile: CandidateProfile) {
        calls += "saveProfile"
        this.profile.value = profile
    }

    override suspend fun clearProfile() {
        if (shouldFailOnClear()) throw IllegalStateException("clear failed")
        calls += "clearProfile"
        profile.value = null
    }
}

private class BalancePaymentGateway(
    entitlement: PurchaseEntitlement,
) : PaymentGateway {

    private var current: PurchaseEntitlement = entitlement

    override suspend fun packs(): List<ApplicationPack> = emptyList()

    override suspend fun purchase(packId: String): PurchaseResult = PurchaseResult.Failed(
        reason = PurchaseFailureReason.PurchaseUnavailable,
        entitlement = current,
    )

    override suspend fun entitlement(): PurchaseEntitlement = current

    override suspend fun restorePurchases(): PurchaseEntitlement = current

    override suspend fun consumeCredit(): CreditSpend = if (current.totalCredits > 0) {
        current = current.copy(freeCredits = current.freeCredits - 1)
        CreditSpend.Spent(current)
    } else {
        CreditSpend.NoCreditLeft
    }

    override suspend fun clearCredits(): PurchaseEntitlement {
        current = PurchaseEntitlement(freeCredits = 0, purchasedCredits = 0, pendingPackIds = emptyList())
        return current
    }
}

private class ThrowingPaymentGateway : PaymentGateway {

    override suspend fun packs(): List<ApplicationPack> = throw IllegalStateException("no packs")

    override suspend fun purchase(packId: String): PurchaseResult = throw IllegalStateException("no purchase")

    override suspend fun entitlement(): PurchaseEntitlement = PurchaseEntitlement(
        freeCredits = 4,
        purchasedCredits = 0,
        pendingPackIds = emptyList(),
    )

    override suspend fun restorePurchases(): PurchaseEntitlement = entitlement()

    override suspend fun consumeCredit(): CreditSpend = throw IllegalStateException("no credit service")

    override suspend fun clearCredits(): PurchaseEntitlement = throw IllegalStateException("no credit service")
}
