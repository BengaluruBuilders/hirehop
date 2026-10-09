package com.tailormyresume.feature.settings.impl.yourdata

import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrConfirmDialog
import com.tailormyresume.core.designsystem.component.TmrContentSwitch
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrOutlinedButton
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrStepProgress
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.settings.impl.R
import com.tailormyresume.feature.settings.impl.common.SettingsErrorNotice
import com.tailormyresume.feature.settings.impl.common.SettingsLoading
import com.tailormyresume.feature.settings.impl.common.SettingsTopBar
import com.tailormyresume.feature.settings.impl.common.formatMediumDate
import com.tailormyresume.feature.settings.impl.common.formatPriceInPaise
import com.tailormyresume.feature.settings.impl.common.stepStatusWords

@Composable
internal fun YourDataScreen(
    uiState: YourDataUiState,
    actions: YourDataActions,
    modifier: Modifier = Modifier,
) {
    val content = uiState as? YourDataUiState.Content
    val isPreparing = content?.export == YourDataExport.PREPARING
    TmrScreen(
        modifier = modifier,
        sheet = false,
        lightTop = true,
        header = {
            SettingsTopBar(
                title = stringResource(R.string.feature_settings_impl_your_data_title),
                onBack = actions.onBack,
                backContentDescription = stringResource(R.string.feature_settings_impl_your_data_back),
            )
        },
        bottomBar = if (content != null && !isPreparing) {
            { DataActionsBar(content = content, actions = actions) }
        } else {
            null
        },
        bottomBarNotice = if (content != null && !isPreparing) {
            { DownloadNotice(isOffline = content.isOffline) }
        } else {
            null
        },
    ) { padding ->
        TmrContentSwitch(targetState = content, contentKey = { it?.export?.equals(YourDataExport.PREPARING) }) { state ->
            when {
                state == null -> SettingsLoading(padding = padding)
                state.export == YourDataExport.PREPARING -> PreparingContent(content = state, padding = padding)
                else -> LedgerContent(content = state, actions = actions, padding = padding)
            }
        }
    }
    if (content?.deletion == YourDataDeletion.CONFIRMING) {
        DeleteMyDataDialog(content = content, actions = actions)
    }
    val target = content?.deleteTarget
    if (target != null) {
        TmrConfirmDialog(
            title = stringResource(
                R.string.feature_settings_impl_your_data_delete_title,
                applicationLabel(target),
            ),
            message = stringResource(R.string.feature_settings_impl_your_data_delete_body),
            confirmLabel = stringResource(R.string.feature_settings_impl_your_data_delete_confirm),
            cancelLabel = stringResource(R.string.feature_settings_impl_your_data_delete_keep),
            onConfirm = actions.onDeleteConfirm,
            onCancel = actions.onDeleteDismiss,
            destructive = true,
        )
    }
}

@Composable
private fun DeleteMyDataDialog(content: YourDataUiState.Content, actions: YourDataActions) {
    TmrConfirmDialog(
        title = stringResource(R.string.feature_settings_impl_your_data_delete_all_title),
        message = stringResource(
            R.string.feature_settings_impl_your_data_delete_all_body,
            pluralStringResource(
                R.plurals.feature_settings_impl_your_data_delete_all_facts,
                content.profileFactCount,
                content.profileFactCount,
            ),
            pluralStringResource(
                R.plurals.feature_settings_impl_your_data_delete_all_applications,
                content.applications.size,
                content.applications.size,
            ),
        ),
        confirmLabel = stringResource(R.string.feature_settings_impl_your_data_delete_all_confirm),
        cancelLabel = stringResource(R.string.feature_settings_impl_your_data_delete_all_cancel),
        onConfirm = actions.onDeleteMyDataConfirm,
        onCancel = actions.onDeleteMyDataDismiss,
        destructive = true,
    )
}

