package com.hirehop.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

internal val errorJson = Json { ignoreUnknownKeys = true }

suspend fun <T> apiResult(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (failure: Exception) {
    Result.failure(ApiException(failure.toApiError()))
}

internal fun Exception.toApiError(): ApiError = when (this) {
    is ApiException -> error
    is HttpException -> httpApiError()
    is SocketTimeoutException -> ApiError.Timeout
    is IOException -> ApiError.Offline
    is SerializationException -> ApiError.Unknown(httpStatus = 0)
    else -> throw this
}

private fun HttpException.httpApiError(): ApiError {
    val errorCode = runCatching {
        errorJson.decodeFromString<ErrorEnvelope>(response()?.errorBody()?.string().orEmpty()).error?.code
    }.getOrNull()
    val retryAfter = response()?.headers()?.get("Retry-After")?.toIntOrNull()
    return apiErrorOf(code(), errorCode, retryAfter)
}
