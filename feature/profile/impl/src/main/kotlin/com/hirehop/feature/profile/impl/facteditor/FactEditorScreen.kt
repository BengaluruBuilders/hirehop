package com.hirehop.feature.profile.impl.facteditor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhConfirmDialog
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.fact.FactDraftErrorReason
import com.hirehop.core.domain.fact.FactField
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.ui.FactIdTag
import com.hirehop.core.ui.FactProvenanceChip
import com.hirehop.feature.profile.impl.R

data class FactEditorActions(
    val onTitleChange: (String) -> Unit,
    val onDetailChange: (String) -> Unit,
    val onToolsChange: (String) -> Unit,
    val onStartDateChange: (String) -> Unit,
    val onEndDateChange: (String) -> Unit,
    val onSave: () -> Unit,
    val onCancel: () -> Unit,
    val onRequestDelete: () -> Unit,
    val onConfirmDelete: () -> Unit,
    val onDismissDelete: () -> Unit,
)

@Composable
fun FactEditorRoute(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    viewModel: FactEditorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) {
        FactEditorActions(
            onTitleChange = viewModel::onTitleChange,
            onDetailChange = viewModel::onDetailChange,
            onToolsChange = viewModel::onToolsChange,
            onStartDateChange = viewModel::onStartDateChange,
            onEndDateChange = viewModel::onEndDateChange,
            onSave = viewModel::save,
            onCancel = viewModel::cancel,
            onRequestDelete = viewModel::requestDelete,
            onConfirmDelete = viewModel::confirmDelete,
            onDismissDelete = viewModel::dismissDelete,
        )
    }
    LaunchedEffect(uiState.outcome) {
        if (uiState.outcome != FactEditorOutcome.Editing) onClose()
    }
    FactEditorScreen(uiState = uiState, actions = actions, modifier = modifier)
}

@Composable
fun FactEditorScreen(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = { FactEditorTopBar(uiState = uiState) },
        bottomBar = { FactEditorActionBar(uiState = uiState, actions = actions) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            FactEditorBody(uiState = uiState, actions = actions)
        }
    }
    if (uiState.isDeleteDialogVisible) {
        FactEditorDeleteDialog(uiState = uiState, actions = actions)
    }
}

@Composable
private fun FactEditorTopBar(uiState: FactEditorUiState) {
    HhTopAppBar(
        title = stringResource(
            if (uiState.mode == FactEditorMode.New) {
                R.string.feature_profile_impl_fact_editor_title_new
            } else {
                R.string.feature_profile_impl_fact_editor_title_edit
            },
        ),
    )
}

