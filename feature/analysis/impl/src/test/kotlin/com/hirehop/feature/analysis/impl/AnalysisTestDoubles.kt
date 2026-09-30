package com.hirehop.feature.analysis.impl

import com.hirehop.core.data.repository.ApplicationRepository
import com.hirehop.core.data.repository.ProfileRepository
import com.hirehop.core.domain.FabricationGuard
import com.hirehop.core.domain.GapMatcher
import com.hirehop.core.domain.JobDescriptionAnalyzer
import com.hirehop.core.domain.ResumeTailor
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import com.hirehop.core.model.TailoredResume

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

    override fun analyze(rawText: String): JobDescription {
        check(!failing) { "analyzer failure" }
        return describe(rawText)
    }

    private fun describe(rawText: String) = JobDescription(
        title = "Android Developer",
        company = "Acme",
        rawText = rawText,
        requirements = listOf(kotlinRequirement, graphQlRequirement, sqlRequirement, dockerRequirement),
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
    override fun tailor(profile: CandidateProfile, job: JobDescription, gap: GapAnalysis) =
        TailoredResume(bullets = emptyList())
}

class AcceptingFabricationGuard : FabricationGuard {
    override fun check(
        proposedText: String,
        sources: List<EvidenceBullet>,
        profile: CandidateProfile,
    ): List<GuardrailViolation> = emptyList()
}

class FlakyProfileRepository(private val delegate: ProfileRepository) : ProfileRepository by delegate {
    var failOnSave = false

    override suspend fun saveProfile(profile: CandidateProfile) {
        check(!failOnSave) { "profile save failure" }
        delegate.saveProfile(profile)
    }
}

class FlakyApplicationRepository(
    private val delegate: ApplicationRepository,
) : ApplicationRepository by delegate {
    var failOnUpsert = false
    var failOnNotes = false

    override suspend fun upsertApplication(application: JobApplication) {
        check(!failOnUpsert) { "application save failure" }
        delegate.upsertApplication(application)
    }

    override suspend fun updateNotes(id: String, notes: String) {
        check(!failOnNotes) { "notes update failure" }
        delegate.updateNotes(id, notes)
    }
}
