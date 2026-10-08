package com.tailormyresume.feature.tailor.impl.export.docx

import com.tailormyresume.feature.tailor.impl.document.ResumeDocument
import com.tailormyresume.feature.tailor.impl.export.ExportDirectory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File

internal class JdkDocxResumeRenderer(
    private val exportDirectory: ExportDirectory,
    private val ioDispatcher: CoroutineDispatcher,
) : ResumeDocxRenderer {

    override suspend fun render(document: ResumeDocument, fileName: String): File =
        withContext(ioDispatcher) {
            exportDirectory.write(fileName) { out -> DocxPackage.write(document, out) }
        }
}

internal fun docxExportDirectory(cacheDirectory: File): ExportDirectory =
    ExportDirectory(File(cacheDirectory, EXPORT_DIRECTORY))

private const val EXPORT_DIRECTORY = "exports"
