package com.hirehop.feature.tailor.impl.exported

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.feature.tailor.api.navigation.ExportedNavKey
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.packpurchase.TestPaymentGateway
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

private const val APPLICATION_ID = "application-northwind-1"

private const val EXPORT_DIRECTORY = "exports"

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class ExportedScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    private val applicationRepository = TestApplicationRepository()
    private val profileRepository = TestProfileRepository()
    private val paymentGateway = TestPaymentGateway()

    @Test
    fun freeCreditUsed_saysTheFreeApplicationPaidForTheExport() {
        capture("ExportedFreeCreditUsed", stateFor(freeCredits = 1, purchasedCredits = 0))
    }

    @Test
    fun paidCreditUsed_rollsTheCounterDownByOne() {
        capture("ExportedPaidCreditUsed", stateFor(freeCredits = 0, purchasedCredits = 5))
    }

    @Test
    fun shareRequested_leavesTheAppShareSheetToAndroid() {
        capture(
            screenName = "ExportedShareRequested",
            viewModel = stateFor(freeCredits = 0, purchasedCredits = 5),
        ) { state, viewModel ->
            writeExportedFile(state.fileName)
            viewModel.onAction(ExportedAction.RequestShare)
        }
    }

    @Test
    fun markedApplied_swapsThePromptForTheStatusChip() {
        capture(
            screenName = "ExportedMarkedApplied",
            viewModel = stateFor(freeCredits = 0, purchasedCredits = 5),
        ) { _, viewModel ->
            viewModel.onAction(ExportedAction.OpenStatusSheet)
            viewModel.onAction(ExportedAction.ConfirmStatus(ApplicationStatus.APPLIED))
        }
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun markedApplied_atLargeTextKeepsTheStatusChipWhole() {
        capture(
            screenName = "ExportedMarkedAppliedFont200",
            viewModel = stateFor(freeCredits = 0, purchasedCredits = 5),
            device = HhTestDevices.boardLargeFont,
        ) { _, viewModel ->
            viewModel.onAction(ExportedAction.OpenStatusSheet)
            viewModel.onAction(ExportedAction.ConfirmStatus(ApplicationStatus.APPLIED))
        }
    }

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun paidCreditUsed_atLargeTextStacksTheRowsAndKeepsTheChips() {
        capture(
            "ExportedPaidCreditUsedFont200",
            stateFor(freeCredits = 0, purchasedCredits = 5),
            device = HhTestDevices.boardLargeFont,
        )
    }

    private fun capture(
        screenName: String,
        viewModel: ExportedViewModel,
        device: HhTestDevice = HhTestDevices.board,
        prepare: ((ExportedUiState, ExportedViewModel) -> Unit)? = null,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            ExportedHost(viewModel = viewModel, dark = darkTheme.value)
        }
        composeRule.waitForIdle()
        writeExportedFile(viewModel.uiState.value.fileName)
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        prepare?.invoke(viewModel.uiState.value, viewModel)
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }

    private fun stateFor(
        freeCredits: Int,
        purchasedCredits: Int,
    ): ExportedViewModel {
        applicationRepository.sendApplications(listOf(canonicalApplication))
        profileRepository.sendProfile(canonicalCandidateProfile)
        paymentGateway.withFreeCredits(credits = freeCredits)
            .withPurchasedCredits(credits = purchasedCredits)
        val viewModel = ExportedViewModel(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            assembler = ResumeDocumentAssembler(),
            paymentGateway = paymentGateway,
            fileStore = ExportedFileStore(ApplicationProvider.getApplicationContext()),
        )
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        return viewModel
    }

    private fun writeExportedFile(fileName: String) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val directory = File(context.cacheDir, EXPORT_DIRECTORY)
        directory.mkdirs()
        File(directory, fileName).writeText("%PDF-1.4")
    }
}

@Composable
private fun ExportedHost(
    viewModel: ExportedViewModel,
    dark: Boolean,
) {
    val uiState by viewModel.uiState.collectAsState()
    com.hirehop.core.designsystem.theme.HhTheme(darkTheme = dark) {
        ExportedScreen(
            uiState = uiState,
            actions = ExportedActions(
                onOpenStatusSheet = {},
                onDismissStatusSheet = {},
                onConfirmStatus = {},
                onShare = {},
                onNavigateBack = {},
            ),
        )
    }
}
