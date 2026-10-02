package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.profile.impl.common.SpotCircle

private val SpotSize = 140.dp
private val TextGap = 6.dp

@Composable
internal fun ProfileEmptyScreen(
    state: ProfileUiState.Empty,
    navigation: ProfileNavigation,
    modifier: Modifier = Modifier,
) {
    ProfileFrame(
        header = ProfileHeaderState(
            headerLine = state.headerLine,
            factCount = 0,
            confirmedCount = 0,
            userStatedCount = 0,
            toConfirmCount = 0,
        ),
        onAddEvidence = navigation.onAddEvidence,
        modifier = modifier,
    ) {
        emptyContent(navigation)
    }
}

private fun LazyListScope.emptyContent(navigation: ProfileNavigation) {
    item(key = "empty") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
        ) {
            SpotCircle(
                kind = HhSpotKind.Empty,
                size = SpotSize,
                contentDescription = stringResource(R.string.feature_profile_impl_empty_illustration),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(TextGap),
            ) {
                Text(
                    text = stringResource(R.string.feature_profile_impl_empty_headline),
                    style = HhTheme.typography.titleL,
                    color = HhTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.feature_profile_impl_empty_body),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                HhPrimaryButton(
                    label = stringResource(R.string.feature_profile_impl_import_resume),
                    onClick = navigation.onImportResume,
                    modifier = Modifier.weight(1f),
                )
                HhOutlineButton(
                    label = stringResource(R.string.feature_profile_impl_build_step_by_step),
                    onClick = navigation.onBuildStepByStep,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
