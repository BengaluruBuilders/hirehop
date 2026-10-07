package com.hirehop.app.ai

import com.hirehop.core.domain.ImportRemovalNotice
import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.domain.fact.FactIdAllocator
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.dto.ParsedEntryDto
import com.hirehop.core.network.dto.ResumeParseRequest
import com.hirehop.core.network.dto.SensitiveField
import javax.inject.Inject

class RemoteResumeTextParser @Inject constructor(
    private val api: HirehopApi,
    private val ids: FactIdAllocator,
    private val removalNotice: ImportRemovalNotice,
) : ResumeTextParser {
    override suspend fun parse(rawText: String): CandidateProfile {
        val response = remoteAi { api.parseResume(ResumeParseRequest(rawText)) }
        removalNotice.record(response.droppedSensitive.any { it == SensitiveField.DATE_OF_BIRTH || it == SensitiveField.PHOTO })
        val parsed = response.profile
        return CandidateProfile(
            fullName = parsed.fullName.orEmpty(),
            email = parsed.email.orEmpty(),
            phone = parsed.phone.orEmpty(),
            headline = parsed.headline.orEmpty(),
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
                organization = entry.organization.orEmpty(),
                startDate = entry.startDate.orEmpty(),
                endDate = entry.endDate.orEmpty(),
                bullets = entry.bullets.filter { it.text.isNotBlank() }
                    .mapIndexed { index, bullet -> EvidenceBullet("$id-b${index + 1}", bullet.text) },
                source = FactSource.IMPORTED,
                isConfirmed = false,
            )
        }
        return entries
    }
}
