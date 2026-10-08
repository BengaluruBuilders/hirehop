package com.tailormyresume.feature.tailor.impl.prepquestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.feature.tailor.api.navigation.PrepQuestionsNavKey

@Composable
internal fun PrepQuestionsRoute(
    key: PrepQuestionsNavKey,
    onNavigateBack: () -> Unit,
    onOpenPrepPlan: () -> Unit,
    onEditFact: (entryId: String, entryType: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrepQuestionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, onNavigateBack, onOpenPrepPlan, onEditFact) {
        viewModel.toActions(onNavigateBack, onOpenPrepPlan, onEditFact)
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    PrepQuestionsScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun PrepQuestionsViewModel.toActions(
    onNavigateBack: () -> Unit,
    onOpenPrepPlan: () -> Unit,
    onEditFact: (entryId: String, entryType: String) -> Unit,
): PrepQuestionsActions = PrepQuestionsActions(
    onReportInaccurate = { questionId -> onAction(PrepQuestionsAction.ReportInaccurate(questionId)) },
    onDismissMessage = { onAction(PrepQuestionsAction.DismissMessage) },
    onRetry = { onAction(PrepQuestionsAction.Retry) },
    onOpenPrepPlan = onOpenPrepPlan,
    onEditFact = onEditFact,
    onNavigateBack = onNavigateBack,
)
