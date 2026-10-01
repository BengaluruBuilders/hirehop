package com.hirehop.feature.onboarding.impl.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.onboarding.api.navigation.SignInNavKey

@Composable
internal fun SignInRoute(
    key: SignInNavKey,
    onSkipToJobDescription: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    SignInScreen(
        uiState = uiState,
        actions = actions,
        onSkipToJobDescription = onSkipToJobDescription,
        modifier = modifier,
    )
}

private fun SignInViewModel.toActions(): SignInActions = SignInActions(
    onAdultConfirmationChange = { isConfirmed -> onAction(SignInAction.AdultConfirmationChanged(isConfirmed)) },
    onContinue = { onAction(SignInAction.Continue) },
    onNotNow = { onAction(SignInAction.NotNow) },
    onRevisit = { onAction(SignInAction.Revisit) },
    onUnderEighteen = { onAction(SignInAction.UnderEighteen) },
    onBackFromUnderEighteen = { onAction(SignInAction.BackFromUnderEighteen) },
)
