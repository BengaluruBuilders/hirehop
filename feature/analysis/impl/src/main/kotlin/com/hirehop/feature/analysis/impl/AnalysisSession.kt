package com.hirehop.feature.analysis.impl

import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.MatchStatus

internal sealed interface AnalysisSession {
    data class Idle(val previous: Ready? = null) : AnalysisSession {
        fun resume(analysis: JobAnalysisResult): Ready {
            val sameText = previous?.analysis?.job?.rawText == analysis.job.rawText
            return if (previous != null && sameText) previous.withFreshAnalysis(analysis) else Ready.of(analysis)
        }
    }

    data object Analyzing : AnalysisSession

    data class Ready(
        val analysis: JobAnalysisResult,
        val title: String,
        val company: String,
        val prepRequirementIds: Set<String>,
    ) : AnalysisSession {

        fun gapRequirementIds(): Set<String> = analysis.gap.matches
            .filter { it.status == MatchStatus.GAP }
            .map { it.requirement.id }
            .toSet()

        fun findRequirement(requirementId: String): JobRequirement? = analysis.gap.matches
            .firstOrNull { it.requirement.id == requirementId }
            ?.requirement

        fun prepRequirements(): List<JobRequirement> = analysis.gap.matches
            .filter { it.status == MatchStatus.GAP && it.requirement.id in prepRequirementIds }
            .map { it.requirement }

        fun withEditedJob(): JobAnalysisResult = analysis.copy(
            job = analysis.job.copy(title = title.trim(), company = company.trim()),
        )

        fun withPrepToggled(requirementId: String): Ready = copy(
            prepRequirementIds = if (requirementId in prepRequirementIds) {
                prepRequirementIds - requirementId
            } else {
                prepRequirementIds + requirementId
            },
        )

        fun withFreshAnalysis(fresh: JobAnalysisResult): Ready {
            val refreshed = copy(analysis = fresh)
            return refreshed.copy(prepRequirementIds = prepRequirementIds intersect refreshed.gapRequirementIds())
        }

        companion object {
            fun of(analysis: JobAnalysisResult): Ready = Ready(
                analysis = analysis,
                title = analysis.job.title,
                company = analysis.job.company,
                prepRequirementIds = emptySet(),
            )
        }
    }

    data object Saving : AnalysisSession

    data class Saved(val applicationId: String) : AnalysisSession
}
