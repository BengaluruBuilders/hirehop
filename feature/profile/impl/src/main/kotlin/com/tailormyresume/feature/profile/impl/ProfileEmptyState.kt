package com.tailormyresume.feature.profile.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

private val IconDiscSize = 88.dp

@Composable
internal fun ProfileEmptyScreen(
    navigation: ProfileNavigation,
    modifier: Modifier = Modifier,
) {
    ProfileFrame(modifier = modifier) {
        emptyContent(navigation)
    }
}

private fun LazyListScope.emptyContent(navigation: ProfileNavigation) {
    item(key = "empty") {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.lg),
        ) {
            Box(
                modifier = Modifier.size(IconDiscSize).background(TmrTheme.colors.card, TmrTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = TmrIcons.Profile,
                    contentDescription = stringResource(R.string.feature_profile_impl_empty_illustration),
                    tint = TmrTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(40.dp),
                )
            }
            TmrHeadline(
                text = stringResource(R.string.feature_profile_impl_empty_headline),
                style = TmrTheme.typography.headlineL,
                color = TmrTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_profile_impl_empty_body),
                style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
                color = TmrTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
                TmrPrimaryButton(
                    label = stringResource(R.string.feature_profile_impl_import_resume),
                    onClick = navigation.onImportResume,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = TmrIcons.Add,
                )
                TmrSecondaryButton(
                    label = stringResource(R.string.feature_profile_impl_build_step_by_step),
                    onClick = navigation.onBuildStepByStep,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = TmrIcons.ArrowForward,
                )
            }
        }
    }
}