@Composable
private fun PreparingContent(content: YourDataUiState.Content, padding: PaddingValues) {
    val stepNames = listOf(
        pluralStringResource(
            R.plurals.feature_settings_impl_your_data_preparing_facts,
            content.profileFactCount,
            content.profileFactCount,
        ),
        pluralStringResource(
            R.plurals.feature_settings_impl_your_data_preparing_applications,
            content.applications.size,
            content.applications.size,
        ),
        if (content.purchasesKnown) {
            pluralStringResource(
                R.plurals.feature_settings_impl_your_data_preparing_purchases,
                content.purchases.size,
                content.purchases.size,
            )
        } else {
            stringResource(R.string.feature_settings_impl_your_data_preparing_purchases_unknown)
        },
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        TmrHeadline(
            text = stringResource(R.string.feature_settings_impl_your_data_preparing_title),
            style = TmrTheme.typography.headlineL,
        )
        TmrStepProgress(
            stepNames = stepNames,
            currentStepIndex = 0,
            ordinalLabel = "",
            stepStatuses = stepStatusWords(stepNames.size, 0),
            footnote = stringResource(R.string.feature_settings_impl_your_data_preparing_note),
        )
    }
}

@Composable
private fun LedgerContent(
    content: YourDataUiState.Content,
    actions: YourDataActions,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        TmrOfflineBanner(
            message = stringResource(R.string.feature_settings_impl_your_data_offline_message),
            visible = content.isOffline,
        )
        if (content.export == YourDataExport.FAILED) {
            SettingsErrorNotice(
                text = stringResource(R.string.feature_settings_impl_your_data_export_error_title) +
                    " " + stringResource(R.string.feature_settings_impl_your_data_export_error_body),
            )
        }
        if (content.deletion == YourDataDeletion.FAILED) {
            SettingsErrorNotice(text = stringResource(R.string.feature_settings_impl_your_data_delete_all_error))
        }
        TmrHeadline(
            text = stringResource(R.string.feature_settings_impl_your_data_headline),
            style = TmrTheme.typography.headlineL,
        )
        ProfileRow(content = content, actions = actions)
        ApplicationsRow(content = content, actions = actions)
        PurchasesRow(content = content, actions = actions)
        UploadedResumeRow()
        NoticeLine(icon = TmrIcons.Info, text = stringResource(R.string.feature_settings_impl_your_data_delete_all_note))
    }
}

@Composable
private fun ProfileRow(content: YourDataUiState.Content, actions: YourDataActions) {
    LedgerRow(
        icon = TmrIcons.Facts,
        title = stringResource(R.string.feature_settings_impl_your_data_profile_title),
        summary = pluralStringResource(
            R.plurals.feature_settings_impl_your_data_profile_summary,
            content.confirmedFactCount,
            content.confirmedFactCount,
            content.userStatedFactCount,
        ),
        trailing = { LedgerCount(content.profileFactCount) },
        actions = {
            TmrOutlineButton(
                label = stringResource(R.string.feature_settings_impl_your_data_action_view),
                onClick = actions.onViewProfile,
                leadingIcon = TmrIcons.Search,
                size = TmrButtonSize.Compact,
            )
            TmrOutlineButton(
                label = stringResource(R.string.feature_settings_impl_your_data_action_correct),
                onClick = actions.onCorrectProfile,
                leadingIcon = TmrIcons.Edit,
                size = TmrButtonSize.Compact,
            )
        },
    )
}

@Composable
private fun ApplicationsRow(content: YourDataUiState.Content, actions: YourDataActions) {
    LedgerRow(
        icon = TmrIcons.Applications,
        title = stringResource(R.string.feature_settings_impl_your_data_applications_title),
        summary = stringResource(R.string.feature_settings_impl_your_data_applications_summary),
        trailing = { LedgerCount(content.applications.size) },
        actions = {
            TmrOutlineButton(
                label = stringResource(R.string.feature_settings_impl_your_data_action_view),
                onClick = actions.onViewApplications,
                leadingIcon = TmrIcons.Search,
                size = TmrButtonSize.Compact,
            )
        },
        extra = {
            content.applications.forEach { application ->
                ApplicationItem(
                    application = application,
                    canDelete = !content.isOffline,
                    onDelete = { actions.onDeleteRequest(application.id) },
                )
            }
        },
    )
}

