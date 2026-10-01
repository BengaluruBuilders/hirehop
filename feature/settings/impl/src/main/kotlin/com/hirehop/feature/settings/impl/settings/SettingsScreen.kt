package com.hirehop.feature.settings.impl.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.settings.impl.R

private val HH_SETTINGS_TOUCH_TARGET: Dp = 48.dp
private val HH_SETTINGS_ROW_MIN_HEIGHT: Dp = 56.dp
private val HH_SETTINGS_GROUP_RADIUS: Dp = 12.dp
private val HH_SETTINGS_HAIRLINE: Dp = 1.dp

@Composable
internal fun SettingsScreen(
    uiState: SettingsUiState,
    actions: SettingsActions,
    modifier: Modifier = Modifier,
    versionName: String? = null,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_settings_impl_settings_screen_title),
            )
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
                message = stringResource(R.string.feature_settings_impl_settings_offline_message),
                visible = uiState.isOffline,
            )
            uiState.groups.forEach { group ->
                SettingsGroup(
                    group = group,
                    uiState = uiState,
                    versionName = versionName,
                    onRowClick = actions.onRowClick,
                )
            }
        }
    }
}

@Composable
private fun SettingsGroup(
    group: SettingsGroupState,
    uiState: SettingsUiState,
    versionName: String?,
    onRowClick: (SettingsRowState) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        Text(
            text = stringResource(group.label.labelResource()),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(HH_SETTINGS_GROUP_RADIUS))
                .background(color = HhTheme.colors.surface)
                .border(
                    border = BorderStroke(
                        width = HH_SETTINGS_HAIRLINE,
                        color = HhTheme.colors.hairline,
                    ),
                    shape = RoundedCornerShape(HH_SETTINGS_GROUP_RADIUS),
                ),
        ) {
            group.rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HhDivider()
                }
                SettingsRow(
                    row = row,
                    uiState = uiState,
                    versionName = versionName,
                    onClick = onRowClick,
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    row: SettingsRowState,
    uiState: SettingsUiState,
    versionName: String?,
    onClick: (SettingsRowState) -> Unit,
) {
    val isClickable = row.destination != null && row.isEnabled
    val contentColor = if (row.isEnabled) {
        HhTheme.colors.onSurface
    } else {
        HhTheme.colors.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HH_SETTINGS_ROW_MIN_HEIGHT)
            .clickable(enabled = isClickable) { onClick(row) }
            .padding(
                start = HhTheme.spacing.d16,
                top = HhTheme.spacing.md,
                bottom = HhTheme.spacing.md,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
        ) {
            Text(
                text = stringResource(row.key.titleResource()),
                style = row.style.textStyle(),
                color = contentColor,
            )
            row.supporting?.let { supporting ->
                Text(
                    text = stringResource(supporting.resource()),
                    style = HhTheme.typography.bodySmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
            if (row.showsAccountSignedInAs) {
                Text(
                    text = stringResource(
                        R.string.feature_settings_impl_settings_row_account_signed_in,
                        uiState.accountDisplayName.orEmpty(),
                    ),
                    style = HhTheme.typography.bodySmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
            row.slot?.let { slot ->
                SettingsSlotChip(slot = slot)
            }
        }
        trailingOf(row = row, uiState = uiState, versionName = versionName)?.let { trailing ->
            Text(
                text = trailing,
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
        }
        if (row.showChevron) {
            SettingsChevron()
        }
    }
}

@Composable
private fun SettingsSlotChip(slot: SettingsSlotKind) {
    val text = stringResource(slot.resource())
    Box(
        modifier = Modifier
            .background(color = HhTheme.colors.surfaceVariant, shape = RoundedCornerShape(HhTheme.shapes.xs))
            .border(
                border = BorderStroke(width = HH_SETTINGS_HAIRLINE, color = HhTheme.colors.onSurfaceVariant),
                shape = RoundedCornerShape(HhTheme.shapes.xs),
            ),
    ) {
        Text(
            text = text,
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = HhTheme.spacing.d8,
                vertical = HhTheme.spacing.d2,
            ),
        )
    }
}

@Composable
private fun SettingsChevron() {
    val strokeColor = HhTheme.colors.onSurfaceVariant
    Canvas(modifier = Modifier.size(HH_SETTINGS_TOUCH_TARGET)) {
        val centreX = size.width / 2f
        val centreY = size.height / 2f
        val left = centreX - CHEVRON_HALF_SPAN
        val right = centreX + CHEVRON_HALF_SPAN
        val top = centreY - CHEVRON_HALF_SPAN
        val bottom = centreY + CHEVRON_HALF_SPAN
        drawLine(
            color = strokeColor,
            start = Offset(x = left, y = top),
            end = Offset(x = right, y = centreY),
            strokeWidth = CHEVRON_STROKE_DP.toPx(),
            cap = StrokeCap.Round,
        )
        drawLine(
            color = strokeColor,
            start = Offset(x = right, y = centreY),
            end = Offset(x = left, y = bottom),
            strokeWidth = CHEVRON_STROKE_DP.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun trailingOf(
    row: SettingsRowState,
    uiState: SettingsUiState,
    versionName: String?,
): String? = when {
    row.showsCreditsTrailing -> pluralStringResource(
        R.plurals.feature_settings_impl_settings_credits_left,
        uiState.creditsLeft,
        uiState.creditsLeft,
    )

    row.key == SettingsRowKey.VERSION ->
        versionName
            ?: stringResource(R.string.feature_settings_impl_settings_row_version_missing)

    else -> null
}

@Composable
private fun SettingsRowStyle.textStyle(): TextStyle = when (this) {
    SettingsRowStyle.NORMAL -> HhTheme.typography.titleSmall
    SettingsRowStyle.BIG -> HhTheme.typography.headlineSmall
    SettingsRowStyle.SMALL -> HhTheme.typography.bodyMedium
}

private fun SettingsGroupLabel.labelResource(): Int = when (this) {
    SettingsGroupLabel.ACCOUNT -> R.string.feature_settings_impl_settings_group_account
    SettingsGroupLabel.CREDITS -> R.string.feature_settings_impl_settings_group_credits
    SettingsGroupLabel.YOUR_DATA -> R.string.feature_settings_impl_settings_group_your_data
    SettingsGroupLabel.PRIVACY -> R.string.feature_settings_impl_settings_group_privacy
    SettingsGroupLabel.ABOUT -> R.string.feature_settings_impl_settings_group_about
    SettingsGroupLabel.DELETE_ACCOUNT -> R.string.feature_settings_impl_settings_group_delete_account
}

private fun SettingsRowKey.titleResource(): Int = when (this) {
    SettingsRowKey.ACCOUNT -> R.string.feature_settings_impl_settings_row_account_title
    SettingsRowKey.SIGN_OUT -> R.string.feature_settings_impl_settings_row_sign_out_title
    SettingsRowKey.CREDITS_AND_HELP -> R.string.feature_settings_impl_settings_row_credits_title
    SettingsRowKey.YOUR_DATA -> R.string.feature_settings_impl_settings_row_your_data_title
    SettingsRowKey.PRIVACY_POLICY -> R.string.feature_settings_impl_settings_row_privacy_policy_title
    SettingsRowKey.CONSENT_NOTICE -> R.string.feature_settings_impl_settings_row_consent_notice_title
    SettingsRowKey.GRIEVANCE_CONTACT -> R.string.feature_settings_impl_settings_row_grievance_title
    SettingsRowKey.PROMISE -> R.string.feature_settings_impl_settings_row_promise
    SettingsRowKey.VERSION -> R.string.feature_settings_impl_settings_row_version_title
    SettingsRowKey.DELETE_ACCOUNT -> R.string.feature_settings_impl_settings_row_delete_account_title
    SettingsRowKey.DELETE_ACCOUNT_WEB -> R.string.feature_settings_impl_settings_row_delete_account_web
}

private fun SettingsSupporting.resource(): Int = when (this) {
    SettingsSupporting.NO_ACCOUNT -> R.string.feature_settings_impl_settings_row_account_signed_out
    SettingsSupporting.SIGN_OUT -> R.string.feature_settings_impl_settings_row_sign_out_supporting
    SettingsSupporting.CREDITS_AND_HELP -> R.string.feature_settings_impl_settings_row_credits_supporting
    SettingsSupporting.YOUR_DATA -> R.string.feature_settings_impl_settings_row_your_data_supporting
    SettingsSupporting.PRIVACY_POLICY_OFFLINE -> R.string.feature_settings_impl_settings_row_privacy_policy_offline_supporting
    SettingsSupporting.CONSENT_NOTICE -> R.string.feature_settings_impl_settings_row_consent_notice_supporting
    SettingsSupporting.DELETE_ACCOUNT -> R.string.feature_settings_impl_settings_row_delete_account_supporting
    SettingsSupporting.DELETE_ACCOUNT_OFFLINE -> R.string.feature_settings_impl_settings_row_delete_account_offline_supporting
}

private fun SettingsSlotKind.resource(): Int = when (this) {
    SettingsSlotKind.PRIVACY_POLICY_ADDRESS -> R.string.feature_settings_impl_settings_row_privacy_policy_address_missing
    SettingsSlotKind.GRIEVANCE_CONTACT_ADDRESS -> R.string.feature_settings_impl_settings_row_grievance_address_missing
    SettingsSlotKind.DELETE_ACCOUNT_WEB_ADDRESS -> R.string.feature_settings_impl_settings_row_delete_account_web_missing
}

private val CHEVRON_STROKE_DP: Dp = 1.5.dp
private val CHEVRON_HALF_SPAN: Float = 4f
