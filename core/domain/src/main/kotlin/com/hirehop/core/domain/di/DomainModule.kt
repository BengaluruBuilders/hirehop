package com.hirehop.core.domain.di

import com.hirehop.core.domain.FabricationGuard
import com.hirehop.core.domain.GapMatcher
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.domain.JobDescriptionAnalyzer
import com.hirehop.core.domain.PaymentGateway
import com.hirehop.core.domain.ResumeTailor
import com.hirehop.core.domain.ResumeTextParser
import com.hirehop.core.domain.SignInGateway
import com.hirehop.core.domain.offline.OfflineFabricationGuard
import com.hirehop.core.domain.offline.OfflineGapMatcher
import com.hirehop.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.hirehop.core.domain.offline.OfflinePaymentGateway
import com.hirehop.core.domain.offline.OfflineResumeTailor
import com.hirehop.core.domain.offline.OfflineResumeTextParser
import com.hirehop.core.domain.offline.OfflineSignInGateway
import com.hirehop.core.domain.offline.UuidIdGenerator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DomainModule {
    @Binds
    abstract fun bindJobDescriptionAnalyzer(impl: OfflineJobDescriptionAnalyzer): JobDescriptionAnalyzer

    @Binds
    abstract fun bindGapMatcher(impl: OfflineGapMatcher): GapMatcher

    @Binds
    abstract fun bindResumeTailor(impl: OfflineResumeTailor): ResumeTailor

    @Binds
    abstract fun bindFabricationGuard(impl: OfflineFabricationGuard): FabricationGuard

    @Binds
    abstract fun bindResumeTextParser(impl: OfflineResumeTextParser): ResumeTextParser

    @Binds
    abstract fun bindIdGenerator(impl: UuidIdGenerator): IdGenerator

    @Binds
    abstract fun bindSignInGateway(impl: OfflineSignInGateway): SignInGateway

    @Binds
    abstract fun bindPaymentGateway(impl: OfflinePaymentGateway): PaymentGateway
}