@Composable
private fun ApplicationItem(
    application: YourDataApplication,
    canDelete: Boolean,
    onDelete: () -> Unit,
) {
    val description = stringResource(
        R.string.feature_settings_impl_your_data_application_delete_description,
        application.titleOrFallback(),
        application.companyOrFallback(),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = TmrTheme.spacing.sm)
            .heightIn(min = TmrTheme.spacing.touch),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        Text(
            text = applicationLabel(application),
            style = TmrTheme.typography.labelM,
            color = TmrTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        TmrTextButton(
            label = stringResource(R.string.feature_settings_impl_your_data_action_delete),
            onClick = onDelete,
            modifier = Modifier.semantics { contentDescription = description },
            enabled = canDelete,
        )
    }
}

@Composable
private fun YourDataApplication.titleOrFallback(): String =
    title.ifBlank { stringResource(R.string.feature_settings_impl_your_data_role_not_set) }

@Composable
private fun YourDataApplication.companyOrFallback(): String =
    company.ifBlank { stringResource(R.string.feature_settings_impl_your_data_company_not_set) }

@Composable
private fun applicationLabel(application: YourDataApplication): String = stringResource(
    R.string.feature_settings_impl_your_data_application_item,
    application.titleOrFallback(),
    application.companyOrFallback(),
)

@Composable
private fun PurchasesRow(content: YourDataUiState.Content, actions: YourDataActions) {
    LedgerRow(
        icon = TmrIcons.Award,
        title = stringResource(R.string.feature_settings_impl_your_data_purchases_title),
        summary = if (!content.purchasesKnown) {
            stringResource(R.string.feature_settings_impl_your_data_purchases_unavailable)
        } else if (content.purchases.isEmpty()) {
            stringResource(R.string.feature_settings_impl_your_data_purchases_none)
        } else {
            content.purchases.map { purchase -> purchaseLine(purchase) }.joinToString(separator = "\n")
        },
        trailing = { if (content.purchasesKnown) LedgerCount(content.purchases.size) },
        actions = {
            TmrOutlineButton(
                label = stringResource(R.string.feature_settings_impl_your_data_action_view),
                onClick = actions.onViewPurchases,
                leadingIcon = TmrIcons.Search,
                size = TmrButtonSize.Compact,
            )
        },
    )
}

@Composable
private fun purchaseLine(purchase: YourDataPurchase): String {
    val credits = purchase.credits
    val pack = if (credits == null) {
        stringResource(R.string.feature_settings_impl_your_data_purchase_unknown_pack)
    } else {
        pluralStringResource(R.plurals.feature_settings_impl_your_data_purchase_credits, credits, credits)
    }
    val price = if (purchase.priceInPaise != null && purchase.currencyCode != null) {
        formatPriceInPaise(purchase.priceInPaise, purchase.currencyCode)
    } else {
        null
    }
    val date = remember(purchase.purchasedAt) { purchase.purchasedAt.formatMediumDate() }
    val pending = if (purchase.isPending) {
        stringResource(R.string.feature_settings_impl_your_data_purchase_pending)
    } else {
        null
    }
    return listOfNotNull(pack, price, date, pending).joinToString(separator = LINE_SEPARATOR)
}

@Composable
private fun UploadedResumeRow() {
    LedgerRow(
        icon = TmrIcons.Description,
        title = stringResource(R.string.feature_settings_impl_your_data_resume_title),
        summary = stringResource(R.string.feature_settings_impl_your_data_resume_nothing),
        trailing = { DeletedAfterReadingChip() },
    )
}

