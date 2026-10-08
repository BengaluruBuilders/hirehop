package com.tailormyresume.app.di

import com.tailormyresume.core.data.repository.ContentReportRepository
import com.tailormyresume.core.data.repository.StoredContentReportRepository
import com.tailormyresume.core.domain.account.AccountDataExporter
import com.tailormyresume.core.domain.offline.OfflineAccountDataExporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AccountBindings {
    @Binds
    fun bindAccountDataExporter(impl: OfflineAccountDataExporter): AccountDataExporter

    @Binds
    fun bindContentReportRepository(impl: StoredContentReportRepository): ContentReportRepository
}
