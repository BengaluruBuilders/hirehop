package com.hirehop.feature.settings.impl.common

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
import com.hirehop.core.designsystem.component.HhBackButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.settings.impl.R

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
                start = if (onBack != null) HhTheme.spacing.md else HhTheme.spacing.gutter,
                end = HhTheme.spacing.md,
            ),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            HhBackButton(contentDescription = backContentDescription, onClick = onBack)
        }
        if (title != null) {
            Text(text = title, style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
        }
    }
}

@Composable
internal fun SettingsErrorNotice(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Assertive }
            .background(HhTheme.colors.errorContainer, HhTheme.shapes.banner)
            .padding(horizontal = HhTheme.spacing.lg, vertical = NOTICE_VERTICAL_PADDING),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Icon(
            imageVector = HhIcons.Error,
            contentDescription = null,
            tint = HhTheme.colors.error,
            modifier = Modifier.size(NOTICE_ICON_SIZE),
        )
        Text(
            text = text,
            style = HhTheme.typography.titleS,
            color = HhTheme.colors.onSurface,
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
