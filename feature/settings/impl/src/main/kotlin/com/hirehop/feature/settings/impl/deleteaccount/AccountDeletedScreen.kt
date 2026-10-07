package com.hirehop.feature.settings.impl.deleteaccount

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
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.settings.impl.R

@Composable
internal fun AccountDeletedScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhScreen(
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
            .padding(horizontal = HhTheme.spacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier
                .size(CIRCLE_SIZE)
                .background(HhTheme.colors.metContainer, HhTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = HhIcons.CheckCircle,
                contentDescription = stringResource(R.string.feature_settings_impl_delete_account_done_illustration),
                tint = HhTheme.colors.met,
                modifier = Modifier.size(ICON_SIZE),
            )
        }
        HhHeadline(
            text = stringResource(R.string.feature_settings_impl_delete_account_done_title),
            style = HhTheme.typography.headlineL,
        )
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_done_body),
            style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DoneBar(onDone: () -> Unit) {
    HhBottomActionBar {
        HhPrimaryButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_done_action),
            onClick = onDone,
            modifier = Modifier.weight(1f),
        )
    }
}

private val CIRCLE_SIZE = 88.dp
private val ICON_SIZE = 40.dp
