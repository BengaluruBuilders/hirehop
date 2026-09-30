package com.hirehop.core.designsystem.component

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val HhWidthHairline: Dp = 1.dp
internal val HhWidthStroke: Dp = 1.5.dp
internal val HhSizeIcon: Dp = 20.dp
internal val HhSizeDisc: Dp = 8.dp
internal val HhSizeSpotWidth: Dp = 64.dp
internal val HhSizeSpotHeight: Dp = 80.dp
internal val HhSizeDragHandleWidth: Dp = 32.dp
internal val HhSizeDragHandleHeight: Dp = 4.dp
internal val HhSpacingFourteen: Dp = 14.dp
internal val HhRadiusSheet: Dp = 28.dp
internal val HhHeightListRow: Dp = 56.dp
internal const val HH_TABULAR_FIGURES: String = "tnum"

@Composable
internal fun hhReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}
