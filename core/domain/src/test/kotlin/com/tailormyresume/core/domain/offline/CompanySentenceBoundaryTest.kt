package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CompanySentenceBoundaryTest {
    private val analyzer = OfflineJobDescriptionAnalyzer()

    @Test
    fun sentenceEndAfterCompanyIsNotPartOfTheCompany() = runTest {
        val job = analyzer.analyze("Data Analyst at Acme Corp. We need SQL")
        assertThat(job.title).isEqualTo("Data Analyst")
        assertThat(job.company).isEqualTo("Acme Corp")
    }

    @Test
    fun sentenceEndAfterCompanyIsNotPartOfTheCompanyWithTrailingSection() = runTest {
        val job = analyzer.analyze("Data Analyst at Acme Corp. We need SQL\nRequirements:\n- SQL")
        assertThat(job.title).isEqualTo("Data Analyst")
        assertThat(job.company).isEqualTo("Acme Corp")
    }

    @Test
    fun abbreviationPeriodsAreNotSentenceEnds() = runTest {
        val job = analyzer.analyze("Android Developer at Zenith Apps Pvt. Ltd.\nRequirements:\n- Kotlin")
        assertThat(job.title).isEqualTo("Android Developer")
        assertThat(job.company).isEqualTo("Zenith Apps Pvt. Ltd")
    }

    @Test
    fun labelledCompanyKeepsAbbreviationPeriods() = runTest {
        val job = analyzer.analyze("Company: Orbit Analytics Pvt. Ltd.\nRole: Analyst\n- SQL")
        assertThat(job.company).isEqualTo("Orbit Analytics Pvt. Ltd")
    }

    @Test
    fun commaSeparatedLocationIsStillDropped() = runTest {
        val job = analyzer.analyze("Senior Android Developer at Northwind Mobile, Bengaluru\nRequirements:\n- Kotlin")
        assertThat(job.title).isEqualTo("Senior Android Developer")
        assertThat(job.company).isEqualTo("Northwind Mobile")
    }

    @Test
    fun longParagraphIsNotUsedAsTitleOrCompany() = runTest {
        val job = analyzer.analyze(
            "Senior Android Developer at Northwind Mobile, Bengaluru. We are building a banking app used by millions. " +
                "You will write Kotlin and Jetpack Compose. Requirements: 3 years of Kotlin experience.",
        )
        assertThat(job.title).isEmpty()
        assertThat(job.company).isEmpty()
    }
}
