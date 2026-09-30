package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons

data class AnalysisActions(
    val onBackClick: () -> Unit = {},
    val onOpenProfile: () -> Unit = {},
    val onJobTextChange: (String) -> Unit = {},
    val onAnalyze: () -> Unit = {},
    val onEditJobText: () -> Unit = {},
    val onTitleChange: (String) -> Unit = {},
    val onCompanyChange: (String) -> Unit = {},
    val onSubmitEvidence: (requirementId: String, statement: String) -> Unit = { _, _ -> },
    val onTogglePrepPlan: (String) -> Unit = {},
    val onSave: () -> Unit = {},
)

@Composable
internal fun AnalysisRoute(
    onBackClick: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenTailor: (applicationId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedApplicationId = (uiState as? AnalysisUiState.Saved)?.applicationId
    LaunchedEffect(savedApplicationId) {
        if (savedApplicationId != null) {
            onOpenTailor(savedApplicationId)
            viewModel.onNavigationConsumed()
        }
    }
    AnalysisScreen(
        uiState = uiState,
        actions = AnalysisActions(
            onBackClick = onBackClick,
            onOpenProfile = onOpenProfile,
            onJobTextChange = viewModel::onJobTextChange,
            onAnalyze = viewModel::onAnalyze,
            onEditJobText = viewModel::onEditJobText,
            onTitleChange = viewModel::onTitleChange,
            onCompanyChange = viewModel::onCompanyChange,
            onSubmitEvidence = viewModel::onSubmitEvidence,
            onTogglePrepPlan = viewModel::onTogglePrepPlan,
            onSave = viewModel::onSave,
        ),
        modifier = modifier,
    )
}

@Composable
internal fun AnalysisScreen(
    uiState: AnalysisUiState,
    actions: AnalysisActions,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_analysis_title),
                navigationIcon = HhIcons.ArrowBack,
                navigationIconContentDescription = stringResource(R.string.feature_analysis_back),
                onNavigationClick = actions.onBackClick,
            )
        },
    ) { padding ->
        AnalysisContent(uiState, actions, Modifier.padding(padding))
    }
}

@Composable
private fun AnalysisContent(
    uiState: AnalysisUiState,
    actions: AnalysisActions,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        AnalysisUiState.Loading, is AnalysisUiState.Saved ->
            ProgressContent(R.string.feature_analysis_loading, modifier)
        AnalysisUiState.Analyzing -> ProgressContent(R.string.feature_analysis_analyzing, modifier)
        AnalysisUiState.Saving -> ProgressContent(R.string.feature_analysis_saving, modifier)
        AnalysisUiState.NoProfile -> NoProfileContent(actions.onOpenProfile, modifier)
        is AnalysisUiState.Input -> InputContent(
            state = uiState,
            onJobTextChange = actions.onJobTextChange,
            onAnalyze = actions.onAnalyze,
            modifier = modifier,
        )
        is AnalysisUiState.Result -> ResultContent(uiState, actions, modifier)
    }
}

@Composable
private fun ProgressContent(@StringRes messageRes: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val message = stringResource(messageRes)
            HhLoadingWheel(contentDesc = message)
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

internal val ScreenPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
