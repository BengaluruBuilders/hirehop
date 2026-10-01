package com.hirehop.feature.tailor.impl.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

internal fun createPdfShareIntent(
    context: Context,
    file: File,
    subject: String,
    chooserTitle: String,
): Intent = createShareIntent(context, file, subject, chooserTitle, PDF_MIME_TYPE)

internal fun createDocxShareIntent(
    context: Context,
    file: File,
    subject: String,
    chooserTitle: String,
): Intent = createShareIntent(context, file, subject, chooserTitle, DOCX_MIME_TYPE)

private fun createShareIntent(
    context: Context,
    file: File,
    subject: String,
    chooserTitle: String,
    mimeType: String,
): Intent {
    val authority = "${context.packageName}.tailor.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(send, chooserTitle)
}

private const val PDF_MIME_TYPE = "application/pdf"
private const val DOCX_MIME_TYPE =
    "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
