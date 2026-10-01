package com.hirehop.feature.analysis.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType

@Preview(showBackground = true)
@Composable
private fun AnalysisInputPreview() {
    HhTheme {
        AnalysisScreen(
            uiState = AnalysisUiState.Input(
                jobText = "Android developer. You know Kotlin, Jetpack Compose and Room.",
                canAnalyze = true,
            ),
            actions = AnalysisActions(),
        )
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
private fun AnalysisResultPreview() {
    HhTheme {
        AnalysisScreen(uiState = previewResultState, actions = AnalysisActions())
    }
}

private val previewResultState = AnalysisUiState.Result(
    title = "Android Developer",
    company = "Acme",
    keywordCoverage = KeywordCoverage(covered = 2, total = 4),
    sections = listOf(
        RequirementSection(
            RequirementGroup.MustHaveGaps,
            listOf(previewItem("Experience with Room", MatchStatus.GAP, isInPrepPlan = true)),
        ),
        RequirementSection(
            RequirementGroup.Partial,
            listOf(
                previewItem(
                    "Jetpack Compose",
                    MatchStatus.PARTIAL,
                    type = RequirementType.TOOL,
                    evidence = listOf("Built a small Compose demo app"),
                    factRefs = listOf(
                        RequirementFactRef("P-02", "Placement Stats Dashboard", FactSource.IMPORTED, true),
                    ),
                ),
            ),
        ),
        RequirementSection(
            RequirementGroup.Met,
            listOf(previewItem("Kotlin", MatchStatus.MET, evidence = listOf("Skill: kotlin"))),
        ),
        RequirementSection(
            RequirementGroup.NiceToHaveGaps,
            listOf(
                previewItem("CI with GitHub Actions", MatchStatus.GAP, priority = RequirementPriority.NICE_TO_HAVE),
            ),
        ),
    ),
    prepPlanCount = 1,
    canSave = true,
)

private fun previewItem(
    text: String,
    status: MatchStatus,
    priority: RequirementPriority = RequirementPriority.MUST_HAVE,
    type: RequirementType = RequirementType.SKILL,
    evidence: List<String> = emptyList(),
    factRefs: List<RequirementFactRef> = emptyList(),
    isInPrepPlan: Boolean = false,
) = RequirementItem(
    requirement = JobRequirement(
        id = text,
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
