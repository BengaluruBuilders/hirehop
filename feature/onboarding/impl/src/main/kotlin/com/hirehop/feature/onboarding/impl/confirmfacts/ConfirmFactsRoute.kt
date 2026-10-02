package com.hirehop.feature.onboarding.impl.confirmfacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.feature.onboarding.api.navigation.ConfirmFactsNavKey

@Composable
fun ConfirmFactsRoute(
    key: ConfirmFactsNavKey,
    onBack: () -> Unit,
    onNavigateToStep: (OnboardingStep) -> Unit,
    onImportResume: () -> Unit,
    onEditFact: (String?, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmFactsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onBack, onImportResume) {
        ConfirmFactsActions(
            onBack = onBack,
            onConfirm = { factId -> viewModel.onAction(ConfirmFactsAction.Confirm(factId)) },
            onEdit = { factId, category -> viewModel.onAction(ConfirmFactsAction.Edit(factId, category)) },
            onAddOne = { section -> viewModel.onAction(ConfirmFactsAction.AddOne(section)) },
            onSkip = { section -> viewModel.onAction(ConfirmFactsAction.Skip(section)) },
            onContinue = { viewModel.onAction(ConfirmFactsAction.Continue) },
            onImportResume = onImportResume,
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.pendingEdit) {
        val pendingEdit = uiState.pendingEdit
        if (pendingEdit != null) {
            onEditFact(pendingEdit.factId, pendingEdit.category.name)
            viewModel.onAction(ConfirmFactsAction.EditConsumed)
        }
    }
    LaunchedEffect(uiState.nextStep) {
        val step = uiState.nextStep
        if (step != null) {
            viewModel.onAction(ConfirmFactsAction.NextStepConsumed)
            onNavigateToStep(step)
        }
    }
    ConfirmFactsScreen(uiState = uiState, actions = actions, modifier = modifier)
}
