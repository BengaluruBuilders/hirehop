package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.ProposeJobLabelUseCase
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OneParagraphJobDescriptionTest {
    private val analyzer = OfflineJobDescriptionAnalyzer()
    private val useCase = ProposeJobLabelUseCase(analyzer)

    private val text =
        "Senior Android Developer at Northwind Mobile, Bengaluru. We are building a banking app used by " +
            "millions. You will write Kotlin and Jetpack Compose. Requirements: 3 years of Kotlin experience. " +
            "Experience with Room and Retrofit."

    @Test
    fun oneParagraphJdYieldsRoleAndCompany() = runTest {
        val job = analyzer.analyze(text)
        assertThat(job.title).isEqualTo("Senior Android Developer")
        assertThat(job.company).isEqualTo("Northwind Mobile")
    }

    @Test
    fun oneParagraphJdProposesRoleAndCompany() = runTest {
        val proposal = useCase(text)
        assertThat(proposal.role).isEqualTo("Senior Android Developer")
        assertThat(proposal.company).isEqualTo("Northwind Mobile")
    }

    @Test
    fun oneParagraphHeadlineSentenceIsNotARequirement() = runTest {
        val job = analyzer.analyze(text)
        assertThat(job.requirements.none { it.text.contains("Northwind Mobile") }).isTrue()
    }

    @Test
    fun oneParagraphRestStillYieldsRequirements() = runTest {
        val job = analyzer.analyze(text)
        assertThat(job.requirements).isNotEmpty()
        assertThat(job.requirements.map { it.text }.any { it.contains("Kotlin") }).isTrue()
    }

    @Test
    fun oneParagraphWithoutHeadlinePatternStaysBlank() = runTest {
        val job = analyzer.analyze(
            "We are building a banking app used by millions. You will write Kotlin and Jetpack Compose and " +
                "ship every week to a large user base across the country.",
        )
        assertThat(job.title).isEmpty()
        assertThat(job.company).isEmpty()
    }
}
