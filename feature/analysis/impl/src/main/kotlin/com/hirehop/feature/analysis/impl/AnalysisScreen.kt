package com.hirehop.feature.analysis.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme

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
    val onErrorShown: () -> Unit = {},
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
            onErrorShown = viewModel::onErrorShown,
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
    var visibleError by remember { mutableStateOf<AnalysisError?>(null) }
    val currentError = uiState.errorOrNull
    LaunchedEffect(currentError) {
        if (currentError != null) visibleError = currentError
    }
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_analysis_impl_title),
                navigationIcon = HhIcons.ArrowBack,
                navigationIconContentDescription = stringResource(R.string.feature_analysis_impl_back),
                onNavigationClick = actions.onBackClick,
            )
        },
    ) { padding ->
        AnalysisContent(
            uiState = uiState,
            actions = actions,
            visibleError = visibleError,
            onErrorDismiss = {
                visibleError = null
                actions.onErrorShown()
            },
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
private fun AnalysisContent(
    uiState: AnalysisUiState,
    actions: AnalysisActions,
    visibleError: AnalysisError?,
    onErrorDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        when (uiState) {
            AnalysisUiState.Loading, is AnalysisUiState.Saved ->
                ProgressContent(R.string.feature_analysis_impl_loading)
            AnalysisUiState.Analyzing -> ProgressContent(R.string.feature_analysis_impl_analyzing)
            AnalysisUiState.Saving -> ProgressContent(R.string.feature_analysis_impl_saving)
            AnalysisUiState.NoProfile -> NoProfileContent(actions.onOpenProfile)
            is AnalysisUiState.Input -> InputContent(
                state = uiState,
                onJobTextChange = actions.onJobTextChange,
                onAnalyze = actions.onAnalyze,
            )
            is AnalysisUiState.Result -> ResultContent(uiState, actions)
        }
        if (visibleError != null) {
            HhErrorCallout(
                title = stringResource(visibleError.titleRes()),
                supportingText = stringResource(visibleError.supportingRes()),
                actionLabel = stringResource(R.string.feature_analysis_impl_error_dismiss),
                onAction = onErrorDismiss,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(screenPadding()),
            )
        }
    }
}

@Composable
private fun ProgressContent(@StringRes messageRes: Int) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
        ) {
            val message = stringResource(messageRes)
            HhLoadingWheel(contentDesc = message)
            Text(
                text = message,
                style = HhTheme.typography.bodyLarge,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@StringRes
private fun AnalysisError.titleRes(): Int = when (this) {
    AnalysisError.AnalyzeFailed -> R.string.feature_analysis_impl_error_analyze
    AnalysisError.AddEvidenceFailed -> R.string.feature_analysis_impl_error_add_evidence
    AnalysisError.SaveFailed -> R.string.feature_analysis_impl_error_save
}

@StringRes
private fun AnalysisError.supportingRes(): Int = when (this) {
    AnalysisError.AnalyzeFailed -> R.string.feature_analysis_impl_error_analyze_supporting
    AnalysisError.AddEvidenceFailed -> R.string.feature_analysis_impl_error_add_evidence_supporting
    AnalysisError.SaveFailed -> R.string.feature_analysis_impl_error_save_supporting
}

@Composable
internal fun screenPadding(): PaddingValues = PaddingValues(
    horizontal = HhTheme.spacing.md,
    vertical = HhTheme.spacing.sm,
)
