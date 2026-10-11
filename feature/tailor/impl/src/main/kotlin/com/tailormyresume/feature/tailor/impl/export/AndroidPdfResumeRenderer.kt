package com.tailormyresume.feature.tailor.impl.export

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers
import com.tailormyresume.core.model.PageSize
import com.tailormyresume.feature.tailor.impl.document.ResumeDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal class AndroidPdfResumeRenderer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(TmrDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
    @param:Dispatcher(TmrDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ResumePdfRenderer {

    override suspend fun render(document: ResumeDocument, fileName: String, pageSize: PageSize): RenderedResume =
        withContext(ioDispatcher) {
            val pdf = PdfDocument()
            try {
                val writer = PdfPageWriter(pdf)
                ResumePdfComposer(writer = writer, style = PdfResumeStyle()).compose(document)
                val file = ExportDirectory(File(context.cacheDir, EXPORT_DIRECTORY)).write(fileName) { pdf.writeTo(it) }
                RenderedResume(file = file, pageCount = writer.pageCount, lines = emptyList())
            } finally {
                pdf.close()
            }
        }

    override suspend fun pageCount(document: ResumeDocument, pageSize: PageSize): Int = TODO()

    private companion object {
        const val EXPORT_DIRECTORY = "exports"
    }
}
