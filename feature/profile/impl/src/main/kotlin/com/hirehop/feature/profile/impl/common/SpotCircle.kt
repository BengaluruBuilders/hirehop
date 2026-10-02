package com.hirehop.feature.profile.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun SpotCircle(
    kind: HhSpotKind,
    size: Dp,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(HhTheme.colors.primaryContainer),
        contentAlignment = Alignment.BottomCenter,
    ) {
        HhSpotIllustration(
            kind = kind,
            modifier = Modifier.requiredSize(size),
            contentDescription = contentDescription,
        )
    }
}
