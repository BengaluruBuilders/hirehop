package com.tailormyresume.app.di

import com.tailormyresume.app.ai.RemoteCoverLetterSource
import com.tailormyresume.app.ai.RemoteJobAnalysisSource
import com.tailormyresume.app.ai.RemotePrepQuestionSource
import com.tailormyresume.app.ai.RemoteResumeTailor
import com.tailormyresume.app.ai.RemoteResumeTextParser
import com.tailormyresume.core.domain.JobAnalysisSource
import com.tailormyresume.core.domain.ResumeTailor
import com.tailormyresume.core.domain.ResumeTextParser
import com.tailormyresume.core.domain.coverletter.CoverLetterSource
import com.tailormyresume.core.domain.prep.PrepQuestionSource
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
