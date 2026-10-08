package com.tailormyresume.feature.tailor.impl.export

import java.io.File
import java.io.IOException
import java.io.OutputStream

internal class ExportDirectory(private val directory: File) {

    fun write(fileName: String, writeContent: (OutputStream) -> Unit): File {
        directory.mkdirs()
        directory.listFiles()?.forEach { it.delete() }
        val target = File(directory, fileName)
        val temporary = File(directory, "$fileName$TEMPORARY_SUFFIX")
        try {
            temporary.outputStream().use(writeContent)
            if (!temporary.renameTo(target)) throw IOException("Could not move the export to ${target.name}")
        } finally {
            temporary.delete()
        }
        return target
    }

    private companion object {
        const val TEMPORARY_SUFFIX = ".tmp"
    }
}
