package com.tailormyresume.feature.profile.impl.facteditor

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrConfirmDialog
import com.tailormyresume.core.designsystem.component.TmrErrorCallout
import com.tailormyresume.core.designsystem.component.TmrExpandable
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrProvenanceChip
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.component.TmrTextField
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.fact.FactDraftErrorReason
import com.tailormyresume.core.domain.fact.FactField
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.feature.profile.api.navigation.FactEditorNavKey
import com.tailormyresume.feature.profile.impl.R
import com.tailormyresume.feature.profile.impl.common.FactStatus
import com.tailormyresume.feature.profile.impl.common.NeutralChip
import com.tailormyresume.feature.profile.impl.common.Note
import com.tailormyresume.feature.profile.impl.common.NoteTone
import com.tailormyresume.feature.profile.impl.common.RemovableChip
import com.tailormyresume.feature.profile.impl.common.ToConfirmChip
import com.tailormyresume.feature.profile.impl.common.labelRes
import com.tailormyresume.feature.profile.impl.common.provenanceKind
import com.tailormyresume.feature.profile.impl.common.status

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
    val onMoreBulletChange: (Int, String) -> Unit = { _, _ -> },
) {
    companion object {
        val None = FactEditorActions(
            onTitleChange = {},
            onDetailChange = {},
            onToolsChange = {},
            onStartDateChange = {},
            onEndDateChange = {},
            onSave = {},
            onCancel = {},
            onRequestDelete = {},
            onConfirmDelete = {},
            onDismissDelete = {},
        )
    }
}

private val DeleteHeight = 56.dp
private val DeleteBorder = 1.5.dp
private val FieldGap = 14.dp
private val ToolGap = 6.dp

@Composable
fun FactEditorRoute(
    key: FactEditorNavKey,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FactEditorViewModel = hiltViewModel<FactEditorViewModel, FactEditorViewModel.Factory>(
        key = key.toString(),
    ) { factory -> factory.create(key) },
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
            onMoreBulletChange = viewModel::onMoreBulletChange,
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
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = stringResource(
                    if (uiState.mode == FactEditorMode.New) {
                        R.string.feature_profile_impl_fact_editor_title_new
                    } else {
                        R.string.feature_profile_impl_fact_editor_title_edit
                    },
                ),
                onBack = actions.onCancel,
                backContentDescription = stringResource(R.string.feature_profile_impl_fact_editor_navigate_back),
            )
        },
        bottomBar = { FactEditorActionBar(uiState = uiState, actions = actions) },
        bottomBarNotice = {
            Note(
                text = stringResource(R.string.feature_profile_impl_fact_editor_hint),
                tone = NoteTone.Plain,
                icon = TmrIcons.Info,
            )
        },
    ) { padding ->
        FactEditorBody(uiState = uiState, actions = actions, padding = padding)
    }
    if (uiState.isDeleteDialogVisible) {
        FactEditorDeleteDialog(uiState = uiState, actions = actions)
    }
}

@Composable
private fun FactEditorBody(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
    padding: PaddingValues,
) {
    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            TmrLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_fact_editor_loading))
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        if (uiState.isOffline) {
            TmrOfflineBanner(message = stringResource(R.string.feature_profile_impl_fact_editor_offline))
        }
        if (uiState.isSaveFailed) {
            TmrErrorCallout(title = stringResource(R.string.feature_profile_impl_fact_editor_message_save_failed))
        }
        FactEditorIdentity(uiState = uiState)
        if (!uiState.isConfirmed) {
            Note(text = stringResource(R.string.feature_profile_impl_fact_editor_unconfirmed))
        }
        FactEditorFields(uiState = uiState, actions = actions)
        FactEditorLiveLine(uiState = uiState)
        if (uiState.canDelete) {
            FactEditorDeleteAction(onClick = actions.onRequestDelete)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactEditorIdentity(uiState: FactEditorUiState) {
    val categoryName = stringResource(uiState.draft.category.nameRes())
    val status = if (uiState.mode == FactEditorMode.New) {
        FactStatus.UserStated
    } else {
        uiState.provenance.status(uiState.isConfirmed)
    }
    val statusLabel = stringResource(status.labelRes())
    val announcement = stringResource(
        R.string.feature_profile_impl_fact_editor_fact_announcement,
        categoryName,
        uiState.draft.title,
        statusLabel,
        uiState.displayId,
    )
    FlowRow(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = announcement },
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        if (uiState.displayId.isNotBlank()) {
            TmrFactId(id = uiState.displayId)
        }
        val kind = status.provenanceKind()
        if (kind != null) {
            TmrProvenanceChip(kind = kind, label = statusLabel)
        } else {
            ToConfirmChip(label = statusLabel)
        }
        NeutralChip(label = categoryName, icon = uiState.draft.category.icon())
    }
}

