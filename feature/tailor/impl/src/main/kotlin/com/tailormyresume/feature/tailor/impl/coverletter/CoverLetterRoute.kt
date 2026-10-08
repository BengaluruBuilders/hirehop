package com.tailormyresume.feature.tailor.impl.coverletter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.feature.tailor.api.navigation.CoverLetterNavKey

@Composable
internal fun CoverLetterRoute(
    key: CoverLetterNavKey,
    onNavigateBack: () -> Unit,
    onSkipLetter: () -> Unit,
    onPreviewExport: () -> Unit,
    onPrepQuestions: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CoverLetterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onNavigateBack, onSkipLetter, onPreviewExport, onPrepQuestions) {
        viewModel.toActions(onNavigateBack, onSkipLetter, onPreviewExport, onPrepQuestions)
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    CoverLetterScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun CoverLetterViewModel.toActions(
    onNavigateBack: () -> Unit,
    onSkipLetter: () -> Unit,
    onPreviewExport: () -> Unit,
    onPrepQuestions: () -> Unit,
): CoverLetterActions = CoverLetterActions(
    onWriteOne = { onAction(CoverLetterAction.WriteOne) },
    onBeginEdit = { ordinal -> onAction(CoverLetterAction.BeginEdit(ordinal)) },
    onEditTextChanged = { value -> onAction(CoverLetterAction.EditTextChanged(value)) },
    onSaveEdit = { onAction(CoverLetterAction.SaveEdit) },
    onCancelEdit = { onAction(CoverLetterAction.CancelEdit) },
    onReportInaccurate = { ordinal -> onAction(CoverLetterAction.ReportInaccurate(ordinal)) },
    onDismissMessage = { onAction(CoverLetterAction.DismissMessage) },
    onRetry = { onAction(CoverLetterAction.Retry) },
    onNavigateBack = onNavigateBack,
    onSkipLetter = onSkipLetter,
    onPreviewExport = onPreviewExport,
    onPrepQuestions = onPrepQuestions,
)
