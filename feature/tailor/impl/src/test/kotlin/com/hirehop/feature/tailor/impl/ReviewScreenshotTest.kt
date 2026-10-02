package com.hirehop.feature.tailor.impl

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class ReviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun loading_showsNamedSteps() = capture(
        "TailoringReviewLoading",
        TailorUiState.Loading(
            job = JobHeader("Associate Analyst", "Northwind GCC"),
            factCount = 9,
            lineCount = 7,
        ),
    )

    @Test
    fun ready_listsEveryChangeToReview() = capture("TailoringReviewReady", ReviewFixtures.state())

    @Test
    fun partlyReviewed_showsProgressAndFlag() = capture("TailoringReviewPartlyReviewed", ReviewFixtures.partlyReviewed())

    @Test
    fun allReviewed_offersPreviewExport() = capture("TailoringReviewAllReviewed", ReviewFixtures.allReviewed())

    @Test
    fun noChangedLine_saysEveryLineComesFromTheFacts() = capture(
        "TailoringReviewNoChanges",
        ReviewFixtures.state().copy(changes = emptyList()),
    )

    @Test
    fun regenerationsUsed_explainsTheLimit() = capture(
        "TailoringReviewRegenerationsUsed",
        ReviewFixtures.state(
            decisions = mapOf(ReviewFixtures.ORDERS to BulletDecision.ACCEPTED),
            regenerationsUsed = 2,
        ),
    )

    @Test
    fun offline_keepsTheSavedResumeReadable() = capture(
        "TailoringReviewOffline",
        ReviewFixtures.partlyReviewed().copy(isOffline = true),
    )

    @Test
    fun failed_offersTryAgain() = capture(
        "TailoringReviewFailed",
        TailorUiState.Failed(JobHeader("Associate Analyst", "Northwind GCC")),
    )

    @Test
    fun notFound_explainsTheMissingResume() = capture("TailoringReviewNotFound", TailorUiState.NotFound)

    @Test
    fun reportedSection_showsReportedInTheMenuAndThanksThePerson() = capture(
        "TailoringReviewReportedToast",
        ReviewFixtures.state(reportedIds = setOf(sectionReportId("EXPERIENCE"))),
        toast = ReviewToastState.Reported,
    )

    @Test
    fun acceptedToast_offersUndo() = capture(
        "TailoringReviewAcceptedToast",
        ReviewFixtures.partlyReviewed(),
        toast = ReviewToastState.Accepted(ReviewFixtures.ORDERS, position = 2),
    )

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun ready_atLargeTextStacksFullWidth() = capture(
        "TailoringReviewReadyFont200",
        ReviewFixtures.partlyReviewed(),
        device = HhTestDevices.boardLargeFont,
    )

    private fun capture(
        screenName: String,
        state: TailorUiState,
        device: HhTestDevice = HhTestDevices.board,
        toast: ReviewToastState? = null,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                TailorScreen(
                    uiState = state,
                    actions = noTailorActions(),
                    interaction = ReviewInteraction(initialToast = toast),
                )
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

internal fun noTailorActions(): TailorActions = TailorActions(
    onBack = {},
    onPreviewExport = {},
    onAccept = {},
    onKeepOriginal = {},
    onUndo = {},
    onEditByHand = { _, _ -> },
    onRegenerate = {},
    onRetry = {},
    onReportBullet = {},
    onReportSection = {},
    onEditFact = { _, _ -> },
)
