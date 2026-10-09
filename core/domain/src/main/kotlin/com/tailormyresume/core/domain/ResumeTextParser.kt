package com.tailormyresume.core.domain

import com.tailormyresume.core.model.CandidateProfile

interface ResumeTextParser {
    suspend fun parse(rawText: String): CandidateProfile

    suspend fun parse(rawText: String, keptEntries: Int): CandidateProfile = parse(rawText)
}
