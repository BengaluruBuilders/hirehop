package com.hirehop.core.domain.di

import com.hirehop.core.domain.FabricationGuard
import com.hirehop.core.domain.GapMatcher
import com.hirehop.core.domain.IdGenerator
import com.hirehop.core.domain.JobDescriptionAnalyzer
import com.hirehop.core.domain.LocalAnalyzer
import com.hirehop.core.domain.offline.OfflineFabricationGuard
import com.hirehop.core.domain.offline.OfflineGapMatcher
import com.hirehop.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.hirehop.core.domain.offline.UuidIdGenerator
import com.hirehop.core.domain.sample.OfflineSampleDataController
import com.hirehop.core.domain.sample.SampleDataController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DomainModule {
    @Binds
    @LocalAnalyzer
    abstract fun bindLocalAnalyzer(impl: OfflineJobDescriptionAnalyzer): JobDescriptionAnalyzer

    @Binds
    abstract fun bindGapMatcher(impl: OfflineGapMatcher): GapMatcher

    @Binds
    abstract fun bindFabricationGuard(impl: OfflineFabricationGuard): FabricationGuard

    @Binds
    abstract fun bindIdGenerator(impl: UuidIdGenerator): IdGenerator

    @Binds
    abstract fun bindSampleDataController(impl: OfflineSampleDataController): SampleDataController
}
