package com.hirehop.app.di

import com.hirehop.app.account.RemoteAccountDataExporter
import com.hirehop.app.account.RemoteContentReportRepository
import com.hirehop.core.data.repository.ContentReportRepository
import com.hirehop.core.domain.account.AccountDataExporter
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface AccountBindings {
    @Binds
    fun bindAccountDataExporter(impl: RemoteAccountDataExporter): AccountDataExporter

    @Binds
    fun bindContentReportRepository(impl: RemoteContentReportRepository): ContentReportRepository
}
