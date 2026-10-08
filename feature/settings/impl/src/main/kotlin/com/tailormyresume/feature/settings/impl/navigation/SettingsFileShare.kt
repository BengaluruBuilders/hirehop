package com.tailormyresume.feature.settings.impl.navigation

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class SettingsFileProvider : FileProvider()

internal fun createSettingsShareIntent(
    context: Context,
    file: File,
    subject: String,
    chooserTitle: String,
): Intent {
    val authority = "${context.packageName}.settings.fileprovider"
    val uri = FileProvider.getUriForFile(context, authority, file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = ZIP_MIME_TYPE
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(send, chooserTitle)
}

private const val ZIP_MIME_TYPE = "application/zip"
