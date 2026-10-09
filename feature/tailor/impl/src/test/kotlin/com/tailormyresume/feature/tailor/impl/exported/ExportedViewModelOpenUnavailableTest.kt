package com.tailormyresume.feature.tailor.impl.exported

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.gateway.TestPaymentGateway
import com.tailormyresume.core.testing.repository.TestApplicationRepository
import com.tailormyresume.core.testing.repository.TestExportHistoryRepository
import com.tailormyresume.core.testing.repository.TestProfileRepository
import com.tailormyresume.core.testing.util.MainDispatcherRule
import com.tailormyresume.core.testing.util.TestClock
import com.tailormyresume.feature.tailor.api.navigation.ExportedNavKey
import com.tailormyresume.feature.tailor.impl.document.ResumeDocumentAssembler
import com.tailormyresume.feature.tailor.impl.document.TestResumeHeadings
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

private const val APPLICATION_ID = "application-northwind-1"

@RunWith(AndroidJUnit4::class)
class ExportedViewModelOpenUnavailableTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()

    private val viewModel = ExportedViewModel(
        applicationRepository = applicationRepository,
        profileRepository = profileRepository,
        assembler = ResumeDocumentAssembler(TestResumeHeadings),
        paymentGateway = TestPaymentGateway(),
        exportHistoryRepository = TestExportHistoryRepository(),
        fileStore = ExportedFileStore(context),
        clock = TestClock(),
    )

    private fun enteredWithAFile() {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT, spentFreeCredit = true))
        val directory = File(context.cacheDir, "exports").apply { mkdirs() }
        File(directory, viewModel.uiState.value.fileName).writeText("%PDF-1.4")
    }

    @Test
    fun whenNoAppCanOpenTheFile_theRequestIsDroppedAndTheMessageIsRaised() = runTest {
        enteredWithAFile()
        viewModel.onAction(ExportedAction.RequestOpen)

        viewModel.onAction(ExportedAction.OpenUnavailable)

        assertThat(viewModel.uiState.value.openUnavailable).isTrue()
        assertThat(viewModel.uiState.value.fileRequest).isNull()
    }

    @Test
    fun dismissingTheMessageClearsIt() = runTest {
        enteredWithAFile()
        viewModel.onAction(ExportedAction.OpenUnavailable)

        viewModel.onAction(ExportedAction.DismissOpenUnavailable)

        assertThat(viewModel.uiState.value.openUnavailable).isFalse()
    }
}
