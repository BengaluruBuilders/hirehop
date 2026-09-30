package com.hirehop.feature.tailor.impl.exported

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.testing.data.canonicalApplication
import com.hirehop.core.testing.data.canonicalCandidateProfile
import com.hirehop.core.testing.repository.TestApplicationRepository
import com.hirehop.core.testing.repository.TestProfileRepository
import com.hirehop.feature.tailor.api.navigation.ExportedNavKey
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import com.hirehop.feature.tailor.impl.packpurchase.TestPaymentGateway
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w393dp-h852dp-normal-long-notround-any-440dpi-keyshidden-nonav")
class ExportedStatusSheetRenderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val confirmed = mutableStateOf<ApplicationStatus?>(null)
    private var dismissals: Int = 0

    @Test
    fun theSheetOffersEveryStatusAndSavesTheCurrentValue() {
        host(status = ApplicationStatus.SAVED)
        composeRule.onNodeWithText("Saved").assertIsDisplayed()
        composeRule.onNodeWithText("Applied").assertIsDisplayed()
        composeRule.onNodeWithText("Interview").assertIsDisplayed()
        composeRule.onNodeWithText("Offer").assertIsDisplayed()
        composeRule.onNodeWithText("Rejected").assertIsDisplayed()
        composeRule.onNodeWithText("No response").assertIsDisplayed()

        composeRule.onNodeWithText("Save").performClick()

        assert(confirmed.value == ApplicationStatus.SAVED) { "expected Saved, got ${confirmed.value}" }
        assert(dismissals == 0) { "Save must not dismiss" }
    }

    @Test
    fun theSheetSavesTheStatusTheUserPicks() {
        host(status = ApplicationStatus.SAVED)
        composeRule.onNodeWithText("Offer").performClick()

        composeRule.onNodeWithText("Save").performClick()

        assert(confirmed.value == ApplicationStatus.OFFER) { "expected Offer, got ${confirmed.value}" }
    }

    @Test
    fun theSheetStartsOnTheCurrentValueOfAnApplicationAlreadyMarked() {
        host(status = ApplicationStatus.INTERVIEW)
        composeRule.onNodeWithText("Save").performClick()

        assert(confirmed.value == ApplicationStatus.INTERVIEW) { "got ${confirmed.value}" }
    }

    @Test
    fun theSheetClosesWithoutAChoiceOnCancel() {
        host(status = ApplicationStatus.SAVED)
        composeRule.onNodeWithText("Applied").performClick()

        composeRule.onNodeWithText("Cancel").performClick()

        assert(confirmed.value == null) { "Cancel must not confirm" }
        assert(dismissals == 1) { "Cancel must dismiss once, got $dismissals" }
    }

    private fun host(status: ApplicationStatus) {
        val viewModel = viewModelAt(status)
        viewModel.onEnter(ExportedNavKey(APPLICATION_ID, "pdf", DebugScenario.DEFAULT))
        viewModel.onAction(ExportedAction.OpenStatusSheet)
        composeRule.setContent {
            ExportedSheetHost(viewModel = viewModel)
        }
        composeRule.waitForIdle()
    }

    private fun viewModelAt(status: ApplicationStatus): ExportedViewModel {
        val applicationRepository = TestApplicationRepository()
        val profileRepository = TestProfileRepository()
        applicationRepository.sendApplications(listOf(canonicalApplication.copy(status = status)))
        profileRepository.sendProfile(canonicalCandidateProfile)
        return ExportedViewModel(
            applicationRepository = applicationRepository,
            profileRepository = profileRepository,
            assembler = ResumeDocumentAssembler(),
            paymentGateway = TestPaymentGateway().withFreeCredits(credits = 1),
            fileStore = ExportedFileStore(ApplicationProvider.getApplicationContext()),
        )
    }

    @Composable
    private fun ExportedSheetHost(viewModel: ExportedViewModel) {
        val uiState by viewModel.uiState.collectAsState()
        HhTheme {
            ExportedScreen(
                uiState = uiState,
                actions = ExportedActions(
                    onOpenStatusSheet = { viewModel.onAction(ExportedAction.OpenStatusSheet) },
                    onDismissStatusSheet = {
                        dismissals += 1
                        viewModel.onAction(ExportedAction.DismissStatusSheet)
                    },
                    onConfirmStatus = { status -> confirmed.value = status },
                    onShare = {},
                    onNavigateBack = {},
                ),
            )
        }
    }
}

private const val APPLICATION_ID = "application-northwind-1"
