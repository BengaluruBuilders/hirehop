package com.hirehop.feature.settings.impl.settings

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.PurchaseEntitlement
import com.hirehop.core.domain.SignInAccount
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.offline.OfflineSignInGateway
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.settings.api.navigation.SettingsNavKey
import com.hirehop.feature.settings.impl.yourdata.TestSettingsPaymentGateway
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var paymentGateway: TestSettingsPaymentGateway
    private lateinit var signInGateway: SignInGateway
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setup() {
        paymentGateway = TestSettingsPaymentGateway(
            entitlement = PurchaseEntitlement(
                freeCredits = 1,
                purchasedCredits = 3,
                pendingPackIds = listOf("application_pack_5"),
            ),
        )
        signInGateway = OfflineSignInGateway()
        viewModel = SettingsViewModel(
            paymentGateway = paymentGateway,
            signInGateway = signInGateway,
        )
    }

    @Test
    fun onEnter_beforeAnyEntry_showsEveryGroup() {
        assertThat(viewModel.uiState.value.groups.map { group -> group.label }).containsExactly(
            SettingsGroupLabel.ACCOUNT,
            SettingsGroupLabel.CREDITS,
            SettingsGroupLabel.YOUR_DATA,
            SettingsGroupLabel.PRIVACY,
            SettingsGroupLabel.ABOUT,
            SettingsGroupLabel.DELETE_ACCOUNT,
        )
    }

    @Test
    fun onEnter_default_readsTheRealCreditTotalFromTheGateway() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        val state = viewModel.uiState.first()
        assertThat(state.creditsLeft).isEqualTo(4)
        assertThat(state.isOffline).isFalse()
    }

    @Test
    fun onEnter_default_withNoSignedInAccount_saysSoInWords() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        val accountRow = viewModel.uiState.first().rowOf(SettingsRowKey.ACCOUNT)
        assertThat(accountRow.supporting).isEqualTo(SettingsSupporting.NO_ACCOUNT)
        assertThat(accountRow.showsAccountSignedInAs).isFalse()
    }

    @Test
    fun onEnter_default_afterSignIn_namesTheSignedInAccount() = runTest {
        signInGateway = OfflineSignInGateway().apply {
            signIn()
        }
        viewModel = SettingsViewModel(paymentGateway = paymentGateway, signInGateway = signInGateway)

        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        val state = viewModel.uiState.first()
        assertThat(state.accountDisplayName).isEqualTo(SignInAccount.localAccount.displayName)
        assertThat(state.rowOf(SettingsRowKey.ACCOUNT).showsAccountSignedInAs).isTrue()
    }

    @Test
    fun onEnter_default_printsTheGrievanceContactRow() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        val row = viewModel.uiState.first().rowOf(SettingsRowKey.GRIEVANCE_CONTACT)
        assertThat(row.slot).isEqualTo(SettingsSlotKind.GRIEVANCE_CONTACT_ADDRESS)
    }

    @Test
    fun onEnter_default_printsNoPrivacyOrWebAddressItCannotKnow() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        val state = viewModel.uiState.first()
        assertThat(state.rowOf(SettingsRowKey.PRIVACY_POLICY).slot)
            .isEqualTo(SettingsSlotKind.PRIVACY_POLICY_ADDRESS)
        assertThat(state.rowOf(SettingsRowKey.DELETE_ACCOUNT_WEB).slot)
            .isEqualTo(SettingsSlotKind.DELETE_ACCOUNT_WEB_ADDRESS)
    }

    @Test
    fun onEnter_default_omitsTheDesignFixtureConsentDate() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        val row = viewModel.uiState.first().rowOf(SettingsRowKey.CONSENT_NOTICE)
        assertThat(row.supporting).isEqualTo(SettingsSupporting.CONSENT_NOTICE)
        assertThat(row.consentDate).isNull()
    }

    @Test
    fun onEnter_offline_saysInWordsThatTheNetworkRowsAreOff() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.OFFLINE))

        val state = viewModel.uiState.first()
        assertThat(state.isOffline).isTrue()
        assertThat(state.rowOf(SettingsRowKey.PRIVACY_POLICY).supporting)
            .isEqualTo(SettingsSupporting.PRIVACY_POLICY_OFFLINE)
        assertThat(state.rowOf(SettingsRowKey.DELETE_ACCOUNT).supporting)
            .isEqualTo(SettingsSupporting.DELETE_ACCOUNT_OFFLINE)
    }

    @Test
    fun onEnter_offline_disablesTheDeleteAccountRowOnly() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.OFFLINE))

        val state = viewModel.uiState.first()
        assertThat(state.rowOf(SettingsRowKey.DELETE_ACCOUNT).isEnabled).isFalse()
        assertThat(state.rowOf(SettingsRowKey.YOUR_DATA).isEnabled).isTrue()
        assertThat(state.rowOf(SettingsRowKey.CONSENT_NOTICE).isEnabled).isTrue()
        assertThat(state.rowOf(SettingsRowKey.GRIEVANCE_CONTACT).isEnabled).isTrue()
    }

    @Test
    fun onEnter_whenTheGatewayFails_readsZeroCreditsRatherThanFailing() = runTest {
        paymentGateway = TestSettingsPaymentGateway().withFailure()
        viewModel = SettingsViewModel(paymentGateway = paymentGateway, signInGateway = signInGateway)

        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.first().creditsLeft).isEqualTo(0)
    }

    @Test
    fun onEnter_whenCalledTwice_keepsTheFirstState() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.OFFLINE))
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.isOffline).isTrue()
    }

    @Test
    fun onAction_deleteAccountRow_asksForTheDeleteAccountScreen() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(SettingsAction.DestinationSelected(SettingsDestination.DELETE_ACCOUNT))

        assertThat(viewModel.uiState.value.destination).isEqualTo(SettingsDestination.DELETE_ACCOUNT)
    }

    @Test
    fun onAction_destinationConsumed_clearsTheDestination() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))
        viewModel.onAction(SettingsAction.DestinationSelected(SettingsDestination.YOUR_DATA))

        viewModel.onAction(SettingsAction.DestinationConsumed)

        assertThat(viewModel.uiState.value.destination).isNull()
    }

    @Test
    fun onAction_yourDataRow_asksForTheYourDataScreen() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(SettingsAction.DestinationSelected(SettingsDestination.YOUR_DATA))

        assertThat(viewModel.uiState.value.destination).isEqualTo(SettingsDestination.YOUR_DATA)
    }

    @Test
    fun onAction_signOutRow_asksForSignOut() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.DEFAULT))

        viewModel.onAction(SettingsAction.DestinationSelected(SettingsDestination.SIGN_OUT))

        assertThat(viewModel.uiState.value.destination).isEqualTo(SettingsDestination.SIGN_OUT)
    }

    @Test
    fun onAction_offline_ignoresADisabledDeleteAccountRow() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.OFFLINE))

        viewModel.onAction(SettingsAction.DestinationSelected(SettingsDestination.DELETE_ACCOUNT))

        assertThat(viewModel.uiState.value.destination).isNull()
    }

    @Test
    fun onAction_offline_stillAllowsTheLocalRows() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.OFFLINE))

        viewModel.onAction(SettingsAction.DestinationSelected(SettingsDestination.YOUR_DATA))

        assertThat(viewModel.uiState.value.destination).isEqualTo(SettingsDestination.YOUR_DATA)
    }

    @Test
    fun onEnter_offline_theConsentNoticeRowStillReadsLocally() = runTest {
        viewModel.onEnter(SettingsNavKey(scenario = DebugScenario.OFFLINE))

        val row = viewModel.uiState.value.rowOf(SettingsRowKey.CONSENT_NOTICE)
        assertThat(row.isEnabled).isTrue()
        assertThat(row.destination).isEqualTo(SettingsDestination.CONSENT_NOTICE)
    }

    @Test
    fun settingsGroups_alwaysCarryTheAboutPromiseRow() {
        val about = settingsGroups().first { group -> group.label == SettingsGroupLabel.ABOUT }

        assertThat(about.rows.map { row -> row.key }).containsExactly(
            SettingsRowKey.PROMISE,
            SettingsRowKey.VERSION,
        )
    }

    private fun SettingsUiState.rowOf(key: SettingsRowKey): SettingsRowState = groups
        .flatMap { group -> group.rows }
        .first { row -> row.key == key }
}
