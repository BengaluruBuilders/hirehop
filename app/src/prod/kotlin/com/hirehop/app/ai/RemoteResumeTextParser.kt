package com.hirehop.app.ai

import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.domain.fact.FactIdAllocator
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.dto.ParsedEntryDto
import com.hirehop.core.network.dto.ResumeParseRequest
import javax.inject.Inject

class RemoteResumeTextParser @Inject constructor(
    private val api: HirehopApi,
    private val ids: FactIdAllocator,
) : ResumeTextParser {
    override suspend fun parse(rawText: String): CandidateProfile {
        val parsed = remoteAi { api.parseResume(ResumeParseRequest(rawText)) }.profile
        return CandidateProfile(
            fullName = parsed.fullName,
            email = parsed.email,
            phone = parsed.phone,
            headline = parsed.headline,
            skills = parsed.skills,
            entries = entriesOf(parsed.entries),
        )
    }

    private fun entriesOf(parsed: List<ParsedEntryDto>): List<ProfileEntry> {
        val entries = mutableListOf<ProfileEntry>()
        parsed.forEach { entry ->
            val id = ids.nextId(entry.category, entries, entry.title)
            entries += ProfileEntry(
                id = id,
                category = entry.category,
                title = entry.title,
                organization = entry.organization,
                startDate = entry.startDate,
                endDate = entry.endDate,
                bullets = entry.bullets.filter { it.text.isNotBlank() }
                    .mapIndexed { index, bullet -> EvidenceBullet("$id-b${index + 1}", bullet.text) },
                source = FactSource.IMPORTED,
                isConfirmed = false,
            )
        }
        return entries
    }
}
