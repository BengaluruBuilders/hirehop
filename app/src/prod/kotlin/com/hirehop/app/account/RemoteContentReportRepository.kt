package com.hirehop.app.account

import com.hirehop.core.common.network.di.ApplicationScope
import com.hirehop.core.data.repository.ContentReportRepository
import com.hirehop.core.data.repository.PendingReportQueue
import com.hirehop.core.data.repository.StoredContentReportRepository
import com.hirehop.core.model.ContentReport
import com.hirehop.core.network.ApiError
import com.hirehop.core.network.ApiException
import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.apiResult
import com.hirehop.core.network.mapper.toRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteContentReportRepository @Inject constructor(
    private val local: StoredContentReportRepository,
    private val queue: PendingReportQueue,
    private val api: HirehopApi,
    @ApplicationScope private val scope: CoroutineScope,
) : ContentReportRepository by local {

    private val flushMutex = Mutex()

    init {
        scope.launch { flush() }
    }

    override suspend fun report(report: ContentReport) {
        local.report(report)
        queue.add(report)
        scope.launch { flush() }
    }

    suspend fun flush() = flushMutex.withLock {
        queue.pending().forEach { report ->
            val failure = apiResult { api.reportContent(report.toRequest()) }.exceptionOrNull()
            if (failure == null || (failure as ApiException).error.isPermanent()) queue.remove(report)
        }
    }

    private fun ApiError.isPermanent() = when (this) {
        ApiError.InvalidInput, ApiError.PayloadTooLarge, ApiError.Forbidden, ApiError.CrossAppToken,
        ApiError.NotFound, ApiError.AppIdInvalid, ApiError.AppNotFound, ApiError.AccountDeleted,
        -> true
        else -> false
    }
}
