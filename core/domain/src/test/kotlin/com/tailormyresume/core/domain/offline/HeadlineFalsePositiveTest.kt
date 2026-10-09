package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class HeadlineFalsePositiveTest {
    private val analyzer = OfflineJobDescriptionAnalyzer()

    private suspend fun assertBlank(text: String) {
        val job = analyzer.analyze(text)
        assertThat(job.title).isEmpty()
        assertThat(job.company).isEmpty()
    }

    @Test
    fun sentenceAboutApplicationReviewIsNotAHeadline() = runTest {
        assertBlank("Hiring managers will review all applications. $FOLLOW_ON")
    }

    @Test
    fun sentenceAboutOpeningAnAccountIsNotAHeadline() = runTest {
        assertBlank("Opening a bank account is not required. $FOLLOW_ON")
    }

    @Test
    fun sentenceAboutOpeningsIsNotAHeadline() = runTest {
        assertBlank("Openings available across teams. $FOLLOW_ON")
    }

    @Test
    fun teamLookingForACommunicatorIsNotAHeadline() = runTest {
        assertBlank("Our team is looking for a strong communicator to join us. $FOLLOW_ON")
    }

    @Test
    fun idealCandidateSeekingGrowthIsNotAHeadline() = runTest {
        assertBlank("The ideal candidate is seeking growth. $FOLLOW_ON")
    }

    @Test
    fun experienceAtGoogleIsNotAHeadlineButStaysARequirement() = runTest {
        assertBlank("Experience as a software engineer at Google is a plus. $FOLLOW_ON")
        val job = analyzer.analyze("Experience as a software engineer at Google is a plus. $FOLLOW_ON")
        assertThat(job.requirements.map { it.text }.any { it.contains("Google") }).isTrue()
    }

    @Test
    fun leadingTeamsSentenceIsNotAHeadline() = runTest {
        assertBlank("You will lead teams at scale. $FOLLOW_ON")
    }

    @Test
    fun immediateJoinersSentenceIsNotAHeadline() = runTest {
        assertBlank("We need a developer - immediate joiners. $FOLLOW_ON")
    }

    @Test
    fun clientHiringSentenceIsNotAHeadline() = runTest {
        assertBlank("Our client is hiring for a Java developer in Pune. $FOLLOW_ON")
    }

    @Test
    fun germanHiringSentenceIsNotAHeadline() = runTest {
        assertBlank("Wir suchen einen Softwareentwickler fuer unser Team in Berlin. $FOLLOW_ON")
    }

    @Test
    fun hindiHiringSentenceIsNotAHeadline() = runTest {
        assertBlank(
            "हम बेंगलुरु में एक सॉफ्टवेयर इंजीनियर की तलाश कर रहे हैं। " +
                "आप हमारे बैंकिंग ऐप के लिए कोटलिन और कंपोज़ लिखेंगे जिसका उपयोग देश भर में " +
                "लाखों उपयोगकर्ता करते हैं और यह हमारी सबसे बड़ी परियोजना है।",
        )
    }

    @Test
    fun headlineSentenceAfterOpeningSentenceIsNotAHeadline() = runTest {
        assertBlank(
            "We are building a banking app used by millions across the country. " +
                "Northwind Mobile is hiring a Senior Android Engineer in Bengaluru. $FOLLOW_ON",
        )
    }

    @Test
    fun hiringHeadlineWithFollowOnStillYieldsRoleAndCompany() = runTest {
        val job =
            analyzer.analyze(
                "Northwind Mobile is hiring a Senior Android Engineer in Bengaluru. $FOLLOW_ON",
            )
        assertThat(job.title).isEqualTo("Senior Android Engineer")
        assertThat(job.company).isEqualTo("Northwind Mobile")
    }

    @Test
    fun roleAtCompanyWithFollowOnStillYieldsRoleAndCompany() = runTest {
        val job = analyzer.analyze("Senior Android Engineer at Northwind Mobile. $FOLLOW_ON")
        assertThat(job.title).isEqualTo("Senior Android Engineer")
        assertThat(job.company).isEqualTo("Northwind Mobile")
    }

    @Test
    fun rolePipeCompanyWithFollowOnStillYieldsRoleAndCompany() = runTest {
        val job = analyzer.analyze("Senior Android Engineer | Northwind Mobile. $FOLLOW_ON")
        assertThat(job.title).isEqualTo("Senior Android Engineer")
        assertThat(job.company).isEqualTo("Northwind Mobile")
    }

    private companion object {
        private const val FOLLOW_ON =
            "You will write Kotlin and Compose for our banking app used by millions of users across " +
                "the country."
    }
}