@Composable
private fun FactEditorFields(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    val category = uiState.draft.category
    Column(verticalArrangement = Arrangement.spacedBy(FieldGap)) {
        TmrTextField(
            value = uiState.draft.title,
            onValueChange = actions.onTitleChange,
            label = stringResource(category.titleLabelRes()),
            placeholder = stringResource(category.titlePlaceholderRes()),
            errorText = uiState.errorTextFor(FactField.TITLE),
        )
        TmrTextField(
            value = uiState.draft.detail,
            onValueChange = actions.onDetailChange,
            label = stringResource(category.detailLabelRes()),
            placeholder = stringResource(R.string.feature_profile_impl_fact_editor_field_detail_placeholder),
            singleLine = false,
            minLines = DETAIL_MIN_LINES,
            errorText = uiState.visibleBulletReason(uiState.draft.detail)?.let { stringResource(it.messageRes()) },
        )
        uiState.draft.moreBullets.forEachIndexed { index, bullet ->
            TmrTextField(
                value = bullet.text,
                onValueChange = { actions.onMoreBulletChange(index, it) },
                label = stringResource(R.string.feature_profile_impl_fact_editor_field_detail_numbered, index + 2),
                singleLine = false,
                minLines = DETAIL_MIN_LINES,
                errorText = uiState.visibleBulletReason(bullet.text)?.let { stringResource(it.messageRes()) },
            )
        }
        if (category == EntryCategory.PROJECT) {
            FactEditorToolsField(uiState = uiState, onToolsChange = actions.onToolsChange)
        } else {
            TmrTextField(
                value = uiState.draft.organization,
                onValueChange = actions.onToolsChange,
                label = stringResource(category.organizationLabelRes()),
                errorText = uiState.errorTextFor(FactField.ORGANIZATION),
            )
        }
        TmrTextField(
            value = uiState.draft.startDate,
            onValueChange = actions.onStartDateChange,
            label = stringResource(R.string.feature_profile_impl_fact_editor_field_start),
            placeholder = stringResource(R.string.feature_profile_impl_fact_editor_date_placeholder),
            errorText = uiState.errorTextFor(FactField.START_DATE),
        )
        TmrTextField(
            value = uiState.draft.endDate,
            onValueChange = actions.onEndDateChange,
            label = stringResource(R.string.feature_profile_impl_fact_editor_field_end),
            placeholder = stringResource(R.string.feature_profile_impl_fact_editor_date_placeholder),
            errorText = uiState.errorTextFor(FactField.END_DATE),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactEditorToolsField(
    uiState: FactEditorUiState,
    onToolsChange: (String) -> Unit,
) {
    var isAdding by rememberSaveable { mutableStateOf(false) }
    var pending by rememberSaveable { mutableStateOf("") }
    val tools = uiState.toolTokens
    Column(verticalArrangement = Arrangement.spacedBy(ToolGap)) {
        TmrSectionLabel(text = stringResource(R.string.feature_profile_impl_fact_editor_field_tools))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(ToolGap),
            verticalArrangement = Arrangement.spacedBy(ToolGap),
        ) {
            tools.forEach { tool ->
                RemovableChip(label = tool, onRemove = { onToolsChange((tools - tool).joinToString(TOOL_JOINER)) })
            }
            TmrOutlineButton(
                label = stringResource(R.string.feature_profile_impl_fact_editor_add_tool),
                onClick = { isAdding = true },
                trailingIcon = TmrIcons.Add,
                size = TmrButtonSize.Compact,
            )
        }
        TmrExpandable(expanded = isAdding) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TmrTextField(
                    value = pending,
                    onValueChange = { pending = it },
                    modifier = Modifier.weight(1f),
                    placeholder = stringResource(R.string.feature_profile_impl_fact_editor_field_tools_placeholder),
                )
                TmrSecondaryButton(
                    label = stringResource(R.string.feature_profile_impl_add),
                    onClick = {
                        val added = pending.trim()
                        if (added.isNotEmpty() && tools.none { it.equals(added, ignoreCase = true) }) {
                            onToolsChange((tools + added).joinToString(TOOL_JOINER))
                        }
                        pending = ""
                        isAdding = false
                    },
                    enabled = pending.isNotBlank(),
                )
            }
        }
    }
}

