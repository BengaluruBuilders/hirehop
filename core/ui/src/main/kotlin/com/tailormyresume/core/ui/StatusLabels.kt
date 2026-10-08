package com.tailormyresume.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tailormyresume.core.designsystem.component.TmrApplicationStatusKind
import com.tailormyresume.core.designsystem.component.TmrProvenanceKind
import com.tailormyresume.core.designsystem.component.TmrStatusKind

@Composable
internal fun TmrApplicationStatusKind.label(): String = stringResource(
    when (this) {
        TmrApplicationStatusKind.Saved -> R.string.core_ui_application_status_saved
        TmrApplicationStatusKind.Applied -> R.string.core_ui_application_status_applied
        TmrApplicationStatusKind.Interview -> R.string.core_ui_application_status_interview
        TmrApplicationStatusKind.Offer -> R.string.core_ui_application_status_offer
        TmrApplicationStatusKind.Rejected -> R.string.core_ui_application_status_rejected
        TmrApplicationStatusKind.NoResponse -> R.string.core_ui_application_status_no_response
    },
)

@Composable
internal fun TmrProvenanceKind.label(): String = stringResource(
    when (this) {
        TmrProvenanceKind.Confirmed -> R.string.core_ui_provenance_confirmed
        TmrProvenanceKind.UserStated -> R.string.core_ui_provenance_user_stated
        TmrProvenanceKind.UserEdited -> R.string.core_ui_provenance_user_edited
        TmrProvenanceKind.Scanned -> R.string.core_ui_provenance_scanned
    },
)

@Composable
internal fun TmrStatusKind.label(): String = stringResource(
    when (this) {
        TmrStatusKind.Met -> R.string.core_ui_requirement_met
        TmrStatusKind.Partial -> R.string.core_ui_requirement_partial
        TmrStatusKind.Gap -> R.string.core_ui_requirement_gap
    },
)
