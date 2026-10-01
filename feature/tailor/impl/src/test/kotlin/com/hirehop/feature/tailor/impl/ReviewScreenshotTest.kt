package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EditType
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import com.hirehop.feature.tailor.impl.document.ResumeDocumentAssembler
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.time.Instant

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class ReviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun withChanges_readsInLightAndDark() = captureReview("TailoringReviewWithChanges", reviewState(BulletDecision.PENDING))

    @Test
    fun withRejectedChange_staysVisibleInLightAndDark() =
        captureReview("TailoringReviewWithRejected", reviewState(BulletDecision.REJECTED))

    @Test
    fun withFlagsAndStaleLine_readsInLightAndDark() =
        captureReview("TailoringReviewWithFlags", reviewState(BulletDecision.ACCEPTED, includeStale = true))

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun withChanges_atLargeTextStacksFullWidth() = captureReview(
        screenName = "TailoringReviewWithChangesFont200",
        state = reviewState(BulletDecision.PENDING),
        device = HhTestDevices.boardLargeFont,
    )

    @Test
    fun resumePreview_readsInLightAndDark() = capturePreview("TailoringReviewResumePreview")

    private fun captureReview(
        screenName: String,
        state: TailorUiState.Success,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                HhScaffold { padding ->
                    ReviewContent(
                        state = state,
                        onAccept = {},
                        onReject = {},
                        onAcceptAllSafeChanges = {},
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = padding,
                    )
                }
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }

    private fun capturePreview(screenName: String, device: HhTestDevice = HhTestDevices.board) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                HhScaffold { padding ->
                    ResumePreviewContent(
                        document = reviewState(BulletDecision.ACCEPTED).document,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = padding,
                    )
                }
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

private fun reviewState(decision: BulletDecision, includeStale: Boolean = false): TailorUiState.Success {
    val application = JobApplication(
        id = "app-1",
        job = JobDescription("Associate Analyst", "Northwind GCC", "", emptyList()),
        status = ApplicationStatus.SAVED,
        notes = "",
        gapAnalysis = null,
        tailoredResume = TailoredResume(reviewBullets(decision, includeStale)),
        createdAt = Instant.fromEpochSeconds(0),
        updatedAt = Instant.fromEpochSeconds(0),
    )
    val state = buildTailorUiState(application, reviewProfile(), ResumeDocumentAssembler())
    return state as TailorUiState.Success
}

private fun reviewBullets(decision: BulletDecision, includeStale: Boolean): List<TailoredBullet> {
    val bullets = mutableListOf(
        TailoredBullet(
            id = "t1",
            entryId = "exp-1",
            originalText = "Made a dashboard for placement data using Excel",
            proposedText = "Built a dashboard of 3 batches of placement data for the college T&P cell",
            sourceIds = listOf("src-1"),
            editTypes = listOf(EditType.REWORD, EditType.EMPHASISE),
            keywordsUsed = listOf("dashboard"),
            violations = emptyList(),
            decision = decision,
        ),
        TailoredBullet(
            id = "t2",
            entryId = "exp-1",
            originalText = "Cleaned 12,000 rows of sales data in Excel. Built weekly pivot reports.",
            proposedText = "Cleaned 12,000 rows of sales data in Excel and built weekly pivot reports",
            sourceIds = listOf("src-2"),
            editTypes = listOf(EditType.MERGE),
            keywordsUsed = emptyList(),
            violations = emptyList(),
            decision = BulletDecision.PENDING,
        ),
        TailoredBullet(
            id = "t3",
            entryId = "exp-1",
            originalText = "Helped 3 classmates test the app",
            proposedText = "Led a team of 30 testers",
            sourceIds = listOf("src-3"),
            editTypes = emptyList(),
            keywordsUsed = emptyList(),
            violations = listOf(
                GuardrailViolation.UnsupportedNumber("30"),
                GuardrailViolation.VerbEscalation(from = "Helped", to = "Led"),
            ),
            decision = BulletDecision.PENDING,
        ),
        TailoredBullet(
            id = "t4",
            entryId = "exp-1",
            originalText = "Wrote SQL queries for the course database",
            proposedText = "Wrote SQL queries for the course database",
            sourceIds = listOf("src-4"),
            editTypes = emptyList(),
            keywordsUsed = emptyList(),
            violations = emptyList(),
            decision = BulletDecision.PENDING,
        ),
    )
    if (includeStale) {
        bullets += TailoredBullet(
            id = "t5",
            entryId = "exp-1",
            originalText = "Line you edited after the tailoring ran",
            proposedText = "Reworded line you edited after the tailoring ran",
            sourceIds = listOf("src-5"),
            editTypes = listOf(EditType.REWORD),
            keywordsUsed = emptyList(),
            violations = emptyList(),
            decision = BulletDecision.PENDING,
        )
    }
    return bullets
}

private fun reviewProfile(): CandidateProfile = CandidateProfile(
    fullName = "Priya Sharma",
    email = "priya@example.com",
    phone = "+91 98765 43210",
    headline = "Final-year computer science student",
    skills = listOf("Excel", "SQL", "Kotlin"),
    entries = listOf(
        ProfileEntry(
            id = "exp-1",
            category = EntryCategory.EXPERIENCE,
            title = "Data intern",
            organization = "Kiran Agro Exports",
            startDate = "May 2025",
            endDate = "Jul 2025",
            bullets = listOf(
                EvidenceBullet("src-1", "Made a dashboard for placement data using Excel"),
                EvidenceBullet("src-2", "Cleaned 12,000 rows of sales data in Excel. Built weekly pivot reports."),
                EvidenceBullet("src-3", "Helped 3 classmates test the app"),
                EvidenceBullet("src-4", "Wrote SQL queries for the course database"),
                EvidenceBullet("src-5", "Rewrote the project README after the tailoring ran"),
            ),
            source = FactSource.IMPORTED,
            isConfirmed = true,
        ),
    ),
)
