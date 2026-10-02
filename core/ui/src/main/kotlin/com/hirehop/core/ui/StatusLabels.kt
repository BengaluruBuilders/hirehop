package com.hirehop.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhApplicationStatusKind
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhStatusKind

@Composable
internal fun HhApplicationStatusKind.label(): String = stringResource(
    when (this) {
        HhApplicationStatusKind.Saved -> R.string.core_ui_application_status_saved
        HhApplicationStatusKind.Applied -> R.string.core_ui_application_status_applied
        HhApplicationStatusKind.Interview -> R.string.core_ui_application_status_interview
        HhApplicationStatusKind.Offer -> R.string.core_ui_application_status_offer
        HhApplicationStatusKind.Rejected -> R.string.core_ui_application_status_rejected
        HhApplicationStatusKind.NoResponse -> R.string.core_ui_application_status_no_response
    },
)

@Composable
internal fun HhProvenanceKind.label(): String = stringResource(
    when (this) {
        HhProvenanceKind.Confirmed -> R.string.core_ui_provenance_confirmed
        HhProvenanceKind.UserStated -> R.string.core_ui_provenance_user_stated
        HhProvenanceKind.UserEdited -> R.string.core_ui_provenance_user_edited
        HhProvenanceKind.Scanned -> R.string.core_ui_provenance_scanned
    },
)

@Composable
internal fun HhStatusKind.label(): String = stringResource(
    when (this) {
        HhStatusKind.Met -> R.string.core_ui_requirement_met
        HhStatusKind.Partial -> R.string.core_ui_requirement_partial
        HhStatusKind.Gap -> R.string.core_ui_requirement_gap
    },
)
