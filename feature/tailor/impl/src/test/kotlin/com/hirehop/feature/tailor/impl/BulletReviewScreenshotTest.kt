package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class BulletReviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun reviewableBullet_readsInLightAndDark() = captureBullets("BulletReview", reviewableState())

    @Test
    fun withRejects_keepsRejectedVisibleAndRestorable() = captureBullets("BulletReviewWithRejects", withRejectsState())

    @Test
    fun guardedAndUnchangedStates_readInLightAndDark() = captureBullets("BulletReviewGuardedStates", guardedState())

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun withRejects_atLargeTextStacksAndWraps() = captureBullets(
        screenName = "BulletReviewWithRejectsFont200",
        items = withRejectsState(),
        device = HhTestDevices.boardLargeFont,
    )

    private fun captureBullets(
        screenName: String,
        items: List<TailorBulletUi>,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(HhTheme.spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
                ) {
                    items.forEach { item ->
                        BulletCard(
                            item = item,
                            onAccept = {},
                            onReject = {},
                        )
                    }
                }
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

private fun reviewableState(): List<TailorBulletUi> = listOf(
    bulletUi(
        tailored = tailored(
            id = "b1",
            original = "Made a dashboard for placement data using Power BI.",
            proposed = "Built a Power BI dashboard of 3 batches of placement data for the college T&P cell.",
            sourceIds = listOf("P-02"),
            editTypes = listOf(EditType.REWORD, EditType.EMPHASISE),
            keywordsUsed = listOf("Power BI", "dashboard"),
        ),
        sourceTexts = listOf("Built a Power BI dashboard of 3 batches of placement data for the college T&P cell."),
    ),
)

private fun withRejectsState(): List<TailorBulletUi> = listOf(
    bulletUi(
        tailored = tailored(
            id = "b1",
            original = "Made a dashboard for placement data using Power BI.",
            proposed = "Built a Power BI dashboard of 3 batches of placement data for the college T&P cell.",
            sourceIds = listOf("P-02"),
            editTypes = listOf(EditType.REWORD, EditType.EMPHASISE),
            keywordsUsed = listOf("Power BI", "dashboard"),
            decision = BulletDecision.ACCEPTED,
        ),
        sourceTexts = listOf("Built a Power BI dashboard of 3 batches of placement data for the college T&P cell."),
    ),
    bulletUi(
        tailored = tailored(
            id = "b2",
            original = "Excel, SQL, Power BI, C++, HTML",
            proposed = "SQL, Excel (pivot tables), Power BI, C++, HTML",
            sourceIds = listOf("C-01", "I-01", "P-02"),
            editTypes = listOf(EditType.REORDER, EditType.EMPHASISE),
            keywordsUsed = listOf("SQL", "Excel", "Power BI"),
            decision = BulletDecision.REJECTED,
        ),
        sourceTexts = listOf(
            "Coursework: DBMS, Probability & Statistics",
            "Data intern, Kiran Agro Exports, Nashik",
            "Built a Power BI dashboard of 3 batches of placement data for the college T&P cell.",
        ),
    ),
    bulletUi(
        tailored = tailored(
            id = "b3",
            original = "Cleaned 12,000 rows of sales data in Excel. Built weekly pivot reports.",
            proposed = "Cleaned 12,000 rows of sales data in Excel and built weekly pivot reports",
            sourceIds = listOf("I-01", "C-01"),
            editTypes = listOf(EditType.MERGE, EditType.EMPHASISE),
            keywordsUsed = listOf("Excel", "reports"),
        ),
        sourceTexts = listOf(
            "Data intern, Kiran Agro Exports, Nashik",
            "Coursework: DBMS, Probability & Statistics",
        ),
    ),
)

private fun guardedState(): List<TailorBulletUi> = listOf(
    bulletUi(
        tailored = tailored(
            id = "b1",
            original = "Made a dashboard for placement data using Power BI.",
            proposed = "Built a Power BI dashboard for the college T&P cell.",
            sourceIds = listOf("P-02"),
            editTypes = listOf(EditType.REWORD),
            violations = listOf(GuardrailViolation.VerbEscalation("made", "built")),
        ),
        sourceTexts = listOf("Built a Power BI dashboard of 3 batches of placement data for the college T&P cell."),
    ),
    bulletUi(
        tailored = tailored(
            id = "b2",
            original = "Smart India Hackathon 2024, internal-round finalist",
            proposed = "Smart India Hackathon 2024, internal-round finalist",
            sourceIds = listOf("X-01"),
            editTypes = listOf(EditType.EMPHASISE),
        ),
        sourceTexts = listOf("Smart India Hackathon 2024, internal-round finalist"),
    ),
    bulletUi(
        tailored = tailored(
            id = "b3",
            original = "Data intern at Kiran Agro Exports in Nashik",
            proposed = "Data intern at Kiran Agro Exports in Nashik",
            sourceIds = listOf("I-01"),
            editTypes = listOf(EditType.EMPHASISE),
        ),
        sourceTexts = listOf("Data intern, Kiran Agro Exports, Nashik"),
        isStale = true,
    ),
)

private fun tailored(
    id: String,
    original: String,
    proposed: String,
    sourceIds: List<String>,
    editTypes: List<EditType>,
    keywordsUsed: List<String> = emptyList(),
    violations: List<GuardrailViolation> = emptyList(),
    decision: BulletDecision = BulletDecision.PENDING,
): TailoredBullet = TailoredBullet(
    id = id,
    entryId = "entry-1",
    originalText = original,
    proposedText = proposed,
    sourceIds = sourceIds,
    editTypes = editTypes,
    keywordsUsed = keywordsUsed,
    violations = violations,
    decision = decision,
)

private fun bulletUi(
    tailored: TailoredBullet,
    sourceTexts: List<String>,
    isStale: Boolean = false,
): TailorBulletUi = TailorBulletUi(
    bullet = tailored,
    sourceTexts = sourceTexts,
    isStale = isStale,
)
