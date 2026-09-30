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

    override suspend fun render(document: ResumeDocument, fileName: String): File =
        withContext(ioDispatcher) {
            val target = exportFile(fileName)
            val pdf = PdfDocument()
            try {
                ResumePdfComposer(PdfPageWriter(pdf), PdfResumeStyle()).compose(document)
                target.outputStream().use { pdf.writeTo(it) }
            } finally {
                pdf.close()
            }
            target
        }

    private fun exportFile(fileName: String): File {
        val directory = File(context.cacheDir, EXPORT_DIRECTORY)
        directory.mkdirs()
        return File(directory, fileName)
    }

    private companion object {
        const val EXPORT_DIRECTORY = "exports"
    }
}
