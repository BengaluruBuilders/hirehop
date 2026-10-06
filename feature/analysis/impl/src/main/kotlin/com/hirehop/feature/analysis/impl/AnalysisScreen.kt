package com.hirehop.feature.analysis.impl

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhContentSwitch
import com.hirehop.core.designsystem.component.HhDecoration
import com.hirehop.core.designsystem.component.HhDecorationKind
import com.hirehop.core.designsystem.component.HhHeaderIconButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.HhToastResult
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
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

private val HeaderTopPadding = 36.dp
private val HeaderBottomPadding = 24.dp
private val HeaderGap = 16.dp
private val MonogramSize = 56.dp
private val ChipGap = 6.dp
private val ChipHeight = 32.dp
private val PillHeight = 48.dp
private val PillIconSize = 18.dp
private val ShareIconSize = 18.dp
private val RingTop = 104.dp
private val RingEnd = 22.dp
private val SquiggleTop = 150.dp
private val SquiggleEnd = 86.dp

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
                    uiState = uiState,
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
private fun AnalysisUiState.headerTitle(): String = when {
    job.title.isNotBlank() -> job.title
    this is AnalysisUiState.Result -> stringResource(R.string.feature_analysis_impl_role_not_set)
    else -> stringResource(R.string.feature_analysis_impl_title_fallback)
}

@Composable
private fun AnalysisUiState.headerSubtitle(): String? = when {
    job.company.isNotBlank() -> job.company
    this is AnalysisUiState.Result -> stringResource(R.string.feature_analysis_impl_company_not_set)
    else -> null
}

@Composable
private fun AnalysisHeader(
    uiState: AnalysisUiState,
    result: AnalysisUiState.Result?,
    onBack: () -> Unit,
    onShare: () -> Unit,
) {
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val shape = HhTheme.shapes.heroBottom
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(HhTheme.colors.header),
    ) {
        HeaderDecorations()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = HhTheme.spacing.gutter,
                    end = HhTheme.spacing.gutter,
                    top = statusTop + HeaderTopPadding,
                    bottom = HeaderBottomPadding,
                ),
            verticalArrangement = Arrangement.spacedBy(HeaderGap),
        ) {
            HeaderTopRow(result = result, onBack = onBack, onShare = onShare)
            JobIdentityRow(uiState = uiState)
            if (result != null) {
                ResultSummaryChips(result)
            }
        }
    }
}

@Composable
private fun BoxScope.HeaderDecorations() {
    val colors = HhTheme.colors
    HhDecoration(
        kind = HhDecorationKind.Ring,
        color = colors.special,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = RingTop, end = RingEnd),
    )
    HhDecoration(
        kind = HhDecorationKind.Squiggle,
        color = colors.headerShape,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(top = SquiggleTop, end = SquiggleEnd),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HeaderTopRow(
    result: AnalysisUiState.Result?,
    onBack: () -> Unit,
    onShare: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhHeaderIconButton(
            icon = HhIcons.ArrowBack,
            contentDescription = stringResource(R.string.feature_analysis_impl_back),
            onClick = onBack,
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (result != null) {
                ShareFitPill(onClick = onShare)
            }
        }
    }
}

@Composable
private fun ShareFitPill(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.brandPressed, HhTheme.shapes.pill)
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = PillHeight)
                .padding(start = 14.dp, end = HhTheme.spacing.gutter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            Icon(
                imageVector = HhIcons.Share,
                contentDescription = null,
                tint = HhTheme.colors.onHeader,
                modifier = Modifier.size(ShareIconSize),
            )
            Text(
                text = stringResource(R.string.feature_analysis_impl_share_my_fit),
                style = HhTheme.typography.titleS,
                color = HhTheme.colors.onHeader,
            )
        }
    }
}

@Composable
private fun JobIdentityRow(uiState: AnalysisUiState) {
    val title = uiState.headerTitle()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HeaderMonogram(name = uiState.job.company.ifBlank { title })
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(text = title, style = HhTheme.typography.headlineL, color = HhTheme.colors.onHeader)
            uiState.headerSubtitle()?.let { subtitle ->
                Text(text = subtitle, style = HhTheme.typography.labelL, color = HhTheme.colors.onHeaderVariant)
            }
        }
    }
}

@Composable
private fun HeaderMonogram(name: String) {
    Box(
        modifier = Modifier
            .defaultMinSize(MonogramSize, MonogramSize)
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.onHeader),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = name.initials(), style = HhTheme.typography.titleM, color = HhTheme.colors.brand)
    }
}

private fun String.initials(): String =
    trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).joinToString("") { it.take(1) }.uppercase()

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ResultSummaryChips(result: AnalysisUiState.Result) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(ChipGap),
        verticalArrangement = Arrangement.spacedBy(ChipGap),
    ) {
        HhStatusChip(
            kind = HhStatusKind.Met,
            label = stringResource(R.string.feature_analysis_impl_summary_met, result.countOf(MatchStatus.MET)),
            onHero = true,
        )
        HhStatusChip(
            kind = HhStatusKind.Partial,
            label = stringResource(
                R.string.feature_analysis_impl_summary_partial,
                result.countOf(MatchStatus.PARTIAL),
            ),
            onHero = true,
        )
        HhStatusChip(
            kind = HhStatusKind.Gap,
            label = stringResource(R.string.feature_analysis_impl_summary_gap, result.gapCount),
            onHero = true,
        )
    }
}

private fun AnalysisUiState.Result.countOf(status: MatchStatus): Int = items.count { it.status == status }
