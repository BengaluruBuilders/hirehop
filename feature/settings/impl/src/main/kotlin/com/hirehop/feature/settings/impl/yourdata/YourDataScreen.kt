package com.hirehop.feature.settings.impl.yourdata

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhConfirmDialog
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.settings.impl.R

private val HH_DATA_TOUCH_TARGET: Dp = 48.dp
private val HH_DATA_ITEM_MIN_HEIGHT: Dp = 52.dp
private val HH_DATA_STEP_MIN_HEIGHT: Dp = 36.dp

@Composable
internal fun YourDataScreen(
    uiState: YourDataUiState,
    actions: YourDataActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_settings_impl_your_data_screen_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_settings_impl_your_data_navigation_back_content_description,
                ),
                onNavigationClick = actions.onBack,
            )
        },
        bottomBar = {
            YourDataExportBar(uiState = uiState, actions = actions)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = HhTheme.spacing.d16)
                .padding(bottom = HhTheme.spacing.d24),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            HhOfflineBanner(
                message = stringResource(R.string.feature_settings_impl_your_data_offline_message),
                visible = uiState.isOffline,
            )
            Text(
                text = stringResource(R.string.feature_settings_impl_your_data_headline),
                style = HhTheme.typography.displaySmall,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_settings_impl_your_data_subhead),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            YourDataLedgerCard(uiState = uiState, actions = actions)
        }
    }
    uiState.deleteTarget?.let { target ->
        YourDataDeleteDialog(
            target = target,
            profileFactCount = uiState.profileFactCount,
            onConfirm = actions.onDeleteConfirmed,
            onDismiss = actions.onDeleteDismissed,
        )
    }
}

@Composable
private fun YourDataLedgerCard(
    uiState: YourDataUiState,
    actions: YourDataActions,
) {
    HhCard(contentPadding = PaddingValues(all = 0.dp)) {
        uiState.ledger.forEachIndexed { index, row ->
            if (index > 0) {
                HhDivider()
            }
            YourDataLedgerRowView(row = row, actions = actions)
        }
    }
}

@Composable
private fun YourDataLedgerRowView(
    row: YourDataLedgerRow,
    actions: YourDataActions,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d8),
        ) {
            Text(
                text = stringResource(row.kind.titleResource()),
                style = HhTheme.typography.titleSmall,
                color = HhTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = row.count.toString(),
                style = tabularCountStyle(),
                color = HhTheme.colors.onSurface,
            )
            row.unit.resource()?.let { unit ->
                Text(
                    text = stringResource(unit),
                    style = HhTheme.typography.bodySmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
        row.subTexts().forEach { sub ->
            Text(
                text = sub,
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        if (row.actions.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d16),
            ) {
                row.actions.forEach { action ->
                    Text(
                        text = stringResource(action.resource()),
                        style = HhTheme.typography.titleSmall,
                        color = HhTheme.colors.primary,
                        modifier = Modifier
                            .heightIn(min = HH_DATA_TOUCH_TARGET)
                            .clickable { actions.onLedgerAction(row.kind, action) },
                    )
                }
            }
        }
        row.items.forEach { item ->
            HhDivider()
            YourDataApplicationItem(item = item, actions = actions)
        }
    }
}

@Composable
private fun YourDataApplicationItem(
    item: YourDataLedgerItem,
    actions: YourDataActions,
) {
    val talkBack = stringResource(
        R.string.feature_settings_impl_your_data_application_item_talkback,
        item.title,
        item.company,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HH_DATA_ITEM_MIN_HEIGHT)
            .semantics(mergeDescendants = true) { contentDescription = talkBack },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                R.string.feature_settings_impl_your_data_application_item,
                item.title,
                item.company,
            ),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = HH_DATA_TOUCH_TARGET)
                .padding(end = HhTheme.spacing.d8),
        )
        Text(
            text = stringResource(R.string.feature_settings_impl_your_data_action_delete),
            style = HhTheme.typography.titleSmall,
            color = HhTheme.colors.primary,
            modifier = Modifier
                .heightIn(min = HH_DATA_TOUCH_TARGET)
                .clickable { actions.onDeleteRequested(item.applicationId) }
                .padding(horizontal = HhTheme.spacing.d8),
        )
    }
}

@Composable
private fun YourDataExportBar(
    uiState: YourDataUiState,
    actions: YourDataActions,
) {
    HhBottomActionBar(
        creditDisclosure = {
            YourDataExportDisclosure(uiState = uiState, actions = actions)
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            HhButton(
                onClick = when {
                    uiState.isOffline -> ({})
                    uiState.stage == YourDataStage.READY -> actions.onShare
                    else -> actions.onDownload
                },
                enabled = !uiState.isOffline,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HH_DATA_TOUCH_TARGET),
            ) {
                Text(
                    text = if (uiState.stage == YourDataStage.READY) {
                        stringResource(R.string.feature_settings_impl_your_data_export_share_action)
                    } else {
                        stringResource(R.string.feature_settings_impl_your_data_export_action)
                    },
                )
            }
        }
    }
}

