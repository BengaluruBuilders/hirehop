package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun ProfileEmptyState(
    onImportResume: () -> Unit,
    onBuildStepByStep: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HhSpotIllustration(
            kind = HhSpotKind.Empty,
            contentDescription = stringResource(
                R.string.feature_profile_impl_empty_illustration,
            ),
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_empty_headline),
            modifier = Modifier.fillMaxWidth(),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
            textAlign = TextAlign.Start,
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_empty_body),
            modifier = Modifier.fillMaxWidth(),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            HhButton(
                onClick = onImportResume,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_profile_impl_import_resume),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
            HhOutlinedButton(
                onClick = onBuildStepByStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_profile_impl_build_step_by_step),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
        }
        Text(
            text = stringResource(R.string.feature_profile_impl_trust_copy),
            modifier = Modifier.fillMaxWidth(),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
    }
}
