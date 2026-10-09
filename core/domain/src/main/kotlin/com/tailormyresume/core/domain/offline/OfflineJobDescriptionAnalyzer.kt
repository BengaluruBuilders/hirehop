package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.domain.JobDescriptionAnalyzer
import com.tailormyresume.core.model.JobDescription
import javax.inject.Inject

class OfflineJobDescriptionAnalyzer @Inject constructor() : JobDescriptionAnalyzer {
    override suspend fun analyze(rawText: String): JobDescription {
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
