package com.tailormyresume.feature.applications.impl

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.core.designsystem.component.TmrContentSwitch
import com.tailormyresume.core.designsystem.component.TmrHeaderIconButton
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrToastHost
import com.tailormyresume.core.designsystem.component.TmrToastState
import com.tailormyresume.core.designsystem.component.rememberTmrToastState
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.designsystem.theme.tmrShadow
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.ui.component.ApplicationStatusSheet
import com.tailormyresume.core.ui.component.applicationStatusOptions
import kotlin.time.Clock
import kotlin.time.Instant

private val MENU_WIDTH = 236.dp

@Composable
fun ApplicationDetailRoute(
    viewModel: ApplicationDetailViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    scenario: DebugScenario = DebugScenario.defaultValue,
    onReviewResumeClick: (String) -> Unit = {},
    onPrepQuestionsClick: () -> Unit = {},
    onCoverLetterClick: () -> Unit = {},
    onExportPreviewClick: () -> Unit = {},
    onShareExportClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val applicationId = viewModel.applicationId
    val toast = rememberTmrToastState()
    val thanks = stringResource(R.string.feature_applications_impl_report_thanks)
    LaunchedEffect(viewModel, thanks) {
        viewModel.events.collect { event ->
            when (event) {
                ApplicationDetailEvent.ReportRecorded -> toast.show(thanks)
            }
        }
    }
    LaunchedEffect(scenario) { viewModel.onEnter(scenario) }
    LaunchedEffect(uiState) {
        if (uiState == ApplicationDetailUiState.Deleted) onBackClick()
    }
    ApplicationDetailScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ApplicationWorkspaceAction.BackChosen -> onBackClick()
                ApplicationWorkspaceAction.ResumeReviewChosen -> onReviewResumeClick(applicationId)
                ApplicationWorkspaceAction.ResumePreviewChosen -> onExportPreviewClick()
                ApplicationWorkspaceAction.ResumeShareChosen -> onShareExportClick()
                ApplicationWorkspaceAction.PrepQuestionsChosen -> onPrepQuestionsClick()
                ApplicationWorkspaceAction.CoverLetterChosen -> onCoverLetterClick()
                else -> viewModel.onAction(action)
            }
        },
        now = Clock.System.now(),
        modifier = modifier,
        toast = toast,
    )
}

@Composable
fun ApplicationDetailScreen(
    uiState: ApplicationDetailUiState,
    onAction: (ApplicationWorkspaceAction) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    now: Instant = Clock.System.now(),
    toast: TmrToastState = rememberTmrToastState(),
) {
    val ready = uiState as? ApplicationDetailUiState.Ready
    TmrScreen(
        modifier = modifier,
        sheet = false,
        snackbarHost = { TmrToastHost(state = toast) },
        header = {
            TmrInnerHeader(
                title = "",
                onBack = { onAction(ApplicationWorkspaceAction.BackChosen) },
                backContentDescription = stringResource(R.string.feature_applications_impl_workspace_back),
                trailing = {
                    TmrHeaderIconButton(
                        icon = TmrIcons.More,
                        contentDescription = stringResource(R.string.feature_applications_impl_workspace_more),
                        onClick = { onAction(ApplicationWorkspaceAction.MoreChosen) },
                    )
                },
            )
        },
    ) { padding ->
        TmrContentSwitch(targetState = uiState, contentKey = { it::class }) { state ->
            when (state) {
                ApplicationDetailUiState.Loading -> WorkspaceLoading(padding = padding)
                ApplicationDetailUiState.NotFound,
                ApplicationDetailUiState.Deleted,
                -> WorkspaceMissing(padding = padding)
                is ApplicationDetailUiState.Ready -> WorkspaceContent(
                    state = state,
                    padding = padding,
                    scrollState = scrollState,
                    now = now,
                    onAction = onAction,
                )
            }
        }
    }
    if (ready == null) return
    if (ready.isMoreOpen) {
        WorkspaceOverflowMenu(
            onDismiss = { onAction(ApplicationWorkspaceAction.MoreDismissed) },
            onDelete = { onAction(ApplicationWorkspaceAction.DeleteChosen) },
        )
    }
    if (ready.statusSheet != null) {
        WorkspaceStatusSheetHost(state = ready.statusSheet, onAction = onAction)
    }
    if (ready.isDeleteDialogVisible && ready.deleteScope != null) {
        ApplicationDeleteDialog(
            jobTitle = roleOrFallback(ready.jobTitle),
            company = companyOrFallback(ready.company),
            scope = ready.deleteScope,
            onConfirm = { onAction(ApplicationWorkspaceAction.DeleteConfirmed) },
            onCancel = { onAction(ApplicationWorkspaceAction.DeleteDismissed) },
        )
    }
}

