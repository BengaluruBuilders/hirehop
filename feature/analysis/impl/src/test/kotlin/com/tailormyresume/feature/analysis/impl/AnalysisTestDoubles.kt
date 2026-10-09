package com.tailormyresume.feature.analysis.impl

import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.domain.FabricationGuard
import com.tailormyresume.core.domain.GapMatcher
import com.tailormyresume.core.domain.JobDescriptionAnalyzer
import com.tailormyresume.core.domain.ResumeTailor
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.GuardrailViolation
import com.tailormyresume.core.model.JobApplication
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.RequirementMatch
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import com.tailormyresume.core.model.TailoredResume

const val TEST_JOB_TEXT = "Android developer needed. Kotlin, GraphQL, SQL and Docker are useful."
const val TEST_PROFILE_BULLET_ID = "bullet-1"

val kotlinRequirement = requirement("req-kotlin", "Kotlin", RequirementPriority.MUST_HAVE, "kotlin")
val graphQlRequirement = requirement("req-graphql", "GraphQL APIs", RequirementPriority.MUST_HAVE, "graphql")
val sqlRequirement = requirement("req-sql", "SQL databases", RequirementPriority.MUST_HAVE, "sql")
val dockerRequirement = requirement("req-docker", "Docker", RequirementPriority.NICE_TO_HAVE, "docker")

private fun requirement(
    id: String,
    text: String,
    priority: RequirementPriority,
    keyword: String,
) = JobRequirement(
    id = id,
    text = text,
    type = RequirementType.SKILL,
    priority = priority,
    keywords = listOf(keyword),
)

fun confirmedProfile() = CandidateProfile(
    fullName = "Priya Rao",
    email = "priya@example.com",
    phone = "0000000000",
    headline = "Final-year student",
    skills = listOf("Kotlin"),
    entries = listOf(
        ProfileEntry(
            id = "entry-1",
            category = EntryCategory.PROJECT,
            title = "Dashboard app",
            organization = "College",
            startDate = "2025-01",
            endDate = "2025-05",
            bullets = listOf(
                EvidenceBullet(TEST_PROFILE_BULLET_ID, "Built dashboards backed by a GraphQL API"),
            ),
            source = FactSource.IMPORTED,
            isConfirmed = true,
        ),
    ),
)

fun unconfirmedProfile() = confirmedProfile().let { profile ->
    profile.copy(entries = profile.entries.map { it.copy(isConfirmed = false) })
}

class FixedJobDescriptionAnalyzer : JobDescriptionAnalyzer {
    var failing = false
    var failure: AiFailure? = null
    var retryAfterSeconds: Int? = null
    var withoutRequirements = false

    override suspend fun analyze(rawText: String): JobDescription {
        check(!failing) { "analyzer failure" }
        failure?.let { throw AiException(it, retryAfterSeconds) }
        return describe(rawText)
    }

    private fun describe(rawText: String) = JobDescription(
        title = "Android Developer",
        company = "Acme",
        rawText = rawText,
        requirements = if (withoutRequirements) {
            emptyList()
        } else {
            listOf(kotlinRequirement, graphQlRequirement, sqlRequirement, dockerRequirement)
        },
    )
}

class KeywordGapMatcher : GapMatcher {
    val receivedProfiles = mutableListOf<CandidateProfile>()

    override fun match(profile: CandidateProfile, job: JobDescription): GapAnalysis {
        receivedProfiles += profile
        val matches = job.requirements.map { matchRequirement(profile, it) }
        return GapAnalysis(
            matches = matches,
            keywordCoverage = KeywordCoverage(
                covered = matches.count { it.status == MatchStatus.MET },
                total = matches.size,
            ),
        )
    }

    private fun matchRequirement(profile: CandidateProfile, requirement: JobRequirement): RequirementMatch {
        val skill = requirement.keywords.firstOrNull { keyword ->
            profile.skills.any { it.equals(keyword, ignoreCase = true) }
        }
        val bullet = profile.entries
            .filter { it.isConfirmed }
            .flatMap { it.bullets }
            .firstOrNull { bullet ->
                requirement.keywords.any { bullet.text.contains(it, ignoreCase = true) }
            }
        return when {
            skill != null -> RequirementMatch(requirement, MatchStatus.MET, listOf("skill:$skill"))
            bullet != null -> RequirementMatch(requirement, MatchStatus.PARTIAL, listOf(bullet.id))
            else -> RequirementMatch(requirement, MatchStatus.GAP, emptyList())
        }
    }
}

class EmptyResumeTailor : ResumeTailor {
    var failure: AiFailure? = null

    override suspend fun tailor(profile: CandidateProfile, job: JobDescription, gap: GapAnalysis, applicationId: String, section: EntryCategory?): TailoredResume {
        failure?.let { throw AiException(it) }
        return TailoredResume(bullets = emptyList())
    }
}

class AcceptingFabricationGuard : FabricationGuard {
    override fun check(
        proposedText: String,
        sources: List<EvidenceBullet>,
        profile: CandidateProfile,
    ): List<GuardrailViolation> = emptyList()
}

class FlakyApplicationRepository(
    private val delegate: ApplicationRepository,
) : ApplicationRepository by delegate {
    var failOnUpsert = false
    val attemptedIds = mutableListOf<String>()

    override suspend fun upsertApplication(application: JobApplication) {
        attemptedIds += application.id
        check(!failOnUpsert) { "application save failure" }
        delegate.upsertApplication(application)
    }
}

class CitingEveryBulletMatcher : GapMatcher {
    override fun match(profile: CandidateProfile, job: JobDescription): GapAnalysis {
        val bullets = profile.entries.filter { it.isConfirmed }.flatMap { it.bullets }
        val matches = job.requirements.map { requirement ->
            val cited = bullets
                .filter { bullet -> requirement.keywords.any { bullet.text.contains(it, ignoreCase = true) } }
                .map { it.id }
            RequirementMatch(requirement, if (cited.isEmpty()) MatchStatus.GAP else MatchStatus.PARTIAL, cited)
        }
        return GapAnalysis(matches, KeywordCoverage(matches.count { it.status != MatchStatus.GAP }, matches.size))
    }
}
