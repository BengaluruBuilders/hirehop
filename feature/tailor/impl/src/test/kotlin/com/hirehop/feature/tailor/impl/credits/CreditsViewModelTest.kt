package com.hirehop.feature.tailor.impl.credits

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.tailor.api.navigation.CreditsNavKey
import com.hirehop.feature.tailor.impl.packpurchase.TestPaymentGateway
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CreditsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(gateway: TestPaymentGateway) = CreditsViewModel(paymentGateway = gateway)

    @Test
    fun defaultScenario_showsTheFreeAllowanceOnItsOwn() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway())

        subject.onEnter(key(DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.FREE_ONLY)
        assertThat(state.freeCredits).isEqualTo(1)
        assertThat(state.purchasedCredits).isEqualTo(0)
    }

    @Test
    fun loadingScenario_neverAsksTheGateway() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway())

        subject.onEnter(key(DebugScenario.LOADING))

        assertThat(subject.uiState.value.stage).isEqualTo(CreditsStage.LOADING)
    }

    @Test
    fun freeOnly_keepsPurchasedAtZero() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withFreeCredits(3).withPurchasedCredits(0))

        subject.onEnter(key(DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.freeCredits).isEqualTo(3)
        assertThat(state.purchasedCredits).isEqualTo(0)
        assertThat(state.hasFreeCredits).isTrue()
        assertThat(state.hasPurchasedCredits).isFalse()
    }

    @Test
    fun purchasedOnly_showsThePurchasedCreditsOnTheirOwn() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withFreeCredits(0).withPurchasedCredits(5))

        subject.onEnter(key(DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.PURCHASED_ONLY)
        assertThat(state.freeCredits).isEqualTo(0)
        assertThat(state.purchasedCredits).isEqualTo(5)
    }

    @Test
    fun mixed_showsBothBucketsWithoutMergingThem() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withFreeCredits(2).withPurchasedCredits(5))

        subject.onEnter(key(DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.MIXED)
        assertThat(state.freeCredits).isEqualTo(2)
        assertThat(state.purchasedCredits).isEqualTo(5)
    }

    @Test
    fun zero_isAStageOfItsOwnAndNotAFailure() = runTest {
        val gateway = TestPaymentGateway().withFreeCredits(0).withPurchasedCredits(0)
        val subject = viewModel(gateway = gateway)

        subject.onEnter(key(DebugScenario.EMPTY))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.ZERO)
        assertThat(state.failureReason).isNull()
        assertThat(state.hasAnyCredits).isFalse()
    }

    @Test
    fun aPendingPurchase_isShownWithoutCountingTheCredits() = runTest {
        val gateway = TestPaymentGateway()
            .withFreeCredits(1)
            .withPendingPackIds(listOf(ApplicationPack.APPLICATION_PACK_FIVE))
        val subject = viewModel(gateway = gateway)

        subject.onEnter(key(DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.PENDING)
        assertThat(state.purchasedCredits).isEqualTo(0)
        assertThat(state.hasPendingPurchase).isTrue()
        assertThat(state.purchases).hasSize(1)
        assertThat(state.purchases.first().packId).isEqualTo(ApplicationPack.APPLICATION_PACK_FIVE)
    }

    @Test
    fun aPendingPurchase_neverListsAPackTheUserDidNotBuy() = runTest {
        val gateway = TestPaymentGateway()
            .withPurchasedCredits(5)
            .withPendingPackIds(listOf(ApplicationPack.SINGLE_APPLICATION))
        val subject = viewModel(gateway = gateway)

        subject.onEnter(key(DebugScenario.DEFAULT))

        assertThat(subject.uiState.value.purchases).hasSize(1)
        assertThat(subject.uiState.value.purchases.first().packId).isEqualTo(ApplicationPack.SINGLE_APPLICATION)
    }

    @Test
    fun offline_keepsTheSavedHistoryReadable() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withFreeCredits(1).withPurchasedCredits(5))

        subject.onEnter(key(DebugScenario.OFFLINE))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.OFFLINE)
        assertThat(state.isOffline).isTrue()
        assertThat(state.freeCredits).isEqualTo(1)
        assertThat(state.purchasedCredits).isEqualTo(5)
    }

    @Test
    fun errorScenario_reportsAFailureWithoutClaimingAnythingIsLost() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway())

        subject.onEnter(key(DebugScenario.ERROR))

        assertThat(subject.uiState.value.stage).isEqualTo(CreditsStage.ERROR)
        assertThat(subject.uiState.value.failureReason).isNull()
    }

    @Test
    fun aGatewayThatCannotBeRead_reportsAnError() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withEntitlementFailure())

        subject.onEnter(key(DebugScenario.DEFAULT))

        assertThat(subject.uiState.value.stage).isEqualTo(CreditsStage.ERROR)
    }

    @Test
    fun purchasedScenario_showsPurchasedCredits() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway())

        subject.onEnter(key(DebugScenario.PURCHASED))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.PURCHASED_ONLY)
        assertThat(state.purchasedCredits).isEqualTo(ApplicationPack.catalogue.sumOf { pack -> pack.credits })
    }

    @Test
    fun partialScenario_showsTheFreeBucketUsedUp() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withFreeCredits(2))

        subject.onEnter(key(DebugScenario.PARTIAL))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.MIXED)
        assertThat(state.freeCredits).isEqualTo(0)
        assertThat(state.purchasedCredits).isGreaterThan(0)
    }

    @Test
    fun deletingScenario_showsTheRestoreInProgress() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway())

        subject.onEnter(key(DebugScenario.DELETING))

        assertThat(subject.uiState.value.stage).isEqualTo(CreditsStage.RESTORING)
    }

    @Test
    fun restore_keepsBothBucketsUntouched() = runTest {
        val gateway = TestPaymentGateway().withFreeCredits(2).withPurchasedCredits(5)
        val subject = viewModel(gateway = gateway)
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(CreditsAction.Restore)

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.RESTORED)
        assertThat(state.freeCredits).isEqualTo(2)
        assertThat(state.purchasedCredits).isEqualTo(5)
    }

    @Test
    fun aFailedRestore_reportsAFailureAndKeepsBothBuckets() = runTest {
        val gateway = TestPaymentGateway().withFreeCredits(2).withPurchasedCredits(5).withRestoreFailure()
        val subject = viewModel(gateway = gateway)
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(CreditsAction.Restore)

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(CreditsStage.ERROR)
        assertThat(state.failureReason).isEqualTo(PurchaseFailureReason.PurchaseUnavailable)
        assertThat(state.freeCredits).isEqualTo(2)
        assertThat(state.purchasedCredits).isEqualTo(5)
    }

    @Test
    fun theNeverExpireClaimIsOnlyMadeWhenEveryRealPackSaysSo() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withPurchasedCredits(5))
        subject.onEnter(key(DebugScenario.DEFAULT))
        assertThat(subject.uiState.value.purchasedCreditsNeverExpire).isTrue()

        val expiring = ApplicationPack.applicationPackFive.copy(id = "expiring", creditsExpire = true)
        val mixedState = CreditsUiState(
            purchasedCredits = 5,
            purchasedCreditsNeverExpire = false,
            purchasedCreditsMayExpire = true,
        )
        assertThat(expiring.creditsExpire).isTrue()
        assertThat(mixedState.purchasedCreditsNeverExpire).isFalse()
        assertThat(mixedState.purchasedCreditsMayExpire).isTrue()
    }

    @Test
    fun theStateNeverExposesASummedTotalOfBothBuckets() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withFreeCredits(3).withPurchasedCredits(5))

        subject.onEnter(key(DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.freeCredits).isEqualTo(3)
        assertThat(state.purchasedCredits).isEqualTo(5)
        assertThat(state.toString()).doesNotContain("totalCredits")
    }

    @Test
    fun anEntitlementWithNothingInIt_isZeroNotAnError() {
        val empty = PurchaseEntitlement(freeCredits = 0, purchasedCredits = 0, pendingPackIds = emptyList())
        assertThat(creditsStageFor(entitlement = empty, isPending = false)).isEqualTo(CreditsStage.ZERO)
    }

    private fun key(scenario: DebugScenario) = CreditsNavKey(scenario = scenario)
}
