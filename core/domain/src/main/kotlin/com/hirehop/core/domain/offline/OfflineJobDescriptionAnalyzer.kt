package com.hirehop.core.domain.offline

import com.hirehop.core.domain.JobDescriptionAnalyzer
import com.hirehop.core.model.JobDescription
import javax.inject.Inject

internal class OfflineJobDescriptionAnalyzer @Inject constructor() : JobDescriptionAnalyzer {
    override fun analyze(rawText: String): JobDescription {
        val lines = JdLineReader.nonEmptyLines(rawText)
        val titleCompany = TitleCompanyExtractor.extract(lines)
        val contentLines = JdLineReader.read(lines).filterNot { it.index in titleCompany.consumedIndexes }
        return JobDescription(
            title = titleCompany.title,
            company = titleCompany.company,
            rawText = rawText,
            requirements = RequirementExtractor.extract(contentLines),
        )
    }
}
