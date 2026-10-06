package com.hirehop.feature.settings.impl.yourdata

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.hirehop.core.designsystem.component.HhAccent
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhConfirmDialog
import com.hirehop.core.designsystem.component.HhContentSwitch
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSolidCard
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.settings.impl.R
import com.hirehop.feature.settings.impl.common.SettingsNeutralCallout
import com.hirehop.feature.settings.impl.common.formatMediumDate
import com.hirehop.feature.settings.impl.common.formatPriceInPaise

@Composable
internal fun YourDataScreen(
    uiState: YourDataUiState,
    actions: YourDataActions,
    modifier: Modifier = Modifier,
) {
    val content = uiState as? YourDataUiState.Content
    val isPreparing = content?.export == YourDataExport.PREPARING
    HhScreen(
        modifier = modifier,
        sheet = true,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_settings_impl_your_data_title),
                subtitle = stringResource(R.string.feature_settings_impl_your_data_subtitle),
                onBack = actions.onBack,
                extended = false,
                backContentDescription = stringResource(R.string.feature_settings_impl_your_data_back),
            )
        },
        bottomBar = if (content != null && !isPreparing) {
            { DownloadBar(content = content, onDownload = actions.onDownload) }
        } else {
            null
        },
        bottomBarNotice = if (content != null && !isPreparing) {
            { DownloadNotice(isOffline = content.isOffline) }
        } else {
            null
        },
    ) { padding ->
        HhContentSwitch(targetState = content, contentKey = { it?.export?.equals(YourDataExport.PREPARING) }) { state ->
            when {
                state == null -> Unit
                state.export == YourDataExport.PREPARING -> PreparingContent(content = state, padding = padding)
                else -> LedgerContent(content = state, actions = actions, padding = padding)
            }
        }
    }
    val target = content?.deleteTarget
    if (target != null) {
        HhConfirmDialog(
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
private fun PreparingContent(content: YourDataUiState.Content, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = HhTheme.spacing.gutter),
    ) {
        HhStepProgress(
            stepNames = listOf(
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
                pluralStringResource(
                    R.plurals.feature_settings_impl_your_data_preparing_purchases,
                    content.purchases.size,
                    content.purchases.size,
                ),
            ),
            currentStepIndex = 0,
            ordinalLabel = stringResource(R.string.feature_settings_impl_your_data_preparing_title),
            stepDetails = listOf(
                null,
                stringResource(R.string.feature_settings_impl_your_data_preparing_applications_detail),
                null,
            ),
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
            .padding(horizontal = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_settings_impl_your_data_offline_message),
            visible = content.isOffline,
        )
        if (content.export == YourDataExport.FAILED) {
            SettingsNeutralCallout(
                text = stringResource(R.string.feature_settings_impl_your_data_export_error_title) +
                    " " + stringResource(R.string.feature_settings_impl_your_data_export_error_body),
            )
        }
        Text(
            text = stringResource(R.string.feature_settings_impl_your_data_headline),
            style = HhTheme.typography.titleL,
            color = HhTheme.colors.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            ProfileRow(content = content, actions = actions)
            ApplicationsRow(content = content, actions = actions)
            PurchasesRow(content = content, actions = actions)
            UploadedResumeRow()
        }
    }
}

@Composable
private fun ProfileRow(content: YourDataUiState.Content, actions: YourDataActions) {
    LedgerRow(
        count = content.profileFactCount,
        accent = HhAccent.Jade,
        title = stringResource(R.string.feature_settings_impl_your_data_profile_title),
        summary = listOf(
            pluralStringResource(
                R.plurals.feature_settings_impl_your_data_profile_summary,
                content.confirmedFactCount,
                content.confirmedFactCount,
                content.userStatedFactCount,
            ),
        ),
        actions = {
            HhOutlineButton(
                label = stringResource(R.string.feature_settings_impl_your_data_action_view),
                onClick = actions.onViewProfile,
            )
            HhOutlineButton(
                label = stringResource(R.string.feature_settings_impl_your_data_action_correct),
                onClick = actions.onCorrectProfile,
            )
        },
    )
}

@Composable
private fun ApplicationsRow(content: YourDataUiState.Content, actions: YourDataActions) {
    LedgerRow(
        count = content.applications.size,
        accent = HhAccent.Coral,
        title = stringResource(R.string.feature_settings_impl_your_data_applications_title),
        summary = listOf(stringResource(R.string.feature_settings_impl_your_data_applications_summary)),
        actions = {
            HhOutlineButton(
                label = stringResource(R.string.feature_settings_impl_your_data_action_view),
                onClick = actions.onViewApplications,
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
            .padding(start = HhTheme.spacing.sm)
            .heightIn(min = HhTheme.spacing.touch),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = applicationLabel(application),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        HhTextButton(
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
        count = content.purchases.size,
        accent = HhAccent.Marigold,
        title = stringResource(R.string.feature_settings_impl_your_data_purchases_title),
        summary = if (content.purchases.isEmpty()) {
            listOf(stringResource(R.string.feature_settings_impl_your_data_purchases_none))
        } else {
            content.purchases.map { purchase -> purchaseLine(purchase) }
        },
        actions = {
            HhOutlineButton(
                label = stringResource(R.string.feature_settings_impl_your_data_action_view),
                onClick = actions.onViewPurchases,
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
        count = 0,
        accent = HhAccent.Jade,
        title = stringResource(R.string.feature_settings_impl_your_data_resume_title),
        summary = listOf(stringResource(R.string.feature_settings_impl_your_data_resume_summary)),
        actions = {
            Row(
                modifier = Modifier.heightIn(min = HhTheme.spacing.touch),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                Icon(
                    imageVector = HhIcons.Check,
                    contentDescription = null,
                    tint = HhTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(HhTheme.spacing.lg),
                )
                Text(
                    text = stringResource(R.string.feature_settings_impl_your_data_resume_nothing),
                    style = HhTheme.typography.labelM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LedgerRow(
    count: Int,
    accent: HhAccent,
    title: String,
    summary: List<String>,
    actions: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    extra: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HhSolidCard(
            accent = accent,
            monogram = count.toString(),
            title = title,
            subtitle = summary.joinToString(separator = "\n"),
            decoration = null,
        )
        FlowRow(
            modifier = Modifier.padding(start = HhTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) { actions() }
        extra()
    }
}

@Composable
private fun DownloadNotice(isOffline: Boolean) {
    HhCard(contentPadding = PaddingValues(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md)) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            Icon(
                imageVector = if (isOffline) HhIcons.Offline else HhIcons.Info,
                contentDescription = null,
                tint = HhTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(HhTheme.spacing.xl),
            )
            Text(
                text = stringResource(
                    if (isOffline) {
                        R.string.feature_settings_impl_your_data_export_offline_note
                    } else {
                        R.string.feature_settings_impl_your_data_export_note
                    },
                ),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun DownloadBar(content: YourDataUiState.Content, onDownload: () -> Unit) {
    HhBottomActionBar {
        HhPrimaryButton(
            label = stringResource(R.string.feature_settings_impl_your_data_export_action),
            onClick = onDownload,
            modifier = Modifier.weight(1f),
            enabled = !content.isOffline,
            trailingIcon = HhIcons.Download,
        )
    }
}

private const val LINE_SEPARATOR = " · "
