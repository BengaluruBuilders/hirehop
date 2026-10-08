package com.tailormyresume.feature.onboarding.impl.consent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.feature.onboarding.api.navigation.ConsentNavKey

@Composable
internal fun ConsentRoute(
    key: ConsentNavKey,
    onBack: () -> Unit,
    onNavigateToStep: (OnboardingStep) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConsentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack) { viewModel.toActions(onBack) }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.nextStep) {
        val step = uiState.nextStep
        if (step != null) {
            viewModel.onAction(ConsentAction.NextStepConsumed)
            onNavigateToStep(step)
        }
    }
    ConsentScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun ConsentViewModel.toActions(onBack: () -> Unit): ConsentActions = ConsentActions(
    onPurposeToggle = { purpose -> onAction(ConsentAction.PurposeToggled(purpose)) },
    onAgree = { onAction(ConsentAction.Agree) },
    onNotNow = { onAction(ConsentAction.NotNow) },
    onReadAgain = { onAction(ConsentAction.ReadAgain) },
    onBack = onBack,
)
