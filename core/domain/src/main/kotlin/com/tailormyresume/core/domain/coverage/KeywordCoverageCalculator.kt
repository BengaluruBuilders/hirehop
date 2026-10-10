package com.tailormyresume.core.domain.coverage

import com.tailormyresume.core.model.ApplicationKeywordCoverage
import com.tailormyresume.core.model.QuickAnswer
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.TailoredResume

object KeywordCoverageCalculator {
    fun compute(
        matches: List<RequirementMatch>,
        quickAnswer: QuickAnswer?,
        tailoredResume: TailoredResume?,
    ): ApplicationKeywordCoverage = ApplicationKeywordCoverage(now = 0, upTo = 0, final = null)
}
