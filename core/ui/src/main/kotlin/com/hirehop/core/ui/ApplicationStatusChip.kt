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
    val kind = ApplicationStatusKindMapper().kindOf(status)
    HhApplicationStatusChip(kind = kind, modifier = modifier, label = kind.label())
}

@Composable
fun ApplicationStatusChip(
    application: JobApplication,
    modifier: Modifier = Modifier,
) {
    val kind = ApplicationStatusKindMapper().kindOf(application)
    HhApplicationStatusChip(kind = kind, modifier = modifier, label = kind.label())
}
