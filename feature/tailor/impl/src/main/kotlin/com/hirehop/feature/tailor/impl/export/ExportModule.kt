package com.hirehop.feature.tailor.impl.export

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface ExportModule {

    @Binds
    fun bindResumePdfRenderer(renderer: AndroidPdfResumeRenderer): ResumePdfRenderer
}
