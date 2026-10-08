package com.tailormyresume.feature.tailor.impl.exported

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

internal class ExportedFileStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun fileFor(fileName: String): File? {
        if (fileName.isBlank()) return null
        val file = File(File(context.cacheDir, EXPORT_DIRECTORY), fileName)
        return file.takeIf { candidate -> candidate.isFile }
    }

    private companion object {
        const val EXPORT_DIRECTORY = "exports"
    }
}
