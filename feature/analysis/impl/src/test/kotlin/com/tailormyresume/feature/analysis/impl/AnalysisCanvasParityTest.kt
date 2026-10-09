package com.tailormyresume.feature.analysis.impl

import android.content.Context
import android.provider.Settings
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.TimeZone
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class AnalysisCanvasParityTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val originalZone = TimeZone.getDefault()

    @Before
    fun useUtc() = TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    @After
    fun restoreZone() = TimeZone.setDefault(originalZone)

    private val job = JobLabel("Associate Analyst", "Northwind Logistics")
    private val cloud = item("req-cloud", "Cloud data warehouse", MatchStatus.GAP)
    private val warehouse = item("req-dax", "DAX", MatchStatus.GAP, keywords = listOf("northwind", "sql"))
    private val sql = item("req-sql", "SQL", MatchStatus.MET, factId = "W-01")
    private val excel = item("req-excel", "Excel", MatchStatus.PARTIAL, factId = "W-02")

    private fun result(vararg groups: Pair<RequirementGroup, List<RequirementItem>>) = AnalysisUiState.Result(
        job = job,
        keywordCoverage = KeywordCoverage(covered = 9, total = 14),
        sections = groups.map { RequirementSection(it.first, it.second) },
        totalCredits = 1,
    )

    private fun show(state: AnalysisUiState) {
        composeRule.setContent { TmrTheme { AnalysisScreen(uiState = state, actions = AnalysisActions()) } }
    }

    @Test
    fun summaryChipsSumToTheKeyTermTotal() {
        show(
            result(
                RequirementGroup.MustHaveGaps to listOf(cloud),
                RequirementGroup.Partial to listOf(excel),
                RequirementGroup.Met to listOf(sql),
            ),
        )

        composeRule.onNodeWithText("9 Met").assertExists()
        composeRule.onNodeWithText("5 To prepare").assertExists()
        composeRule.onNodeWithText("1 Partly met").assertDoesNotExist()
    }

    @Test
    fun mustHaveHeaderCountMatchesTheGapCards() {
        val gaps = listOf(cloud, item("req-b", "dbt", MatchStatus.GAP), item("req-c", "ETL", MatchStatus.GAP))
        show(result(RequirementGroup.MustHaveGaps to gaps))

        composeRule.onNodeWithContentDescription("Must-haves to prepare · 3").assertExists()
    }

    @Test
    fun notInYourFactsNeverListsTheCompanyName() {
        show(result(RequirementGroup.MustHaveGaps to listOf(warehouse)))

        composeRule.onNodeWithText("Not in your facts yet: SQL").assertExists()
    }

    @Test
    fun noCreditNoteUnderTheTailorButton() {
        show(result(RequirementGroup.Met to listOf(sql)))

        composeRule.onNodeWithText("Free to tailor and preview", substring = true).assertDoesNotExist()
    }

    @Test
    fun questionSheetNamesTheNextFactId() {
        composeRule.setContent {
            TmrTheme { QuestionSheetContent(warehouse, AnalysisActions(), nextFactId = "U-07") }
        }

        composeRule.onNodeWithText("U-07").assertExists()
        composeRule.onNodeWithText("Save as U-07").assertExists()
        composeRule.onNodeWithText("Must-have · DAX").assertExists()
    }

    @Test
    fun shareCardShowsTheCompanyLineAndShareMyFit() {
        composeRule.setContent {
            TmrTheme {
                ShareFitScreen(
                    state = result(RequirementGroup.Met to listOf(sql)),
                    actions = AnalysisActions(),
                )
            }
        }

        composeRule.onNodeWithText("Northwind Logistics").assertExists()
        composeRule.onNodeWithText("Share my fit").assertExists()
    }

    @Test
    fun shareTextUsesTheKeyTermCountsOfTheCard() {
        val shared = mutableListOf<String>()
        composeRule.setContent {
            TmrTheme {
                ShareFitScreen(
                    state = result(RequirementGroup.Met to listOf(sql)),
                    actions = AnalysisActions(onShareText = { shared += it }),
                )
            }
        }

        composeRule.onNodeWithText("Share my fit").performClick()

        assertThat(shared).containsExactly("My fit for Associate Analyst: 9 met, 5 to prepare. Made with TailorMyResume.")
    }

    @Test
    fun notInYourFactsKeepsAKnownSkillThatIsAlsoACompanyWord() {
        val aws = item("req-aws", "Hands-on AWS", MatchStatus.GAP, keywords = listOf("aws", "india"))
        val state = result(RequirementGroup.MustHaveGaps to listOf(aws))
            .copy(job = JobLabel("Associate Analyst", "AWS India"))
        show(state)

        composeRule.onNodeWithText("Not in your facts yet: AWS").assertExists()
    }

    @Test
    fun questionSheetOpenedThroughTheScreenNamesTheNextFactId() {
        val state = result(RequirementGroup.MustHaveGaps to listOf(warehouse))
            .copy(overlay = AnalysisOverlay.Question(warehouse.id), nextFactId = "U-04")
        show(state)

        composeRule.onNodeWithText("U-04").assertExists()
        composeRule.onNodeWithText("Save as U-04").assertExists()
    }

    @Test
    fun offlineBannerFollowsTheDevice12HourSetting() {
        setClockFormat("12")
        val tenTwelveToday = LocalDate.now(ZoneOffset.UTC).atTime(10, 12).toInstant(ZoneOffset.UTC)
        show(
            result(RequirementGroup.Met to listOf(sql))
                .copy(isOffline = true, analysedAt = Instant.fromEpochMilliseconds(tenTwelveToday.toEpochMilli())),
        )

        composeRule.onNodeWithText("This is your last result, from today at 10:12", substring = true).assertExists()
        composeRule.onNodeWithText("You're offline. This is your last result, from today at 10:12.").assertDoesNotExist()
    }

    private fun setClockFormat(format: String) {
        Settings.System.putString(
            ApplicationProvider.getApplicationContext<Context>().contentResolver,
            Settings.System.TIME_12_24,
            format,
        )
    }

    @Test
    fun offlineShowsTheTimedBannerAndOnlyTheMetList() {
        setClockFormat("24")
        val tenTwelveToday = LocalDate.now(ZoneOffset.UTC).atTime(10, 12).toInstant(ZoneOffset.UTC)
        show(
            result(
                RequirementGroup.MustHaveGaps to listOf(cloud),
                RequirementGroup.Met to listOf(sql),
            ).copy(isOffline = true, analysedAt = Instant.fromEpochMilliseconds(tenTwelveToday.toEpochMilli())),
        )

        composeRule.onNodeWithText("You're offline. This is your last result, from today at 10:12.").assertExists()
        composeRule.onNodeWithText("You cover 9 of 14 key terms. This is not a score.").assertExists()
        composeRule.onNodeWithContentDescription("Met · 1").assertExists()
        composeRule.onNodeWithText("Cloud data warehouse").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Must-haves to prepare · 1").assertDoesNotExist()
        composeRule.onNodeWithText("Tailor needs a connection.").assertDoesNotExist()
        composeRule.onNodeWithText("Tailor when I'm back").assertExists()
    }
}

private fun item(
    id: String,
    text: String,
    status: MatchStatus,
    keywords: List<String> = listOf(text.lowercase()),
    factId: String? = null,
) = RequirementItem(
    requirement = JobRequirement(
        id = id,
        text = text,
        type = RequirementType.SKILL,
        priority = RequirementPriority.MUST_HAVE,
        keywords = keywords,
    ),
    status = status,
    skills = emptyList(),
    isInPrepPlan = false,
    factRefs = listOfNotNull(
        factId?.let {
            RequirementFactRef(
                factId = it,
                displayId = it,
                title = "Entry",
                organization = "",
                startDate = "",
                endDate = "",
                lines = emptyList(),
                source = FactSource.IMPORTED,
                isConfirmed = true,
            )
        },
    ),
)
