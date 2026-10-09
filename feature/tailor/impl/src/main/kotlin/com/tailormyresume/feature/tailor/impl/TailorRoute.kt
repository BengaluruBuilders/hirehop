package com.tailormyresume.feature.tailor.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
internal fun TailorRoute(
    onBackClick: () -> Unit,
    onPreviewExport: () -> Unit,
    onEditFact: (entryId: String, entryType: String) -> Unit,
    viewModel: TailorViewModel,
    modifier: Modifier = Modifier,
    initialBulletId: String? = null,
    onBulletSheetClosed: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBackClick, onPreviewExport, onEditFact, onBulletSheetClosed) {
        TailorActions(
            onBack = onBackClick,
            onPreviewExport = onPreviewExport,
            onAccept = viewModel::onAccept,
            onKeepOriginal = viewModel::onKeepOriginal,
            onUndo = viewModel::onUndo,
            onEditByHand = viewModel::onEditByHand,
            onRegenerate = viewModel::onRegenerate,
            onRetry = viewModel::onRetry,
            onReportBullet = viewModel::onReportBullet,
            onReportSection = viewModel::onReportSection,
            onEditFact = onEditFact,
            onBulletSheetClosed = onBulletSheetClosed,
        )
    }
    val interaction = remember { ReviewInteraction(initialBulletId) }
    LaunchedEffect(viewModel) {
        viewModel.regenerateFailures.collect { result ->
            interaction.toast = when (result) {
                RegenerateResult.NoCredit -> ReviewToastState.RegenerateNoCredit
                is RegenerateResult.Blocked -> ReviewToastState.RegenerateBlocked(result.notice)
                else -> ReviewToastState.RegenerateFailed
            }
        }
    }
    TailorScreen(uiState = uiState, actions = actions, modifier = modifier, interaction = interaction)
}
