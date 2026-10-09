package com.tailormyresume.core.domain

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.domain.offline.OfflineFabricationGuard
import com.tailormyresume.core.domain.offline.OfflineGapMatcher
import com.tailormyresume.core.domain.offline.OfflineJobAnalysisSource
import com.tailormyresume.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.tailormyresume.core.domain.offline.OfflineResumeTailor
import com.tailormyresume.core.domain.offline.resourceText
import com.tailormyresume.core.domain.offline.sampleProfile
import com.tailormyresume.core.model.KeptJobDescription
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class CreateApplicationPrefillLabelsTest {
    private val repository = FakeApplicationRepository()
    private val useCase = CreateApplicationUseCase(
        applicationRepository = repository,
        tailorResume = TailorResumeUseCase(OfflineResumeTailor(), OfflineFabricationGuard()),
        clock = FixedClock(Instant.fromEpochSeconds(1_800_000_000)),
        idGenerator = SequentialIdGenerator("app"),
    )
    private val analysis = runBlocking {
        AnalyzeJobUseCase(OfflineJobAnalysisSource(OfflineJobDescriptionAnalyzer(), OfflineGapMatcher()))(
            sampleProfile,
            resourceText("jd_android.txt"),
        )
    }

    @Test
    fun untouchedPrefillYieldsToTheBackendLabel() = runTest {
        val kept = KeptJobDescription("jd", company = "Mis-parsed Co", role = "Mis-parsed Role", companyIsPrefill = true, roleIsPrefill = true)

        val saved = checkNotNull(repository.current(useCase(sampleProfile, analysis, kept)))

        assertThat(saved.job.title).isEqualTo(analysis.job.title)
        assertThat(saved.job.company).isEqualTo(analysis.job.company)
    }

    @Test
    fun typedLabelsStillWin() = runTest {
        val kept = KeptJobDescription("jd", company = "Typed Co", role = "Typed Role")

        val saved = checkNotNull(repository.current(useCase(sampleProfile, analysis, kept)))

        assertThat(saved.job.title).isEqualTo("Typed Role")
        assertThat(saved.job.company).isEqualTo("Typed Co")
    }
}