@Composable
private fun WorkspaceStatusSheetHost(
    state: ApplicationStatusSheetState,
    onAction: (ApplicationWorkspaceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = ApplicationStatus.entries.associateWith { status -> status.label() }
    ApplicationStatusSheet(
        current = state.current,
        options = applicationStatusOptions { status -> labels.getValue(status) },
        onConfirm = { chosen -> onAction(ApplicationWorkspaceAction.StatusChosen(chosen)) },
        onDismiss = { onAction(ApplicationWorkspaceAction.StatusSheetDismissed) },
        saveLabel = stringResource(R.string.feature_applications_impl_sheet_save),
        cancelLabel = stringResource(R.string.feature_applications_impl_sheet_cancel),
        eyebrow = stringResource(R.string.feature_applications_impl_sheet_eyebrow),
        title = stringResource(R.string.feature_applications_impl_sheet_title),
        note = stringResource(R.string.feature_applications_impl_sheet_note),
        modifier = modifier,
    )
}

@Composable
private fun WorkspaceOverflowMenu(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
) {
    val density = LocalDensity.current
    val gutter = with(density) { TmrTheme.spacing.gutter.roundToPx() }
    val top = with(density) {
        (WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + MENU_TOP).roundToPx()
    }
    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(x = -gutter, y = top),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = Modifier
                .width(MENU_WIDTH)
                .tmrShadow(TmrTheme.elevation.modal, TmrTheme.shapes.banner)
                .background(TmrTheme.colors.surface, TmrTheme.shapes.banner)
                .padding(vertical = TmrTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = TmrTheme.spacing.touch)
                    .clickable(role = Role.Button, onClick = onDelete)
                    .padding(horizontal = TmrTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = TmrIcons.Delete,
                    contentDescription = null,
                    tint = TmrTheme.colors.error,
                    modifier = Modifier.size(TmrTheme.spacing.xl),
                )
                Text(
                    text = stringResource(R.string.feature_applications_impl_workspace_delete),
                    style = TmrTheme.typography.bodyM,
                    color = TmrTheme.colors.error,
                )
            }
        }
    }
}

private val MENU_TOP = 72.dp

@Composable
private fun WorkspaceLoading(
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

@Composable
private fun WorkspaceMissing(
    padding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = TmrTheme.spacing.d24),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.feature_applications_impl_workspace_missing),
            style = TmrTheme.typography.bodyL,
            color = TmrTheme.colors.body,
        )
    }
}

@Composable
private fun WorkspaceContent(
    state: ApplicationDetailUiState.Ready,
    padding: PaddingValues,
    scrollState: ScrollState,
    now: Instant,
    onAction: (ApplicationWorkspaceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(
                start = TmrTheme.spacing.gutter,
                end = TmrTheme.spacing.gutter,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md - TmrTheme.spacing.xxs),
    ) {
        if (state.isOffline) {
            TmrOfflineBanner(message = stringResource(R.string.feature_applications_impl_offline))
        }
        WorkspaceIdentityCard(
            role = state.jobTitle,
            company = state.company,
            status = state.status,
            updatedAt = state.updatedAt,
            now = now,
            onStatusClick = { onAction(ApplicationWorkspaceAction.StatusChipChosen) },
        )
        WorkspaceGapSection(
            gapCounts = state.gapCounts,
            coverage = state.coverage,
            matches = state.matches,
            isExpanded = state.isGapExpanded,
            onToggle = { onAction(ApplicationWorkspaceAction.GapAnalysisToggled) },
        )
        WorkspaceResumeSection(
            resume = state.resume,
            canReview = state.reviewProgress != null,
            onPreview = { onAction(ApplicationWorkspaceAction.ResumePreviewChosen) },
            onShare = { onAction(ApplicationWorkspaceAction.ResumeShareChosen) },
            onReview = { onAction(ApplicationWorkspaceAction.ResumeReviewChosen) },
        )
        if (state.hasCoverLetter) {
            val written = state.coverLetter
            WorkspaceEntrySection(
                title = stringResource(R.string.feature_applications_impl_workspace_letter_heading),
                icon = TmrIcons.Edit,
                summary = if (written == null) {
                    stringResource(R.string.feature_applications_impl_workspace_letter_summary)
                } else {
                    pluralStringResource(
                        id = R.plurals.feature_applications_impl_workspace_letter_written,
                        count = written.wordCount,
                        written.wordCount,
                        dayMonthLabel(written.writtenAt),
                    )
                },
                actionLabel = stringResource(
                    if (written == null) {
                        R.string.feature_applications_impl_workspace_letter_open
                    } else {
                        R.string.feature_applications_impl_workspace_letter_open_written
                    },
                ),
                onClick = { onAction(ApplicationWorkspaceAction.CoverLetterChosen) },
                trailing = if (written == null) {
                    stringResource(R.string.feature_applications_impl_workspace_letter_optional)
                } else {
                    null
                },
            )
        }
        WorkspacePrepPlanSection(
            tasks = state.prepTasks,
            onToggle = { id -> onAction(ApplicationWorkspaceAction.PrepTaskToggled(id)) },
            onReport = { id -> onAction(ApplicationWorkspaceAction.PrepTaskInaccuracyReported(id)) },
        )
        if (state.prepQuestionCount > 0) {
            WorkspaceEntrySection(
                title = stringResource(R.string.feature_applications_impl_workspace_questions_heading),
                icon = TmrIcons.Chat,
                summary = pluralStringResource(
                    id = R.plurals.feature_applications_impl_workspace_questions_summary,
                    count = state.prepQuestionCount,
                    state.prepQuestionCount,
                ),
                actionLabel = stringResource(R.string.feature_applications_impl_workspace_questions_open),
                onClick = { onAction(ApplicationWorkspaceAction.PrepQuestionsChosen) },
            )
        }
        WorkspaceNotesSection(
            notes = state.notes,
            notesState = state.notesState,
            isFocused = state.isNotesFocused,
            onNotesChange = { notes -> onAction(ApplicationWorkspaceAction.NotesChanged(notes)) },
            onFocusChange = { focused -> onAction(ApplicationWorkspaceAction.NotesFocusChanged(focused)) },
        )
        WorkspaceJobDescriptionSection(
            text = state.jobDescriptionText,
            wordCount = state.jobDescriptionWordCount,
            isExpanded = state.isJobDescriptionExpanded,
            onToggle = { onAction(ApplicationWorkspaceAction.JobDescriptionToggled) },
        )
    }
}
