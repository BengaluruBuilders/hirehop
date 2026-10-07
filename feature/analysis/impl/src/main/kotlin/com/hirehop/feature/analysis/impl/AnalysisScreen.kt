package com.hirehop.feature.analysis.impl

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhContentSwitch
import com.hirehop.core.designsystem.component.HhHeaderIconButton
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.HhToastResult
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.domain.onboarding.OnboardingStep
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.model.MatchStatus

data class AnalysisActions(
    val onBackClick: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onBackToJobDescription: () -> Unit = {},
    val onOpenMenu: (String) -> Unit = {},
    val onSeeSource: (String) -> Unit = {},
    val onIHaveThis: (String) -> Unit = {},
    val onOpenShareCard: () -> Unit = {},
    val onDismissOverlay: () -> Unit = {},
    val onReport: (String) -> Unit = {},
    val onTogglePrepPlan: (String) -> Unit = {},
    val onSubmitEvidence: (requirementId: String, statement: String) -> Unit = { _, _ -> },
    val onEditFact: (String) -> Unit = {},
    val onShareText: (String) -> Unit = {},
    val onTailor: () -> Unit = {},
    val onUndo: () -> Unit = {},
    val onToastDismiss: () -> Unit = {},
)

@Composable
internal fun AnalysisRoute(
    scenario: DebugScenario,
    onBackClick: () -> Unit,
    onLeave: (OnboardingStep) -> Unit,
    onOpenTailor: (applicationId: String) -> Unit,
    onEditFact: (factId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val shareTitle = stringResource(R.string.feature_analysis_impl_share_chooser)
    LaunchedEffect(scenario) { viewModel.onEnter(scenario) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }
    LaunchedEffect(viewModel) {
        viewModel.destinations.collect { destination ->
            when (destination) {
                is AnalysisDestination.Leave -> onLeave(destination.step)
                is AnalysisDestination.Tailor -> onOpenTailor(destination.applicationId)
            }
        }
    }
    AnalysisScreen(
        uiState = uiState,
        actions = AnalysisActions(
            onBackClick = onBackClick,
            onRetry = viewModel::onRetry,
            onBackToJobDescription = viewModel::onBackToJobDescription,
            onOpenMenu = viewModel::onOpenMenu,
            onSeeSource = viewModel::onSeeSource,
            onIHaveThis = viewModel::onIHaveThis,
            onOpenShareCard = viewModel::onOpenShareCard,
            onDismissOverlay = viewModel::onDismissOverlay,
            onReport = viewModel::onReport,
            onTogglePrepPlan = viewModel::onTogglePrepPlan,
            onSubmitEvidence = viewModel::onSubmitEvidence,
            onEditFact = { factId ->
                viewModel.onDismissOverlay()
                onEditFact(factId)
            },
            onShareText = { text ->
                viewModel.onDismissOverlay()
                context.startActivity(shareTextIntent(shareTitle, text))
            },
            onTailor = viewModel::onTailor,
            onUndo = viewModel::onUndo,
            onToastDismiss = viewModel::onToastDismiss,
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
    val result = uiState as? AnalysisUiState.Result
    var menuAnchor by remember { mutableStateOf(Rect.Zero) }
    val menuId = (result?.overlay as? AnalysisOverlay.Menu)?.requirementId
    val toastState = rememberHhToastState()
    AnalysisToastEffect(result?.toast, toastState, actions)
    BackHandler(enabled = menuId != null, onBack = actions.onDismissOverlay)
    Box(modifier = modifier.fillMaxSize()) {
        HhScreen(
            header = {
                AnalysisHeader(
                    result = result,
                    onBack = actions.onBackClick,
                    onShare = actions.onOpenShareCard,
                )
            },
            sheet = false,
            bottomBar = analysisBottomBar(uiState, actions),
            bottomBarNotice = analysisBottomBarNotice(uiState),
            snackbarHost = { HhToastHost(toastState) },
        ) { padding ->
            HhContentSwitch(targetState = uiState, contentKey = { it.contentKey() }) { state ->
                AnalysisBody(
                    uiState = state,
                    actions = actions,
                    contentPadding = padding,
                    onMenuAnchor = { id, bounds -> if (id == menuId) menuAnchor = bounds },
                )
            }
        }
        if (result != null) {
            RowMenuOverlay(result, menuAnchor, actions)
            AnalysisSheets(result, actions)
        }
    }
}

@Composable
private fun AnalysisToastEffect(
    toast: AnalysisToast?,
    toastState: com.hirehop.core.designsystem.component.HhToastState,
    actions: AnalysisActions,
) {
    val message = toast?.let { toastText(it) }
    val undoLabel = if (toast?.hasUndo == true) stringResource(R.string.feature_analysis_impl_undo) else null
    LaunchedEffect(toast) {
        if (message != null) {
            val outcome = toastState.show(message = message, actionLabel = undoLabel)
            if (outcome == HhToastResult.ActionPerformed) actions.onUndo() else actions.onToastDismiss()
        }
    }
}

private fun AnalysisUiState.contentKey(): Any =
    if (this is AnalysisUiState.Loading) AnalysisUiState.Analyzing::class else this::class

@Composable
private fun AnalysisBody(
    uiState: AnalysisUiState,
    actions: AnalysisActions,
    contentPadding: PaddingValues,
    onMenuAnchor: (String, Rect) -> Unit,
) {
    when (uiState) {
        AnalysisUiState.Loading -> WaitingContent(AnalysisUiState.Analyzing(uiState.job, 0), contentPadding)
        is AnalysisUiState.Analyzing -> WaitingContent(uiState, contentPadding)
        is AnalysisUiState.Failed -> MessageContent(
            title = stringResource(R.string.feature_analysis_impl_error_title),
            body = stringResource(R.string.feature_analysis_impl_error_body),
            contentPadding = contentPadding,
        )
        is AnalysisUiState.DailyLimit -> MessageContent(
            title = stringResource(R.string.feature_analysis_impl_daily_limit_title),
            body = stringResource(R.string.feature_analysis_impl_daily_limit_body),
            chip = stringResource(R.string.feature_analysis_impl_daily_limit_left),
            note = stringResource(R.string.feature_analysis_impl_daily_limit_note),
            contentPadding = contentPadding,
        )
        is AnalysisUiState.Result -> ResultContent(uiState, actions, contentPadding, onMenuAnchor)
    }
}

internal fun shareTextIntent(chooserTitle: String, text: String): Intent {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    return Intent.createChooser(send, chooserTitle)
}

@Composable
internal fun AnalysisUiState.headerTitle(): String = when {
    job.title.isNotBlank() -> job.title
    this is AnalysisUiState.Result -> stringResource(R.string.feature_analysis_impl_role_not_set)
    else -> stringResource(R.string.feature_analysis_impl_title_fallback)
}

@Composable
internal fun AnalysisUiState.headerSubtitle(): String? = when {
    job.company.isNotBlank() -> job.company
    this is AnalysisUiState.Result -> stringResource(R.string.feature_analysis_impl_company_not_set)
    else -> null
}

@Composable
private fun AnalysisHeader(
    result: AnalysisUiState.Result?,
    onBack: () -> Unit,
    onShare: () -> Unit,
) {
    HhInnerHeader(
        title = stringResource(R.string.feature_analysis_impl_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.feature_analysis_impl_back),
        trailing = result?.let {
            {
                HhHeaderIconButton(
                    icon = HhIcons.Share,
                    contentDescription = stringResource(R.string.feature_analysis_impl_share_my_fit),
                    onClick = onShare,
                )
            }
        },
    )
}

internal fun AnalysisUiState.Result.countOf(status: MatchStatus): Int = items.count { it.status == status }
