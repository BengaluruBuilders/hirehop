package com.tailormyresume.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.designsystem.component.TmrApplicationStatusChip
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.JobApplication

@Composable
fun ApplicationStatusChip(
    status: ApplicationStatus,
    modifier: Modifier = Modifier,
) {
    val kind = ApplicationStatusKindMapper().kindOf(status)
    TmrApplicationStatusChip(kind = kind, modifier = modifier, label = kind.label())
}

@Composable
fun ApplicationStatusChip(
    application: JobApplication,
    modifier: Modifier = Modifier,
) {
    val kind = ApplicationStatusKindMapper().kindOf(application)
    TmrApplicationStatusChip(kind = kind, modifier = modifier, label = kind.label())
}
