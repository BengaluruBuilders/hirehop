package com.tailormyresume.app.di

import com.tailormyresume.app.ai.GuardedCoverLetterSource
import com.tailormyresume.app.ai.GuardedJobAnalysisSource
import com.tailormyresume.app.ai.GuardedPrepQuestionSource
import com.tailormyresume.app.ai.GuardedResumeTailor
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
    fun bindJobAnalysisSource(impl: GuardedJobAnalysisSource): JobAnalysisSource

    @Binds
    fun bindResumeTailor(impl: GuardedResumeTailor): ResumeTailor

    @Binds
    fun bindResumeTextParser(impl: RemoteResumeTextParser): ResumeTextParser

    @Binds
    fun bindCoverLetterSource(impl: GuardedCoverLetterSource): CoverLetterSource

    @Binds
    fun bindPrepQuestionSource(impl: GuardedPrepQuestionSource): PrepQuestionSource
}
