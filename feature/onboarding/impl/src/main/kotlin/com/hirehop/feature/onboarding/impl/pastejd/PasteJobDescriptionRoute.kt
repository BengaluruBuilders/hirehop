package com.hirehop.feature.onboarding.impl.pastejd

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.onboarding.api.navigation.PasteJobDescriptionNavKey

@Composable
internal fun PasteJobDescriptionRoute(
    key: PasteJobDescriptionNavKey,
    onNavigateBack: () -> Unit,
    onAnalyseRequested: (PasteJobDescriptionHandoff) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PasteJobDescriptionViewModel = hiltViewModel(),
    sharedText: String = "",
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    LaunchedEffect(key) { viewModel.onEnter(key = key, sharedText = sharedText) }
    LaunchedEffect(uiState.analysisRequest) {
        val request = uiState.analysisRequest
        if (request != null) {
            viewModel.onAction(PasteJobDescriptionAction.AnalysisRequestConsumed)
            onAnalyseRequested(request)
        }
    }
    PasteJobDescriptionScreen(
        uiState = uiState,
        actions = actions,
        modifier = modifier,
        onBack = onNavigateBack,
    )
}

private fun PasteJobDescriptionViewModel.toActions(): PasteJobDescriptionActions =
    PasteJobDescriptionActions(
        onTextChange = { value -> onAction(PasteJobDescriptionAction.TextChanged(value)) },
        onCompanyChange = { value -> onAction(PasteJobDescriptionAction.CompanyChanged(value)) },
        onRoleChange = { value -> onAction(PasteJobDescriptionAction.RoleChanged(value)) },
        onClear = { onAction(PasteJobDescriptionAction.ClearTapped) },
        onAnalyse = { onAction(PasteJobDescriptionAction.AnalyseTapped) },
        onRetry = { onAction(PasteJobDescriptionAction.RetryTapped) },
        onDismissMessage = { onAction(PasteJobDescriptionAction.DismissMessageTapped) },
    )
