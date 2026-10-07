package com.hirehop.feature.profile.impl

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
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

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
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
        ) {
            Box(
                modifier = Modifier.size(IconDiscSize).background(HhTheme.colors.card, HhTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = HhIcons.Profile,
                    contentDescription = stringResource(R.string.feature_profile_impl_empty_illustration),
                    tint = HhTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(40.dp),
                )
            }
            HhHeadline(
                text = stringResource(R.string.feature_profile_impl_empty_headline),
                style = HhTheme.typography.headlineL,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_profile_impl_empty_body),
                style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
                color = HhTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                HhPrimaryButton(
                    label = stringResource(R.string.feature_profile_impl_import_resume),
                    onClick = navigation.onImportResume,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = HhIcons.Add,
                )
                HhSecondaryButton(
                    label = stringResource(R.string.feature_profile_impl_build_step_by_step),
                    onClick = navigation.onBuildStepByStep,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = HhIcons.ArrowForward,
                )
            }
        }
    }
}