@Composable
private fun DeletedAfterReadingChip() {
    Row(
        modifier = Modifier
            .heightIn(min = CHIP_HEIGHT)
            .background(TmrTheme.colors.primaryContainer, TmrTheme.shapes.pill)
            .padding(horizontal = TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
    ) {
        Icon(
            imageVector = TmrIcons.Check,
            contentDescription = null,
            tint = TmrTheme.colors.primary,
            modifier = Modifier.size(CHIP_ICON_SIZE),
        )
        Text(
            text = stringResource(R.string.feature_settings_impl_your_data_resume_summary),
            style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = TmrTheme.colors.primary,
        )
    }
}

@Composable
private fun LedgerCount(count: Int) {
    Text(text = count.toString(), style = TmrTheme.typography.headlineL, color = TmrTheme.colors.onSurface)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LedgerRow(
    icon: ImageVector,
    title: String,
    summary: String,
    trailing: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    actions: (@Composable () -> Unit)? = null,
    extra: @Composable () -> Unit = {},
) {
    TmrCard(modifier = modifier, contentPadding = PaddingValues(TmrTheme.spacing.lg)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(TILE_SIZE)
                    .background(TmrTheme.colors.primaryContainer, TmrTheme.shapes.tag),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TmrTheme.colors.onSurface,
                    modifier = Modifier.size(TILE_ICON_SIZE),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs)) {
                Text(text = title, style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
                Text(text = summary, style = TmrTheme.typography.bodyS, color = TmrTheme.colors.onSurfaceVariant)
            }
            trailing()
        }
        if (actions != null) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) { actions() }
        }
        extra()
    }
}

@Composable
private fun DownloadNotice(isOffline: Boolean) {
    Column(
        modifier = Modifier.padding(horizontal = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
    ) {
        NoticeLine(
            icon = if (isOffline) TmrIcons.Offline else TmrIcons.Info,
            text = stringResource(
                if (isOffline) {
                    R.string.feature_settings_impl_your_data_export_offline_note
                } else {
                    R.string.feature_settings_impl_your_data_export_note
                },
            ),
        )
    }
}

@Composable
private fun NoticeLine(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TmrTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(NOTE_ICON_SIZE),
        )
        Text(text = text, style = TmrTheme.typography.bodyS, color = TmrTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun DataActionsBar(content: YourDataUiState.Content, actions: YourDataActions) {
    val busy = content.deletion == YourDataDeletion.DELETING
    TmrBottomActionBar(stacked = true, primaryLast = false) {
        TmrPrimaryButton(
            label = stringResource(R.string.feature_settings_impl_your_data_export_action),
            onClick = actions.onDownload,
            modifier = Modifier.fillMaxWidth(),
            enabled = !content.isOffline && !busy,
            leadingIcon = TmrIcons.Download,
        )
        val canDelete = !content.isOffline && !busy
        if (canDelete) {
            TmrOutlinedButton(onClick = actions.onDeleteMyData, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = TmrIcons.Delete,
                    contentDescription = null,
                    tint = TmrTheme.colors.error,
                    modifier = Modifier.size(BUTTON_ICON_SIZE),
                )
                Text(
                    text = stringResource(R.string.feature_settings_impl_your_data_delete_all_action),
                    color = TmrTheme.colors.error,
                )
            }
        } else {
            TmrSecondaryButton(
                label = stringResource(R.string.feature_settings_impl_your_data_delete_all_action),
                onClick = actions.onDeleteMyData,
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                leadingIcon = TmrIcons.Delete,
            )
        }
    }
}

private val TILE_SIZE = 44.dp
private val TILE_ICON_SIZE = 22.dp
private val CHIP_HEIGHT = 26.dp
private val CHIP_ICON_SIZE = 16.dp
private val NOTE_ICON_SIZE = 18.dp
private val BUTTON_ICON_SIZE = 20.dp
private const val LINE_SEPARATOR = ", "
