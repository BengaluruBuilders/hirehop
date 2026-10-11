package com.tailormyresume.feature.tailor.impl.navigation

import com.tailormyresume.core.data.repository.ContentReportRepository
import com.tailormyresume.core.data.repository.StoredContentReportRepository
import com.tailormyresume.core.domain.ResumeTailor
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.GapAnalysis
import com.tailormyresume.core.model.JobDescription
import com.tailormyresume.core.model.TailoredResume
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TailorHiltTestBindings {

    @Binds
    abstract fun bindContentReports(impl: StoredContentReportRepository): ContentReportRepository

    companion object {
        @Provides
        fun provideResumeTailor(): ResumeTailor = object : ResumeTailor {
            override suspend fun tailor(
                profile: CandidateProfile,
                job: JobDescription,
                gap: GapAnalysis,
                applicationId: String,
                section: EntryCategory?,
            ): TailoredResume = error("tailoring is not part of this test")
        }
    }
}
