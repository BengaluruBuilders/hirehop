package com.hirehop.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.JobApplication

@Composable
fun ApplicationStatusChip(
    status: ApplicationStatus,
    modifier: Modifier = Modifier,
) {
    HhApplicationStatusChip(
        kind = ApplicationStatusKindMapper().kindOf(status),
        modifier = modifier,
    )
}

@Composable
fun ApplicationStatusChip(
    application: JobApplication,
    modifier: Modifier = Modifier,
) {
    HhApplicationStatusChip(
        kind = ApplicationStatusKindMapper().kindOf(application),
        modifier = modifier,
    )
}
