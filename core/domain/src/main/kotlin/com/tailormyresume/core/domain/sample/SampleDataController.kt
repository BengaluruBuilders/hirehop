package com.tailormyresume.core.domain.sample

interface SampleDataController {
    suspend fun load()

    suspend fun reset()

    suspend fun keepSampleJobDescription()

    suspend fun clearSampleJobDescription()
}
