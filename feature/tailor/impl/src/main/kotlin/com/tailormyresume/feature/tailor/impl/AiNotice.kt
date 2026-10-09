package com.tailormyresume.feature.tailor.impl

import androidx.annotation.StringRes
import com.tailormyresume.core.domain.AiException
import com.tailormyresume.core.domain.AiFailure

enum class AiNotice {
    Generic,
    InProgress,
    RateLimited,
    QuotaReached,
    SignInRequired,
}

internal fun Throwable.toAiNotice(): AiNotice = when ((this as? AiException)?.failure) {
    AiFailure.RateLimited -> AiNotice.RateLimited
    AiFailure.AnalysisInProgress -> AiNotice.InProgress
    AiFailure.QuotaExceeded -> AiNotice.QuotaReached
    AiFailure.SignInRequired -> AiNotice.SignInRequired
    else -> AiNotice.Generic
}

@StringRes
internal fun AiNotice.titleRes(@StringRes generic: Int): Int = when (this) {
    AiNotice.Generic -> generic
    AiNotice.InProgress -> R.string.feature_tailor_impl_ai_busy_title
    AiNotice.RateLimited -> R.string.feature_tailor_impl_ai_rate_limited_title
    AiNotice.QuotaReached -> R.string.feature_tailor_impl_ai_quota_title
    AiNotice.SignInRequired -> R.string.feature_tailor_impl_ai_sign_in_title
}

@StringRes
internal fun AiNotice.bodyRes(@StringRes generic: Int): Int = when (this) {
    AiNotice.Generic -> generic
    AiNotice.InProgress -> R.string.feature_tailor_impl_ai_busy_body
    AiNotice.RateLimited -> R.string.feature_tailor_impl_ai_rate_limited_body
    AiNotice.QuotaReached -> R.string.feature_tailor_impl_ai_quota_body
    AiNotice.SignInRequired -> R.string.feature_tailor_impl_ai_sign_in_body
}
