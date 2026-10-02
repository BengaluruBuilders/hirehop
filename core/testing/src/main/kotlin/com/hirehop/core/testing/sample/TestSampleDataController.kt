package com.hirehop.core.testing.sample

import com.hirehop.core.domain.sample.SampleDataController

class TestSampleDataController : SampleDataController {

    var loadCount = 0
        private set

    var resetCount = 0
        private set

    override suspend fun load() {
        loadCount++
    }

    var sampleJobKept = false
        private set

    override suspend fun reset() {
        resetCount++
    }

    override suspend fun keepSampleJobDescription() {
        sampleJobKept = true
    }

    override suspend fun clearSampleJobDescription() {
        sampleJobKept = false
    }
}
