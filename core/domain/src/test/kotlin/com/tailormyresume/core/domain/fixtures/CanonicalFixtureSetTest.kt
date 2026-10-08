package com.tailormyresume.core.domain.fixtures

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.offline.OfflineGapMatcher
import com.tailormyresume.core.model.BulletDecision
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.testing.data.CANONICAL_TAILORING_EFFECT
import com.tailormyresume.core.testing.data.canonicalApplication
import com.tailormyresume.core.testing.data.canonicalCandidateProfile
import com.tailormyresume.core.testing.data.canonicalGapAnalysis
import com.tailormyresume.core.testing.data.canonicalJobDescription
import com.tailormyresume.core.testing.data.canonicalTailoredResume
import org.junit.Test

class CanonicalFixtureSetTest {

    @Test
    fun theCanonicalProfileHoldsEighteenFacts() {
        assertThat(canonicalCandidateProfile.entries).hasSize(18)
    }

    @Test
    fun fifteenFactsAreConfirmedAndThreeAreUserStated() {
        val confirmed = canonicalCandidateProfile.entries.count { entry -> entry.isConfirmed }
        val userStated = canonicalCandidateProfile.entries.count { entry -> entry.source == FactSource.USER_STATED }

        assertThat(confirmed).isEqualTo(15)
        assertThat(userStated).isEqualTo(3)
    }

    @Test
    fun theUnconfirmedFactsAreTheUserStatedOnes() {
        val unconfirmed = canonicalCandidateProfile.entries.filterNot { entry -> entry.isConfirmed }

        assertThat(unconfirmed.map { entry -> entry.id })
            .containsExactly("P-05", "C-03", "R-04")
    }

    @Test
    fun everyFactIdFollowsTheAgreedScheme() {
        val scheme = Regex("^[PICXRU]-[0-9]{2}$")

        canonicalCandidateProfile.entries.forEach { entry ->
            assertThat(scheme.matches(entry.id)).isTrue()
        }
    }

    @Test
    fun theSchemeUsesThePersonInternshipProjectCertificationRoleAndUniversityPrefixes() {
        val prefixes = canonicalCandidateProfile.entries.map { entry -> entry.id.substringBefore("-") }.toSet()

        assertThat(prefixes).containsExactly("P", "I", "C", "X", "R", "U")
    }

    @Test
    fun theAnchoredFactIdsAreTheOnesThePlanNames() {
        val ids = canonicalCandidateProfile.entries.map { entry -> entry.id }

        assertThat(ids).containsAtLeast("P-02", "I-01", "C-01", "X-01", "R-01", "U-01")
    }

    @Test
    fun everyFactIdIsUnique() {
        val ids = canonicalCandidateProfile.entries.map { entry -> entry.id }

        assertThat(ids.distinct()).hasSize(ids.size)
    }

    @Test
    fun everyBulletIdIsUniqueAcrossTheWholeProfile() {
        val bulletIds = canonicalCandidateProfile.entries.flatMap { entry -> entry.bullets }.map { it.id }

        assertThat(bulletIds.distinct()).hasSize(bulletIds.size)
    }

    @Test
    fun everyFactHasAtLeastOneBullet() {
        canonicalCandidateProfile.entries.forEach { entry ->
            assertThat(entry.bullets).isNotEmpty()
        }
    }

    @Test
    fun theCanonicalProfileIsAboutPriyaDeshmukhInPune() {
        assertThat(canonicalCandidateProfile.fullName).isEqualTo("Priya Deshmukh")
        assertThat(canonicalCandidateProfile.email).isEqualTo("priya.d@example.com")
        assertThat(canonicalCandidateProfile.headline).contains("Pune")
    }

    @Test
    fun theCanonicalJobIsNorthwindGcc() {
        assertThat(canonicalJobDescription.company).isEqualTo("Northwind GCC")
        assertThat(canonicalJobDescription.title).isEqualTo("Associate Android Engineer")
        assertThat(canonicalJobDescription.requirements).isNotEmpty()
    }

