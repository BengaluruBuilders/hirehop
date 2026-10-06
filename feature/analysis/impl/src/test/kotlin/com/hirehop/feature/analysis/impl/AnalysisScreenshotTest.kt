package com.hirehop.feature.analysis.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.component.HhSheet
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
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
class AnalysisScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun waiting_f1s7_01() = capture("AnalysisWaiting", waitingState())

    @Test
    fun resultSettled_f1s7_03() = capture("AnalysisResult", resultState())

    @Test
    fun rowMenu_f1s7_05() = capture("AnalysisRowMenu", resultState(overlay = AnalysisOverlay.Menu(SQL)))

    @Test
    fun sourceSheet_f1s7_06() = capture("AnalysisSourceSheet", resultState()) {
        SheetOver { SourceSheetContent(resultState().item(SQL), AnalysisActions()) }
    }

    @Test
    fun questionSheet_f1s7_07() = capture("AnalysisQuestionSheet", resultState()) {
        SheetOver { QuestionSheetContent(resultState().item(CLOUD), AnalysisActions()) }
    }

    @Test
    fun shareScreen() = capture("AnalysisShareScreen", resultState(overlay = AnalysisOverlay.ShareCard))

    @Test
    fun questionNotClosed() = capture("AnalysisQuestionNotClosed", resultState()) {
        SheetOver { QuestionSheetContent(resultState().item(CLOUD), AnalysisActions(), notClosed = true) }
    }

    @Test
    fun gapClosed_f1s7_09() = capture("AnalysisGapClosed", closedState())

    @Test
    fun prepPlanAdded_f1s7_10() = capture("AnalysisPrepAdded", prepAddedState())

    @Test
    fun manyGaps_f1s7_11() = capture("AnalysisManyGaps", manyGapsState())

    @Test
    fun allMet_f1s7_12() = capture("AnalysisAllMet", allMetState())

    @Test
    fun error_f1s7_13() = capture("AnalysisError", AnalysisUiState.Failed(JOB))

    @Test
    fun offline_f1s7_14() = capture("AnalysisOffline", resultState().copy(isOffline = true))

    @Test
    fun dailyLimit_f1s7_15() = capture("AnalysisDailyLimit", AnalysisUiState.DailyLimit(JOB))

    @Test
    fun freeTailoringLimit_f1s7_16() = capture("AnalysisTailorLimit", resultState().copy(tailorLimitReached = true))

    @Test
    fun noKeyTerms() = capture(
        "AnalysisNoKeyTerms",
        resultState().copy(keywordCoverage = KeywordCoverage(0, 0), sections = emptyList()),
    )

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun text200_f1s7_17() = capture("AnalysisFont200", resultState(), device = HhTestDevices.boardLargeFont)

    private fun capture(
        screenName: String,
        uiState: AnalysisUiState,
        device: HhTestDevice = HhTestDevices.board,
        overlay: @Composable () -> Unit = {},
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                Box(Modifier.fillMaxSize()) {
                    AnalysisScreen(uiState = uiState, actions = AnalysisActions())
                    overlay()
                }
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }

    @Composable
    private fun SheetOver(content: @Composable () -> Unit) {
        Box(Modifier.fillMaxSize().background(HhTheme.colors.scrim)) {
            HhSheet(Modifier.align(Alignment.BottomCenter)) {
                content()
            }
        }
    }
}

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"
private const val CLOUD = "req-dw"
private const val SQL = "req-sql"

private val JOB = JobLabel(title = "Associate Analyst", company = "Northwind GCC")

private fun AnalysisUiState.Result.item(id: String) = requireNotNull(itemOrNull(id))

private fun waitingState() = AnalysisUiState.Analyzing(
    job = JOB,
    factCount = 18,
    keyTermCount = 14,
    requirementCount = 8,
)

