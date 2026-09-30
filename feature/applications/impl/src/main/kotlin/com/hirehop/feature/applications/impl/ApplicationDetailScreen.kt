package com.hirehop.feature.applications.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.ui.component.ApplicationStatusSheet
import com.hirehop.core.ui.component.applicationStatusOptions
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun ApplicationDetailRoute(
    onBackClick: () -> Unit,
    onReviewResumeClick: (String) -> Unit,
    viewModel: ApplicationDetailViewModel,
    modifier: Modifier = Modifier,
    onPrepQuestionsClick: () -> Unit = {},
    onCoverLetterClick: () -> Unit = {},
    onExportPreviewClick: () -> Unit = {},
    onShareResumeClick: (String?) -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val applicationId = viewModel.applicationId
    ApplicationDetailScreen(
        uiState = uiState,
        onAction = { action ->
            when (action) {
                ApplicationWorkspaceAction.BackChosen -> onBackClick()
                ApplicationWorkspaceAction.ResumeReviewChosen -> onReviewResumeClick(applicationId)
                ApplicationWorkspaceAction.ResumePreviewChosen -> onExportPreviewClick()
                is ApplicationWorkspaceAction.ResumeShareChosen -> onShareResumeClick(action.fileName)
                ApplicationWorkspaceAction.PrepQuestionsChosen -> onPrepQuestionsClick()
                ApplicationWorkspaceAction.CoverLetterChosen -> onCoverLetterClick()
                else -> viewModel.onAction(action)
            }
        },
        now = Clock.System.now(),
        modifier = modifier,
    )
}

@Composable
fun ApplicationDetailScreen(
    uiState: ApplicationDetailUiState,
    onAction: (ApplicationWorkspaceAction) -> Unit,
    modifier: Modifier = Modifier,
    now: Instant = Clock.System.now(),
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            WorkspaceTopBar(
                jobTitle = (uiState as? ApplicationDetailUiState.Ready)?.jobTitle,
                onBack = { onAction(ApplicationWorkspaceAction.BackChosen) },
                onMore = { onAction(ApplicationWorkspaceAction.MoreChosen) },
            )
        },
    ) { padding ->
        when (uiState) {
            ApplicationDetailUiState.Loading -> WorkspaceLoading(padding = padding)
            ApplicationDetailUiState.NotFound,
            ApplicationDetailUiState.Deleted,
            -> WorkspaceMissing(padding = padding)
            is ApplicationDetailUiState.Ready -> WorkspaceContent(
                state = uiState,
                padding = padding,
                now = now,
                onAction = onAction,
            )
        }
    }
    val ready = uiState as? ApplicationDetailUiState.Ready
    if (ready?.statusSheet != null) {
        WorkspaceStatusSheetHost(state = ready.statusSheet, onAction = onAction)
    }
    if (ready != null && ready.isDeleteDialogVisible && ready.deleteScope != null) {
        ApplicationDeleteDialog(
            jobTitle = ready.jobTitle,
            company = ready.company,
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
private fun WorkspaceTopBar(
    jobTitle: String?,
    onBack: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhTopAppBar(
        title = jobTitle.orEmpty(),
        modifier = modifier,
        navigationIcon = HhIcons.ArrowBack,
        navigationIconContentDescription = stringResource(R.string.feature_applications_impl_workspace_back),
        onNavigationClick = onBack,
        actions = {
            HhIconButton(
                icon = HhIcons.More,
                contentDescription = stringResource(R.string.feature_applications_impl_workspace_more),
                onClick = onMore,
                modifier = Modifier.padding(end = HhTheme.spacing.d16),
            )
        },
    )
}

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
        HhLoadingWheel(contentDesc = stringResource(R.string.feature_applications_impl_loading))
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
            .padding(horizontal = HhTheme.spacing.d24),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.feature_applications_impl_workspace_missing),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun WorkspaceContent(
    state: ApplicationDetailUiState.Ready,
    padding: PaddingValues,
    now: Instant,
    onAction: (ApplicationWorkspaceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d20, vertical = HhTheme.spacing.d12),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12),
    ) {
        if (state.isOffline) {
            HhOfflineBanner(message = stringResource(R.string.feature_applications_impl_offline))
        }
        WorkspaceHeader(
            state = state,
            now = now,
            onStatusClick = { onAction(ApplicationWorkspaceAction.StatusChipChosen) },
        )
        WorkspaceGapSection(gapCounts = state.gapCounts, coverage = state.coverage)
        WorkspaceResumeSection(
            state = state,
            onPreview = { onAction(ApplicationWorkspaceAction.ResumePreviewChosen) },
            onReview = { onAction(ApplicationWorkspaceAction.ResumeReviewChosen) },
            onShare = { fileName -> onAction(ApplicationWorkspaceAction.ResumeShareChosen(fileName)) },
        )
        WorkspacePrepTasksSection(
            tasks = state.prepTasks,
            prepQuestionCount = state.prepQuestionCount,
            hasCoverLetter = state.hasCoverLetter,
            onToggle = { id -> onAction(ApplicationWorkspaceAction.PrepTaskToggled(id)) },
            onOverflow = { id -> onAction(ApplicationWorkspaceAction.PrepTaskOverflowToggled(id)) },
            onReport = { id -> onAction(ApplicationWorkspaceAction.PrepTaskInaccuracyReported(id)) },
            onQuestions = { onAction(ApplicationWorkspaceAction.PrepQuestionsChosen) },
            onCoverLetter = { onAction(ApplicationWorkspaceAction.CoverLetterChosen) },
        )
        WorkspaceJobDescriptionSection(
            text = state.jobDescriptionText,
            isExpanded = state.isJobDescriptionExpanded,
            onToggle = { onAction(ApplicationWorkspaceAction.JobDescriptionToggled) },
        )
        WorkspaceNotesSection(
            notes = state.notes,
            notesState = state.notesState,
            isFocused = state.isNotesFocused,
            onNotesChange = { notes -> onAction(ApplicationWorkspaceAction.NotesChanged(notes)) },
            onFocusChange = { focused -> onAction(ApplicationWorkspaceAction.NotesFocusChanged(focused)) },
        )
        if (state.isMoreOpen) {
            WorkspaceOverflow(
                onDismiss = { onAction(ApplicationWorkspaceAction.MoreDismissed) },
                onDelete = { onAction(ApplicationWorkspaceAction.DeleteChosen) },
            )
        }
    }
}

