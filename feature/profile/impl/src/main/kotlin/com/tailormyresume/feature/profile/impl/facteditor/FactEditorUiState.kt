package com.tailormyresume.feature.profile.impl.facteditor

import com.tailormyresume.core.domain.fact.FactDraft
import com.tailormyresume.core.domain.fact.FactDraftErrorReason
import com.tailormyresume.core.domain.fact.FactField
import com.tailormyresume.core.domain.fact.FactLineRenderer
import com.tailormyresume.core.model.FactSource

enum class FactEditorMode { New, Editing }

enum class FactEditorOutcome { Editing, Saved, Deleted, Cancelled }

const val FACT_TOOL_SEPARATOR: String = ","

data class FactEditorUiState(
    val factId: String,
    val mode: FactEditorMode,
    val outcome: FactEditorOutcome,
    val draft: FactDraft,
    val fieldErrors: Map<FactField, FactDraftErrorReason>,
    val touchedFields: Set<FactField>,
    val isDeleteDialogVisible: Boolean,
    val isOffline: Boolean,
    val isLoading: Boolean,
    val isSaving: Boolean,
    val isSaveFailed: Boolean,
    val wasQueued: Boolean,
    val provenance: FactSource,
    val isConfirmed: Boolean,
    val displayId: String = factId,
) {
    val liveLine: String
        get() = FactLineRenderer.render(draft)

    val hasLiveLine: Boolean
        get() = liveLine.isNotBlank()

    val toolTokens: List<String>
        get() = draft.organization.split(FACT_TOOL_SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    val isSaveEnabled: Boolean
        get() = !isLoading && !isSaving && fieldErrors.isEmpty()

    val canDelete: Boolean
        get() = mode == FactEditorMode.Editing && !isSaving

    val saveBlockReason: FactDraftErrorReason?
        get() = when {
            fieldErrors.isNotEmpty() -> fieldErrors.values.firstOrNull()
            draft.title.isBlank() -> FactDraftErrorReason.REQUIRED
            else -> null
        }

    fun visibleReasonFor(field: FactField): FactDraftErrorReason? =
        fieldErrors[field]?.takeIf { field in touchedFields }
}
