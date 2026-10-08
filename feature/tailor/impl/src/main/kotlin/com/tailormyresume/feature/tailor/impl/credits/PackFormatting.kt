package com.tailormyresume.feature.tailor.impl.credits

import com.tailormyresume.core.domain.ApplicationPack
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Currency
import java.util.Locale
import kotlin.time.Instant as KotlinInstant

private const val PAISE_PER_RUPEE = 100L

private const val PURCHASE_DATE_PATTERN = "d MMM yyyy"

internal fun ApplicationPack.formattedPrice(): String {
    val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))
    val currency = runCatching { Currency.getInstance(currencyCode) }.getOrNull()
    if (currency != null) {
        format.currency = currency
    }
    if (priceInPaise % PAISE_PER_RUPEE == 0L) {
        format.minimumFractionDigits = 0
        format.maximumFractionDigits = 0
    }
    return format.format(BigDecimal.valueOf(priceInPaise, 2))
}

internal fun KotlinInstant.formattedDate(zone: ZoneId = ZoneId.systemDefault()): String =
    DateTimeFormatter.ofPattern(PURCHASE_DATE_PATTERN, Locale.ENGLISH)
        .withZone(zone)
        .format(Instant.ofEpochMilli(toEpochMilliseconds()))
