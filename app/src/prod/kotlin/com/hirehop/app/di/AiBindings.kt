package com.hirehop.app.di

import com.hirehop.core.domain.JobDescriptionAnalyzer
import com.hirehop.core.domain.ResumeTailor
import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.domain.coverletter.CoverLetterSource
import com.hirehop.core.domain.coverletter.GenerateCoverLetterUseCase
import com.hirehop.core.domain.offline.OfflineJobDescriptionAnalyzer
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
    fun bindJobDescriptionAnalyzer(impl: OfflineJobDescriptionAnalyzer): JobDescriptionAnalyzer

    @Binds
    fun bindResumeTailor(impl: OfflineResumeTailor): ResumeTailor

    @Binds
    fun bindResumeTextParser(impl: OfflineResumeTextParser): ResumeTextParser

    @Binds
    fun bindCoverLetterSource(impl: GenerateCoverLetterUseCase): CoverLetterSource

    @Binds
    fun bindPrepQuestionSource(impl: GeneratePrepQuestionsUseCase): PrepQuestionSource
}
