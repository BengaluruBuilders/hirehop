package com.tailormyresume.core.domain

import com.tailormyresume.core.model.JobDescription

interface JobDescriptionAnalyzer {
    suspend fun analyze(rawText: String): JobDescription
}
