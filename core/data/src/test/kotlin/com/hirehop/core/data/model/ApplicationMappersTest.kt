package com.hirehop.core.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApplicationMappersTest {

    @Test
    fun applicationRoundTripsThroughEntity() {
        assertThat(testApplication.asEntity().asExternalModel()).isEqualTo(testApplication)
    }

    @Test
    fun applicationWithoutAnalysisRoundTrips() {
        assertThat(testBareApplication.asEntity().asExternalModel())
            .isEqualTo(testBareApplication)
    }

    @Test
    fun entityFlattensJobFields() {
        val entity = testApplication.asEntity()

        assertThat(entity.jobTitle).isEqualTo(testJob.title)
        assertThat(entity.company).isEqualTo(testJob.company)
        assertThat(entity.rawText).isEqualTo(testJob.rawText)
    }

    @Test
    fun bareApplicationHasNullJsonColumns() {
        val entity = testBareApplication.asEntity()

        assertThat(entity.gapAnalysis).isNull()
        assertThat(entity.tailoredResume).isNull()
    }

    @Test
    fun gapAnalysisRoundTrips() {
        assertThat(testGapAnalysis.asDto().asExternalModel()).isEqualTo(testGapAnalysis)
    }

    @Test
    fun tailoredResumeRoundTrips() {
        assertThat(testTailoredResume.asDto().asExternalModel()).isEqualTo(testTailoredResume)
    }

    @Test
    fun everyGuardrailViolationRoundTrips() {
        everyViolation.forEach { violation ->
            assertThat(violation.asDto().asExternalModel()).isEqualTo(violation)
        }
    }

    @Test
    fun guardrailViolationDtosCoverEveryViolation() {
        val dtoTypes = everyViolation.map { it.asDto()::class }.distinct()

        assertThat(dtoTypes).hasSize(everyViolation.size)
    }
}
