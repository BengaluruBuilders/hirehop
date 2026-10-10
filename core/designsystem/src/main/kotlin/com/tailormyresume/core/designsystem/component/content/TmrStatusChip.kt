package com.tailormyresume.core.designsystem.component.content

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.tailormyresume.core.designsystem.R
import com.tailormyresume.core.designsystem.theme.TmrColors

enum class TmrApplicationStatus(@StringRes val labelRes: Int) {
    Saved(R.string.content_status_saved),
    Applied(R.string.content_status_applied),
    Interview(R.string.content_status_interview),
    Offer(R.string.content_status_offer),
    Rejected(R.string.content_status_rejected),
}

@Immutable
data class TmrStatusChipColors(val text: Color, val fill: Color, val border: Color)

internal fun tmrStatusChipColors(status: TmrApplicationStatus, colors: TmrColors): TmrStatusChipColors =
    TmrStatusChipColors(Color.Unspecified, Color.Unspecified, Color.Unspecified)

@Composable
fun TmrStatusChip(status: TmrApplicationStatus, modifier: Modifier = Modifier) {
}
