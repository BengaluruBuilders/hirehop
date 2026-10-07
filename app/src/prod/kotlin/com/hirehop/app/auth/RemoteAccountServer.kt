package com.hirehop.app.auth

import com.hirehop.core.domain.ConsentUploader
import com.hirehop.core.domain.account.ServerAccountDeleter
import com.hirehop.core.model.ConsentPurpose
import com.hirehop.core.model.ConsentRecord
import com.hirehop.core.network.ApiError
import com.hirehop.core.network.ApiException
import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.apiResult
import com.hirehop.core.network.dto.ConsentRequest
import java.util.Locale
import javax.inject.Inject

internal fun ConsentPurpose.wireName(): String = name.lowercase(Locale.ROOT).replace('_', '-')

class RemoteConsentUploader @Inject constructor(private val api: HirehopApi) : ConsentUploader {
    override suspend fun upload(record: ConsentRecord): Result<Unit> {
        val policyVersion = record.noticeVersion.lowercase(Locale.ROOT)
        record.purposes.forEach { purpose ->
            apiResult { api.recordConsent(ConsentRequest(purpose.wireName(), granted = true, policyVersion)) }
                .onFailure { return Result.failure(it) }
        }
        return Result.success(Unit)
    }
}

class RemoteServerAccountDeleter @Inject constructor(private val api: HirehopApi) : ServerAccountDeleter {
    override suspend fun delete(): Result<Unit> = apiResult { api.deleteMe() }
        .map { }
        .recoverCatching { failure ->
            if ((failure as? ApiException)?.error == ApiError.AccountDeleted) Unit else throw failure
        }
}
