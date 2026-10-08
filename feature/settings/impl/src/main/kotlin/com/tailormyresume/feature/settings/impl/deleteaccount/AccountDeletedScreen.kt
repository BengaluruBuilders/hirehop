package com.tailormyresume.feature.settings.impl.deleteaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.settings.impl.R

@Composable
internal fun AccountDeletedScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TmrScreen(
        modifier = modifier,
        sheet = false,
        bottomBar = { DoneBar(onDone = onDone) },
    ) { padding ->
        DoneContent(padding = padding)
    }
}

@Composable
private fun DoneContent(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TmrTheme.spacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.lg, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(CIRCLE_SIZE)
                .background(TmrTheme.colors.metContainer, TmrTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TmrIcons.CheckCircle,
                contentDescription = stringResource(R.string.feature_settings_impl_delete_account_done_illustration),
                tint = TmrTheme.colors.met,
                modifier = Modifier.size(ICON_SIZE),
            )
        }
        TmrHeadline(
            text = stringResource(R.string.feature_settings_impl_delete_account_done_title),
            style = TmrTheme.typography.headlineL,
        )
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_done_body),
            style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            color = TmrTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DoneBar(onDone: () -> Unit) {
    TmrBottomActionBar {
        TmrPrimaryButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_done_action),
            onClick = onDone,
            modifier = Modifier.weight(1f),
        )
    }
}

private val CIRCLE_SIZE = 88.dp
private val ICON_SIZE = 40.dp
