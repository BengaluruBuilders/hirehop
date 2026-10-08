package com.tailormyresume.feature.applications.impl

import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.TailoredResume

data class GapSummary(
    val met: Int,
    val partial: Int,
    val gap: Int,
    val mustHaveGaps: List<String>,
)

data class ReviewProgress(val reviewed: Int, val total: Int)

fun GapAnalysis.toGapSummary(): GapSummary = GapSummary(
    met = matches.count { it.status == MatchStatus.MET },
    partial = matches.count { it.status == MatchStatus.PARTIAL },
    gap = matches.count { it.status == MatchStatus.GAP },
    mustHaveGaps = matches
        .filter { it.status == MatchStatus.GAP && it.requirement.priority == RequirementPriority.MUST_HAVE }
        .map { it.requirement.text },
)

fun TailoredResume.toReviewProgress(): ReviewProgress {
    val changes = bullets.filter { it.editTypes.isNotEmpty() }
    return ReviewProgress(
        reviewed = changes.count { it.decision != BulletDecision.PENDING },
        total = changes.size,
    )
}

fun JobApplication.gapSummaryOrNull(): GapSummary? = gapAnalysis?.toGapSummary()

fun JobApplication.reviewProgressOrNull(): ReviewProgress? = tailoredResume?.toReviewProgress()
