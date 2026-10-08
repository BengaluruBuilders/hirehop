package com.tailormyresume.app.ai

import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure
import com.tailormyresume.core.network.ApiError
import com.tailormyresume.core.network.ApiException
import com.tailormyresume.core.network.apiResult

internal fun ApiError.toAiFailure(): AiFailure = when (this) {
    ApiError.ConsentRequired -> AiFailure.ConsentRequired
    ApiError.NoCredit -> AiFailure.NoCredit
    ApiError.AllowanceExhausted -> AiFailure.AllowanceExhausted
    ApiError.InvalidInput, ApiError.PayloadTooLarge -> AiFailure.InvalidInput
    ApiError.AccountDeleted -> AiFailure.AccountDeleted
    ApiError.Offline -> AiFailure.Network
    ApiError.Timeout -> AiFailure.Timeout
    else -> AiFailure.Unavailable
}

internal fun <T> Result<T>.orAiFailure(): T = getOrElse { failure ->
    throw AiException((failure as? ApiException)?.error?.toAiFailure() ?: AiFailure.Unavailable)
}

internal suspend fun <T> remoteAi(block: suspend () -> T): T = apiResult(block).orAiFailure()
