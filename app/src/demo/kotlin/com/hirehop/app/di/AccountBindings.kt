package com.hirehop.app.di

import com.hirehop.core.data.repository.ContentReportRepository
import com.hirehop.core.data.repository.StoredContentReportRepository
import com.hirehop.core.domain.account.AccountDataExporter
import com.hirehop.core.domain.offline.OfflineAccountDataExporter
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
