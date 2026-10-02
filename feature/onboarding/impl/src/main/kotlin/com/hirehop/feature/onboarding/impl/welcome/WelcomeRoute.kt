package com.hirehop.feature.onboarding.impl.welcome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.onboarding.api.navigation.WelcomeNavKey

@Composable
internal fun WelcomeRoute(
    key: WelcomeNavKey,
    onNavigateToPasteJobDescription: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToConsent: () -> Unit,
    onNavigateToImportResume: () -> Unit,
    onNavigateToBuildProfileStepByStep: () -> Unit,
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

            WelcomeDestination.IMPORT_RESUME -> {
                viewModel.onAction(WelcomeAction.DestinationConsumed)
                onNavigateToImportResume()
            }

            WelcomeDestination.BUILD_PROFILE_STEP_BY_STEP -> {
                viewModel.onAction(WelcomeAction.DestinationConsumed)
                onNavigateToBuildProfileStepByStep()
            }

            null -> Unit
        }
    }
    WelcomeScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun WelcomeViewModel.toActions(): WelcomeActions = WelcomeActions(
    onPasteJobDescription = { onAction(WelcomeAction.PasteJobDescriptionTapped) },
    onImportResume = { onAction(WelcomeAction.ImportResumeTapped) },
    onBuildProfileStepByStep = { onAction(WelcomeAction.BuildProfileStepByStepTapped) },
    onRetry = { onAction(WelcomeAction.RetryTapped) },
    onDismissMessage = { onAction(WelcomeAction.DismissMessageTapped) },
)
