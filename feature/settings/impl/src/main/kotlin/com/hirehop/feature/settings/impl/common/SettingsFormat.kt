package com.hirehop.feature.settings.impl.common

import java.math.BigDecimal
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import kotlin.time.Instant
import kotlin.time.toJavaInstant

internal fun Instant.formatMediumDate(): String =
    DateTimeFormatter
        .ofPattern(DATE_PATTERN, Locale.forLanguageTag(INDIA_LANGUAGE_TAG))
        .withZone(ZoneId.systemDefault())
        .format(toJavaInstant())

internal fun formatPriceInPaise(priceInPaise: Long, currencyCode: String): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag(INDIA_LANGUAGE_TAG))
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
    if (currency != null) {
        format.currency = currency
    }
    if (priceInPaise % PAISE_PER_RUPEE == 0L) {
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
    }
    return format.format(BigDecimal.valueOf(priceInPaise, PAISE_SCALE))
}

private const val DATE_PATTERN = "d MMM yyyy"
private const val INDIA_LANGUAGE_TAG = "en-IN"
private const val PAISE_PER_RUPEE = 100L
private const val PAISE_SCALE = 2
