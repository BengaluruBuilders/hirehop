package com.hirehop.feature.applications.impl

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

internal const val MINUTES_PER_HOUR = 60L
internal const val HOURS_PER_DAY = 24L
internal const val DAYS_PER_WEEK = 7L
private const val DAY_MONTH_PATTERN = "d MMM"

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
internal fun roleOrFallback(role: String): String =
    role.ifBlank { stringResource(R.string.feature_applications_impl_role_not_set) }

@Composable
internal fun companyOrFallback(company: String): String =
    company.ifBlank { stringResource(R.string.feature_applications_impl_company_not_set) }

@Composable
internal fun monogramOf(company: String): String =
    company.ifBlank { stringResource(R.string.feature_applications_impl_monogram_not_set) }

@Composable
internal fun coverageFraction(coverage: KeywordCoverage): String = stringResource(
    id = R.string.feature_applications_impl_coverage_fraction,
    coverage.covered,
    coverage.total,
)

@Composable
internal fun coverageLine(coverage: KeywordCoverage): String = stringResource(
    R.string.feature_applications_impl_coverage_key_terms,
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
internal fun dayMonthLabel(instant: Instant): String {
    val locale = Locale.getDefault()
    return DateTimeFormatter.ofPattern(DAY_MONTH_PATTERN, locale)
        .withZone(ZoneId.systemDefault())
        .format(instant.toJavaInstant())
}

@Composable
internal fun updatedLabel(updatedAt: Instant, now: Instant): String {
    val minutes = (now - updatedAt).inWholeMinutes
    val hours = minutes / MINUTES_PER_HOUR
    val days = hours / HOURS_PER_DAY
    return when {
        minutes < MINUTES_PER_HOUR -> {
            val shown = minutes.coerceAtLeast(1L).toInt()
            pluralStringResource(id = R.plurals.feature_applications_impl_time_minutes, shown, shown)
        }
        hours < HOURS_PER_DAY -> pluralStringResource(
            id = R.plurals.feature_applications_impl_time_hours,
            hours.toInt(),
            hours.toInt(),
        )
        days < DAYS_PER_WEEK -> pluralStringResource(
            id = R.plurals.feature_applications_impl_time_days,
            days.toInt(),
            days.toInt(),
        )
        else -> dayMonthLabel(updatedAt)
    }
}

@Composable
internal fun updatedSentence(updatedAt: Instant, now: Instant): String {
    val minutes = (now - updatedAt).inWholeMinutes
    if (minutes <= 0L) return stringResource(R.string.feature_applications_impl_updated_just_now)
    return stringResource(R.string.feature_applications_impl_updated, updatedLabel(updatedAt = updatedAt, now = now))
}