@Composable
private fun FactEditorBody(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            HhLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_fact_editor_loading))
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        if (uiState.isOffline) {
            HhOfflineBanner(message = stringResource(R.string.feature_profile_impl_fact_editor_offline))
        }
        FactEditorIdentity(uiState = uiState)
        Text(
            text = stringResource(R.string.feature_profile_impl_fact_editor_hint),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (!uiState.isConfirmed) {
            Text(
                text = stringResource(R.string.feature_profile_impl_fact_editor_unconfirmed),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        if (uiState.isSaveFailed) {
            HhErrorCallout(title = stringResource(R.string.feature_profile_impl_fact_editor_message_save_failed))
        }
        HhSectionCard {
            FactEditorFields(uiState = uiState, actions = actions)
        }
        FactEditorPaperLine(uiState = uiState)
        if (uiState.canDelete) {
            FactEditorDeleteAction(onClick = actions.onRequestDelete)
        }
    }
}

@Composable
private fun FactEditorIdentity(uiState: FactEditorUiState) {
    val categoryName = stringResource(uiState.draft.category.nameRes())
    val provenanceName = stringResource(uiState.provenance.nameRes())
    val announcement = stringResource(
        R.string.feature_profile_impl_fact_editor_fact_announcement,
        categoryName,
        uiState.draft.title,
        provenanceName,
        uiState.factId,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = announcement },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        if (uiState.factId.isNotBlank()) {
            FactIdTag(factId = uiState.factId)
        }
        Text(
            text = categoryName,
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        FactProvenanceChip(source = uiState.provenance)
    }
}

@Composable
private fun FactEditorFields(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        HhTextField(
            value = uiState.draft.title,
            onValueChange = actions.onTitleChange,
            label = stringResource(R.string.feature_profile_impl_fact_editor_field_title),
            placeholder = stringResource(R.string.feature_profile_impl_fact_editor_field_title_placeholder),
            errorText = uiState.errorTextFor(FactField.TITLE),
        )
        HhTextField(
            value = uiState.draft.detail,
            onValueChange = actions.onDetailChange,
            label = stringResource(R.string.feature_profile_impl_fact_editor_field_detail),
            placeholder = stringResource(R.string.feature_profile_impl_fact_editor_field_detail_placeholder),
            singleLine = false,
            minLines = 3,
            errorText = uiState.errorTextFor(FactField.DETAIL),
        )
        FactEditorToolsField(uiState = uiState, onToolsChange = actions.onToolsChange)
        HhTextField(
            value = uiState.draft.startDate,
            onValueChange = actions.onStartDateChange,
            label = stringResource(R.string.feature_profile_impl_fact_editor_field_start),
            placeholder = stringResource(R.string.feature_profile_impl_fact_editor_date_placeholder),
            errorText = uiState.errorTextFor(FactField.START_DATE),
        )
        HhTextField(
            value = uiState.draft.endDate,
            onValueChange = actions.onEndDateChange,
            label = stringResource(R.string.feature_profile_impl_fact_editor_field_end),
            placeholder = stringResource(R.string.feature_profile_impl_fact_editor_date_placeholder),
            errorText = uiState.errorTextFor(FactField.END_DATE) ?: uiState.errorTextFor(FactField.START_DATE),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactEditorToolsField(
    uiState: FactEditorUiState,
    onToolsChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhTextField(
            value = uiState.draft.organization,
            onValueChange = onToolsChange,
            label = stringResource(R.string.feature_profile_impl_fact_editor_field_tools),
            placeholder = stringResource(R.string.feature_profile_impl_fact_editor_field_tools_placeholder),
            errorText = uiState.errorTextFor(FactField.ORGANIZATION),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            uiState.toolTokens.forEach { token -> FactEditorToolChip(label = token) }
        }
    }
}

@Composable
private fun FactEditorToolChip(label: String, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(HhTheme.shapes.sm)
    Text(
        text = label,
        style = HhTheme.typography.bodyMedium,
        color = HhTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .background(color = HhTheme.colors.surfaceContainerLow, shape = shape)
            .border(width = 1.dp, color = HhTheme.colors.hairline, shape = shape)
            .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.xs),
    )
}

@Composable
private fun FactEditorPaperLine(uiState: FactEditorUiState) {
    val announcement = if (uiState.hasLiveLine) {
        stringResource(R.string.feature_profile_impl_fact_editor_live_announcement, uiState.liveLine)
    } else {
        ""
    }
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        Text(
            text = stringResource(R.string.feature_profile_impl_fact_editor_live_label).uppercase(),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        val shape = RoundedCornerShape(HhTheme.shapes.md)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = HhTheme.colors.surface, shape = shape)
                .border(width = 1.dp, color = HhTheme.colors.hairline, shape = shape)
                .padding(HhTheme.spacing.md)
                .heightIn(min = 48.dp)
                .semantics(mergeDescendants = true) {
                    if (announcement.isNotBlank()) contentDescription = announcement
                    liveRegion = LiveRegionMode.Polite
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            if (uiState.hasLiveLine) {
                Text(
                    text = uiState.liveLine,
                    style = HhTheme.typography.bodyLarge,
                    color = HhTheme.colors.onSurface,
                )
            } else {
                Text(
                    text = stringResource(R.string.feature_profile_impl_fact_editor_live_empty),
                    style = HhTheme.typography.bodyLarge,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FactEditorDeleteAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.feature_profile_impl_fact_editor_delete),
        style = HhTheme.typography.labelLarge,
        color = HhTheme.colors.error,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = HhTheme.spacing.sm),
    )
}

@Composable
private fun FactEditorActionBar(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    HhBottomActionBar(
        creditDisclosure = { FactEditorActionNote(uiState = uiState) },
    ) {
        HhOutlinedButton(
            onClick = actions.onCancel,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_profile_impl_fact_editor_cancel),
                style = HhTheme.typography.labelLarge,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        HhButton(
            onClick = actions.onSave,
            enabled = uiState.isSaveEnabled,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_profile_impl_fact_editor_save),
                style = HhTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun FactEditorActionNote(uiState: FactEditorUiState) {
    val note = when {
        uiState.isLoading -> null
        uiState.outcome == FactEditorOutcome.Saved && uiState.wasQueued ->
            R.string.feature_profile_impl_fact_editor_message_saved_queued
        uiState.outcome == FactEditorOutcome.Saved ->
            R.string.feature_profile_impl_fact_editor_message_saved
        uiState.outcome == FactEditorOutcome.Deleted ->
            R.string.feature_profile_impl_fact_editor_message_deleted
        uiState.saveBlockReason == FactDraftErrorReason.END_BEFORE_START ->
            R.string.feature_profile_impl_fact_editor_blocked_dates
        uiState.saveBlockReason == FactDraftErrorReason.TOO_LONG ->
            R.string.feature_profile_impl_fact_editor_error_too_long
        uiState.isSaveEnabled -> null
        else -> R.string.feature_profile_impl_fact_editor_blocked_title
    }
    if (note == null) return
    Text(
        text = stringResource(note),
        style = HhTheme.typography.labelMedium,
        color = HhTheme.colors.onSurface,
    )
}

@Composable
private fun FactEditorDeleteDialog(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    HhConfirmDialog(
        title = stringResource(
            R.string.feature_profile_impl_fact_editor_delete_title,
            uiState.factId,
            uiState.draft.title,
        ),
        message = stringResource(R.string.feature_profile_impl_fact_editor_delete_message),
        confirmLabel = stringResource(R.string.feature_profile_impl_fact_editor_delete_confirm),
        cancelLabel = stringResource(R.string.feature_profile_impl_fact_editor_delete_cancel),
        destructive = true,
        onConfirm = actions.onConfirmDelete,
        onCancel = actions.onDismissDelete,
    )
}

@Composable
private fun FactEditorUiState.errorTextFor(field: FactField): String? =
    visibleReasonFor(field)?.let { reason ->
        stringResource(
            when (reason) {
                FactDraftErrorReason.REQUIRED -> R.string.feature_profile_impl_fact_editor_error_required
                FactDraftErrorReason.END_BEFORE_START ->
                    R.string.feature_profile_impl_fact_editor_error_end_before_start
                FactDraftErrorReason.TOO_LONG -> R.string.feature_profile_impl_fact_editor_error_too_long
            },
        )
    }

private fun EntryCategory.nameRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_fact_editor_category_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_fact_editor_category_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_fact_editor_category_project
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_fact_editor_category_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_fact_editor_category_achievement
}

private fun FactSource.nameRes(): Int = when (this) {
    FactSource.IMPORTED -> R.string.feature_profile_impl_fact_editor_provenance_imported
    FactSource.USER_STATED -> R.string.feature_profile_impl_fact_editor_provenance_user_stated
    FactSource.USER_EDITED -> R.string.feature_profile_impl_fact_editor_provenance_user_edited
}
