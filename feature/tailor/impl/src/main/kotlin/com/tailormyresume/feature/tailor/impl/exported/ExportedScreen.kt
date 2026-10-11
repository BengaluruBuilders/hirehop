package com.tailormyresume.feature.tailor.impl.exported

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.model.ApplicationStatus

@Composable
internal fun ExportedScreen(
    state: ExportedUiState.Ready,
    onShare: () -> Unit,
    onOpen: () -> Unit,
    onMarkApplied: () -> Unit,
    onChangeStatus: () -> Unit,
    onPickStatus: (ApplicationStatus) -> Unit,
    onDismissStatusSheet: () -> Unit,
    onSaveStatus: () -> Unit,
    onSoon: () -> Unit,
    onGoToApplications: () -> Unit,
    modifier: Modifier = Modifier,
) {
}
