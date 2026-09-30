package com.hirehop.feature.applications.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus

@Composable
internal fun ApplicationDetailRoute(
    onBackClick: () -> Unit,
    onReviewResumeClick: (String) -> Unit,
    viewModel: ApplicationDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(uiState) {
        if (uiState is ApplicationDetailUiState.Deleted) onBackClick()
    }
    ApplicationDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onStatusSelected = viewModel::updateStatus,
        onNotesChange = viewModel::updateNotes,
        onReviewResumeClick = { onReviewResumeClick(viewModel.applicationId) },
        onDeleteConfirmed = viewModel::deleteApplication,
        modifier = modifier,
    )
}

@Composable
internal fun ApplicationDetailScreen(
    uiState: ApplicationDetailUiState,
    onBackClick: () -> Unit,
    onStatusSelected: (ApplicationStatus) -> Unit,
    onNotesChange: (String) -> Unit,
    onReviewResumeClick: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isDeleteDialogVisible by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = {
            ApplicationDetailTopBar(
                title = (uiState as? ApplicationDetailUiState.Success)?.application?.job?.title.orEmpty(),
                canDelete = uiState is ApplicationDetailUiState.Success,
                onBackClick = onBackClick,
                onDeleteClick = { isDeleteDialogVisible = true },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ApplicationDetailBody(
                uiState = uiState,
                onStatusSelected = onStatusSelected,
                onNotesChange = onNotesChange,
                onReviewResumeClick = onReviewResumeClick,
            )
        }
    }
    if (isDeleteDialogVisible) {
        DeleteApplicationDialog(
            onConfirm = {
                isDeleteDialogVisible = false
                onDeleteConfirmed()
            },
            onDismiss = { isDeleteDialogVisible = false },
        )
    }
}

@Composable
private fun ApplicationDetailTopBar(
    title: String,
    canDelete: Boolean,
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    HhTopAppBar(
        title = title,
        navigationIcon = HhIcons.ArrowBack,
        navigationIconContentDescription = stringResource(R.string.feature_applications_detail_back),
        onNavigationClick = onBackClick,
        actions = {
            if (canDelete) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = HhIcons.Delete,
                        contentDescription = stringResource(R.string.feature_applications_detail_delete),
                    )
                }
            }
        },
    )
}

@Composable
private fun BoxScope.ApplicationDetailBody(
    uiState: ApplicationDetailUiState,
    onStatusSelected: (ApplicationStatus) -> Unit,
    onNotesChange: (String) -> Unit,
    onReviewResumeClick: () -> Unit,
) {
    when (uiState) {
        ApplicationDetailUiState.Loading,
        ApplicationDetailUiState.Deleted,
        -> HhLoadingWheel(
            contentDesc = stringResource(R.string.feature_applications_loading),
            modifier = Modifier.align(Alignment.Center),
        )
        ApplicationDetailUiState.NotFound -> Text(
            text = stringResource(R.string.feature_applications_detail_not_found),
            modifier = Modifier.align(Alignment.Center).padding(32.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        is ApplicationDetailUiState.Success -> ApplicationDetailContent(
            state = uiState,
            onStatusSelected = onStatusSelected,
            onNotesChange = onNotesChange,
            onReviewResumeClick = onReviewResumeClick,
        )
    }
}

@Composable
private fun ApplicationDetailContent(
    state: ApplicationDetailUiState.Success,
    onStatusSelected: (ApplicationStatus) -> Unit,
    onNotesChange: (String) -> Unit,
    onReviewResumeClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = 16.dp, vertical = 8.dp)),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column {
            Text(text = state.application.job.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = state.application.job.company,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        GapSummarySection(summary = state.gapSummary)
        TailoredResumeSection(
            progress = state.reviewProgress,
            onReviewResumeClick = onReviewResumeClick,
        )
        StatusSection(selected = state.application.status, onStatusSelected = onStatusSelected)
        NotesSection(notes = state.notes, onNotesChange = onNotesChange)
    }
}

@Composable
private fun SectionHeading(@StringRes textRes: Int) {
    Text(
        text = stringResource(textRes),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun GapSummarySection(summary: GapSummary?) {
    Column {
        SectionHeading(R.string.feature_applications_detail_gap_heading)
        if (summary == null) {
            Text(stringResource(R.string.feature_applications_detail_gap_unavailable))
        } else {
            GapSummaryContent(summary)
        }
    }
}

@Composable
private fun GapSummaryContent(summary: GapSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        GapCount(summary.met, R.string.feature_applications_detail_met)
        GapCount(summary.partial, R.string.feature_applications_detail_partial)
        GapCount(summary.gap, R.string.feature_applications_detail_gaps)
    }
    if (summary.mustHaveGaps.isNotEmpty()) {
        MustHaveGaps(summary.mustHaveGaps)
    }
}

@Composable
private fun GapCount(count: Int, @StringRes labelRes: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MustHaveGaps(gaps: List<String>) {
    Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.feature_applications_detail_must_have_gaps),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.error,
        )
        gaps.forEach { gap -> Text(text = "• $gap", style = MaterialTheme.typography.bodyMedium) }
    }
}

