package com.hirehop.app.di

import com.hirehop.app.ai.RemoteCoverLetterSource
import com.hirehop.app.ai.RemoteJobAnalysisSource
import com.hirehop.app.ai.RemotePrepQuestionSource
import com.hirehop.app.ai.RemoteResumeTailor
import com.hirehop.app.ai.RemoteResumeTextParser
import com.hirehop.core.domain.JobAnalysisSource
import com.hirehop.core.domain.ResumeTailor
import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.domain.coverletter.CoverLetterSource
import com.hirehop.core.domain.prep.PrepQuestionSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AiBindings {
    @Binds
    fun bindJobAnalysisSource(impl: RemoteJobAnalysisSource): JobAnalysisSource

    @Binds
    fun bindResumeTailor(impl: RemoteResumeTailor): ResumeTailor

    @Binds
    fun bindResumeTextParser(impl: RemoteResumeTextParser): ResumeTextParser

    @Binds
    fun bindCoverLetterSource(impl: RemoteCoverLetterSource): CoverLetterSource

    @Binds
    fun bindPrepQuestionSource(impl: RemotePrepQuestionSource): PrepQuestionSource
}
