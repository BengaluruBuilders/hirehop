package com.hirehop.feature.applications.impl

import androidx.compose.runtime.Immutable
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.KeywordCoverage
import kotlin.time.Instant

@Immutable
data class ApplicationsHeader(
    val firstName: String? = null,
    val credits: Int? = null,
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
    val isExported: Boolean = false,
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
    val header: ApplicationsHeader

    data class Loading(override val header: ApplicationsHeader = ApplicationsHeader()) : ApplicationsUiState

    data class Empty(override val header: ApplicationsHeader) : ApplicationsUiState

    data class Applications(
        override val header: ApplicationsHeader,
        val rows: List<ApplicationListRow>,
        val isOffline: Boolean,
        val statusSheet: ApplicationStatusSheetState?,
        val message: ApplicationStatusMessage?,
    ) : ApplicationsUiState
}