@Composable
private fun WorkspaceHeader(
    state: ApplicationDetailUiState.Ready,
    now: Instant,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        Text(
            text = state.jobTitle,
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = state.company,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        WorkspaceStatusChip(status = state.status, onClick = onStatusClick)
        Text(
            text = updatedSentence(updatedAt = state.updatedAt, now = now),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun WorkspaceResumeSection(
    state: ApplicationDetailUiState.Ready,
    onPreview: () -> Unit,
    onReview: () -> Unit,
    onShare: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    HhSectionCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feature_applications_impl_workspace_resume_heading),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        when (val resume = state.resume) {
            WorkspaceResume.Absent -> Text(
                text = stringResource(R.string.feature_applications_impl_workspace_resume_absent),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            WorkspaceResume.NotExported -> {
                Text(
                    text = stringResource(R.string.feature_applications_impl_workspace_resume_not_exported),
                    style = HhTheme.typography.bodyMedium,
                    color = HhTheme.colors.onSurfaceVariant,
                )
                WorkspaceActionRow(
                    label = stringResource(R.string.feature_applications_impl_workspace_resume_preview),
                    onClick = onPreview,
                )
            }
            is WorkspaceResume.Exported -> {
                Text(
                    text = resume.fileName,
                    style = HhTheme.typography.bodyMedium,
                    color = HhTheme.colors.onSurfaceVariant,
                )
                WorkspaceActionRow(
                    label = stringResource(R.string.feature_applications_impl_workspace_resume_share_again),
                    onClick = { onShare(resume.fileName) },
                )
            }
        }
        if (state.reviewProgress != null) {
            WorkspaceActionRow(
                label = stringResource(R.string.feature_applications_impl_workspace_resume_review),
                onClick = onReview,
            )
        }
    }
}

@Composable
private fun WorkspacePrepTasksSection(
    tasks: List<WorkspacePrepTask>,
    prepQuestionCount: Int,
    hasCoverLetter: Boolean,
    onToggle: (String) -> Unit,
    onOverflow: (String) -> Unit,
    onReport: (String) -> Unit,
    onQuestions: () -> Unit,
    onCoverLetter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhSectionCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feature_applications_impl_workspace_tasks_heading),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        if (tasks.isEmpty()) {
            Text(
                text = stringResource(R.string.feature_applications_impl_workspace_tasks_none),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        } else {
            tasks.forEach { task ->
                WorkspacePrepTaskRow(
                    task = task,
                    onToggle = { onToggle(task.id) },
                    onOverflowToggle = { onOverflow(task.id) },
                    onReport = { onReport(task.id) },
                )
            }
        }
        if (prepQuestionCount > 0) {
            WorkspaceActionRow(
                label = stringResource(R.string.feature_applications_impl_workspace_questions_open),
                onClick = onQuestions,
            )
        }
        if (hasCoverLetter) {
            WorkspaceActionRow(
                label = stringResource(R.string.feature_applications_impl_workspace_letter_open),
                onClick = onCoverLetter,
            )
        }
    }
}

@Composable
private fun WorkspaceNotesSection(
    notes: String,
    notesState: WorkspaceNotesState,
    isFocused: Boolean,
    onNotesChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusLabel = when (notesState) {
        WorkspaceNotesState.Idle -> stringResource(R.string.feature_applications_impl_workspace_notes_saved)
        WorkspaceNotesState.Saving -> stringResource(R.string.feature_applications_impl_workspace_notes_saving)
        WorkspaceNotesState.SavedJustNow -> stringResource(R.string.feature_applications_impl_workspace_notes_saved_just_now)
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isFocused) {
        if (isFocused) focusRequester.requestFocus()
    }
    HhSectionCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feature_applications_impl_workspace_notes_heading),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        HhTextField(
            value = notes,
            onValueChange = onNotesChange,
            label = stringResource(R.string.feature_applications_impl_workspace_notes_label),
            placeholder = stringResource(R.string.feature_applications_impl_workspace_notes_placeholder),
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { change -> onFocusChange(change.isFocused) },
        )
        Text(
            text = statusLabel,
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun WorkspaceOverflow(
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhSectionCard(modifier = modifier.fillMaxWidth()) {
        HhIconButton(
            icon = HhIcons.Close,
            contentDescription = stringResource(R.string.feature_applications_impl_sheet_cancel),
            onClick = onDismiss,
        )
        HhDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48)
                .clickable(onClick = onDelete)
                .padding(vertical = HhTheme.spacing.d12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.feature_applications_impl_workspace_delete),
                style = HhTheme.typography.bodyLarge,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun WorkspaceActionRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HhDivider()
        Text(
            text = label,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48)
                .clickable(onClick = onClick)
                .padding(vertical = HhTheme.spacing.d12),
        )
    }
}
