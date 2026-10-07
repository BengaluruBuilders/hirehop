package com.hirehop.feature.applications.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhCollapsingHomeHeader
import com.hirehop.core.designsystem.component.HhContentSwitch
import com.hirehop.core.designsystem.component.HhHeaderCollapseState
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.HhToastResult
import com.hirehop.core.designsystem.component.HhToastState
import com.hirehop.core.designsystem.component.hhListEnter
import com.hirehop.core.designsystem.component.rememberHhHeaderCollapseState
import com.hirehop.core.designsystem.component.rememberHhListEnterState
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.DebugScenario
import com.hirehop.core.ui.component.ApplicationStatusSheet
import com.hirehop.core.ui.component.applicationStatusOptions
import kotlin.time.Clock
import kotlin.time.Instant
import com.hirehop.feature.applications.api.R as apiR

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
    collapse: HhHeaderCollapseState = rememberHhHeaderCollapseState(),
    now: Instant = Clock.System.now(),
) {
    val expanded = remember { HhHeaderCollapseState() }
    val message = (uiState as? ApplicationsUiState.Applications)?.message
    val toast = rememberHhToastState()
    ApplicationStatusToastEffect(message = message, toast = toast, onAction = onAction)
    HhScreen(
        modifier = modifier,
        header = {
            ApplicationsHeaderBar(
                header = uiState.header,
                collapse = if (uiState is ApplicationsUiState.Applications) collapse else expanded,
                onAction = onAction,
            )
        },
        snackbarHost = { HhToastHost(state = toast) },
    ) { padding ->
        HhContentSwitch(targetState = uiState, contentKey = { it::class }) { state ->
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
    collapse: HhHeaderCollapseState,
    onAction: (ApplicationsAction) -> Unit,
) {
    val name = header.firstName
    HhCollapsingHomeHeader(
        collapse = collapse,
        title = stringResource(apiR.string.feature_applications_api_title),
        greeting = if (name == null) {
            stringResource(R.string.feature_applications_impl_greeting_anonymous)
        } else {
            stringResource(R.string.feature_applications_impl_greeting, name)
        },
        headline = stringResource(R.string.feature_applications_impl_headline),
        trailing = {
            header.credits?.let { credits ->
                CreditsAction(credits = credits, onClick = { onAction(ApplicationsAction.CreditsChosen) })
            }
        },
        action = {
            HhPrimaryButton(
                label = stringResource(R.string.feature_applications_impl_paste_job),
                onClick = { onAction(ApplicationsAction.PasteJobChosen) },
                trailingIcon = HhIcons.ArrowForward,
            )
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
            .heightIn(min = HhTheme.spacing.touch)
            .clip(HhTheme.shapes.pill)
            .clickable(onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = pluralStringResource(R.plurals.feature_applications_impl_credits_pill, credits, credits),
            style = HhTheme.typography.labelL,
            color = HhTheme.colors.onHeaderControl,
            modifier = Modifier
                .clip(HhTheme.shapes.pill)
                .background(HhTheme.colors.headerControl)
                .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.sm),
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
        HhLoadingWheel(contentDesc = stringResource(R.string.feature_applications_impl_loading))
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
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        itemVerticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = stringResource(R.string.feature_applications_impl_list_heading),
            style = HhTheme.typography.titleL,
            color = HhTheme.colors.onSurface,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = count.toString(),
                style = HhTheme.typography.numeralM.copy(
                    fontSize = HhTheme.typography.labelL.fontSize,
                    lineHeight = HhTheme.typography.labelL.lineHeight,
                ),
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_applications_impl_list_order),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
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
            .padding(horizontal = HhTheme.spacing.gutter),
    ) {
        HhCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                Box(
                    modifier = Modifier
                        .size(EMPTY_TILE)
                        .clip(EMPTY_TILE_SHAPE)
                        .background(HhTheme.colors.primaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = HhIcons.Applications,
                        contentDescription = null,
                        tint = HhTheme.colors.onSurface,
                        modifier = Modifier.size(HhTheme.spacing.xl),
                    )
                }
                Text(
                    text = stringResource(R.string.feature_applications_impl_empty_title),
                    style = HhTheme.typography.titleM,
                    color = HhTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.feature_applications_impl_empty_message),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.onSurfaceVariant,
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
    val listEnter = rememberHhListEnterState()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(
            start = HhTheme.spacing.gutter,
            top = padding.calculateTopPadding(),
            end = HhTheme.spacing.gutter,
            bottom = padding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md - HhTheme.spacing.xxs),
    ) {
        if (state.isOffline) {
            item(key = OFFLINE_ITEM_KEY) {
                HhOfflineBanner(message = stringResource(R.string.feature_applications_impl_offline))
            }
        }
        item(key = HEADING_ITEM_KEY) { ListHeading(count = state.rows.size) }
        itemsIndexed(items = state.rows, key = { _, row -> row.id }) { index, row ->
            ApplicationRow(
                row = row,
                now = now,
                onClick = { onAction(ApplicationsAction.ApplicationChosen(row.id)) },
                onStatusClick = { onAction(ApplicationsAction.StatusChipChosen(row.id)) },
                modifier = Modifier.hhListEnter(state = listEnter, index = index),
            )
        }
    }
}

@Composable
private fun ApplicationStatusToastEffect(
    message: ApplicationStatusMessage?,
    toast: HhToastState,
    onAction: (ApplicationsAction) -> Unit,
) {
    val text = message?.let { stringResource(R.string.feature_applications_impl_status_saved_message, it.status.label()) }
    val undoLabel = stringResource(R.string.feature_applications_impl_status_undo)
    LaunchedEffect(message) {
        if (message == null || text == null) return@LaunchedEffect
        val result = toast.show(message = text, actionLabel = if (message.canUndo) undoLabel else null)
        onAction(
            if (result == HhToastResult.ActionPerformed) {
                ApplicationsAction.StatusUndoChosen
            } else {
                ApplicationsAction.MessageDismissed
            },
        )
    }
}

private const val OFFLINE_ITEM_KEY = "offline"
private const val HEADING_ITEM_KEY = "heading"

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenEmptyPreview() {
    HhTheme(darkTheme = false) {
        ApplicationsScreen(
            uiState = ApplicationsUiState.Empty(ApplicationsHeader(firstName = "Priya", credits = 4)),
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenListPreview() {
    HhTheme(darkTheme = false) {
        ApplicationsScreen(uiState = previewListState(), onAction = {}, now = PREVIEW_INSTANT)
    }
}
