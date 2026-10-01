package com.hirehop.feature.applications.impl

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.ui.component.ApplicationStatusSheet
import com.hirehop.core.ui.component.applicationStatusOptions
import kotlinx.coroutines.delay
import kotlin.coroutines.coroutineContext
import kotlin.time.Clock
import kotlin.time.Instant
import com.hirehop.feature.applications.api.R as apiR

internal const val ROW_STAGGER_STEP_MS = 30
internal const val ROW_ENTER_TRANSLATE_DP = 8f

@Composable
fun ApplicationsRoute(
    onApplicationClick: (String) -> Unit,
    onNewApplicationClick: () -> Unit,
    modifier: Modifier = Modifier,
    scenario: DebugScenario = DebugScenario.defaultValue,
    credits: ApplicationCreditLine? = null,
    clock: Clock = Clock.System,
) {
    val viewModel: ApplicationsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(scenario) { viewModel.onEnter(scenario) }
    ApplicationsScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                is ApplicationsAction.ApplicationChosen -> onApplicationClick(action.id)
                ApplicationsAction.NewApplicationChosen -> onNewApplicationClick()
                else -> viewModel.onAction(action)
            }
        },
        credits = credits,
        now = clock.now(),
        modifier = modifier,
    )
}

@Composable
fun ApplicationsScreen(
    uiState: ApplicationsUiState,
    onAction: (ApplicationsAction) -> Unit,
    modifier: Modifier = Modifier,
    credits: ApplicationCreditLine? = null,
    now: Instant = Clock.System.now(),
) {
    HhScaffold(
        modifier = modifier,
        topBar = { ApplicationsTopBar(credits = credits) },
        floatingActionButton = {
            ApplicationsFab(onClick = { onAction(ApplicationsAction.NewApplicationChosen) })
        },
    ) { padding ->
        when (uiState) {
            ApplicationsUiState.Loading -> ApplicationsLoading(padding = padding)
            ApplicationsUiState.Empty -> ApplicationsEmpty(padding = padding, onAction = onAction)
            is ApplicationsUiState.Applications -> ApplicationsListContent(
                state = uiState,
                padding = padding,
                now = now,
                onAction = onAction,
            )
        }
    }
    val sheetState = (uiState as? ApplicationsUiState.Applications)?.statusSheet
    if (sheetState != null) {
        ApplicationStatusSheetHost(state = sheetState, onAction = onAction)
    }
}

@Composable
private fun ApplicationStatusSheetHost(
    state: ApplicationStatusSheetState,
    onAction: (ApplicationsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = ApplicationStatus.entries.associateWith { status -> status.label() }
    ApplicationStatusSheet(
        current = state.current,
        options = applicationStatusOptions { status -> labels.getValue(status) },
        onConfirm = { chosen -> onAction(ApplicationsAction.StatusChosen(chosen)) },
        onDismiss = { onAction(ApplicationsAction.StatusSheetDismissed) },
        saveLabel = stringResource(R.string.feature_applications_impl_sheet_save),
        cancelLabel = stringResource(R.string.feature_applications_impl_sheet_cancel),
        eyebrow = stringResource(R.string.feature_applications_impl_sheet_eyebrow),
        title = stringResource(R.string.feature_applications_impl_sheet_title),
        note = stringResource(R.string.feature_applications_impl_sheet_note),
        modifier = modifier,
    )
}

@Composable
private fun ApplicationsTopBar(
    credits: ApplicationCreditLine?,
    modifier: Modifier = Modifier,
) {
    HhTopAppBar(
        title = stringResource(apiR.string.feature_applications_api_title),
        modifier = modifier,
        actions = {
            if (credits != null) {
                ApplicationCreditChip(
                    credits = credits,
                    modifier = Modifier.padding(end = HhTheme.spacing.d16),
                )
            }
        },
    )
}

@Composable
private fun ApplicationsFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhIconButton(
        icon = HhIcons.Add,
        contentDescription = stringResource(R.string.feature_applications_impl_new),
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun ApplicationsLoading(
    padding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        HhLoadingWheel(
            contentDesc = stringResource(R.string.feature_applications_impl_loading),
        )
    }
}

@Composable
private fun ApplicationsEmpty(
    padding: PaddingValues,
    onAction: (ApplicationsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = HhTheme.spacing.d24),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_applications_impl_empty_title),
            style = HhTheme.typography.headlineSmall,
            color = HhTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.feature_applications_impl_empty_message),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        HhButton(
            onClick = { onAction(ApplicationsAction.NewApplicationChosen) },
            modifier = Modifier.padding(top = HhTheme.spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.feature_applications_impl_empty_action),
                style = HhTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun ApplicationsListContent(
    state: ApplicationsUiState.Applications,
    padding: PaddingValues,
    now: Instant,
    onAction: (ApplicationsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        if (state.isOffline) {
            HhOfflineBanner(
                message = stringResource(R.string.feature_applications_impl_offline),
                modifier = Modifier.padding(horizontal = HhTheme.spacing.d20, vertical = HhTheme.spacing.d8),
            )
        }
        ApplicationsList(
            state = state,
            now = now,
            onAction = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )
    }
}

@Composable
private fun ApplicationsList(
    state: ApplicationsUiState.Applications,
    now: Instant,
    onAction: (ApplicationsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val systemReduceMotion = hhSystemReduceMotion()
    val enterMs = HhTheme.motion.fade
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = HhTheme.spacing.d64),
    ) {
        itemsIndexed(items = state.rows, key = { _, row -> row.id }) { index, row ->
            val enter = remember(row.id) { Animatable(0f) }
            LaunchedEffect(row.id) {
                val scale = coroutineContext[MotionDurationScale.Key]?.scaleFactor ?: 1f
                if (systemReduceMotion || scale == 0f) {
                    enter.snapTo(1f)
                } else {
                    delay(index * ROW_STAGGER_STEP_MS.toLong())
                    enter.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = enterMs))
                }
            }
            ApplicationRow(
                row = row,
                now = now,
                onClick = { onAction(ApplicationsAction.ApplicationChosen(row.id)) },
                onStatusClick = { onAction(ApplicationsAction.StatusChipChosen(row.id)) },
                modifier = Modifier.graphicsLayer {
                    alpha = enter.value
                    translationY = (1f - enter.value) * ROW_ENTER_TRANSLATE_DP * density
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenEmptyPreview() {
    HhTheme(darkTheme = false) {
        ApplicationsScreen(uiState = ApplicationsUiState.Empty, onAction = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenListPreview() {
    HhTheme(darkTheme = false) {
        ApplicationsList(
            state = previewListState(),
            now = PREVIEW_INSTANT,
            onAction = {},
        )
    }
}