@Composable
private fun FactEditorLiveLine(uiState: FactEditorUiState) {
    val announcement = if (uiState.hasLiveLine) {
        stringResource(R.string.feature_profile_impl_fact_editor_live_announcement, uiState.liveLine)
    } else {
        ""
    }
    TmrCard {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) {
                if (announcement.isNotBlank()) contentDescription = announcement
                liveRegion = LiveRegionMode.Polite
            },
            verticalArrangement = Arrangement.spacedBy(ToolGap),
        ) {
            Text(
                text = stringResource(R.string.feature_profile_impl_fact_editor_live_label),
                style = TmrTheme.typography.labelM,
                color = TmrTheme.colors.onSurfaceVariant,
            )
            if (uiState.hasLiveLine) {
                Text(text = uiState.liveLine, style = TmrTheme.typography.bodyL, color = TmrTheme.colors.onSurface)
            } else {
                Text(
                    text = stringResource(R.string.feature_profile_impl_fact_editor_live_empty),
                    style = TmrTheme.typography.bodyL,
                    color = TmrTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FactEditorDeleteAction(onClick: () -> Unit) {
    val shape = TmrTheme.shapes.pill
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = DeleteHeight)
            .clip(shape)
            .border(DeleteBorder, TmrTheme.colors.outlineVariant, shape)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = TmrIcons.Delete,
            contentDescription = null,
            tint = TmrTheme.colors.error,
            modifier = Modifier.size(TmrTheme.spacing.xl),
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_fact_editor_delete),
            style = TmrTheme.typography.titleM,
            color = TmrTheme.colors.error,
        )
    }
}

@Composable
private fun FactEditorActionBar(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    TmrBottomActionBar {
        TmrPrimaryButton(
            label = stringResource(R.string.feature_profile_impl_fact_editor_save),
            onClick = actions.onSave,
            enabled = uiState.isSaveEnabled,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FactEditorDeleteDialog(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    TmrConfirmDialog(
        title = stringResource(R.string.feature_profile_impl_fact_editor_delete_title, uiState.displayId),
        message = stringResource(R.string.feature_profile_impl_fact_editor_delete_message),
        confirmLabel = stringResource(R.string.feature_profile_impl_fact_editor_delete_confirm),
        cancelLabel = stringResource(R.string.feature_profile_impl_fact_editor_delete_cancel),
        destructive = true,
        icon = TmrIcons.Error,
        onConfirm = actions.onConfirmDelete,
        onCancel = actions.onDismissDelete,
    )
}

@Composable
private fun FactEditorUiState.errorTextFor(field: FactField): String? =
    visibleReasonFor(field)?.let { stringResource(it.messageRes()) }

@StringRes
private fun FactDraftErrorReason.messageRes(): Int = when (this) {
    FactDraftErrorReason.REQUIRED -> R.string.feature_profile_impl_fact_editor_error_required
    FactDraftErrorReason.END_BEFORE_START -> R.string.feature_profile_impl_fact_editor_error_end_before_start
    FactDraftErrorReason.TOO_LONG -> R.string.feature_profile_impl_fact_editor_error_too_long
    FactDraftErrorReason.INVALID_DATE -> R.string.feature_profile_impl_fact_editor_error_invalid_date
}

private const val DETAIL_MIN_LINES = 3
private const val TOOL_JOINER = ", "
