package com.hirehop.feature.profile.impl.facteditor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButtonSize
import com.hirehop.core.designsystem.component.HhConfirmDialog
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhExpandable
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.fact.FactDraftErrorReason
import com.hirehop.core.domain.fact.FactField
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.profile.api.navigation.FactEditorNavKey
import com.hirehop.feature.profile.impl.R
import com.hirehop.feature.profile.impl.common.FactStatus
import com.hirehop.feature.profile.impl.common.Note
import com.hirehop.feature.profile.impl.common.NoteTone
import com.hirehop.feature.profile.impl.common.RemovableChip
import com.hirehop.feature.profile.impl.common.ToConfirmChip
import com.hirehop.feature.profile.impl.common.labelRes
import com.hirehop.feature.profile.impl.common.provenanceKind
import com.hirehop.feature.profile.impl.common.status

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

private val CloseSize = 18.dp
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
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(
                    if (uiState.mode == FactEditorMode.New) {
                        R.string.feature_profile_impl_fact_editor_title_new
                    } else {
                        R.string.feature_profile_impl_fact_editor_title_edit
                    },
                ),
                subtitle = stringResource(uiState.draft.category.nameRes()),
                onBack = actions.onCancel,
                backContentDescription = stringResource(R.string.feature_profile_impl_fact_editor_navigate_back),
            )
        },
        bottomBar = { FactEditorActionBar(uiState = uiState, actions = actions) },
        bottomBarNotice = {
            Note(
                text = stringResource(R.string.feature_profile_impl_fact_editor_hint),
                tone = NoteTone.Outlined,
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
            HhLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_fact_editor_loading))
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        if (uiState.isOffline) {
            HhOfflineBanner(message = stringResource(R.string.feature_profile_impl_fact_editor_offline))
        }
        if (uiState.isSaveFailed) {
            HhErrorCallout(title = stringResource(R.string.feature_profile_impl_fact_editor_message_save_failed))
        }
        FactEditorIdentity(uiState = uiState)
        if (!uiState.isConfirmed) {
            Note(text = stringResource(R.string.feature_profile_impl_fact_editor_unconfirmed))
        }
        HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg)) {
            FactEditorFields(uiState = uiState, actions = actions)
        }
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
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.md)) {
        FlowRow(
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = announcement },
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            if (uiState.displayId.isNotBlank()) {
                HhFactId(id = uiState.displayId)
            }
            val kind = status.provenanceKind()
            if (kind != null) {
                HhProvenanceChip(kind = kind, label = statusLabel)
            } else {
                ToConfirmChip(label = statusLabel)
            }
            Text(
                text = if (uiState.mode == FactEditorMode.New) {
                    stringResource(R.string.feature_profile_impl_fact_editor_new_caption, categoryName.lowercase())
                } else {
                    categoryName
                },
                style = HhTheme.typography.bodyS,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FactEditorFields(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    val category = uiState.draft.category
    Column(verticalArrangement = Arrangement.spacedBy(FieldGap)) {
        HhTextField(
            value = uiState.draft.title,
            onValueChange = actions.onTitleChange,
            label = stringResource(category.titleLabelRes()),
            placeholder = stringResource(category.titlePlaceholderRes()),
            errorText = uiState.errorTextFor(FactField.TITLE),
        )
        HhTextField(
            value = uiState.draft.detail,
            onValueChange = actions.onDetailChange,
            label = stringResource(category.detailLabelRes()),
            placeholder = stringResource(R.string.feature_profile_impl_fact_editor_field_detail_placeholder),
            singleLine = false,
            minLines = DETAIL_MIN_LINES,
            errorText = uiState.errorTextFor(FactField.DETAIL),
        )
        if (category == EntryCategory.PROJECT) {
            FactEditorToolsField(uiState = uiState, onToolsChange = actions.onToolsChange)
        } else {
            HhTextField(
                value = uiState.draft.organization,
                onValueChange = actions.onToolsChange,
                label = stringResource(category.organizationLabelRes()),
                errorText = uiState.errorTextFor(FactField.ORGANIZATION),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhTextField(
                value = uiState.draft.startDate,
                onValueChange = actions.onStartDateChange,
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.feature_profile_impl_fact_editor_field_start),
                placeholder = stringResource(R.string.feature_profile_impl_fact_editor_date_placeholder),
                errorText = uiState.errorTextFor(FactField.START_DATE),
            )
            HhTextField(
                value = uiState.draft.endDate,
                onValueChange = actions.onEndDateChange,
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.feature_profile_impl_fact_editor_field_end),
                placeholder = stringResource(R.string.feature_profile_impl_fact_editor_date_placeholder),
                errorText = uiState.errorTextFor(FactField.END_DATE),
            )
        }
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
        Text(
            text = stringResource(R.string.feature_profile_impl_fact_editor_field_tools),
            style = HhTheme.typography.labelL,
            color = HhTheme.colors.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(ToolGap),
            verticalArrangement = Arrangement.spacedBy(ToolGap),
        ) {
            tools.forEach { tool ->
                RemovableChip(label = tool, onRemove = { onToolsChange((tools - tool).joinToString(TOOL_JOINER)) })
            }
            HhOutlineButton(
                label = stringResource(R.string.feature_profile_impl_fact_editor_add_tool),
                onClick = { isAdding = true },
                trailingIcon = HhIcons.Add,
                size = HhButtonSize.Compact,
            )
        }
        HhExpandable(expanded = isAdding) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HhTextField(
                    value = pending,
                    onValueChange = { pending = it },
                    modifier = Modifier.weight(1f),
                    placeholder = stringResource(R.string.feature_profile_impl_fact_editor_field_tools_placeholder),
                )
                HhSecondaryButton(
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
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.cardPadding)) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) {
                if (announcement.isNotBlank()) contentDescription = announcement
                liveRegion = LiveRegionMode.Polite
            },
            verticalArrangement = Arrangement.spacedBy(ToolGap),
        ) {
            Text(
                text = stringResource(R.string.feature_profile_impl_fact_editor_live_label),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
            if (uiState.hasLiveLine) {
                Text(text = uiState.liveLine, style = HhTheme.typography.bodyL, color = HhTheme.colors.onSurface)
            } else {
                Text(
                    text = stringResource(R.string.feature_profile_impl_fact_editor_live_empty),
                    style = HhTheme.typography.bodyL,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun FactEditorDeleteAction(onClick: () -> Unit) {
    val label = stringResource(R.string.feature_profile_impl_fact_editor_delete)
    Row(
        modifier = Modifier
            .heightIn(min = HhTheme.spacing.touch)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = HhIcons.Delete,
            contentDescription = null,
            tint = HhTheme.colors.error,
            modifier = Modifier.size(CloseSize + HhTheme.spacing.xxs),
        )
        Text(text = label, style = HhTheme.typography.labelL, color = HhTheme.colors.error)
    }
}

@Composable
private fun FactEditorActionBar(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    HhBottomActionBar {
        HhOutlineButton(
            label = stringResource(R.string.feature_profile_impl_fact_editor_cancel),
            onClick = actions.onCancel,
            modifier = Modifier.weight(1f),
        )
        HhPrimaryButton(
            label = stringResource(R.string.feature_profile_impl_fact_editor_save),
            onClick = actions.onSave,
            enabled = uiState.isSaveEnabled,
            trailingIcon = HhIcons.Check,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun FactEditorDeleteDialog(
    uiState: FactEditorUiState,
    actions: FactEditorActions,
) {
    HhConfirmDialog(
        title = stringResource(
            R.string.feature_profile_impl_fact_editor_delete_title,
            uiState.displayId,
            uiState.draft.title,
        ),
        message = stringResource(R.string.feature_profile_impl_fact_editor_delete_message),
        confirmLabel = stringResource(R.string.feature_profile_impl_fact_editor_delete_confirm, uiState.displayId),
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

private const val DETAIL_MIN_LINES = 3
private const val TOOL_JOINER = ", "
