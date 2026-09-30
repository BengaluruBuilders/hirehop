package com.hirehop.core.domain

import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.JobDescription

data class JobAnalysisResult(val job: JobDescription, val gap: GapAnalysis)
