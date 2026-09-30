package com.hirehop.feature.onboarding.impl.confirmfacts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.onboarding.api.navigation.ConfirmFactsNavKey

@Composable
fun ConfirmFactsRoute(
    key: ConfirmFactsNavKey,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onImportResume: () -> Unit,
    onEditFact: (String?, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmFactsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<String?>(null) }
    val actions = remember(viewModel, onBack, onContinue, onImportResume, onEditFact) {
        ConfirmFactsActions(
            onBack = onBack,
            onConfirm = { factId -> viewModel.onAction(ConfirmFactsAction.Confirm(factId)) },
            onEdit = { factId, category ->
                viewModel.onAction(ConfirmFactsAction.Edit(factId, category))
            },
            onRequestDelete = { factId -> pendingDelete = factId },
            onAddOne = { section -> viewModel.onAction(ConfirmFactsAction.AddOne(section)) },
            onSkip = { section -> viewModel.onAction(ConfirmFactsAction.Skip(section)) },
            onContinue = onContinue,
            onImportResume = onImportResume,
            onDismissRemovedNotice = {
                viewModel.onAction(ConfirmFactsAction.DismissRemovedNotice)
            },
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
    ConfirmFactsScreen(uiState = uiState, actions = actions, modifier = modifier)
    val deleting = pendingDelete
    if (deleting != null) {
        ConfirmFactsDeleteDialog(
            factId = deleting,
            onConfirm = {
                pendingDelete = null
                viewModel.onAction(ConfirmFactsAction.Delete(deleting))
            },
            onDismiss = { pendingDelete = null },
        )
    }
}
