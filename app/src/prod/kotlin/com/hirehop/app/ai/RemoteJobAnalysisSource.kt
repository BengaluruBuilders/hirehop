package com.hirehop.app.ai

import com.hirehop.core.domain.GapMatcher
import com.hirehop.core.domain.JobAnalysisResult
import com.hirehop.core.domain.JobAnalysisSource
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.dto.AnalysisRequest
import com.hirehop.core.network.dto.MatchDto
import com.hirehop.core.network.dto.ProfileFactsDto
import com.hirehop.core.network.mapper.toFactsDto
import com.hirehop.core.network.mapper.toJobDescription
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteJobAnalysisSource @Inject constructor(
    private val api: HirehopApi,
    private val matcher: GapMatcher,
    private val json: Json,
) : JobAnalysisSource {
    private val cache = object : LinkedHashMap<String, JobAnalysisResult>() {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, JobAnalysisResult>) = size > CACHE_SIZE
    }

    override suspend fun analyse(profile: CandidateProfile, rawJobText: String): JobAnalysisResult {
        val facts = profile.toFactsDto()
        val key = cacheKey(rawJobText, facts)
        synchronized(cache) { cache[key] }?.let { return it }
        val response = remoteAi { api.analyse(AnalysisRequest(rawJobText, facts)) }
        val job = response.job.toJobDescription(rawJobText)
        val factIds = facts.entries.flatMap { entry -> entry.bullets.map { it.id } }.toSet()
        val gap = GapAnalysis(
            matches = matchesOf(job, response.matches, factIds),
            keywordCoverage = matcher.match(profile, job).keywordCoverage,
            generationId = response.generationId,
        )
        return JobAnalysisResult(job, gap).also { synchronized(cache) { cache[key] = it } }
    }

    private fun matchesOf(job: JobDescription, matches: List<MatchDto>, factIds: Set<String>): List<RequirementMatch> {
        val byRequirement = matches.associateBy { it.requirementId }
        return job.requirements.map { requirement ->
            val match = byRequirement[requirement.id]
            val evidence = match?.evidenceIds.orEmpty().filter { it in factIds }
            val status = match?.status?.takeUnless { it != MatchStatus.GAP && evidence.isEmpty() } ?: MatchStatus.GAP
            RequirementMatch(requirement, status, evidence)
        }
    }

    private fun cacheKey(rawJobText: String, facts: ProfileFactsDto): String {
        val job = rawJobText.trim().replace(WHITESPACE, " ")
        return sha256(job) + sha256(json.encodeToString(ProfileFactsDto.serializer(), facts))
    }

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }

    private companion object {
        const val CACHE_SIZE = 8
        val WHITESPACE = Regex("\\s+")
    }
}
