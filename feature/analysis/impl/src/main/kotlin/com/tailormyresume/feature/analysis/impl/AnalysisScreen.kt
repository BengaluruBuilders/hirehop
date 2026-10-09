package com.tailormyresume.feature.analysis.impl

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.core.designsystem.component.TmrContentSwitch
import com.tailormyresume.core.designsystem.component.TmrHeaderIconButton
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrToastHost
import com.tailormyresume.core.designsystem.component.TmrToastResult
import com.tailormyresume.core.designsystem.component.rememberTmrToastState
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.MatchStatus

data class AnalysisActions(
    val onBackClick: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSignInAgain: () -> Unit = {},
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
            onSignInAgain = viewModel::onSignInAgain,
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
    val toastState = rememberTmrToastState()
    AnalysisToastEffect(result?.toast, toastState, actions)
    BackHandler(enabled = menuId != null, onBack = actions.onDismissOverlay)
    Box(modifier = modifier.fillMaxSize()) {
        TmrScreen(
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
            snackbarHost = { TmrToastHost(toastState) },
        ) { padding ->
            TmrContentSwitch(targetState = uiState, contentKey = { it.contentKey() }) { state ->
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
    toastState: com.tailormyresume.core.designsystem.component.TmrToastState,
    actions: AnalysisActions,
) {
    val message = toast?.let { toastText(it) }
    val undoLabel = if (toast?.hasUndo == true) stringResource(R.string.feature_analysis_impl_undo) else null
    LaunchedEffect(toast) {
        if (message != null) {
            val outcome = toastState.show(message = message, actionLabel = undoLabel)
            if (outcome == TmrToastResult.ActionPerformed) actions.onUndo() else actions.onToastDismiss()
        }
    }
}

internal const val MIN_WAIT_SECONDS = 1
internal const val MAX_WAIT_SECONDS = 3600

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
        is AnalysisUiState.Failed -> FailedContent(uiState.cause, contentPadding)
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

@Composable
private fun FailedContent(cause: FailureCause, contentPadding: PaddingValues) {
    val (title, body) = when (cause) {
        FailureCause.Generic ->
            R.string.feature_analysis_impl_error_title to stringResource(R.string.feature_analysis_impl_error_body)
        FailureCause.InProgress ->
            R.string.feature_analysis_impl_busy_title to stringResource(R.string.feature_analysis_impl_busy_body)
        is FailureCause.RateLimited -> {
            val waitSeconds = cause.retryAfterSeconds?.takeIf { it in MIN_WAIT_SECONDS..MAX_WAIT_SECONDS }
            R.string.feature_analysis_impl_rate_limited_title to
                if (waitSeconds != null) {
                    pluralStringResource(R.plurals.feature_analysis_impl_rate_limited_wait, waitSeconds, waitSeconds)
                } else {
                    stringResource(R.string.feature_analysis_impl_rate_limited_body)
                }
        }
        FailureCause.QuotaReached ->
            R.string.feature_analysis_impl_quota_title to stringResource(R.string.feature_analysis_impl_quota_body)
        FailureCause.SignInRequired ->
            R.string.feature_analysis_impl_sign_in_title to stringResource(R.string.feature_analysis_impl_sign_in_body)
    }
    MessageContent(title = stringResource(title), body = body, contentPadding = contentPadding)
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
    TmrInnerHeader(
        title = stringResource(R.string.feature_analysis_impl_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.feature_analysis_impl_back),
        trailing = result?.let {
            {
                TmrHeaderIconButton(
                    icon = TmrIcons.Share,
                    contentDescription = stringResource(R.string.feature_analysis_impl_share_my_fit),
                    onClick = onShare,
                )
            }
        },
    )
}

internal fun AnalysisUiState.Result.countOf(status: MatchStatus): Int = items.count { it.status == status }
