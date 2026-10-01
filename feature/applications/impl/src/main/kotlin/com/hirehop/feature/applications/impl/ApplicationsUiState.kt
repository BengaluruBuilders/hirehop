package com.hirehop.feature.applications.impl

import androidx.compose.runtime.Immutable
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import kotlin.time.Instant

enum class ApplicationCreditUnit { Free, Left }

@Immutable
data class ApplicationCreditLine(
    val amount: Int,
    val unit: ApplicationCreditUnit,
)

@Immutable
data class ApplicationListRow(
    val id: String,
    val role: String,
    val company: String,
    val status: ApplicationStatus,
    val coverage: KeywordCoverage,
    val updatedAt: Instant,
    val isSyncPending: Boolean,
)

@Immutable
data class ApplicationStatusSheetState(
    val rowId: String,
    val current: ApplicationStatus,
)

@Immutable
data class ApplicationStatusMessage(
    val status: ApplicationStatus,
    val canUndo: Boolean,
)

sealed interface ApplicationsUiState {
    data object Loading : ApplicationsUiState

    data object Empty : ApplicationsUiState

    data class Applications(
        val rows: List<ApplicationListRow>,
        val isOffline: Boolean,
        val statusSheet: ApplicationStatusSheetState?,
        val message: ApplicationStatusMessage?,
    ) : ApplicationsUiState
}
