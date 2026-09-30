package com.hirehop.feature.onboarding.impl.consent

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.onboarding.api.navigation.ConsentNavKey

@Composable
internal fun ConsentRoute(
    key: ConsentNavKey,
    onSkipToJobDescription: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConsentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    ConsentScreen(
        uiState = uiState,
        actions = actions,
        onSkipToJobDescription = onSkipToJobDescription,
        modifier = modifier,
    )
}

private fun ConsentViewModel.toActions(): ConsentActions = ConsentActions(
    onPurposeToggle = { purpose -> onAction(ConsentAction.PurposeToggled(purpose)) },
    onAgree = { onAction(ConsentAction.Agree) },
    onNotNow = { onAction(ConsentAction.NotNow) },
    onReadAgain = { onAction(ConsentAction.ReadAgain) },
)
