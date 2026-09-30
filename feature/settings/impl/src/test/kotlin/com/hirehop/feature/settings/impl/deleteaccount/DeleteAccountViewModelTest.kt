package com.hirehop.feature.settings.impl.deleteaccount

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.account.AccountCreditBalance
import com.hirehop.core.domain.account.AccountDeletionStep
import com.hirehop.core.domain.account.DeleteAccountUseCase
import com.hirehop.core.domain.offline.OfflinePaymentGateway
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.JobApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.data.sampleApplication
import com.hirehop.core.testing.util.MainDispatcherRule
import com.hirehop.feature.settings.api.navigation.DeleteAccountNavKey
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DeleteAccountViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val applications = MutableStateFlow<List<JobApplication>>(FOUR_APPLICATIONS)
    private val profile = MutableStateFlow<CandidateProfile?>(canonicalCandidateProfile)
    private val gate = CompletableDeferred<Unit>()
    private var shouldFail = false
    private var waitsForGate = false

    @Test
    fun theDefaultStateCountsTheRealData() = runTest {
        val viewModel = viewModel()

        viewModel.onEnter(DeleteAccountNavKey())

        assertThat(viewModel.uiState.value.stage).isEqualTo(DeleteAccountStage.DEFAULT)
        assertThat(viewModel.uiState.value.counts.profileFacts).isEqualTo(18)
        assertThat(viewModel.uiState.value.counts.applications).isEqualTo(4)
        assertThat(viewModel.uiState.value.counts.unusedCredits).isEqualTo(4)
        assertThat(viewModel.uiState.value.isDeleteEnabled).isTrue()
        assertThat(viewModel.uiState.value.isBackEnabled).isTrue()
    }

    @Test
    fun theDeletingStateNamesTheStepsAndLocksBack() = runTest {
        waitsForGate = true
        val viewModel = viewModel()

        viewModel.onEnter(DeleteAccountNavKey())
        viewModel.onAction(DeleteAccountAction.DeleteAccountTapped)

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(DeleteAccountStage.DELETING)
        assertThat(state.isBackEnabled).isFalse()
        assertThat(state.isDeleteEnabled).isFalse()
        assertThat(state.steps.map { it.step }).containsExactly(
            AccountDeletionStep.DELETING_APPLICATIONS,
            AccountDeletionStep.DELETING_PROFILE_FACTS,
            AccountDeletionStep.CLOSING_ACCOUNT,
        ).inOrder()
        assertThat(state.steps.count { it.isCurrent }).isEqualTo(1)
        assertThat(state.steps.count { it.isPending }).isEqualTo(2)
        gate.complete(Unit)
    }

    @Test
    fun theDoneStateMeansTheDataIsActuallyGone() = runTest {
        val viewModel = viewModel()

        viewModel.onEnter(DeleteAccountNavKey())
        viewModel.onAction(DeleteAccountAction.DeleteAccountTapped)

        assertThat(viewModel.uiState.value.stage).isEqualTo(DeleteAccountStage.DONE)
        assertThat(applications.value).isEmpty()
        assertThat(profile.value).isNull()
    }

    @Test
    fun theErrorStateSaysTheDataIsStillThereBecauseItIs() = runTest {
        shouldFail = true
        val viewModel = viewModel()

        viewModel.onEnter(DeleteAccountNavKey())
        viewModel.onAction(DeleteAccountAction.DeleteAccountTapped)

        val state = viewModel.uiState.value
        assertThat(state.stage).isEqualTo(DeleteAccountStage.ERROR)
        assertThat(state.isDataIntact).isTrue()
        assertThat(state.isBackEnabled).isTrue()
        assertThat(applications.value.map { it.id }).containsExactlyElementsIn(
            FOUR_APPLICATIONS.map { it.id },
        )
        assertThat(profile.value).isEqualTo(canonicalCandidateProfile)
    }

    @Test
    fun theOfflineStateBlocksTheButtonAndKeepsTheCopy() = runTest {
        val viewModel = viewModel()

        viewModel.onEnter(DeleteAccountNavKey(scenario = DebugScenario.OFFLINE))
        viewModel.onAction(DeleteAccountAction.DeleteAccountTapped)

        val state = viewModel.uiState.value
        assertThat(state.isOffline).isTrue()
        assertThat(state.isDeleteEnabled).isFalse()
        assertThat(state.stage).isEqualTo(DeleteAccountStage.DEFAULT)
        assertThat(applications.value).isNotEmpty()
    }

    @Test
    fun theDoneStateSendsTheUserBackToWelcome() = runTest {
        val viewModel = viewModel()

        viewModel.onEnter(DeleteAccountNavKey(scenario = DebugScenario.SUCCESS))
        viewModel.onAction(DeleteAccountAction.BackToWelcomeTapped)

        assertThat(viewModel.uiState.value.destination).isEqualTo(DeleteAccountDestination.WELCOME)
    }

    @Test
    fun theDestinationIsConsumedSoItFiresOnce() = runTest {
        val viewModel = viewModel()

        viewModel.onEnter(DeleteAccountNavKey())
        viewModel.onAction(DeleteAccountAction.DownloadDataTapped)
        viewModel.onAction(DeleteAccountAction.DestinationConsumed)

        assertThat(viewModel.uiState.value.destination).isNull()
    }

    @Test
    fun keepingTheAccountChangesNothing() = runTest {
        val viewModel = viewModel()

        viewModel.onEnter(DeleteAccountNavKey())
        viewModel.onAction(DeleteAccountAction.KeepAccountTapped)

        assertThat(viewModel.uiState.value.stage).isEqualTo(DeleteAccountStage.DEFAULT)
        assertThat(applications.value).isNotEmpty()
    }

    @Test
    fun enteringTwiceKeepsTheFirstCounts() = runTest {
        val viewModel = viewModel()

        viewModel.onEnter(DeleteAccountNavKey())
        applications.value = emptyList()
        viewModel.onEnter(DeleteAccountNavKey())

        assertThat(viewModel.uiState.value.counts.applications).isEqualTo(4)
    }

    private fun viewModel(): DeleteAccountViewModel = DeleteAccountViewModel(
        deleteAccount = DeleteAccountUseCase(
            applicationRepository = GateApplicationRepository(
                applications = applications,
                gate = gate,
                waits = { waitsForGate },
                shouldFail = { shouldFail },
            ),
            profileRepository = StaticProfileRepository(profile = profile),
            creditBalance = AccountCreditBalance(
                paymentGateway = OfflinePaymentGateway().withFreeCredits(4),
            ),
        ),
    )

    private class GateApplicationRepository(
        private val applications: MutableStateFlow<List<JobApplication>>,
        private val gate: CompletableDeferred<Unit>,
        private val waits: () -> Boolean,
        private val shouldFail: () -> Boolean,
    ) : ApplicationRepository {

        override fun observeApplications(): Flow<List<JobApplication>> = applications

        override fun observeApplication(id: String): Flow<JobApplication?> = MutableStateFlow(null)

        override suspend fun upsertApplication(application: JobApplication) {
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
            if (waits()) gate.await()
            if (shouldFail()) throw IllegalStateException("delete failed for $id")
            applications.update { current -> current.filterNot { it.id == id } }
        }
    }

    private class StaticProfileRepository(
        private val profile: MutableStateFlow<CandidateProfile?>,
    ) : ProfileRepository {

        override fun observeProfile(): Flow<CandidateProfile?> = profile

        override suspend fun saveProfile(profile: CandidateProfile) {
            this.profile.value = profile
        }

        override suspend fun clearProfile() {
            profile.value = null
        }
    }

    private companion object {
        val FOUR_APPLICATIONS = listOf("1", "2", "3", "4").map { index ->
            sampleApplication.copy(id = "application-$index", status = ApplicationStatus.SAVED)
        }
    }
}
