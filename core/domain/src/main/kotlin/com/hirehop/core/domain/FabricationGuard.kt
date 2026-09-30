package com.hirehop.core.domain

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.GuardrailViolation

interface FabricationGuard {
    fun check(
        proposedText: String,
        sources: List<EvidenceBullet>,
        profile: CandidateProfile,
    ): List<GuardrailViolation>
}
