package com.hirehop.feature.analysis.impl

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
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
    fun fullResult_readsInLightAndDark() = captureAnalysisGap("AnalysisGapFull", resultState())

    @Test
    fun manyGaps_readsInLightAndDark() = captureAnalysisGap("AnalysisGapManyGaps", manyGapsState())

    @Test
    fun allMet_readsInLightAndDark() = captureAnalysisGap("AnalysisGapAllMet", allMetState())

    @Test
    fun gapClosedAndPrepPlan_readsInLightAndDark() = captureAnalysisGap("AnalysisGapClosed", gapClosedState())

    @Test
    fun noKeyTerms_readsInLightAndDark() = captureAnalysisGap("AnalysisGapNoKeyTerms", noKeyTermsState())

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun fullResult_atLargeTextStacksAndWraps() = captureAnalysisGap(
        screenName = "AnalysisGapFullFont200",
        uiState = resultState(),
        device = HhTestDevices.boardLargeFont,
    )

    private fun captureAnalysisGap(
        screenName: String,
        uiState: AnalysisUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                AnalysisScreen(uiState = uiState, actions = AnalysisActions())
            }
        }
        composeRule.captureMultiTheme(TRACKED_OUTPUT_DIR, screenName, device) { dark ->
            darkTheme.value = dark
        }
        Unit
    }
}

private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

private fun resultState(): AnalysisUiState.Result = AnalysisUiState.Result(
    title = "Associate Analyst",
    company = "Northwind GCC",
    keywordCoverage = KeywordCoverage(covered = 9, total = 14),
    sections = listOf(
        RequirementSection(
            RequirementGroup.MustHaveGaps,
            listOf(
                item(
                    id = "req-dw",
                    text = "Cloud data warehouse (Snowflake or BigQuery)",
                    status = MatchStatus.GAP,
                    type = RequirementType.TOOL,
                    isInPrepPlan = true,
                ),
            ),
        ),
        RequirementSection(
            RequirementGroup.Partial,
            listOf(
                item(
                    id = "req-excel",
                    text = "Advanced Excel — pivots yes, macros not yet",
                    status = MatchStatus.PARTIAL,
                    type = RequirementType.TOOL,
                    evidence = listOf("Built the attendance register in Excel for my internship"),
                    factRefs = listOf(
                        factRef("I-01", "Data intern, Kiran Agro Exports", FactSource.IMPORTED, true),
                    ),
                ),
            ),
        ),
        RequirementSection(
            RequirementGroup.Met,
            listOf(
                item(
                    id = "req-sql",
                    text = "SQL databases",
                    status = MatchStatus.MET,
                    type = RequirementType.SKILL,
                    evidence = listOf("Skill: SQL"),
                    factRefs = listOf(factRef("C-01", "DBMS coursework", FactSource.USER_STATED, true)),
                ),
                item(
                    id = "req-bi",
                    text = "Power BI or Tableau",
                    status = MatchStatus.MET,
                    type = RequirementType.TOOL,
                    evidence = listOf("Built a placement stats dashboard the college cell used"),
                    factRefs = listOf(
                        factRef("P-02", "Placement Stats Dashboard", FactSource.IMPORTED, false),
                    ),
                ),
            ),
        ),
        RequirementSection(
            RequirementGroup.NiceToHaveGaps,
            listOf(
                item(
                    id = "req-agile",
                    text = "Agile / JIRA",
                    status = MatchStatus.GAP,
                    type = RequirementType.SOFT_SKILL,
                    priority = RequirementPriority.NICE_TO_HAVE,
                ),
            ),
        ),
    ),
    prepPlanCount = 1,
    canSave = true,
)

private fun manyGapsState(): AnalysisUiState.Result = AnalysisUiState.Result(
    title = "Associate Analyst",
    company = "Northwind GCC",
    keywordCoverage = KeywordCoverage(covered = 3, total = 14),
    sections = listOf(
        RequirementSection(
            RequirementGroup.MustHaveGaps,
            listOf(
                item("req-dw", "Cloud data warehouse (Snowflake or BigQuery)", MatchStatus.GAP, RequirementType.TOOL),
                item("req-sql", "SQL databases", MatchStatus.GAP, RequirementType.SKILL),
                item("req-bi", "Power BI or Tableau", MatchStatus.GAP, RequirementType.TOOL),
            ),
        ),
        RequirementSection(
            RequirementGroup.Met,
            listOf(
                item(
                    id = "req-python",
                    text = "Python scripting",
                    status = MatchStatus.MET,
                    type = RequirementType.SKILL,
                    evidence = listOf("Skill: Python"),
                    factRefs = listOf(factRef("X-01", "Smart India Hackathon 2024", FactSource.USER_STATED, false)),
                ),
            ),
        ),
        RequirementSection(
            RequirementGroup.NiceToHaveGaps,
            listOf(
                item(
                    id = "req-agile",
                    text = "Agile / JIRA",
                    status = MatchStatus.GAP,
                    type = RequirementType.SOFT_SKILL,
                    priority = RequirementPriority.NICE_TO_HAVE,
                ),
                item(
                    id = "req-stake",
                    text = "Stakeholder communication",
                    status = MatchStatus.GAP,
                    type = RequirementType.SOFT_SKILL,
                    priority = RequirementPriority.NICE_TO_HAVE,
                ),
            ),
        ),
    ),
    prepPlanCount = 0,
    canSave = true,
)

