package com.tailormyresume.core.data.model

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.QuickQuestion
import org.junit.Test

class GapAnalysisReasonAndQuestionMapperTest {

    private val question = QuickQuestion("r1", "Have you presented to senior leaders?", "It is a must-have.")

    @Test
    fun matchReasonAndQuestionSurviveTheJsonColumn() {
        val analysis = testGapAnalysis.copy(
            matches = testGapAnalysis.matches.map { it.copy(reason = "Used SQL daily") },
            question = question,
        )

        val restored = analysis.asDto().asExternalModel()

        assertThat(restored.question).isEqualTo(question)
        assertThat(restored.matches.map { it.reason }.distinct()).containsExactly("Used SQL daily")
    }

    @Test
    fun analysisStoredBeforeTheFieldsExistedStillReads() {
        val restored = testGapAnalysis.copy(question = null).asDto().asExternalModel()

        assertThat(restored.question).isNull()
    }
}