@Composable
private fun YourDataExportDisclosure(
    uiState: YourDataUiState,
    actions: YourDataActions,
) {
    when {
        uiState.isOffline -> ExportNote(
            text = stringResource(R.string.feature_settings_impl_your_data_export_offline_note),
            isEmphasis = true,
        )

        uiState.isExporting -> Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            Text(
                text = stringResource(R.string.feature_settings_impl_your_data_export_preparing_title),
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
            uiState.steps.forEach { step ->
                YourDataExportStepRow(step = step)
            }
            ExportNote(text = stringResource(R.string.feature_settings_impl_your_data_export_preparing_note))
        }

        uiState.stage == YourDataStage.READY -> Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
        ) {
            ExportNote(text = stringResource(R.string.feature_settings_impl_your_data_export_ready_note))
            Text(
                text = uiState.exportFileName.orEmpty(),
                style = HhTheme.typography.monoSmall,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_settings_impl_your_data_export_share_metadata),
                style = HhTheme.typography.labelMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }

        else -> ExportNote(
            text = stringResource(R.string.feature_settings_impl_your_data_export_idle_note),
        )
    }
}

@Composable
private fun YourDataExportStepRow(step: YourDataExportStep) {
    val name = when (step.kind) {
        YourDataExportStepKind.PROFILE_FACTS -> stringResource(
            R.string.feature_settings_impl_your_data_export_step_facts,
        )

        YourDataExportStepKind.APPLICATIONS -> pluralStringResource(
            R.plurals.feature_settings_impl_your_data_export_step_applications,
            step.applicationCount,
            step.applicationCount,
        )

        YourDataExportStepKind.PACKING -> stringResource(
            R.string.feature_settings_impl_your_data_export_step_packing,
        )
    }
    Text(
        text = name,
        style = if (step.isCurrent) {
            HhTheme.typography.titleSmall
        } else {
            HhTheme.typography.bodyMedium
        },
        color = if (step.isCurrent) HhTheme.colors.onSurface else HhTheme.colors.onSurfaceVariant,
        modifier = Modifier.heightIn(min = HH_DATA_STEP_MIN_HEIGHT),
    )
}

@Composable
private fun ExportNote(
    text: String,
    isEmphasis: Boolean = false,
) {
    Text(
        text = text,
        style = if (isEmphasis) {
            HhTheme.typography.titleSmall
        } else {
            HhTheme.typography.bodySmall
        },
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
internal fun YourDataDeleteDialog(
    target: YourDataDeleteTarget,
    profileFactCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val body = stringResource(R.string.feature_settings_impl_your_data_delete_dialog_body)
    val note = pluralStringResource(
        R.plurals.feature_settings_impl_your_data_delete_dialog_note,
        profileFactCount,
        profileFactCount,
    )
    HhConfirmDialog(
        title = stringResource(
            R.string.feature_settings_impl_your_data_delete_dialog_title,
            "${target.title} · ${target.company}",
        ),
        message = "$body $note",
        confirmLabel = stringResource(R.string.feature_settings_impl_your_data_delete_dialog_confirm),
        cancelLabel = stringResource(R.string.feature_settings_impl_your_data_delete_dialog_keep),
        onConfirm = onConfirm,
        onCancel = onDismiss,
        destructive = true,
    )
}

@Composable
private fun YourDataLedgerRow.subTexts(): List<String> = when (kind) {
    YourDataLedgerKind.PROFILE -> listOf(
        stringResource(
            R.string.feature_settings_impl_your_data_profile_sub,
            confirmedFactCount,
            userStatedFactCount,
        ),
    )

    YourDataLedgerKind.APPLICATIONS -> listOf(
        stringResource(R.string.feature_settings_impl_your_data_applications_sub),
    )

    YourDataLedgerKind.PURCHASES -> purchasesSubTexts()

    YourDataLedgerKind.UPLOADED_RESUME -> listOf(
        stringResource(R.string.feature_settings_impl_your_data_uploaded_resume_sub),
    )
}

@Composable
private fun YourDataLedgerRow.purchasesSubTexts(): List<String> {
    val recorded = if (purchaseCount == 0) {
        stringResource(R.string.feature_settings_impl_your_data_purchases_none)
    } else {
        pluralStringResource(
            R.plurals.feature_settings_impl_your_data_purchases_recorded,
            purchaseCount,
            purchaseCount,
        )
    }
    return listOf(
        recorded,
        stringResource(R.string.feature_settings_impl_your_data_purchases_no_payment),
        pluralStringResource(
            R.plurals.feature_settings_impl_your_data_purchased_credits,
            purchasedCreditCount,
            purchasedCreditCount,
        ),
    )
}

@Composable
private fun tabularCountStyle(): TextStyle = HhTheme.typography.displayLarge.copy(
    fontFeatureSettings = TABULAR_FIGURES,
)

private fun YourDataLedgerKind.titleResource(): Int = when (this) {
    YourDataLedgerKind.PROFILE -> R.string.feature_settings_impl_your_data_ledger_profile
    YourDataLedgerKind.APPLICATIONS -> R.string.feature_settings_impl_your_data_ledger_applications
    YourDataLedgerKind.PURCHASES -> R.string.feature_settings_impl_your_data_ledger_purchases
    YourDataLedgerKind.UPLOADED_RESUME -> R.string.feature_settings_impl_your_data_ledger_uploaded_resume
}

private fun YourDataLedgerUnit.resource(): Int? = when (this) {
    YourDataLedgerUnit.FACTS -> R.string.feature_settings_impl_your_data_unit_facts
    YourDataLedgerUnit.FILES -> R.string.feature_settings_impl_your_data_unit_files
    YourDataLedgerUnit.NONE -> null
}

private fun YourDataLedgerAction.resource(): Int = when (this) {
    YourDataLedgerAction.VIEW -> R.string.feature_settings_impl_your_data_action_view
    YourDataLedgerAction.CORRECT -> R.string.feature_settings_impl_your_data_action_correct
}

private const val TABULAR_FIGURES: String = "tnum"
