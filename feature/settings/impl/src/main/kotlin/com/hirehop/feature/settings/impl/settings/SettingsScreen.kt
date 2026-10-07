package com.hirehop.feature.settings.impl.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhConfirmDialog
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.settings.impl.R
import com.hirehop.feature.settings.impl.common.SettingsAddressSlot
import com.hirehop.feature.settings.impl.common.SettingsTopBar
import com.hirehop.feature.settings.impl.common.formatMediumDate

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
    versionName: String? = null,
) {
    val content = uiState as? SettingsUiState.Content
    HhScreen(
        modifier = modifier,
        sheet = false,
        lightTop = true,
        header = { SettingsTopBar(title = stringResource(R.string.feature_settings_impl_settings_title)) },
    ) { padding ->
        if (content != null) {
            SettingsContent(
                content = content,
                actions = actions,
                versionName = versionName,
                padding = padding,
            )
        }
    }
    if (content != null && content.isSignOutConfirmVisible) {
        HhConfirmDialog(
            title = stringResource(R.string.feature_settings_impl_settings_sign_out_title),
            message = content.account?.let {
                stringResource(R.string.feature_settings_impl_settings_sign_out_body, it.email)
            } ?: stringResource(R.string.feature_settings_impl_settings_sign_out_body_no_email),
            confirmLabel = stringResource(R.string.feature_settings_impl_settings_sign_out_confirm),
            cancelLabel = stringResource(R.string.feature_settings_impl_settings_sign_out_cancel),
            onConfirm = actions.onSignOutConfirm,
            onCancel = actions.onSignOutDismiss,
        )
    }
}

@Composable
private fun SettingsContent(
    content: SettingsUiState.Content,
    actions: SettingsActions,
    versionName: String?,
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
            message = stringResource(R.string.feature_settings_impl_settings_offline_message),
            visible = content.isOffline,
        )
        AccountGroup(content = content, actions = actions)
        CreditsAndDataGroup(content = content, actions = actions)
        PrivacyGroup(content = content, actions = actions)
        AboutGroup(versionName = versionName)
        DeleteAccountGroup(content = content, actions = actions)
    }
}

@Composable
private fun AccountGroup(content: SettingsUiState.Content, actions: SettingsActions) {
    SettingsGroup(label = stringResource(R.string.feature_settings_impl_settings_group_account)) {
        val account = content.account
        SettingsRow(
            icon = HhIcons.Profile,
            title = account?.email ?: stringResource(R.string.feature_settings_impl_settings_account_missing),
            summary = account?.let { stringResource(R.string.feature_settings_impl_settings_account_provider) },
        )
        SettingsRow(
            icon = HhIcons.ArrowBack,
            title = stringResource(R.string.feature_settings_impl_settings_row_sign_out),
            onClick = actions.onSignOut,
            enabled = !content.isOffline && account != null,
            showDivider = false,
        )
    }
}

@Composable
private fun CreditsAndDataGroup(content: SettingsUiState.Content, actions: SettingsActions) {
    SettingsGroup(label = stringResource(R.string.feature_settings_impl_settings_group_credits_data)) {
        SettingsRow(
            icon = HhIcons.Award,
            title = stringResource(R.string.feature_settings_impl_settings_row_credits),
            summary = pluralStringResource(
                R.plurals.feature_settings_impl_settings_row_credits_summary,
                content.creditsLeft,
                content.creditsLeft,
            ),
            onClick = actions.onCreditsAndHelp,
            showChevron = true,
        )
        SettingsRow(
            icon = HhIcons.Facts,
            title = stringResource(R.string.feature_settings_impl_settings_row_your_data),
            summary = stringResource(R.string.feature_settings_impl_settings_row_your_data_summary),
            onClick = actions.onYourData,
            showChevron = true,
            showDivider = false,
        )
    }
}

