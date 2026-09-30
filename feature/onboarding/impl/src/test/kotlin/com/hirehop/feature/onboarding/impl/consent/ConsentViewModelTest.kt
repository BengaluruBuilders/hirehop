package com.hirehop.feature.onboarding.impl.consent

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConsentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ConsentViewModel

    @Before
    fun setup() {
        viewModel = ConsentViewModel()
    }

    @Test
    fun defaultScenario_nothingIsPreTicked() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.acknowledgedCount).isEqualTo(0)
        assertThat(viewModel.uiState.value.isEveryPurposeAcknowledged).isFalse()
        assertThat(viewModel.uiState.value.canAgree).isFalse()
        assertThat(viewModel.uiState.value.isDeclined).isFalse()
    }

    @Test
    fun thereAreExactlyThreePurposes() {
        viewModel.onEnter(ConsentNavKey())

        assertThat(ConsentPurpose.entries).hasSize(3)
        assertThat(viewModel.uiState.value.entries).hasSize(3)
    }

    @Test
    fun onEnter_whenCalledTwice_keepsTheFirstState() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.SUCCESS))

        viewModel.onEnter(ConsentNavKey(DebugScenario.DEFAULT))

        assertThat(viewModel.uiState.value.isEveryPurposeAcknowledged).isTrue()
    }

    @Test
    fun eachToggleFlipsItsOwnPurpose() {
        viewModel.onEnter(ConsentNavKey())
        val purpose = ConsentPurpose.ANALYSE_ON_DEVICE

        viewModel.onAction(ConsentAction.PurposeToggled(purpose))
        assertThat(viewModel.uiState.value.isAcknowledged(purpose)).isTrue()

        viewModel.onAction(ConsentAction.PurposeToggled(purpose))
        assertThat(viewModel.uiState.value.isAcknowledged(purpose)).isFalse()
    }

    @Test
    fun togglingOnePurpose_leavesTheOthersUntouched() {
        viewModel.onEnter(ConsentNavKey())
        val purpose = ConsentPurpose.READ_AND_BUILD

        viewModel.onAction(ConsentAction.PurposeToggled(purpose))

        assertThat(viewModel.uiState.value.isAcknowledged(ConsentPurpose.ANALYSE_ON_DEVICE)).isFalse()
        assertThat(viewModel.uiState.value.isAcknowledged(ConsentPurpose.KEEP_CONFIRMED_FACTS)).isFalse()
        assertThat(viewModel.uiState.value.acknowledgedCount).isEqualTo(1)
    }

    @Test
    fun allThreeTicked_makesTheProceedActionAvailable() {
        viewModel.onEnter(ConsentNavKey())

        ConsentPurpose.entries.forEach { viewModel.onAction(ConsentAction.PurposeToggled(it)) }

        assertThat(viewModel.uiState.value.acknowledgedCount).isEqualTo(3)
        assertThat(viewModel.uiState.value.isEveryPurposeAcknowledged).isTrue()
        assertThat(viewModel.uiState.value.canAgree).isTrue()
    }

    @Test
    fun agree_withoutEveryPurpose_doesNothing() {
        viewModel.onEnter(ConsentNavKey())
        viewModel.onAction(ConsentAction.PurposeToggled(ConsentPurpose.READ_AND_BUILD))

        viewModel.onAction(ConsentAction.Agree)

        assertThat(viewModel.uiState.value.acknowledgedCount).isEqualTo(1)
    }

    @Test
    fun agree_withEveryPurpose_keepsEveryPurposeTicked() {
        viewModel.onEnter(ConsentNavKey())
        ConsentPurpose.entries.forEach { viewModel.onAction(ConsentAction.PurposeToggled(it)) }

        viewModel.onAction(ConsentAction.Agree)

        assertThat(viewModel.uiState.value.isEveryPurposeAcknowledged).isTrue()
        assertThat(viewModel.uiState.value.isDeclined).isFalse()
    }

    @Test
    fun loadingScenario_showsTheSaveStep() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.LOADING))

        assertThat(viewModel.uiState.value.isSaving).isTrue()
        assertThat(viewModel.uiState.value.canAgree).isFalse()
    }

    @Test
    fun emptyScenario_isTheDeclinedPath() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.EMPTY))

        assertThat(viewModel.uiState.value.isDeclined).isTrue()
        assertThat(viewModel.uiState.value.showAgreementActions).isFalse()
    }

    @Test
    fun offlineScenario_keepsEveryActionAvailable() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.OFFLINE))
        ConsentPurpose.entries.forEach { viewModel.onAction(ConsentAction.PurposeToggled(it)) }

        assertThat(viewModel.uiState.value.isOffline).isTrue()
        assertThat(viewModel.uiState.value.canAgree).isTrue()
    }

    @Test
    fun errorScenario_showsThePlainLedger() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.ERROR))

        assertThat(viewModel.uiState.value.isDeclined).isFalse()
        assertThat(viewModel.uiState.value.acknowledgedCount).isEqualTo(0)
    }

    @Test
    fun partialScenario_showsThePlainLedger() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.PARTIAL))

        assertThat(viewModel.uiState.value.acknowledgedCount).isEqualTo(0)
    }

    @Test
    fun successScenario_hasEveryPurposeAcknowledged() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.SUCCESS))

        assertThat(viewModel.uiState.value.isEveryPurposeAcknowledged).isTrue()
        assertThat(viewModel.uiState.value.canAgree).isTrue()
    }

    @Test
    fun userStatedScenario_showsThePlainLedger() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.USER_STATED))

        assertThat(viewModel.uiState.value.acknowledgedCount).isEqualTo(0)
    }

    @Test
    fun notNow_declinesWithoutBlocking() {
        viewModel.onEnter(ConsentNavKey())

        viewModel.onAction(ConsentAction.NotNow)

        assertThat(viewModel.uiState.value.isDeclined).isTrue()
    }

    @Test
    fun notNow_keepsTheTicksTheUserAlreadyMade() {
        viewModel.onEnter(ConsentNavKey())
        viewModel.onAction(ConsentAction.PurposeToggled(ConsentPurpose.READ_AND_BUILD))

        viewModel.onAction(ConsentAction.NotNow)

        assertThat(viewModel.uiState.value.acknowledgedCount).isEqualTo(1)
    }

    @Test
    fun readAgain_returnsToTheLedger() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.EMPTY))

        viewModel.onAction(ConsentAction.ReadAgain)

        assertThat(viewModel.uiState.value.isDeclined).isFalse()
        assertThat(viewModel.uiState.value.showAgreementActions).isTrue()
    }

    @Test
    fun readAgain_doesNotPreTickAnything() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.EMPTY))

        viewModel.onAction(ConsentAction.ReadAgain)

        assertThat(viewModel.uiState.value.acknowledgedCount).isEqualTo(0)
    }
}
