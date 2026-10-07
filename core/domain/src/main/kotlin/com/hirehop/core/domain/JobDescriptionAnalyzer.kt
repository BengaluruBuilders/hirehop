package com.hirehop.core.domain

import com.hirehop.core.model.JobDescription

interface JobDescriptionAnalyzer {
    suspend fun analyze(rawText: String): JobDescription
}
