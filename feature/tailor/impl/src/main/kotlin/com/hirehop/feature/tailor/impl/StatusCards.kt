package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun StatusCard(
    kind: HhSpotKind,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    HhHeroCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 + HhTheme.spacing.d2),
        ) {
            Box(
                modifier = Modifier
                    .size(HhTheme.spacing.d64 * 2.5f)
                    .background(HhTheme.colors.primaryContainer, HhTheme.shapes.pill),
                contentAlignment = Alignment.BottomCenter,
            ) {
                HhSpotIllustration(kind = kind)
            }
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                Text(
                    text = title,
                    style = HhTheme.typography.titleL,
                    color = HhTheme.colors.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = body,
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
