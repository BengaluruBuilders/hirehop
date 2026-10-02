package com.hirehop.core.domain

import javax.inject.Inject

data class JobLabelProposal(val role: String, val company: String)

class ProposeJobLabelUseCase @Inject constructor(
    private val analyzer: JobDescriptionAnalyzer,
) {
    operator fun invoke(rawText: String): JobLabelProposal {
        if (rawText.isBlank()) return JobLabelProposal(role = "", company = "")
        val job = analyzer.analyze(rawText)
        return JobLabelProposal(role = job.title.trim(), company = job.company.trim())
    }
}