@Composable
private fun TailoredResumeSection(
    progress: ReviewProgress?,
    onReviewResumeClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeading(R.string.feature_applications_detail_resume_heading)
        Text(text = progress.describe(), style = MaterialTheme.typography.bodyMedium)
        HhButton(
            onClick = onReviewResumeClick,
            enabled = progress != null,
            text = { Text(stringResource(R.string.feature_applications_detail_review_resume)) },
        )
    }
}

@Composable
private fun ReviewProgress?.describe(): String = when {
    this == null -> stringResource(R.string.feature_applications_detail_resume_unavailable)
    total == 0 -> stringResource(R.string.feature_applications_detail_resume_no_changes)
    else -> stringResource(R.string.feature_applications_detail_resume_progress, reviewed, total)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusSection(
    selected: ApplicationStatus,
    onStatusSelected: (ApplicationStatus) -> Unit,
) {
    Column {
        SectionHeading(R.string.feature_applications_detail_status_heading)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ApplicationStatus.entries.forEach { status ->
                FilterChip(
                    selected = status == selected,
                    onClick = { onStatusSelected(status) },
                    label = { Text(stringResource(status.labelRes())) },
                )
            }
        }
    }
}

@Composable
private fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit,
) {
    Column {
        SectionHeading(R.string.feature_applications_detail_notes_heading)
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.feature_applications_detail_notes_label)) },
            minLines = 3,
        )
    }
}

@Composable
private fun DeleteApplicationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_applications_detail_delete_title)) },
        text = { Text(stringResource(R.string.feature_applications_detail_delete_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.feature_applications_detail_delete_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_applications_detail_delete_cancel))
            }
        },
    )
}

@Composable
private fun PreviewDetail(uiState: ApplicationDetailUiState) {
    HhTheme {
        ApplicationDetailScreen(
            uiState = uiState,
            onBackClick = {},
            onStatusSelected = {},
            onNotesChange = {},
            onReviewResumeClick = {},
            onDeleteConfirmed = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationDetailSuccessPreview() {
    val application = previewApplication()
    PreviewDetail(
        ApplicationDetailUiState.Success(
            application = application,
            notes = application.notes,
            gapSummary = application.gapSummaryOrNull(),
            reviewProgress = application.reviewProgressOrNull(),
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ApplicationDetailNoAnalysisPreview() {
    val application = previewApplication().copy(gapAnalysis = null, tailoredResume = null)
    PreviewDetail(
        ApplicationDetailUiState.Success(
            application = application,
            notes = "",
            gapSummary = null,
            reviewProgress = null,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ApplicationDetailNotFoundPreview() {
    PreviewDetail(ApplicationDetailUiState.NotFound)
}