@Composable
private fun PrivacyGroup(content: SettingsUiState.Content, actions: SettingsActions) {
    val pendingLabel = stringResource(R.string.feature_settings_impl_settings_address_pending)
    val consentSummary = content.consentAcceptedAt?.let { acceptedAt ->
        val date = remember(acceptedAt) { acceptedAt.formatMediumDate() }
        stringResource(R.string.feature_settings_impl_settings_row_consent_read_only_dated, date)
    } ?: stringResource(R.string.feature_settings_impl_settings_row_consent_read_only)
    SettingsGroup(label = stringResource(R.string.feature_settings_impl_settings_group_privacy)) {
        SettingsRow(
            icon = HhIcons.Lock,
            title = stringResource(R.string.feature_settings_impl_settings_row_privacy_policy),
        )
        SettingsAddressSlot(
            address = stringResource(R.string.feature_settings_impl_settings_privacy_policy_address),
            pendingLabel = pendingLabel,
            modifier = Modifier.padding(start = SLOT_INDENT, bottom = HhTheme.spacing.md),
        )
        SettingsRow(
            icon = HhIcons.Info,
            title = stringResource(R.string.feature_settings_impl_settings_row_consent_notice),
            summary = consentSummary,
            onClick = actions.onConsentNotice,
            showChevron = true,
        )
        SettingsRow(
            icon = HhIcons.Chat,
            title = stringResource(R.string.feature_settings_impl_settings_row_grievance),
            showDivider = false,
        )
        SettingsAddressSlot(
            address = stringResource(R.string.feature_settings_impl_settings_grievance_address),
            pendingLabel = pendingLabel,
            modifier = Modifier.padding(start = SLOT_INDENT, bottom = HhTheme.spacing.md),
        )
    }
}

@Composable
private fun AboutGroup(versionName: String?) {
    SettingsGroup(label = stringResource(R.string.feature_settings_impl_settings_group_about)) {
        SettingsRow(
            icon = HhIcons.Verified,
            title = stringResource(R.string.feature_settings_impl_settings_about_promise),
        )
        SettingsRow(
            icon = HhIcons.Info,
            title = stringResource(R.string.feature_settings_impl_settings_about_version_row),
            summary = versionName ?: stringResource(R.string.feature_settings_impl_settings_about_version_missing),
            showDivider = false,
        )
    }
}

@Composable
private fun DeleteAccountGroup(content: SettingsUiState.Content, actions: SettingsActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhCard(contentPadding = CARD_PADDING) {
            SettingsRow(
                icon = HhIcons.Delete,
                title = stringResource(R.string.feature_settings_impl_settings_row_delete_account),
                titleColor = HhTheme.colors.error,
                iconTint = HhTheme.colors.error,
                onClick = actions.onDeleteAccount,
                enabled = !content.isOffline,
                showDivider = false,
            )
        }
        Text(
            text = stringResource(R.string.feature_settings_impl_settings_delete_web_lead),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = HhTheme.spacing.xs),
        )
        SettingsAddressSlot(
            address = stringResource(R.string.feature_settings_impl_settings_delete_web_address),
            pendingLabel = stringResource(R.string.feature_settings_impl_settings_address_pending),
        )
    }
}

@Composable
private fun SettingsGroup(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhSectionLabel(text = label, modifier = Modifier.padding(top = HhTheme.spacing.xs))
        HhCard(contentPadding = CARD_PADDING) {
            Column { content() }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    titleColor: Color = HhTheme.colors.onSurface,
    iconTint: Color = HhTheme.colors.onSurfaceVariant,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    showChevron: Boolean = false,
    showDivider: Boolean = true,
) {
    val rowModifier = if (onClick == null) {
        modifier.semantics(mergeDescendants = true) {}
    } else {
        modifier
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    }
    Column(modifier = rowModifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = ROW_MIN_HEIGHT)
                .padding(horizontal = HhTheme.spacing.xs, vertical = HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(ICON_SIZE),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
            ) {
                Text(text = title, style = HhTheme.typography.titleS, color = titleColor)
                if (summary != null) {
                    Text(
                        text = summary,
                        style = HhTheme.typography.bodyS,
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                }
            }
            if (showChevron) {
                Icon(
                    imageVector = HhIcons.ArrowForward,
                    contentDescription = null,
                    tint = HhTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(CHEVRON_SIZE),
                )
            }
        }
        if (showDivider) {
            HhDivider()
        }
    }
}

private val ROW_MIN_HEIGHT = 56.dp
private val ICON_SIZE = 20.dp
private val CHEVRON_SIZE = 18.dp
private val SLOT_INDENT = 36.dp
private val CARD_PADDING = PaddingValues(12.dp)
private const val DISABLED_ALPHA = 0.38f
