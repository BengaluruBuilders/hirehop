package com.tailormyresume.feature.settings.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBackButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.settings.impl.R

@Composable
internal fun SettingsTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    onBack: (() -> Unit)? = null,
    backContentDescription: String = "",
) {
    Row(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .heightIn(min = TOP_BAR_HEIGHT)
            .padding(
                start = if (onBack != null) TmrTheme.spacing.md else TmrTheme.spacing.gutter,
                end = TmrTheme.spacing.md,
            ),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            TmrBackButton(contentDescription = backContentDescription, onClick = onBack)
        }
        if (title != null) {
            Text(text = title, style = TmrTheme.typography.titleL, color = TmrTheme.colors.onSurface)
        }
    }
}

@Composable
internal fun SettingsErrorNotice(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Assertive }
            .background(TmrTheme.colors.errorContainer, TmrTheme.shapes.banner)
            .padding(horizontal = TmrTheme.spacing.lg, vertical = NOTICE_VERTICAL_PADDING),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        Icon(
            imageVector = TmrIcons.Error,
            contentDescription = null,
            tint = TmrTheme.colors.error,
            modifier = Modifier.size(NOTICE_ICON_SIZE),
        )
        Text(
            text = text,
            style = TmrTheme.typography.titleS,
            color = TmrTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun stepStatusWords(stepCount: Int, currentStepIndex: Int): List<String> = List(stepCount) { position ->
    stringResource(
        when {
            position < currentStepIndex -> R.string.feature_settings_impl_step_done
            position == currentStepIndex -> R.string.feature_settings_impl_step_in_progress
            else -> R.string.feature_settings_impl_step_waiting
        },
    )
}

private val TOP_BAR_HEIGHT = 64.dp
private val NOTICE_VERTICAL_PADDING = 14.dp
private val NOTICE_ICON_SIZE = 22.dp
