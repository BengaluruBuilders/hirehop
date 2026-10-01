package com.hirehop.feature.analysis.impl

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
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTrustChip
import com.hirehop.core.designsystem.component.HhTrustKind
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun NoProfileContent(
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg, Alignment.CenterVertically),
    ) {
        HhSpotIllustration(
            kind = HhSpotKind.Empty,
            contentDescription = stringResource(
                R.string.feature_analysis_impl_no_profile_illustration,
            ),
        )
        Text(
            text = stringResource(R.string.feature_analysis_impl_no_profile_title),
            modifier = Modifier.fillMaxWidth(),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
            textAlign = TextAlign.Start,
        )
        Text(
            text = stringResource(R.string.feature_analysis_impl_no_profile_message),
            modifier = Modifier.fillMaxWidth(),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
        HhButton(
            onClick = onOpenProfile,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_analysis_impl_open_profile),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
        HhTrustChip(
            kind = HhTrustKind.NeverInvents,
            label = stringResource(R.string.feature_analysis_impl_trust_no_invent),
        )
    }
}
