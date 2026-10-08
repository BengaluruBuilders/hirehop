package com.tailormyresume.app.di

import com.tailormyresume.app.account.CacheExportedFiles
import com.tailormyresume.core.domain.account.ExportedFiles
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface ExportedFilesBindings {
    @Binds
    fun bindExportedFiles(impl: CacheExportedFiles): ExportedFiles
}
