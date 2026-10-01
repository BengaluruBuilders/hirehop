package com.hirehop.feature.tailor.impl.packpurchase

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseResult
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.tailor.api.navigation.PackPurchaseNavKey
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class PackPurchaseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gateway = TestPaymentGateway()

    private fun viewModel(gateway: TestPaymentGateway = this.gateway) = PackPurchaseViewModel(paymentGateway = gateway)

    @Test
    fun defaultScenario_showsTheRealCatalogueAndTheRealBalance() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.READY)
        assertThat(state.packs.map(ApplicationPack::id)).containsExactly(
            ApplicationPack.APPLICATION_PACK_FIVE,
            ApplicationPack.SINGLE_APPLICATION,
        ).inOrder()
        assertThat(state.freeCredits).isEqualTo(1)
        assertThat(state.purchasedCredits).isEqualTo(0)
    }

    @Test
    fun loadingScenario_neverAsksTheGateway() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.LOADING))

        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.LOADING_PACKS)
        assertThat(subject.uiState.value.packs).isEmpty()
    }

    @Test
    fun emptyScenario_showsNoPacksAndStillNoFailure() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.EMPTY))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.READY)
        assertThat(state.packs).isEmpty()
        assertThat(state.hasCatalogue).isFalse()
    }

    @Test
    fun offlineScenario_staysOfflineAndStillShowsTheSavedCatalogue() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.OFFLINE))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.OFFLINE)
        assertThat(state.isOffline).isTrue()
        assertThat(state.hasCatalogue).isTrue()
    }

    @Test
    fun errorScenario_reportsACatalogueFailureWithoutInventingAPack() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.ERROR))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.FAILED)
        assertThat(state.isCatalogueFailure).isTrue()
        assertThat(state.failureReason).isNull()
        assertThat(state.packs).isEmpty()
    }

    @Test
    fun aGatewayThatCannotListPacks_reportsACatalogueFailure() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withPacksFailure())

        subject.onEnter(key(DebugScenario.DEFAULT))

        assertThat(subject.uiState.value.isCatalogueFailure).isTrue()
    }

    @Test
    fun aGatewayThatCannotReadTheBalance_reportsACatalogueFailure() = runTest {
        val subject = viewModel(gateway = TestPaymentGateway().withEntitlementFailure())

        subject.onEnter(key(DebugScenario.DEFAULT))

        assertThat(subject.uiState.value.isCatalogueFailure).isTrue()
    }

    @Test
    fun partialScenario_showsAFreeAllowanceThatIsUsedUp() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.PARTIAL))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.READY)
        assertThat(state.freeCredits).isEqualTo(0)
        assertThat(state.purchasedCredits).isEqualTo(5)
    }

    @Test
    fun successScenario_showsTheRecordedPackAndItsCredits() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.SUCCESS))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.SUCCESS)
        assertThat(state.purchasedCredits).isEqualTo(5)
    }

    @Test
    fun purchasedScenario_showsTheRecordedPack() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.PURCHASED))

        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.SUCCESS)
    }

    @Test
    fun deletingScenario_showsThePurchaseInFlight() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.DELETING))

        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.PURCHASING)
    }

    @Test
    fun buyingTheFivePack_recordsTheCreditsAndNoMoney() = runTest {
        val subject = viewModel()
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.SUCCESS)
        assertThat(state.purchasedCredits).isEqualTo(5)
        assertThat(state.failureReason).isNull()
    }

    @Test
    fun buyingTheSinglePack_recordsOneCredit() = runTest {
        val subject = viewModel()
        subject.onEnter(key(DebugScenario.DEFAULT, ApplicationPack.SINGLE_APPLICATION))

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.SINGLE_APPLICATION))

        assertThat(subject.uiState.value.purchasedCredits).isEqualTo(1)
    }

    @Test
    fun aPendingPurchase_addsNoCreditsAndSaysSo() = runTest {
        val gateway = TestPaymentGateway().withResult(
            PurchaseResult.Pending(entitlement = PurchaseEntitlement(1, 0, listOf(ApplicationPack.APPLICATION_PACK_FIVE))),
        )
        val subject = viewModel(gateway = gateway)
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.PENDING)
        assertThat(state.purchasedCredits).isEqualTo(0)
        assertThat(state.pendingPackIds).contains(ApplicationPack.APPLICATION_PACK_FIVE)
    }

    @Test
    fun aCancelledPurchase_grantsNothingAndIsNotAFailure() = runTest {
        val gateway = TestPaymentGateway().withResult(PurchaseResult.Cancelled)
        val subject = viewModel(gateway = gateway)
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.CANCELLED)
        assertThat(state.failureReason).isNull()
        assertThat(state.purchasedCredits).isEqualTo(0)
    }

    @Test
    fun aFailedPurchase_doesNotChangeTheBalance() = runTest {
        val gateway = TestPaymentGateway()
            .withPurchasedCredits(5)
            .withFreeCredits(1)
            .withResult(
                PurchaseResult.Failed(
                    reason = PurchaseFailureReason.PaymentDeclined,
                    entitlement = PurchaseEntitlement(freeCredits = 1, purchasedCredits = 5, pendingPackIds = emptyList()),
                ),
            )
        val subject = viewModel(gateway = gateway)
        subject.onEnter(key(DebugScenario.DEFAULT))
        val before = subject.uiState.value

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        val after = subject.uiState.value
        assertThat(after.stage).isEqualTo(PackPurchaseStage.FAILED)
        assertThat(after.failureReason).isEqualTo(PurchaseFailureReason.PaymentDeclined)
        assertThat(after.purchasedCredits).isEqualTo(before.purchasedCredits)
        assertThat(after.freeCredits).isEqualTo(before.freeCredits)
    }

    @Test
    fun everyFailureReason_reachesTheScreenWithItsOwnReason() = runTest {
        PurchaseFailureReason.entries.forEach { reason ->
            val gateway = TestPaymentGateway().withResult(
                PurchaseResult.Failed(
                    reason = reason,
                    entitlement = PurchaseEntitlement(freeCredits = 1, purchasedCredits = 0, pendingPackIds = emptyList()),
                ),
            )
            val subject = viewModel(gateway = gateway)
            subject.onEnter(key(DebugScenario.DEFAULT))

            subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

            val state = subject.uiState.value
            assertThat(state.stage).isEqualTo(PackPurchaseStage.FAILED)
            assertThat(state.failureReason).isEqualTo(reason)
        }
    }

    @Test
    fun buyingAPackThatIsNotInTheCatalogue_reportsPurchaseUnavailable() = runTest {
        val subject = viewModel()
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(PackPurchaseAction.Buy("pack_that_does_not_exist"))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.FAILED)
        assertThat(state.failureReason).isEqualTo(PurchaseFailureReason.PurchaseUnavailable)
        assertThat(gateway.purchaseCallCount()).isEqualTo(1)
    }

    @Test
    fun aFailedPurchase_keepsTheBalanceTheGatewayAlreadyReported() = runTest {
        val gateway = TestPaymentGateway().withPurchasedCredits(5).withFreeCredits(2)
        val subject = viewModel(gateway = gateway)
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.SINGLE_APPLICATION))

        assertThat(subject.uiState.value.purchasedCredits).isEqualTo(6)
        assertThat(subject.uiState.value.freeCredits).isEqualTo(2)
    }

    @Test
    fun restore_keepsTheCreditsAndSaysNothingWasRestored() = runTest {
        val gateway = TestPaymentGateway().withPurchasedCredits(5)
        val subject = viewModel(gateway = gateway)
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(PackPurchaseAction.Restore)

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.RESTORED)
        assertThat(state.purchasedCredits).isEqualTo(5)
        assertThat(state.failureReason).isNull()
    }

    @Test
    fun aFailedRestore_reportsAFailureAndKeepsTheBalance() = runTest {
        val gateway = TestPaymentGateway().withPurchasedCredits(5).withRestoreFailure()
        val subject = viewModel(gateway = gateway)
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(PackPurchaseAction.Restore)

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.FAILED)
        assertThat(state.failureReason).isEqualTo(PurchaseFailureReason.PurchaseUnavailable)
        assertThat(state.purchasedCredits).isEqualTo(5)
    }

    @Test
    fun dismiss_returnsToReady() = runTest {
        val gateway = TestPaymentGateway().withResult(
            PurchaseResult.Failed(
                reason = PurchaseFailureReason.PaymentUnavailable,
                entitlement = PurchaseEntitlement(1, 0, emptyList()),
            ),
        )
        val subject = viewModel(gateway = gateway)
        subject.onEnter(key(DebugScenario.DEFAULT))
        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        subject.onAction(PackPurchaseAction.Dismiss)

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.READY)
        assertThat(state.failureReason).isNull()
    }

    @Test
    fun selectingTheOtherPack_swapsThePriceOnScreen() = runTest {
        val subject = viewModel()
        subject.onEnter(key(DebugScenario.DEFAULT))

        subject.onAction(PackPurchaseAction.SelectPack(ApplicationPack.SINGLE_APPLICATION))

        val state = subject.uiState.value
        assertThat(state.selectedPackId).isEqualTo(ApplicationPack.SINGLE_APPLICATION)
        assertThat(state.selectedPack?.credits).isEqualTo(1)
        assertThat(state.otherPacks.map(ApplicationPack::id)).containsExactly(ApplicationPack.APPLICATION_PACK_FIVE)
    }

    @Test
    fun selectingAPackThatDoesNotExist_changesNothing() = runTest {
        val subject = viewModel()
        subject.onEnter(key(DebugScenario.DEFAULT))
        val before = subject.uiState.value

        subject.onAction(PackPurchaseAction.SelectPack("pack_that_does_not_exist"))

        assertThat(subject.uiState.value.selectedPackId).isEqualTo(before.selectedPackId)
    }

    @Test
    fun anUnknownPackIdInTheKey_fallsBackToTheFirstRealPack() = runTest {
        val subject = viewModel()

        subject.onEnter(key(DebugScenario.DEFAULT, "pack_that_does_not_exist"))

        assertThat(subject.uiState.value.selectedPackId).isEqualTo(ApplicationPack.APPLICATION_PACK_FIVE)
    }

    @Test
    fun expiryIsReadFromTheRealPackAndNotAssumed() {
        assertThat(ApplicationPack.applicationPackFive.creditsExpire).isFalse()
        assertThat(ApplicationPack.singleApplication.creditsExpire).isFalse()
        val expiring = ApplicationPack.applicationPackFive.copy(id = "expiring", creditsExpire = true)
        val state = PackPurchaseUiState(
            packs = ApplicationPack.catalogue + expiring,
            entitlement = PurchaseEntitlement(1, 5, emptyList()),
        )
        assertThat(state.purchasedCreditsNeverExpire).isFalse()
        assertThat(state.purchasedCreditsMayExpire).isTrue()
    }

    @Test
    fun theFreeAllowanceIsNeverAddedToThePurchasedCreditsInTheState() = runTest {
        val gateway = TestPaymentGateway().withFreeCredits(3).withPurchasedCredits(5)
        val subject = viewModel(gateway = gateway)

        subject.onEnter(key(DebugScenario.DEFAULT))

        val state = subject.uiState.value
        assertThat(state.freeCredits).isEqualTo(3)
        assertThat(state.purchasedCredits).isEqualTo(5)
    }

    private fun key(
        scenario: DebugScenario,
        packId: String = ApplicationPack.APPLICATION_PACK_FIVE,
    ) = PackPurchaseNavKey(
        applicationId = APPLICATION_ID,
        packId = packId,
        scenario = scenario,
    )

    private companion object {
        const val APPLICATION_ID = "application_1"
    }
}
