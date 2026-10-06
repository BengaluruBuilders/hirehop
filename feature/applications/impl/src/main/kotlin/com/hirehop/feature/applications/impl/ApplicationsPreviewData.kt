package com.hirehop.feature.applications.impl

import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

internal val PREVIEW_INSTANT: Instant = Instant.fromEpochSeconds(1_774_368_000L)

internal const val NORTHWIND_ROLE = "Associate Analyst"
internal const val NORTHWIND_COMPANY = "Northwind GCC"
internal const val PAISA_ROLE = "Data Analyst"
internal const val PAISA_COMPANY = "Paisa Ledger (start-up)"
internal const val SAHYADRI_ROLE = "Operations Analyst"
internal const val SAHYADRI_COMPANY = "Sahyadri Motors"
internal const val MERIDIAN_ROLE = "Business Analyst"
internal const val MERIDIAN_COMPANY = "Meridian GCC"

internal fun previewNorthwindRow(
    isSyncPending: Boolean = false,
    updatedAt: Instant = PREVIEW_INSTANT - 2.hours,
) = ApplicationListRow(
    id = "application-northwind-1",
    role = NORTHWIND_ROLE,
    company = NORTHWIND_COMPANY,
    status = ApplicationStatus.APPLIED,
    coverage = KeywordCoverage(covered = 9, total = 14),
    updatedAt = updatedAt,
    isSyncPending = isSyncPending,
    isExported = true,
)

internal fun previewPaisaRow() = ApplicationListRow(
    id = "application-paisa-2",
    role = PAISA_ROLE,
    company = PAISA_COMPANY,
    status = ApplicationStatus.INTERVIEW,
    coverage = KeywordCoverage(covered = 11, total = 13),
    updatedAt = PREVIEW_INSTANT - 1.days,
    isSyncPending = false,
    isExported = true,
)

internal fun previewSahyadriRow() = ApplicationListRow(
    id = "application-sahyadri-3",
    role = SAHYADRI_ROLE,
    company = SAHYADRI_COMPANY,
    status = ApplicationStatus.SAVED,
    coverage = KeywordCoverage(covered = 6, total = 12),
    updatedAt = PREVIEW_INSTANT - 3.days,
    isSyncPending = false,
)

internal fun previewMeridianRow() = ApplicationListRow(
    id = "application-meridian-4",
    role = MERIDIAN_ROLE,
    company = MERIDIAN_COMPANY,
    status = ApplicationStatus.NO_RESPONSE,
    coverage = KeywordCoverage(covered = 8, total = 15),
    updatedAt = PREVIEW_INSTANT - 12.days,
    isSyncPending = false,
    isExported = true,
)

internal fun previewListRows() = listOf(
    previewNorthwindRow(),
    previewPaisaRow(),
    previewSahyadriRow(),
    previewMeridianRow(),
)

internal val PREVIEW_HEADER = ApplicationsHeader(firstName = "Priya", credits = 4)

internal fun previewListState(
    isOffline: Boolean = false,
    statusSheet: ApplicationStatusSheetState? = null,
    message: ApplicationStatusMessage? = null,
) = ApplicationsUiState.Applications(
    header = PREVIEW_HEADER,
    rows = previewListRows(),
    isOffline = isOffline,
    statusSheet = statusSheet,
    message = message,
)