    @Test
    fun theCanonicalGapAnalysisCoversEveryRequirementOnce() {
        val required = canonicalJobDescription.requirements.map { requirement -> requirement.id }

        assertThat(canonicalGapAnalysis.matches.map { match -> match.requirement.id })
            .containsExactlyElementsIn(required)
    }

    @Test
    fun theCanonicalGapAnalysisNeverCitesAUserStatedFact() {
        val confirmedBulletIds = canonicalCandidateProfile.entries
            .filter { entry -> entry.isConfirmed }
            .flatMap { entry -> entry.bullets }
            .map { bullet -> bullet.id }
            .toSet()

        canonicalGapAnalysis.matches.forEach { match ->
            match.evidenceIds.forEach { evidenceId ->
                assertThat(confirmedBulletIds).contains(evidenceId)
            }
        }
    }

    @Test
    fun theCanonicalGapAnalysisMatchesWhatTheOfflineMatcherProduces() {
        val computed = OfflineGapMatcher().match(canonicalCandidateProfile, canonicalJobDescription)

        assertThat(canonicalGapAnalysis.matches.map { match -> match.requirement.id to match.status })
            .isEqualTo(computed.matches.map { match -> match.requirement.id to match.status })
        assertThat(canonicalGapAnalysis.keywordCoverage).isEqualTo(computed.keywordCoverage)
    }

    @Test
    fun theCanonicalGapAnalysisShowsAGapAndAPartialMatch() {
        val statuses = canonicalGapAnalysis.matches.map { match -> match.status }.toSet()

        assertThat(statuses).contains(MatchStatus.GAP)
        assertThat(statuses).contains(MatchStatus.PARTIAL)
    }

    @Test
    fun theCanonicalApplicationCarriesTheCanonicalJobAndGap() {
        assertThat(canonicalApplication.job).isEqualTo(canonicalJobDescription)
        assertThat(canonicalApplication.gapAnalysis).isEqualTo(canonicalGapAnalysis)
        assertThat(canonicalApplication.tailoredResume).isEqualTo(canonicalTailoredResume)
    }

    @Test
    fun theTailoringEffectPhrasingCarriesNoNumberAndNoGuarantee() {
        assertThat(CANONICAL_TAILORING_EFFECT).doesNotMatch("[0-9]")
        assertThat(CANONICAL_TAILORING_EFFECT.lowercase())
            .doesNotContain("score")
        assertThat(CANONICAL_TAILORING_EFFECT.lowercase()).doesNotContain("guarantee")
    }

    @Test
    fun noCanonicalFactPromisesATailoringResult() {
        canonicalCandidateProfile.entries.forEach { entry ->
            entry.bullets.forEach { bullet ->
                assertThat(bullet.text.lowercase()).doesNotContain("ats")
                assertThat(bullet.text.lowercase()).doesNotContain("guarantee")
            }
        }
    }

    @Test
    fun everyTailoredBulletCitesAConfirmedFact() {
        val confirmedBulletIds = canonicalCandidateProfile.entries
            .filter { entry -> entry.isConfirmed }
            .flatMap { entry -> entry.bullets }
            .map { bullet -> bullet.id }

        canonicalTailoredResume.bullets.forEach { bullet ->
            assertThat(bullet.sourceIds).isNotEmpty()
            bullet.sourceIds.forEach { sourceId ->
                assertThat(confirmedBulletIds).contains(sourceId)
            }
        }
    }

    @Test
    fun theCanonicalResumeIsPartiallyTailored() {
        val decisions = canonicalTailoredResume.bullets.map { bullet -> bullet.decision }.toSet()

        assertThat(decisions).containsExactly(BulletDecision.PENDING, BulletDecision.ACCEPTED, BulletDecision.REJECTED)
        assertThat(canonicalTailoredResume.bullets.any { bullet -> bullet.decision == BulletDecision.PENDING }).isTrue()
    }
}