private fun allMetState(): AnalysisUiState.Result = AnalysisUiState.Result(
    title = "Associate Analyst",
    company = "Northwind GCC",
    keywordCoverage = KeywordCoverage(covered = 14, total = 14),
    sections = listOf(
        RequirementSection(
            RequirementGroup.Met,
            listOf(
                item(
                    id = "req-dw",
                    text = "Cloud data warehouse (Snowflake or BigQuery)",
                    status = MatchStatus.MET,
                    type = RequirementType.TOOL,
                    evidence = listOf("BigQuery sandbox, for my DBMS mini-project on a public dataset"),
                    factRefs = listOf(factRef("U-01", "DBMS mini-project", FactSource.USER_STATED, true)),
                ),
                item(
                    id = "req-sql",
                    text = "SQL databases",
                    status = MatchStatus.MET,
                    type = RequirementType.SKILL,
                    evidence = listOf("Wrote weekly SQL reports in PostgreSQL for the operations team"),
                    factRefs = listOf(
                        factRef("C-01", "DBMS coursework", FactSource.USER_STATED, true),
                        factRef("I-01", "Data intern, Kiran Agro Exports", FactSource.IMPORTED, true),
                    ),
                ),
            ),
        ),
    ),
    prepPlanCount = 0,
    canSave = true,
)

private fun gapClosedState(): AnalysisUiState.Result = resultState().copy(
    keywordCoverage = KeywordCoverage(covered = 10, total = 14),
    sections = listOf(
        RequirementSection(RequirementGroup.MustHaveGaps, emptyList()).let { it },
        RequirementSection(
            RequirementGroup.Partial,
            listOf(
                item(
                    id = "req-excel",
                    text = "Advanced Excel — pivots yes, macros not yet",
                    status = MatchStatus.PARTIAL,
                    type = RequirementType.TOOL,
                    evidence = listOf("Built the attendance register in Excel for my internship"),
                    factRefs = listOf(
                        factRef("I-01", "Data intern, Kiran Agro Exports", FactSource.IMPORTED, true),
                    ),
                ),
            ),
        ),
        RequirementSection(
            RequirementGroup.Met,
            listOf(
                item(
                    id = "req-dw",
                    text = "Cloud data warehouse (Snowflake or BigQuery)",
                    status = MatchStatus.MET,
                    type = RequirementType.TOOL,
                    evidence = listOf("BigQuery sandbox, for my DBMS mini-project on a public dataset"),
                    factRefs = listOf(
                        factRef("U-01", "DBMS mini-project", FactSource.USER_STATED, false),
                    ),
                ),
                item(
                    id = "req-sql",
                    text = "SQL databases",
                    status = MatchStatus.MET,
                    type = RequirementType.SKILL,
                    evidence = listOf("Skill: SQL"),
                    factRefs = listOf(factRef("C-01", "DBMS coursework", FactSource.USER_STATED, true)),
                ),
            ),
        ),
        RequirementSection(
            RequirementGroup.NiceToHaveGaps,
            listOf(
                item(
                    id = "req-agile",
                    text = "Agile / JIRA",
                    status = MatchStatus.GAP,
                    type = RequirementType.SOFT_SKILL,
                    priority = RequirementPriority.NICE_TO_HAVE,
                ),
            ),
        ),
    ),
    prepPlanCount = 0,
)

private fun noKeyTermsState(): AnalysisUiState.Result = AnalysisUiState.Result(
    title = "Associate Analyst",
    company = "Northwind GCC",
    keywordCoverage = KeywordCoverage(covered = 0, total = 0),
    sections = emptyList(),
    prepPlanCount = 0,
    canSave = true,
)

private fun item(
    id: String,
    text: String,
    status: MatchStatus,
    type: RequirementType,
    priority: RequirementPriority = RequirementPriority.MUST_HAVE,
    evidence: List<String> = emptyList(),
    factRefs: List<RequirementFactRef> = emptyList(),
    isInPrepPlan: Boolean = false,
) = RequirementItem(
    requirement = JobRequirement(
        id = id,
        text = text,
        type = type,
        priority = priority,
        keywords = listOf(text.lowercase()),
    ),
    status = status,
    evidence = evidence,
    isInPrepPlan = isInPrepPlan,
    factRefs = factRefs,
)

private fun factRef(
    id: String,
    text: String,
    source: FactSource,
    isConfirmed: Boolean,
) = RequirementFactRef(factId = id, text = text, source = source, isConfirmed = isConfirmed)
