package com.tailormyresume.core.designsystem.component

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val TmrWidthHairline: Dp = 1.dp
internal val TmrWidthStroke: Dp = 1.5.dp
internal val TmrSizeIcon: Dp = 20.dp
internal val TmrSizeDisc: Dp = 8.dp
internal val TmrSizeDragHandleHeight: Dp = 4.dp
internal val TmrSpacingFourteen: Dp = 14.dp
internal val TmrRadiusSheet: Dp = 28.dp

@Composable
internal fun tmrReducedMotion(): Boolean {
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
