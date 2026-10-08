package com.tailormyresume.app.account

import android.content.Context
import com.tailormyresume.core.common.network.Dispatcher
import com.tailormyresume.core.common.network.TmrDispatchers.IO
import com.tailormyresume.core.domain.account.ExportedFiles
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CacheExportedFiles internal constructor(
    private val cacheDirectory: File,
    private val ioDispatcher: CoroutineDispatcher,
) : ExportedFiles {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        @Dispatcher(IO) ioDispatcher: CoroutineDispatcher,
    ) : this(context.cacheDir, ioDispatcher)

    override suspend fun deleteAll() {
    }
}
