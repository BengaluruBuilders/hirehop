package com.hirehop.feature.tailor.impl.export

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class AndroidPdfResumeRenderer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(HhDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ResumePdfRenderer {

    override suspend fun render(document: ResumeDocument, fileName: String): RenderedResume =
        withContext(ioDispatcher) {
            val pdf = PdfDocument()
            try {
                val writer = PdfPageWriter(pdf)
                ResumePdfComposer(
                    writer = writer,
                    style = PdfResumeStyle(document.template.textScale),
                    spaceScale = document.template.spaceScale,
                ).compose(document)
                val file = ExportDirectory(File(context.cacheDir, EXPORT_DIRECTORY)).write(fileName) { pdf.writeTo(it) }
                RenderedResume(file = file, pageCount = writer.pageCount)
            } finally {
                pdf.close()
            }
        }

    private companion object {
        const val EXPORT_DIRECTORY = "exports"
    }
}
