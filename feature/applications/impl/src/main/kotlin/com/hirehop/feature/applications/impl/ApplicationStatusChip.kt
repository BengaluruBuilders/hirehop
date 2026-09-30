package com.hirehop.feature.applications.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme
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

private data class StatusChipColors(
    val container: Color,
    val content: Color,
    val border: BorderStroke? = null,
)

@Composable
private fun ApplicationStatus.chipColors(): StatusChipColors {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        ApplicationStatus.SAVED -> StatusChipColors(colors.surfaceVariant, colors.onSurfaceVariant)
        ApplicationStatus.APPLIED -> StatusChipColors(colors.primaryContainer, colors.onPrimaryContainer)
        ApplicationStatus.INTERVIEW -> StatusChipColors(colors.tertiaryContainer, colors.onTertiaryContainer)
        ApplicationStatus.OFFER -> StatusChipColors(colors.secondaryContainer, colors.onSecondaryContainer)
        ApplicationStatus.REJECTED -> StatusChipColors(colors.errorContainer, colors.onErrorContainer)
        ApplicationStatus.NO_RESPONSE -> StatusChipColors(
            container = Color.Transparent,
            content = colors.onSurfaceVariant,
            border = BorderStroke(1.dp, colors.outline),
        )
    }
}

@Composable
internal fun ApplicationStatusChip(
    status: ApplicationStatus,
    modifier: Modifier = Modifier,
) {
    val colors = status.chipColors()
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = colors.container,
        contentColor = colors.content,
        border = colors.border,
    ) {
        Text(
            text = stringResource(status.labelRes()),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationStatusChipPreview() {
    HhTheme {
        Column {
            ApplicationStatus.entries.forEach { ApplicationStatusChip(status = it) }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101418)
@Composable
private fun ApplicationStatusChipDarkPreview() {
    HhTheme(darkTheme = true) {
        Column {
            ApplicationStatus.entries.forEach { ApplicationStatusChip(status = it) }
        }
    }
}
