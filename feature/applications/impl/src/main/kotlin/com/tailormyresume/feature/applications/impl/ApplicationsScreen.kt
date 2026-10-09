package com.tailormyresume.feature.applications.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrCollapsingHomeHeader
import com.tailormyresume.core.designsystem.component.TmrContentSwitch
import com.tailormyresume.core.designsystem.component.TmrHeaderCollapseState
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrToastHost
import com.tailormyresume.core.designsystem.component.TmrToastResult
import com.tailormyresume.core.designsystem.component.TmrToastState
import com.tailormyresume.core.designsystem.component.rememberTmrHeaderCollapseState
import com.tailormyresume.core.designsystem.component.rememberTmrListEnterState
import com.tailormyresume.core.designsystem.component.rememberTmrToastState
import com.tailormyresume.core.designsystem.component.tmrListEnter
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.ui.component.ApplicationStatusSheet
import com.tailormyresume.core.ui.component.applicationStatusOptionsFor
import kotlin.time.Clock
import kotlin.time.Instant
import com.tailormyresume.feature.applications.api.R as apiR

private val PASTE_JOB_HEIGHT = 56.dp
private val EMPTY_TILE = 44.dp
private val EMPTY_TILE_SHAPE = RoundedCornerShape(14.dp)

@Composable
fun ApplicationsRoute(
    onApplicationClick: (String) -> Unit,
    onPasteJobClick: () -> Unit,
    onCreditsClick: () -> Unit,
    modifier: Modifier = Modifier,
    scenario: DebugScenario = DebugScenario.defaultValue,
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
                ApplicationsAction.PasteJobChosen -> onPasteJobClick()
                ApplicationsAction.CreditsChosen -> onCreditsClick()
                else -> viewModel.onAction(action)
            }
        },
        now = clock.now(),
        modifier = modifier,
    )
}

@Composable
fun ApplicationsScreen(
    uiState: ApplicationsUiState,
    onAction: (ApplicationsAction) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    collapse: TmrHeaderCollapseState = rememberTmrHeaderCollapseState(),
    now: Instant = Clock.System.now(),
) {
    val expanded = remember { TmrHeaderCollapseState() }
    val message = (uiState as? ApplicationsUiState.Applications)?.message
    val toast = rememberTmrToastState()
    ApplicationStatusToastEffect(message = message, toast = toast, onAction = onAction)
    TmrScreen(
        modifier = modifier,
        header = {
            ApplicationsHeaderBar(
                header = uiState.header,
                collapse = if (uiState is ApplicationsUiState.Applications) collapse else expanded,
                onAction = onAction,
            )
        },
        snackbarHost = { TmrToastHost(state = toast) },
    ) { padding ->
        TmrContentSwitch(targetState = uiState, contentKey = { it::class }) { state ->
            when (state) {
                is ApplicationsUiState.Loading -> ApplicationsLoading(padding = padding)
                is ApplicationsUiState.Empty -> ApplicationsEmpty(padding = padding, onAction = onAction)
                is ApplicationsUiState.Applications -> ApplicationsListContent(
                    state = state,
                    padding = padding,
                    listState = listState,
                    now = now,
                    onAction = onAction,
                    modifier = Modifier.nestedScroll(collapse.connection),
                )
            }
        }
    }
    val sheetState = (uiState as? ApplicationsUiState.Applications)?.statusSheet
    if (sheetState != null) {
        ApplicationStatusSheetHost(state = sheetState, onAction = onAction)
    }
}

@Composable
private fun ApplicationsHeaderBar(
    header: ApplicationsHeader,
    collapse: TmrHeaderCollapseState,
    onAction: (ApplicationsAction) -> Unit,
) {
    val name = header.firstName
    val compact by remember(collapse) { derivedStateOf { collapse.fraction >= COMPACT_FRACTION } }
    TmrCollapsingHomeHeader(
        collapse = collapse,
        title = stringResource(apiR.string.feature_applications_api_title),
        greeting = if (name == null) {
            stringResource(R.string.feature_applications_impl_greeting_anonymous)
        } else {
            stringResource(R.string.feature_applications_impl_greeting, name)
        },
        headline = stringResource(R.string.feature_applications_impl_headline),
        trailing = {
            if (compact) {
                TmrPrimaryButton(
                    label = stringResource(R.string.feature_applications_impl_paste_job),
                    onClick = { onAction(ApplicationsAction.PasteJobChosen) },
                    trailingIcon = TmrIcons.ArrowForward,
                    size = TmrButtonSize.Compact,
                )
            } else {
                header.credits?.let { credits ->
                    CreditsAction(credits = credits, onClick = { onAction(ApplicationsAction.CreditsChosen) })
                }
            }
        },
        action = {
            if (compact) {
                Spacer(Modifier.height(PASTE_JOB_HEIGHT))
            } else {
                TmrPrimaryButton(
                    label = stringResource(R.string.feature_applications_impl_paste_job),
                    onClick = { onAction(ApplicationsAction.PasteJobChosen) },
                    trailingIcon = TmrIcons.ArrowForward,
                )
            }
        },
    )
}

