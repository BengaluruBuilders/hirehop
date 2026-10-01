package com.hirehop.feature.applications.impl

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.RequirementPriority
import kotlin.time.Instant

internal const val MINUTES_PER_HOUR = 60L
internal const val HOURS_PER_DAY = 24L
internal const val DAYS_PER_MONTH = 30L

@StringRes
internal fun ApplicationStatus.labelRes(): Int = when (this) {
    ApplicationStatus.SAVED -> R.string.feature_applications_impl_status_saved
    ApplicationStatus.APPLIED -> R.string.feature_applications_impl_status_applied
    ApplicationStatus.INTERVIEW -> R.string.feature_applications_impl_status_interview
    ApplicationStatus.OFFER -> R.string.feature_applications_impl_status_offer
    ApplicationStatus.REJECTED -> R.string.feature_applications_impl_status_rejected
    ApplicationStatus.NO_RESPONSE -> R.string.feature_applications_impl_status_no_response
}

@Composable
internal fun ApplicationStatus.label(): String = stringResource(labelRes())

@Composable
internal fun RequirementPriority.label(): String = when (this) {
    RequirementPriority.MUST_HAVE -> stringResource(R.string.feature_applications_impl_priority_must_have)
    RequirementPriority.NICE_TO_HAVE -> stringResource(R.string.feature_applications_impl_priority_nice_to_have)
}

@Composable
internal fun coverageShort(coverage: KeywordCoverage): String = stringResource(
    id = R.string.feature_applications_impl_coverage_short,
    coverage.covered,
    coverage.total,
)

@Composable
internal fun coveragePhrase(coverage: KeywordCoverage): String = pluralStringResource(
    id = R.plurals.feature_applications_impl_coverage_phrase,
    coverage.total,
    coverage.covered,
    coverage.total,
)

@Composable
internal fun updatedLabel(updatedAt: Instant, now: Instant): String {
    val minutes = (now - updatedAt).inWholeMinutes
    val hours = minutes / MINUTES_PER_HOUR
    val days = hours / HOURS_PER_DAY
    val months = days / DAYS_PER_MONTH
    return when {
        minutes < MINUTES_PER_HOUR -> {
            val shown = minutes.coerceAtLeast(1L)
            pluralStringResource(
                id = R.plurals.feature_applications_impl_time_minutes,
                shown.toInt(),
                shown.toInt(),
            )
        }
        hours < HOURS_PER_DAY -> pluralStringResource(
            id = R.plurals.feature_applications_impl_time_hours,
            hours.toInt(),
            hours.toInt(),
        )
        months < 1L -> pluralStringResource(
            id = R.plurals.feature_applications_impl_time_days,
            days.toInt(),
            days.toInt(),
        )
        else -> pluralStringResource(
            id = R.plurals.feature_applications_impl_time_months,
            months.toInt(),
            months.toInt(),
        )
    }
}

@Composable
internal fun updatedSentence(updatedAt: Instant, now: Instant): String {
    val minutes = (now - updatedAt).inWholeMinutes
    if (minutes <= 0L) return stringResource(R.string.feature_applications_impl_updated_just_now)
    return stringResource(R.string.feature_applications_impl_updated, updatedLabel(updatedAt = updatedAt, now = now))
}
