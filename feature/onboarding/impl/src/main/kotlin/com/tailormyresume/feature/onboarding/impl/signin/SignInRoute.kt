package com.tailormyresume.feature.onboarding.impl.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.feature.onboarding.api.navigation.SignInNavKey

@Composable
internal fun SignInRoute(
    key: SignInNavKey,
    onBack: () -> Unit,
    onBackToStart: () -> Unit,
    onNavigateToStep: (OnboardingStep) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack, onBackToStart) { viewModel.toActions(onBack, onBackToStart) }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.nextStep) {
        val step = uiState.nextStep
        if (step != null) {
            viewModel.onAction(SignInAction.NextStepConsumed)
            onNavigateToStep(step)
        }
    }
    SignInScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun SignInViewModel.toActions(onBack: () -> Unit, onBackToStart: () -> Unit): SignInActions = SignInActions(
    onAdultConfirmationChange = { isConfirmed -> onAction(SignInAction.AdultConfirmationChanged(isConfirmed)) },
    onContinue = { onAction(SignInAction.Continue) },
    onUnderEighteen = { onAction(SignInAction.UnderEighteen) },
    onBackFromUnderEighteen = {
        onAction(SignInAction.BackFromUnderEighteen)
        onBackToStart()
    },
    onBack = onBack,
)
