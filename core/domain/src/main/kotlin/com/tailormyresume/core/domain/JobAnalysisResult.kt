package com.tailormyresume.core.domain

import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobDescription
import kotlin.time.Instant

data class JobAnalysisResult(val job: JobDescription, val gap: GapAnalysis, val analysedAt: Instant? = null)