@Composable
private fun CreditsAction(
    credits: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = pluralStringResource(
        id = R.plurals.feature_applications_impl_credits_description,
        count = credits,
        credits,
    )
    Box(
        modifier = modifier
            .heightIn(min = TmrTheme.spacing.touch)
            .clip(TmrTheme.shapes.pill)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = pluralStringResource(R.plurals.feature_applications_impl_credits_pill, credits, credits),
            style = TmrTheme.typography.labelL,
            color = TmrTheme.colors.onHeaderControl,
            modifier = Modifier
                .clip(TmrTheme.shapes.pill)
                .background(TmrTheme.colors.headerControl)
                .padding(horizontal = TmrTheme.spacing.md, vertical = TmrTheme.spacing.sm),
        )
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
        options = applicationStatusOptionsFor(state.current) { status -> labels.getValue(status) },
        onConfirm = { chosen -> onAction(ApplicationsAction.StatusChosen(chosen)) },
        onDismiss = { onAction(ApplicationsAction.StatusSheetDismissed) },
        saveLabel = stringResource(R.string.feature_applications_impl_sheet_save),
        title = stringResource(R.string.feature_applications_impl_sheet_title),
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
        TmrLoadingWheel(contentDesc = stringResource(R.string.feature_applications_impl_loading))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListHeading(
    count: Int,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
        itemVerticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = stringResource(R.string.feature_applications_impl_list_heading),
            style = TmrTheme.typography.titleL,
            color = TmrTheme.colors.onSurface,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = count.toString(),
                style = TmrTheme.typography.numeralM.copy(
                    fontSize = TmrTheme.typography.labelL.fontSize,
                    lineHeight = TmrTheme.typography.labelL.lineHeight,
                ),
                color = TmrTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_applications_impl_list_order),
                style = TmrTheme.typography.labelM,
                color = TmrTheme.colors.onSurfaceVariant,
            )
        }
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
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = TmrTheme.spacing.gutter),
    ) {
        TmrCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
                Box(
                    modifier = Modifier
                        .size(EMPTY_TILE)
                        .clip(EMPTY_TILE_SHAPE)
                        .background(TmrTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = TmrIcons.Applications,
                        contentDescription = null,
                        tint = TmrTheme.colors.onSurface,
                        modifier = Modifier.size(TmrTheme.spacing.xl),
                    )
                }
                Text(
                    text = stringResource(R.string.feature_applications_impl_empty_title),
                    style = TmrTheme.typography.titleM,
                    color = TmrTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.feature_applications_impl_empty_message),
                    style = TmrTheme.typography.bodyM,
                    color = TmrTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ApplicationsListContent(
    state: ApplicationsUiState.Applications,
    padding: PaddingValues,
    listState: LazyListState,
    now: Instant,
    onAction: (ApplicationsAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listEnter = rememberTmrListEnterState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = TmrTheme.spacing.gutter,
            top = padding.calculateTopPadding(),
            end = TmrTheme.spacing.gutter,
            bottom = padding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md - TmrTheme.spacing.xxs),
    ) {
        if (state.isOffline) {
            item(key = OFFLINE_ITEM_KEY) {
                TmrOfflineBanner(message = stringResource(R.string.feature_applications_impl_offline))
            }
        }
        item(key = HEADING_ITEM_KEY) { ListHeading(count = state.rows.size) }
        itemsIndexed(items = state.rows, key = { _, row -> row.id }) { index, row ->
            ApplicationRow(
                row = row,
                now = now,
                onClick = { onAction(ApplicationsAction.ApplicationChosen(row.id)) },
                onStatusClick = { onAction(ApplicationsAction.StatusChipChosen(row.id)) },
                modifier = Modifier.tmrListEnter(state = listEnter, index = index),
            )
        }
    }
}

@Composable
private fun ApplicationStatusToastEffect(
    message: ApplicationStatusMessage?,
    toast: TmrToastState,
    onAction: (ApplicationsAction) -> Unit,
) {
    val text = message?.let { stringResource(R.string.feature_applications_impl_status_saved_message, it.status.label()) }
    val undoLabel = stringResource(R.string.feature_applications_impl_status_undo)
    LaunchedEffect(message) {
        if (message == null || text == null) return@LaunchedEffect
        val result = toast.show(message = text, actionLabel = if (message.canUndo) undoLabel else null)
        onAction(
            if (result == TmrToastResult.ActionPerformed) {
                ApplicationsAction.StatusUndoChosen
            } else {
                ApplicationsAction.MessageDismissed
            },
        )
    }
}

private const val COMPACT_FRACTION = 0.5f
private const val OFFLINE_ITEM_KEY = "offline"
private const val HEADING_ITEM_KEY = "heading"

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenEmptyPreview() {
    TmrTheme(darkTheme = false) {
        ApplicationsScreen(
            uiState = ApplicationsUiState.Empty(ApplicationsHeader(firstName = "Priya", credits = 4)),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenListPreview() {
    TmrTheme(darkTheme = false) {
        ApplicationsScreen(uiState = previewListState(), onAction = {}, now = PREVIEW_INSTANT)
    }
}
