package com.tailormyresume.feature.tailor.impl.export.docx

import android.content.Context
import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers
import com.tailormyresume.feature.tailor.impl.document.ResumeDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import javax.inject.Inject

internal class AndroidDocxResumeRenderer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(TmrDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ResumeDocxRenderer {

    override suspend fun render(document: ResumeDocument, fileName: String): File =
        JdkDocxResumeRenderer(docxExportDirectory(context.cacheDir), ioDispatcher).render(document, fileName)
}
