package com.hirehop.feature.tailor.impl.packpurchase

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.domain.PurchaseOutcome
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.gateway.TestPaymentGateway
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.core.testing.util.TestClock
import com.hirehop.feature.tailor.api.navigation.PackPurchaseNavKey
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.exportpreview.PendingExportStart
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

private const val APPLICATION_ID = "application-northwind-1"

class PackPurchaseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val gateway = TestPaymentGateway()
    private val applicationRepository = TestApplicationRepository()
    private val connectivity = TestConnectivityMonitor()
    private val pendingExportStart = PendingExportStart()

    private fun viewModel() = PackPurchaseViewModel(
        paymentGateway = gateway,
        applicationRepository = applicationRepository,
        connectivityMonitor = connectivity,
        pendingExportStart = pendingExportStart,
        clock = TestClock(),
    )

    private fun key(
        scenario: DebugScenario = DebugScenario.DEFAULT,
        packId: String = ApplicationPack.APPLICATION_PACK_FIVE,
        applicationId: String = APPLICATION_ID,
        startExportOnReturn: Boolean = false,
        format: String = "pdf",
    ) = PackPurchaseNavKey(
        applicationId = applicationId,
        packId = packId,
        scenario = scenario,
        startExportOnReturn = startExportOnReturn,
        format = format,
    )

    private fun entered(scenario: DebugScenario = DebugScenario.DEFAULT): PackPurchaseViewModel {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        return viewModel().also { subject -> subject.onEnter(key(scenario)) }
    }

    @Test
    fun defaultScenario_showsThePacksFromTheGatewayAndTheJob() = runTest {
        val subject = entered()

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.READY)
        assertThat(state.packs.map(ApplicationPack::id)).containsExactly(
            ApplicationPack.APPLICATION_PACK_FIVE,
            ApplicationPack.SINGLE_APPLICATION,
        ).inOrder()
        assertThat(state.selectedPack?.id).isEqualTo(ApplicationPack.APPLICATION_PACK_FIVE)
        assertThat(state.otherPacks.map(ApplicationPack::id)).containsExactly(ApplicationPack.SINGLE_APPLICATION)
        assertThat(state.jobCompany).isEqualTo("Northwind GCC")
        assertThat(state.totalCredits).isEqualTo(1)
        assertThat(state.canBuy).isTrue()
    }

    @Test
    fun noApplication_leavesTheHeadlineGeneric() = runTest {
        val subject = viewModel()

        subject.onEnter(key(applicationId = ""))

        assertThat(subject.uiState.value.jobCompany).isEmpty()
        assertThat(subject.uiState.value.hasApplication).isFalse()
        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.READY)
        assertThat(packHeadlineRes(subject.uiState.value, compact = false))
            .isEqualTo(R.string.feature_tailor_impl_pack_purchase_headline_credits)
        assertThat(packHeadlineRes(subject.uiState.value, compact = true))
            .isEqualTo(R.string.feature_tailor_impl_pack_purchase_headline_credits)
    }

    @Test
    fun applicationWithACompany_usesTheResumeHeadline() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        val subject = viewModel()

        subject.onEnter(key())

        assertThat(packHeadlineRes(subject.uiState.value, compact = false))
            .isEqualTo(R.string.feature_tailor_impl_pack_purchase_headline)
    }

    @Test
    fun returnAfterPurchase_startsTheExportWhenThePersonTappedDownloadFirst() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        val subject = viewModel()
        subject.onEnter(key(startExportOnReturn = true))
        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        subject.onAction(PackPurchaseAction.ReturnAfterPurchase)

        assertThat(pendingExportStart.applicationId.value).isEqualTo(APPLICATION_ID)
    }

    @Test
    fun returnAfterPurchase_justReturnsWhenThePersonDidNotTapDownloadFirst() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        val subject = viewModel()
        subject.onEnter(key(startExportOnReturn = false))
        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        subject.onAction(PackPurchaseAction.ReturnAfterPurchase)

        assertThat(pendingExportStart.applicationId.value).isNull()
    }

    @Test
    fun returnAfterPurchase_neverStartsAnExportBeforeTheMoneyIsTaken() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        val subject = viewModel()
        subject.onEnter(key(startExportOnReturn = true))

        subject.onAction(PackPurchaseAction.ReturnAfterPurchase)

        assertThat(pendingExportStart.applicationId.value).isNull()
    }

    @Test
    fun returnAfterPurchase_withNoApplicationStartsNothing() = runTest {
        val subject = viewModel()
        subject.onEnter(key(applicationId = "", startExportOnReturn = true))
        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        subject.onAction(PackPurchaseAction.ReturnAfterPurchase)

        assertThat(pendingExportStart.applicationId.value).isNull()
    }

    @Test
    fun loadingScenario_neverAsksTheGateway() = runTest {
        val subject = entered(DebugScenario.LOADING)

        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.LOADING)
        assertThat(subject.uiState.value.packs).isEmpty()
    }

    @Test
    fun errorScenario_isACatalogueFailureWithoutInventingAPack() = runTest {
        val subject = entered(DebugScenario.ERROR)

        val state = subject.uiState.value
        assertThat(state.isCatalogueFailure).isTrue()
        assertThat(state.packs).isEmpty()

        subject.onAction(PackPurchaseAction.ReloadPacks)
        assertThat(subject.uiState.value.isCatalogueFailure).isTrue()
    }

    @Test
    fun offlineScenario_blocksTheBuyButKeepsTheCatalogue() = runTest {
        val subject = entered(DebugScenario.OFFLINE)

        val state = subject.uiState.value
        assertThat(state.isOffline).isTrue()
        assertThat(state.hasCatalogue).isTrue()
        assertThat(state.canBuy).isFalse()

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.READY)
        assertThat(gateway.entitlement().purchasedCredits).isEqualTo(0)
    }

    @Test
    fun theConnectivityMonitorDecidesOffline() = runTest {
        connectivity.setOnline(false)
        val subject = entered()
        assertThat(subject.uiState.value.canBuy).isFalse()

        connectivity.setOnline(true)

        assertThat(subject.uiState.value.canBuy).isTrue()
    }

    @Test
    fun aRequestedSecondPackIsTheSelectedOne() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        val subject = viewModel()

        subject.onEnter(key(packId = ApplicationPack.SINGLE_APPLICATION))

        assertThat(subject.uiState.value.selectedPack?.id).isEqualTo(ApplicationPack.SINGLE_APPLICATION)
    }

    @Test
    fun success_countsFromTheOldBalanceToTheNewOneAndKeepsAReceipt() = runTest {
        val subject = entered()
        gateway.consumeCredit()

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        val state = subject.uiState.value
        assertThat(state.stage).isEqualTo(PackPurchaseStage.SUCCESS)
        assertThat(state.creditsBefore).isEqualTo(0)
        assertThat(state.totalCredits).isEqualTo(5)
        assertThat(state.receipt?.credits).isEqualTo(5)
        assertThat(state.receipt?.formattedPrice).isEqualTo("₹149")
        assertThat(state.receipt?.formattedDate).isNotEmpty()
        assertThat(state.creditsNeverExpire).isTrue()
    }

    @Test
    fun pending_addsNoCredits() = runTest {
        gateway.withOutcome(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Pending)
        val subject = entered()

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.PENDING)
        assertThat(subject.uiState.value.totalCredits).isEqualTo(1)
    }

    @Test
    fun cancelled_chargesNothing() = runTest {
        gateway.withOutcome(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Cancelled)
        val subject = entered()

        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))

        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.CANCELLED)
        assertThat(gateway.entitlement().purchasedCredits).isEqualTo(0)
    }

    @Test
    fun failed_keepsTheRealReasonAndTryAgainBuysTheSamePack() = runTest {
        gateway.withOutcome(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Failed)
            .withFailureReason(PurchaseFailureReason.PaymentDeclined)
        val subject = entered()
        subject.onAction(PackPurchaseAction.Buy(ApplicationPack.APPLICATION_PACK_FIVE))
        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.FAILED)
        assertThat(subject.uiState.value.failureReason).isEqualTo(PurchaseFailureReason.PaymentDeclined)

        gateway.withOutcome(ApplicationPack.APPLICATION_PACK_FIVE, PurchaseOutcome.Success)
        subject.onAction(PackPurchaseAction.RetryBuy)

        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.SUCCESS)
        assertThat(gateway.entitlement().purchasedCredits).isEqualTo(5)
    }

    @Test
    fun buyingAnUnknownPack_isIgnored() = runTest {
        val subject = entered()

        subject.onAction(PackPurchaseAction.Buy("no-such-pack"))

        assertThat(subject.uiState.value.stage).isEqualTo(PackPurchaseStage.READY)
    }

    @Test
    fun format_defaultsToPdf() = runTest {
        val subject = entered()

        assertThat(subject.uiState.value.format).isEqualTo(ExportFormat.PDF)
    }

    @Test
    fun format_followsTheKeyWhenDocxWasPicked() = runTest {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        val subject = viewModel()

        subject.onEnter(key(format = "docx"))

        assertThat(subject.uiState.value.format).isEqualTo(ExportFormat.DOCX)
    }
}
