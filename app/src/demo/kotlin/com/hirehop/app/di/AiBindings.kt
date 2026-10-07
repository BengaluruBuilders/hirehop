package com.hirehop.app.di

import com.hirehop.core.domain.JobAnalysisSource
import com.hirehop.core.domain.ResumeTailor
import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.domain.coverletter.CoverLetterSource
import com.hirehop.core.domain.coverletter.GenerateCoverLetterUseCase
import com.hirehop.core.domain.offline.OfflineJobAnalysisSource
import com.hirehop.core.domain.offline.OfflineResumeTailor
import com.hirehop.core.domain.offline.OfflineResumeTextParser
import com.hirehop.core.domain.prep.GeneratePrepQuestionsUseCase
import com.hirehop.core.domain.prep.PrepQuestionSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AiBindings {
    @Binds
    fun bindJobAnalysisSource(impl: OfflineJobAnalysisSource): JobAnalysisSource

    @Binds
    fun bindResumeTailor(impl: OfflineResumeTailor): ResumeTailor

    @Binds
    fun bindResumeTextParser(impl: OfflineResumeTextParser): ResumeTextParser

    @Binds
    fun bindCoverLetterSource(impl: GenerateCoverLetterUseCase): CoverLetterSource

    @Binds
    fun bindPrepQuestionSource(impl: GeneratePrepQuestionsUseCase): PrepQuestionSource
}
