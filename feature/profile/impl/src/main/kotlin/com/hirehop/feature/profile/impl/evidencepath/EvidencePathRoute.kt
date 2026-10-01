package com.hirehop.feature.profile.impl.evidencepath

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.profile.api.navigation.FactEvidenceNavKey

@Composable
internal fun EvidencePathRoute(
    key: FactEvidenceNavKey,
    onGoToProfile: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EvidencePathViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onGoToProfile) { viewModel.toActions(onGoToProfile) }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    EvidencePathScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun EvidencePathViewModel.toActions(
    onGoToProfile: () -> Unit,
): EvidencePathActions = EvidencePathActions(
    onCategoryChosen = { onAction(EvidencePathAction.CategoryChosen(it)) },
    onAnswerChanged = { prompt, value -> onAction(EvidencePathAction.AnswerChanged(prompt, value)) },
    onNextPrompt = { onAction(EvidencePathAction.NextPrompt) },
    onBackPrompt = { onAction(EvidencePathAction.BackPrompt) },
    onSkipPrompt = { onAction(EvidencePathAction.SkipPrompt) },
    onSave = { onAction(EvidencePathAction.Save) },
    onSkipCategory = { onAction(EvidencePathAction.SkipCategory) },
    onAddMore = { onAction(EvidencePathAction.AddMore) },
    onGoToProfile = {
        onAction(EvidencePathAction.Finish)
        onGoToProfile()
    },
    onDismissMessage = { onAction(EvidencePathAction.DismissMessage) },
)
