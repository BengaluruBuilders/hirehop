package com.hirehop.feature.tailor.impl.prepquestions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.tailor.api.navigation.PrepQuestionsNavKey

@Composable
internal fun PrepQuestionsRoute(
    key: PrepQuestionsNavKey,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PrepQuestionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions(onNavigateBack = onNavigateBack) }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    PrepQuestionsScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun PrepQuestionsViewModel.toActions(onNavigateBack: () -> Unit): PrepQuestionsActions = PrepQuestionsActions(
    onFilterChosen = { filter -> onAction(PrepQuestionsAction.FilterChosen(filter)) },
    onPractiseToggled = { questionId -> onAction(PrepQuestionsAction.PractiseToggled(questionId)) },
    onReportInaccurate = { questionId -> onAction(PrepQuestionsAction.ReportInaccurate(questionId)) },
    onDismissMessage = { onAction(PrepQuestionsAction.DismissMessage) },
    onRetry = { onAction(PrepQuestionsAction.Retry) },
    onNavigateBack = onNavigateBack,
)
