package com.hirehop.feature.applications.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.model.ApplicationStatus

@StringRes
internal fun ApplicationStatus.labelRes(): Int = when (this) {
    ApplicationStatus.SAVED -> R.string.feature_applications_status_saved
    ApplicationStatus.APPLIED -> R.string.feature_applications_status_applied
    ApplicationStatus.INTERVIEW -> R.string.feature_applications_status_interview
    ApplicationStatus.OFFER -> R.string.feature_applications_status_offer
    ApplicationStatus.REJECTED -> R.string.feature_applications_status_rejected
    ApplicationStatus.NO_RESPONSE -> R.string.feature_applications_status_no_response
}

internal fun ApplicationStatus.accentColor(isDark: Boolean): Color = when (this) {
    ApplicationStatus.SAVED -> if (isDark) Color(0xFFB0BEC5) else Color(0xFF546E7A)
    ApplicationStatus.APPLIED -> if (isDark) Color(0xFF90CAF9) else Color(0xFF1565C0)
    ApplicationStatus.INTERVIEW -> if (isDark) Color(0xFFCE93D8) else Color(0xFF6A1B9A)
    ApplicationStatus.OFFER -> if (isDark) Color(0xFFA5D6A7) else Color(0xFF2E7D32)
    ApplicationStatus.REJECTED -> if (isDark) Color(0xFFEF9A9A) else Color(0xFFC62828)
    ApplicationStatus.NO_RESPONSE -> if (isDark) Color(0xFFFFCC80) else Color(0xFF8D5A00)
}

private const val CHIP_BACKGROUND_ALPHA = 0.16f

@Composable
internal fun ApplicationStatusChip(
    status: ApplicationStatus,
    modifier: Modifier = Modifier,
) {
    val accent = status.accentColor(isSystemInDarkTheme())
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = accent.copy(alpha = CHIP_BACKGROUND_ALPHA),
    ) {
        Text(
            text = stringResource(status.labelRes()),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
        )
    }
}
