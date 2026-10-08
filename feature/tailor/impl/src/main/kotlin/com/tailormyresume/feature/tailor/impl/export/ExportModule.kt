package com.tailormyresume.feature.tailor.impl.export

import com.tailormyresume.feature.tailor.impl.document.AndroidResumeHeadings
import com.tailormyresume.feature.tailor.impl.document.ResumeHeadings
import com.tailormyresume.feature.tailor.impl.export.docx.AndroidDocxResumeRenderer
import com.tailormyresume.feature.tailor.impl.export.docx.ResumeDocxRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface ExportModule {

    @Binds
    fun bindResumePdfRenderer(renderer: AndroidPdfResumeRenderer): ResumePdfRenderer

    @Binds
    fun bindResumeDocxRenderer(renderer: AndroidDocxResumeRenderer): ResumeDocxRenderer

    @Binds
    fun bindResumeHeadings(headings: AndroidResumeHeadings): ResumeHeadings
}
