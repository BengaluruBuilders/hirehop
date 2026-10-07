package com.hirehop.feature.onboarding.impl.consent

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.SignInAccount
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.core.testing.util.TestClock
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ConsentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val session = TestSessionRepository()

    private val clock = TestClock()

    private var uploadResult: Result<Unit> = Result.success(Unit)

    private lateinit var viewModel: ConsentViewModel

    @Before
    fun setup() {
        viewModel = ConsentViewModel(
            sessionRepository = session,
            consentUploader = { uploadResult },
            nextOnboardingStep = NextOnboardingStepUseCase(session, TestProfileRepository()),
            clock = clock,
        )
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
    fun thereAreExactlyFourPurposes() {
        viewModel.onEnter(ConsentNavKey())

        assertThat(ConsentPurpose.entries).hasSize(4)
        assertThat(viewModel.uiState.value.entries).hasSize(4)
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
        val purpose = ConsentPurpose.AI_PROCESSING

        viewModel.onAction(ConsentAction.PurposeToggled(purpose))
        assertThat(viewModel.uiState.value.isAcknowledged(purpose)).isTrue()

        viewModel.onAction(ConsentAction.PurposeToggled(purpose))
        assertThat(viewModel.uiState.value.isAcknowledged(purpose)).isFalse()
    }

    @Test
    fun agreeStaysOffUntilEveryPurposeIsTicked() {
        viewModel.onEnter(ConsentNavKey())
        ConsentPurpose.entries.dropLast(1).forEach { viewModel.onAction(ConsentAction.PurposeToggled(it)) }

        assertThat(viewModel.uiState.value.canAgree).isFalse()

        viewModel.onAction(ConsentAction.PurposeToggled(ConsentPurpose.entries.last()))

        assertThat(viewModel.uiState.value.canAgree).isTrue()
    }

    @Test
    fun agree_withAPurposeUnticked_recordsNothing() = runTest {
        viewModel.onEnter(ConsentNavKey())
        viewModel.onAction(ConsentAction.PurposeToggled(ConsentPurpose.READ_AND_BUILD))

        viewModel.onAction(ConsentAction.Agree)

        assertThat(session.observeConsent().first()).isNull()
        assertThat(viewModel.uiState.value.nextStep).isNull()
    }

    @Test
    fun agree_recordsTheConsentAndAsksForTheNextStep() = runTest {
        session.sendAccount(SignInAccount.localAccount)
        viewModel.onEnter(ConsentNavKey())
        ConsentPurpose.entries.forEach { viewModel.onAction(ConsentAction.PurposeToggled(it)) }

        viewModel.onAction(ConsentAction.Agree)

        val record = session.observeConsent().first()
        assertThat(record?.purposes).containsExactlyElementsIn(ConsentPurpose.entries)
        assertThat(record?.acceptedAt).isEqualTo(clock.instant)
        assertThat(record?.noticeVersion).isEqualTo(ConsentRecord.CURRENT_NOTICE_VERSION)
        assertThat(viewModel.uiState.value.nextStep).isEqualTo(OnboardingStep.ImportResume)
        assertThat(viewModel.uiState.value.isSaving).isFalse()
    }

    @Test
    fun agree_whenTheServerRefuses_recordsNothingAndShowsTheFailure() = runTest {
        uploadResult = Result.failure(IllegalStateException("offline"))
        viewModel.onEnter(ConsentNavKey())
        ConsentPurpose.entries.forEach { viewModel.onAction(ConsentAction.PurposeToggled(it)) }

        viewModel.onAction(ConsentAction.Agree)

        assertThat(session.observeConsent().first()).isNull()
        assertThat(viewModel.uiState.value.uploadFailed).isTrue()
        assertThat(viewModel.uiState.value.isSaving).isFalse()
        assertThat(viewModel.uiState.value.nextStep).isNull()
    }

    @Test
    fun nextStepConsumed_clearsTheStep() = runTest {
        viewModel.onEnter(ConsentNavKey())
        ConsentPurpose.entries.forEach { viewModel.onAction(ConsentAction.PurposeToggled(it)) }
        viewModel.onAction(ConsentAction.Agree)

        viewModel.onAction(ConsentAction.NextStepConsumed)

        assertThat(viewModel.uiState.value.nextStep).isNull()
    }

    @Test
    fun notNow_showsTheDeclinedStateAndRecordsNothing() = runTest {
        viewModel.onEnter(ConsentNavKey())

        viewModel.onAction(ConsentAction.NotNow)

        assertThat(viewModel.uiState.value.isDeclined).isTrue()
        assertThat(viewModel.uiState.value.showAgreementActions).isFalse()
        assertThat(session.observeConsent().first()).isNull()
    }

    @Test
    fun readAgain_leavesTheDeclinedState() {
        viewModel.onEnter(ConsentNavKey())
        viewModel.onAction(ConsentAction.NotNow)

        viewModel.onAction(ConsentAction.ReadAgain)

        assertThat(viewModel.uiState.value.isDeclined).isFalse()
    }

    @Test
    fun emptyScenario_startsDeclined() {
        viewModel.onEnter(ConsentNavKey(DebugScenario.EMPTY))

        assertThat(viewModel.uiState.value.isDeclined).isTrue()
    }

    @Test
    fun readOnly_showsTheStoredRecordAndIgnoresToggles() = runTest {
        session.recordConsent(
            ConsentRecord(
                purposes = ConsentPurpose.entries.toSet(),
                acceptedAt = clock.instant,
                noticeVersion = ConsentRecord.CURRENT_NOTICE_VERSION,
            ),
        )
        viewModel.onEnter(ConsentNavKey(readOnly = true))

        viewModel.onAction(ConsentAction.PurposeToggled(ConsentPurpose.READ_AND_BUILD))

        val state = viewModel.uiState.value
        assertThat(state.isReadOnly).isTrue()
        assertThat(state.agreedAt).isEqualTo(clock.instant)
        assertThat(state.isEveryPurposeAcknowledged).isTrue()
        assertThat(state.canAgree).isFalse()
        assertThat(state.showAgreementActions).isFalse()
    }
}
