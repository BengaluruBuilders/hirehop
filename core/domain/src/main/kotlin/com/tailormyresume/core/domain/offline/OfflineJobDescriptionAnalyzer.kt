package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.domain.JobDescriptionAnalyzer
import com.tailormyresume.core.model.JobDescription
import javax.inject.Inject

class OfflineJobDescriptionAnalyzer @Inject constructor() : JobDescriptionAnalyzer {
    override suspend fun analyze(rawText: String): JobDescription {
        val lines = JdLineReader.nonEmptyLines(rawText)
        val titleCompany = TitleCompanyExtractor.extract(lines)
        val contentLines = JdLineReader.read(lines).filterNot { it.index in titleCompany.consumedIndexes }
            .mapNotNull { withoutHeadlineSentence(it, titleCompany) }
        return JobDescription(
            title = titleCompany.title,
            company = titleCompany.company,
            rawText = rawText,
            requirements = RequirementExtractor.extract(contentLines),
        )
    }

    private fun withoutHeadlineSentence(line: JdLine, titleCompany: TitleCompany): JdLine? {
        val sentence = titleCompany.headlineSentence
        if (line.index != titleCompany.headlineSentenceIndex || sentence == null) return line
        if (!line.text.contains(sentence)) return line
        val remaining = line.text.replace(sentence, "").trim()
        return if (remaining.isEmpty()) null else line.copy(text = remaining)
    }
}
