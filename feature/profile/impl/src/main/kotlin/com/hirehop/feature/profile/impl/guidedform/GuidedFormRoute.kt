package com.hirehop.feature.profile.impl.guidedform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.profile.api.navigation.GuidedProfileFormNavKey

@Composable
internal fun GuidedFormRoute(
    key: GuidedProfileFormNavKey,
    onNavigateToEvidence: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GuidedFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.handoff?.category) {
        val category = uiState.handoff?.category
        if (category != null) {
            viewModel.onAction(GuidedFormAction.HandoffConsumed)
            onNavigateToEvidence(category)
        }
    }
    GuidedFormScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun GuidedFormViewModel.toActions(): GuidedFormActions = GuidedFormActions(
    onValueChange = { field, value -> onAction(GuidedFormAction.ValueChanged(field, value)) },
    onNext = { onAction(GuidedFormAction.Next) },
    onBack = { onAction(GuidedFormAction.Back) },
    onSaveAndFinishLater = { onAction(GuidedFormAction.SaveAndFinishLater) },
    onContinueNow = { onAction(GuidedFormAction.ContinueNow) },
    onStartHandoff = { onAction(GuidedFormAction.StartHandoff) },
    onDismissMessage = { onAction(GuidedFormAction.DismissMessage) },
)
