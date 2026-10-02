package com.hirehop.feature.settings.impl.deleteaccount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.illustration.HhIllustration
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
        header = {
            HhInnerHeader(title = stringResource(R.string.feature_settings_impl_delete_account_done_header))
        },
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
            .padding(horizontal = HhTheme.spacing.gutter),
    ) {
        HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.xl)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.cardPadding),
            ) {
                IllustrationCircle(
                    illustration = HhIllustration.Goodbye,
                    size = DONE_CIRCLE_SIZE,
                    description = stringResource(R.string.feature_settings_impl_delete_account_done_illustration),
                )
                Text(
                    text = stringResource(R.string.feature_settings_impl_delete_account_done_title),
                    style = HhTheme.typography.titleL,
                    color = HhTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.feature_settings_impl_delete_account_done_body),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun DoneBar(onDone: () -> Unit) {
    HhBottomActionBar {
        HhPrimaryButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_done_action),
            onClick = onDone,
            modifier = Modifier.weight(1f),
            trailingIcon = HhIcons.ArrowForward,
        )
    }
}

private val DONE_CIRCLE_SIZE = 150.dp