private val cloud = item(CLOUD, "Cloud data warehouse (Snowflake or BigQuery)", MatchStatus.GAP)
private val excel = item(
    "req-excel",
    "Advanced Excel — pivots yes, macros not yet",
    MatchStatus.PARTIAL,
    factRefs = listOf(
        factRef("W-01", "Data Operations Associate", "Saffron Retail, Pune", listOf("Built weekly sales reports in Excel")),
        factRef("I-01", "Data Intern", "Kiran Agro Exports", listOf("Built the attendance register in Excel")),
    ),
)
private val sql = item(
    SQL,
    "SQL",
    MatchStatus.MET,
    factRefs = listOf(
        factRef(
            "W-01",
            "Data Operations Associate",
            "Saffron Retail, Pune",
            listOf("Built weekly sales reports in Excel for 40 stores; cleaned order data with SQL."),
            start = "Jul 2025",
        ),
        factRef("C-01", "Coursework: DBMS (SQL)", "", emptyList()),
    ),
)
private val bi = item(
    "req-bi",
    "Power BI or Tableau",
    MatchStatus.MET,
    factRefs = listOf(factRef("P-02", "Placement Stats Dashboard", "", listOf("Built a dashboard"))),
)
private val stats = item(
    "req-stats",
    "Statistics",
    MatchStatus.MET,
    priority = RequirementPriority.NICE_TO_HAVE,
    factRefs = listOf(factRef("C-02", "Statistics coursework", "", emptyList())),
)
private val agile = item("req-agile", "Agile / JIRA", MatchStatus.GAP, priority = RequirementPriority.NICE_TO_HAVE)
private val python = item("req-python", "Python", MatchStatus.GAP, priority = RequirementPriority.NICE_TO_HAVE)

private fun resultState(overlay: AnalysisOverlay = AnalysisOverlay.None) = AnalysisUiState.Result(
    job = JOB,
    keywordCoverage = KeywordCoverage(covered = 9, total = 14),
    sections = listOf(
        RequirementSection(RequirementGroup.MustHaveGaps, listOf(cloud)),
        RequirementSection(RequirementGroup.Partial, listOf(excel)),
        RequirementSection(RequirementGroup.Met, listOf(sql, bi, stats)),
        RequirementSection(RequirementGroup.NiceToHaveGaps, listOf(agile, python)),
    ),
    freeCredits = 1,
    overlay = overlay,
)

private fun closedState(): AnalysisUiState.Result {
    val closed = cloud.copy(
        status = MatchStatus.MET,
        factRefs = listOf(factRef("U-01", "Additional experience", "", emptyList(), FactSource.USER_STATED)),
    )
    return resultState().copy(
        keywordCoverage = KeywordCoverage(covered = 10, total = 14),
        sections = listOf(
            RequirementSection(RequirementGroup.Partial, listOf(excel)),
            RequirementSection(RequirementGroup.Met, listOf(closed, sql, bi, stats)),
            RequirementSection(RequirementGroup.NiceToHaveGaps, listOf(agile, python)),
        ),
        toast = AnalysisToast.GapClosed,
        closedRequirementId = CLOUD,
    )
}

private fun prepAddedState() = resultState().copy(
    sections = resultState().sections.map { section ->
        section.copy(items = section.items.map { if (it.id == CLOUD) it.copy(isInPrepPlan = true) else it })
    },
    toast = AnalysisToast.PrepAdded(CLOUD, cloud.requirement.text),
)

private fun manyGapsState(): AnalysisUiState.Result {
    val gaps = listOf(
        cloud,
        item("req-bi", "Power BI or Tableau", MatchStatus.GAP),
        item("req-dbt", "dbt", MatchStatus.GAP),
        item("req-etl", "ETL pipelines", MatchStatus.GAP),
        agile,
        python,
        item("req-stake", "Stakeholder communication", MatchStatus.GAP, priority = RequirementPriority.NICE_TO_HAVE),
    )
    return resultState().copy(
        keywordCoverage = KeywordCoverage(covered = 3, total = 14),
        sections = listOf(
            RequirementSection(RequirementGroup.MustHaveGaps, gaps.take(4)),
            RequirementSection(RequirementGroup.Met, listOf(sql)),
            RequirementSection(RequirementGroup.NiceToHaveGaps, gaps.drop(4)),
        ),
    )
}

private fun allMetState() = resultState().copy(
    keywordCoverage = KeywordCoverage(covered = 14, total = 14),
    sections = listOf(RequirementSection(RequirementGroup.Met, listOf(sql, bi, stats))),
)

private fun item(
    id: String,
    text: String,
    status: MatchStatus,
    priority: RequirementPriority = RequirementPriority.MUST_HAVE,
    factRefs: List<RequirementFactRef> = emptyList(),
) = RequirementItem(
    requirement = JobRequirement(
        id = id,
        text = text,
        type = RequirementType.SKILL,
        priority = priority,
        keywords = listOf(text.substringBefore(" ").lowercase()),
    ),
    status = status,
    skills = emptyList(),
    isInPrepPlan = false,
    factRefs = factRefs,
)

private fun factRef(
    id: String,
    title: String,
    organization: String,
    lines: List<String>,
    source: FactSource = FactSource.IMPORTED,
    start: String = "",
) = RequirementFactRef(
    factId = id,
    displayId = id,
    title = title,
    organization = organization,
    startDate = start,
    endDate = "",
    lines = lines,
    source = source,
    isConfirmed = source != FactSource.USER_STATED || id == "U-01",
)
