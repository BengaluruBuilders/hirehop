package com.hirehop.core.domain

import com.hirehop.core.model.CandidateProfile

interface ResumeTextParser {
    fun parse(rawText: String): CandidateProfile
}
