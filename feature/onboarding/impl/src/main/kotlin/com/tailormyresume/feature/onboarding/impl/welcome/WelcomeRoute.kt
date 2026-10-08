package com.tailormyresume.feature.onboarding.impl.welcome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.feature.onboarding.api.navigation.WelcomeNavKey

@Composable
internal fun WelcomeRoute(
    key: WelcomeNavKey,
    onNavigateToPasteJobDescription: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToConsent: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.destination) {
        when (uiState.destination) {
            WelcomeDestination.PASTE_JOB_DESCRIPTION -> {
                viewModel.onAction(WelcomeAction.DestinationConsumed)
                onNavigateToPasteJobDescription()
            }

            WelcomeDestination.SIGN_IN -> {
                viewModel.onAction(WelcomeAction.DestinationConsumed)
                onNavigateToSignIn()
            }

            WelcomeDestination.CONSENT -> {
                viewModel.onAction(WelcomeAction.DestinationConsumed)
                onNavigateToConsent()
            }

            null -> Unit
        }
    }
    WelcomeScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun WelcomeViewModel.toActions(): WelcomeActions = WelcomeActions(
    onPasteJobDescription = { onAction(WelcomeAction.PasteJobDescriptionTapped) },
    onSelectCareerStage = { onAction(WelcomeAction.CareerStageSelected(it)) },
    onHaveAccount = { onAction(WelcomeAction.HaveAccountTapped) },
    onRetry = { onAction(WelcomeAction.RetryTapped) },
    onDismissMessage = { onAction(WelcomeAction.DismissMessageTapped) },
)
