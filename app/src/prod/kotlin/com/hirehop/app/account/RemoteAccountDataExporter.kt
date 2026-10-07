package com.hirehop.app.account

import android.content.Context
import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers.IO
import com.hirehop.core.domain.account.AccountData
import com.hirehop.core.domain.account.AccountDataArchive
import com.hirehop.core.domain.account.AccountDataArchiveWriter
import com.hirehop.core.domain.account.AccountDataExporter
import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.apiResult
import com.hirehop.core.network.dto.ServerExportResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject

class RemoteAccountDataExporter internal constructor(
    private val directory: () -> File,
    private val api: HirehopApi,
    private val json: Json,
    private val ioDispatcher: CoroutineDispatcher,
) : AccountDataExporter {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        api: HirehopApi,
        json: Json,
        @Dispatcher(IO) ioDispatcher: CoroutineDispatcher,
    ) : this({ File(context.cacheDir, EXPORT_DIRECTORY) }, api, json, ioDispatcher)

    override suspend fun export(data: AccountData): AccountDataArchive {
        val serverPart = apiResult { api.exportMe() }.fold(
            onSuccess = { json.encodeToString(ServerExportResponse.serializer(), it) },
            onFailure = { UNAVAILABLE_PART },
        )
        val target = File(directory(), FILE_NAME)
        withContext(ioDispatcher) { AccountDataArchiveWriter().write(data, target, mapOf(SERVER_ENTRY to serverPart)) }
        return AccountDataArchive(fileName = FILE_NAME, file = target)
    }

    private companion object {
        const val EXPORT_DIRECTORY = "data-exports"
        const val FILE_NAME = "hirehop-my-data.zip"
        const val SERVER_ENTRY = "server.json"
        const val UNAVAILABLE_PART =
            """{"unavailable":"The server part of this export could not be fetched. Export again when you are online."}"""
    }
}
