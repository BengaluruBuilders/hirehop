package com.hirehop.feature.profile.impl.guidedform

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.AddUserStatedFactsUseCase
import com.hirehop.core.domain.onboarding.NextOnboardingStepUseCase
import com.hirehop.core.testing.connectivity.TestConnectivityMonitor
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.core.testing.repository.TestSessionRepository
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.core.testing.util.TestIdGenerator
import com.hirehop.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.hirehop.feature.profile.impl.ProfileExitResolver
import com.hirehop.feature.profile.impl.UserFactWriter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class GuidedFormContactValidationTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestProfileRepository()
    private val session = TestSessionRepository()
    private val connectivity = TestConnectivityMonitor()
    private val viewModel = GuidedFormViewModel(
        factWriter = UserFactWriter(repository, AddUserStatedFactsUseCase(repository, TestIdGenerator())),
        exitResolver = ProfileExitResolver(NextOnboardingStepUseCase(session, repository), session),
        connectivityMonitor = connectivity,
    )

    private fun enter(key: GuidedProfileFormNavKey = GuidedProfileFormNavKey()) = viewModel.onEnter(key)

    private fun type(field: GuidedField, value: String) =
        viewModel.onAction(GuidedFormAction.ValueChanged(field, value))

    private fun act(action: GuidedFormAction) = viewModel.onAction(action)

    @Test
    fun next_onContactWithAnInvalidEmail_flagsTheEmailAndStays() = runTest {
        enter()
        type(GuidedField.FULL_NAME, "Priya Deshmukh")
        type(GuidedField.EMAIL, "not-an-email")

        act(GuidedFormAction.Next)

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.CONTACT)
        assertThat(viewModel.uiState.value.fieldProblems[GuidedField.EMAIL]).isEqualTo(GuidedFieldProblem.INVALID_EMAIL)
        assertThat(repository.observeProfile().first()).isNull()
    }

    @Test
    fun next_onContactWithAnInvalidPhone_flagsThePhoneAndStays() = runTest {
        enter()
        type(GuidedField.FULL_NAME, "Priya Deshmukh")
        type(GuidedField.EMAIL, "priya.d@example.com")
        type(GuidedField.PHONE, "abc")

        act(GuidedFormAction.Next)

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.CONTACT)
        assertThat(viewModel.uiState.value.fieldProblems[GuidedField.PHONE]).isEqualTo(GuidedFieldProblem.INVALID_PHONE)
        assertThat(repository.observeProfile().first()).isNull()
    }

    @Test
    fun next_onContactWithAValidEmailAndPhone_advancesWithoutFieldProblems() = runTest {
        enter()
        type(GuidedField.FULL_NAME, "Priya Deshmukh")
        type(GuidedField.EMAIL, "priya.d@example.com")
        type(GuidedField.PHONE, "+91 98765 43210")

        act(GuidedFormAction.Next)

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EDUCATION)
        assertThat(viewModel.uiState.value.fieldProblems).isEmpty()
    }

    @Test
    fun next_onContactWithOnlyAName_advancesWithoutFieldProblems() = runTest {
        enter()
        type(GuidedField.FULL_NAME, "Priya Deshmukh")

        act(GuidedFormAction.Next)

        assertThat(viewModel.uiState.value.step).isEqualTo(GuidedStep.EDUCATION)
    }

    @Test
    fun next_onContactAfterAnInvalidEmail_thenTypingClearsTheFieldProblem() = runTest {
        enter()
        type(GuidedField.FULL_NAME, "Priya Deshmukh")
        type(GuidedField.EMAIL, "not-an-email")

        act(GuidedFormAction.Next)
        assertThat(viewModel.uiState.value.fieldProblems[GuidedField.EMAIL]).isEqualTo(GuidedFieldProblem.INVALID_EMAIL)

        type(GuidedField.EMAIL, "priya.d@example.com")

        assertThat(viewModel.uiState.value.fieldProblems).doesNotContainKey(GuidedField.EMAIL)
    }

    @Test
    fun contactFieldValidator_acceptsOnlyWellFormedEmailsAndPhones() {
        assertThat(ContactFieldValidator.isValidEmail("a.b@c.in")).isTrue()
        assertThat(ContactFieldValidator.isValidEmail("a@b")).isFalse()
        assertThat(ContactFieldValidator.isValidEmail("x y@z.com")).isFalse()

        assertThat(ContactFieldValidator.isValidPhone("9876543210")).isTrue()
        assertThat(ContactFieldValidator.isValidPhone("(080) 2345-6789")).isTrue()
        assertThat(ContactFieldValidator.isValidPhone("+44 20 7946 0958")).isTrue()
        assertThat(ContactFieldValidator.isValidPhone("abc")).isFalse()
        assertThat(ContactFieldValidator.isValidPhone("12345")).isFalse()
        assertThat(ContactFieldValidator.isValidPhone("+1234567890123456")).isFalse()
    }
}
