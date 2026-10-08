package com.tailormyresume.core.domain.di

import com.tailormyresume.core.domain.FabricationGuard
import com.tailormyresume.core.domain.GapMatcher
import com.tailormyresume.core.domain.IdGenerator
import com.tailormyresume.core.domain.JobDescriptionAnalyzer
import com.tailormyresume.core.domain.LocalAnalyzer
import com.tailormyresume.core.domain.offline.OfflineFabricationGuard
import com.tailormyresume.core.domain.offline.OfflineGapMatcher
import com.tailormyresume.core.domain.offline.OfflineJobDescriptionAnalyzer
import com.tailormyresume.core.domain.offline.UuidIdGenerator
import com.tailormyresume.core.domain.sample.OfflineSampleDataController
import com.tailormyresume.core.domain.sample.SampleDataController
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
