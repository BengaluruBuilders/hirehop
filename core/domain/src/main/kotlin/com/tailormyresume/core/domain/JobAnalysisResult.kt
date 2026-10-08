package com.tailormyresume.core.domain

import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobDescription

data class JobAnalysisResult(val job: JobDescription, val gap: GapAnalysis)
