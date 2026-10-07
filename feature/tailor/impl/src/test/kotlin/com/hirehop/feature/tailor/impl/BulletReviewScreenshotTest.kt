package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.GuardrailViolation
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
class BulletReviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun resting_marksTheChangedWords() = captureChange("BulletReviewResting", ReviewFixtures.state(), ReviewFixtures.WEEKLY)

    @Test
    fun acceptedChange_offersUndoAndNext() = captureChange(
        "BulletReviewAccepted",
        ReviewFixtures.partlyReviewed(),
        ReviewFixtures.ORDERS,
    )

    @Test
    fun keptOriginal_offersUndoAndNext() = captureChange(
        "BulletReviewKeptOriginal",
        ReviewFixtures.partlyReviewed(),
        ReviewFixtures.DASHBOARD,
    )

    @Test
    fun flaggedVerb_explainsTheFlag() = captureChange(
        "BulletReviewFlaggedVerb",
        ReviewFixtures.state(weeklyViolations = listOf(GuardrailViolation.VerbEscalation("made", "built"))),
        ReviewFixtures.WEEKLY,
    )

    @Test
    fun flaggedScale_explainsTheFlag() = captureChange(
        "BulletReviewFlaggedScale",
        ReviewFixtures.state(weeklyViolations = listOf(GuardrailViolation.UnsupportedScaleClaim("40 stores"))),
        ReviewFixtures.WEEKLY,
    )

    @Test
    fun repairFailed_countsAsReviewed() = captureChange(
        "BulletReviewRepairFailed",
        ReviewFixtures.state(
            extra = listOf(
                ReviewFixtures.bullets().first { it.id == ReviewFixtures.WEEKLY }.copy(
                    id = "t-failed",
                    proposedText = "Made sales reports every week.",
                    editTypes = emptyList(),
                    violations = listOf(GuardrailViolation.UnsupportedNumber("40")),
                ),
            ),
        ),
        "t-failed",
    )

    @Test
    fun userEdited_showsYourOwnWords() = captureChange(
        "BulletReviewUserEdited",
        ReviewFixtures.partlyReviewed(),
        ReviewFixtures.INTERN,
    )

    @Test
    fun mergedSources_listsEverySourceFact() = captureChange(
        "BulletReviewMergedSources",
        ReviewFixtures.state(),
        ReviewFixtures.INTERN,
    )

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun resting_atLargeTextStacksFullWidth() = captureChange(
        "BulletReviewRestingFont200",
        ReviewFixtures.state(),
        ReviewFixtures.WEEKLY,
        device = HhTestDevices.boardLargeFont,
    )

    @Test
    fun editByHand_showsTheNote() = captureSheet("BulletReviewEditByHand", ReviewFixtures.state()) {
        EditByHandContent(
            text = "Prepared weekly sales reports in Excel for 40 stores.",
            onTextChange = {},
            onCancel = {},
            onSave = {},
        )
    }

    @Test
    fun editByHand_blankShowsTheRequiredMessage() = captureSheet("BulletReviewEditByHandBlank", ReviewFixtures.state()) {
        EditByHandContent(
            text = "",
            onTextChange = {},
            onCancel = {},
            onSave = {},
            showsError = true,
        )
    }

    @Test
    fun sourceFact_showsTheFactBehindTheLine() = captureSheet("BulletReviewSourceFact", ReviewFixtures.partlyReviewed()) {
        SourceFactSheetContent(
            sources = ReviewFixtures.change(ReviewFixtures.partlyReviewed(), ReviewFixtures.ORDERS).sources,
            onEditFact = {},
            onReport = {},
        )
    }

    private fun captureChange(
        screenName: String,
        state: TailorUiState.Success,
        bulletId: String,
        device: HhTestDevice = HhTestDevices.board,
    ) = captureSheet(screenName, state, device) {
        val item = ReviewFixtures.change(state, bulletId)
        val index = state.changeIndexOf(bulletId)
        BulletReviewSheetContent(
            item = item,
            position = index + 1,
            total = state.totalCount,
            actions = BulletSheetActions(
                onPrevious = if (index > 0) ({}) else null,
                onNext = if (index < state.totalCount - 1) ({}) else null,
                onAccept = {},
                onKeepOriginal = {},
                onUndo = {},
                onEditByHand = {},
                onNextChange = {},
                onOpenSource = {},
                onReport = {},
            ),
        )
    }

    private fun captureSheet(
        screenName: String,
        state: TailorUiState.Success,
        device: HhTestDevice = HhTestDevices.board,
        content: @Composable () -> Unit,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                SheetOverReview(state, content)
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

@Composable
private fun SheetOverReview(state: TailorUiState.Success, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        TailorScreen(uiState = state, actions = noTailorActions())
        Box(Modifier.fillMaxSize().background(HhTheme.colors.scrim))
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(HhTheme.colors.surface, HhTheme.shapes.sheet)
                .padding(
                    start = HhTheme.spacing.gutter,
                    end = HhTheme.spacing.gutter,
                    top = HhTheme.spacing.xl,
                    bottom = HhTheme.spacing.xxl,
                ),
        ) { content() }
    }
}
