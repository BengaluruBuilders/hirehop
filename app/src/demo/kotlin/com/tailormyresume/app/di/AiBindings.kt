package com.tailormyresume.app.di

import com.tailormyresume.core.domain.JobAnalysisSource
import com.tailormyresume.core.domain.ResumeTailor
import com.tailormyresume.core.domain.ResumeTextParser
import com.tailormyresume.core.domain.coverletter.CoverLetterSource
import com.tailormyresume.core.domain.coverletter.GenerateCoverLetterUseCase
import com.tailormyresume.core.domain.offline.OfflineJobAnalysisSource
import com.tailormyresume.core.domain.offline.OfflineResumeTailor
import com.tailormyresume.core.domain.offline.OfflineResumeTextParser
import com.tailormyresume.core.domain.prep.GeneratePrepQuestionsUseCase
import com.tailormyresume.core.domain.prep.PrepQuestionSource
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
