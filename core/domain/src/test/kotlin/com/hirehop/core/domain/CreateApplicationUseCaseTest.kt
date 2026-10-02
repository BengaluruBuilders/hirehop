package com.hirehop.core.domain

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.offline.OfflineFabricationGuard
import com.hirehop.core.domain.offline.OfflineGapMatcher
import com.hirehop.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.hirehop.core.domain.offline.OfflineResumeTailor
import com.hirehop.core.domain.offline.resourceText
import com.hirehop.core.domain.offline.sampleProfile
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeptJobDescription
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Instant

class CreateApplicationUseCaseTest {
    private val repository = FakeApplicationRepository()
    private val clock = FixedClock(Instant.fromEpochSeconds(1_800_000_000))
    private val useCase = CreateApplicationUseCase(
        applicationRepository = repository,
        tailorResume = TailorResumeUseCase(OfflineResumeTailor(), OfflineFabricationGuard()),
        clock = clock,
        idGenerator = SequentialIdGenerator("app"),
    )
    private val analysis = AnalyzeJobUseCase(OfflineJobDescriptionAnalyzer(), OfflineGapMatcher())(
        sampleProfile,
        resourceText("jd_android.txt"),
    )

    @Test
    fun savesApplicationWithSavedStatusAndReturnsItsId() = runTest {
        val id = useCase(sampleProfile, analysis)

        assertThat(id).isEqualTo("app-1")
        val saved = checkNotNull(repository.current(id))
        assertThat(saved.status).isEqualTo(ApplicationStatus.SAVED)
        assertThat(saved.notes).isEmpty()
        assertThat(saved.job).isEqualTo(analysis.job)
        assertThat(saved.gapAnalysis).isEqualTo(analysis.gap)
    }

    @Test
    fun storesTailoredResumeAndTimestamps() = runTest {
        val saved = checkNotNull(repository.current(useCase(sampleProfile, analysis)))

        assertThat(checkNotNull(saved.tailoredResume).bullets).isNotEmpty()
        assertThat(saved.createdAt).isEqualTo(Instant.fromEpochSeconds(1_800_000_000))
        assertThat(saved.updatedAt).isEqualTo(saved.createdAt)
    }

    @Test
    fun eachCallCreatesADistinctApplication() = runTest {
        val first = useCase(sampleProfile, analysis)
        val second = useCase(sampleProfile, analysis)

        assertThat(first).isNotEqualTo(second)
        assertThat(repository.upsertCount).isEqualTo(2)
    }

    @Test
    fun keptCompanyAndRoleOverrideWhatTheAnalyzerFound() = runTest {
        val id = useCase(sampleProfile, analysis, KeptJobDescription("text", company = "Kestrel Labs", role = "Android Developer"))

        val job = checkNotNull(repository.current(id)).job
        assertThat(job.company).isEqualTo("Kestrel Labs")
        assertThat(job.title).isEqualTo("Android Developer")
    }

    @Test
    fun blankKeptValuesKeepWhatTheAnalyzerFound() = runTest {
        val id = useCase(sampleProfile, analysis, KeptJobDescription("text", company = "  ", role = ""))

        assertThat(checkNotNull(repository.current(id)).job).isEqualTo(analysis.job)
    }
}
