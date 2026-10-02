package com.hirehop.feature.applications.impl

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhCheckbox
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhExpandable
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.ExportFormat
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.ui.ApplicationStatusKindMapper
import com.hirehop.core.ui.MatchStatusKindMapper
import kotlin.time.Instant

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WorkspaceStatusCard(
    status: ApplicationStatus,
    updatedAt: Instant,
    now: Instant,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = status.label()
    val description = stringResource(R.string.feature_applications_impl_status_chip_description, label)
    HhHeroCard(
        modifier = modifier,
        contentPadding = PaddingValues(HhTheme.spacing.md - HhTheme.spacing.xxs),
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .heightIn(min = HhTheme.spacing.touch)
                    .clickable(role = Role.Button, onClick = onStatusClick)
                    .semantics { contentDescription = description },
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HhApplicationStatusChip(
                    kind = ApplicationStatusKindMapper().kindOf(status),
                    label = label,
                )
                Icon(
                    imageVector = HhIcons.ExpandMore,
                    contentDescription = null,
                    tint = HhTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(HhTheme.spacing.lg),
                )
            }
            Text(
                text = updatedSentence(updatedAt = updatedAt, now = now),
                style = HhTheme.typography.bodyS,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WorkspaceSection(
    title: String,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    content: @Composable () -> Unit,
) {
    HhCard(modifier = modifier) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            itemVerticalAlignment = Alignment.Bottom,
        ) {
            Text(text = title, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
            if (trailing != null) {
                Text(text = trailing, style = HhTheme.typography.labelM, color = HhTheme.colors.onSurfaceVariant)
            }
        }
        content()
    }
}

@Composable
internal fun WorkspaceResumeSection(
    resume: WorkspaceResume,
    canReview: Boolean,
    onPreview: () -> Unit,
    onShare: () -> Unit,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WorkspaceSection(
        title = stringResource(R.string.feature_applications_impl_workspace_resume_heading),
        modifier = modifier,
    ) {
        when (resume) {
            WorkspaceResume.Absent -> Text(
                text = stringResource(R.string.feature_applications_impl_workspace_resume_absent),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
            WorkspaceResume.NotExported -> Text(
                text = stringResource(R.string.feature_applications_impl_workspace_resume_ready),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
            is WorkspaceResume.Exported -> ExportedFile(resume = resume)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            when (resume) {
                WorkspaceResume.Absent -> Unit
                WorkspaceResume.NotExported -> HhPrimaryButton(
                    label = stringResource(R.string.feature_applications_impl_workspace_resume_preview),
                    onClick = onPreview,
                    modifier = Modifier.weight(1f),
                    trailingIcon = HhIcons.ArrowForward,
                )
                is WorkspaceResume.Exported -> HhSecondaryButton(
                    label = stringResource(R.string.feature_applications_impl_workspace_resume_share_again),
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    trailingIcon = HhIcons.Share,
                )
            }
            if (canReview) {
                HhOutlineButton(
                    label = stringResource(R.string.feature_applications_impl_workspace_resume_review),
                    onClick = onReview,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ExportedFile(
    resume: WorkspaceResume.Exported,
    modifier: Modifier = Modifier,
) {
    val format = when (resume.format) {
        ExportFormat.PDF -> stringResource(R.string.feature_applications_impl_workspace_format_pdf)
        ExportFormat.DOCX -> stringResource(R.string.feature_applications_impl_workspace_format_docx)
    }
    val pages = resume.pageCount?.let { count ->
        pluralStringResource(R.plurals.feature_applications_impl_workspace_resume_pages, count, count)
    }
    val details = listOfNotNull(pages, format, resume.templateName).joinToString(separator = META_SEPARATOR)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md - HhTheme.spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Description,
            contentDescription = null,
            tint = HhTheme.colors.primary,
            modifier = Modifier.size(HhTheme.spacing.xxl),
        )
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
            Text(
                text = resume.fileName,
                style = HhTheme.typography.factId,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(
                    id = R.string.feature_applications_impl_workspace_resume_file_meta,
                    details,
                    dayMonthLabel(resume.exportedAt),
                ),
                style = HhTheme.typography.bodyS,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WorkspaceGapSection(
    gapCounts: WorkspaceGapCounts?,
    coverage: KeywordCoverage?,
    matches: List<WorkspaceMatch>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WorkspaceSection(
        title = stringResource(R.string.feature_applications_impl_workspace_gap_heading),
        modifier = modifier,
    ) {
        if (gapCounts == null) {
            Text(
                text = stringResource(R.string.feature_applications_impl_workspace_gap_unavailable),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
            return@WorkspaceSection
        }
        if (coverage != null) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                itemVerticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = coverageFraction(coverage),
                    style = HhTheme.typography.displayM,
                    color = HhTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.feature_applications_impl_workspace_gap_caption),
                    style = HhTheme.typography.labelM,
                    color = HhTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = HhTheme.spacing.xs),
                )
            }
        }
        val requirementTotal = gapCounts.met + gapCounts.partial + gapCounts.gap
        Text(
            text = pluralStringResource(
                R.plurals.feature_applications_impl_workspace_gap_legend,
                requirementTotal,
                requirementTotal,
            ),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            HhStatusChip(
                kind = MatchStatusKindMapper().kindOf(MatchStatus.MET),
                label = pluralStringResource(R.plurals.feature_applications_impl_workspace_gap_met, gapCounts.met, gapCounts.met),
            )
            HhStatusChip(
                kind = MatchStatusKindMapper().kindOf(MatchStatus.PARTIAL),
                label = pluralStringResource(R.plurals.feature_applications_impl_workspace_gap_partial, gapCounts.partial, gapCounts.partial),
            )
            HhStatusChip(
                kind = MatchStatusKindMapper().kindOf(MatchStatus.GAP),
                label = pluralStringResource(R.plurals.feature_applications_impl_workspace_gap_gap, gapCounts.gap, gapCounts.gap),
            )
        }
        HhExpandable(expanded = isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                matches.forEach { match -> WorkspaceMatchRow(match = match) }
            }
        }
        HhTextButton(
            label = stringResource(
                if (isExpanded) {
                    R.string.feature_applications_impl_workspace_gap_close
                } else {
                    R.string.feature_applications_impl_workspace_gap_open
                },
            ),
            onClick = onToggle,
            trailingIcon = if (isExpanded) null else HhIcons.ArrowForward,
        )
    }
}

@Composable
private fun WorkspaceMatchRow(
    match: WorkspaceMatch,
    modifier: Modifier = Modifier,
) {
    val statusLabel = when (match.status) {
        MatchStatus.MET -> stringResource(R.string.feature_applications_impl_match_met)
        MatchStatus.PARTIAL -> stringResource(R.string.feature_applications_impl_match_partial)
        MatchStatus.GAP -> stringResource(R.string.feature_applications_impl_match_gap)
    }
    val description = stringResource(
        id = R.string.feature_applications_impl_match_description,
        match.requirementText,
        statusLabel,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        HhDivider()
        Text(
            text = match.requirementText,
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurface,
        )
        HhStatusChip(kind = MatchStatusKindMapper().kindOf(match.status), label = statusLabel)
    }
}

@Composable
internal fun WorkspacePrepPlanSection(
    tasks: List<WorkspacePrepTask>,
    onToggle: (String) -> Unit,
    onReport: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val doneCount = tasks.count { task -> task.isDone }
    WorkspaceSection(
        title = stringResource(R.string.feature_applications_impl_workspace_tasks_heading),
        modifier = modifier,
        trailing = if (tasks.isEmpty()) {
            null
        } else {
            pluralStringResource(
                id = R.plurals.feature_applications_impl_workspace_tasks_progress,
                count = tasks.size,
                doneCount,
                tasks.size,
            )
        },
    ) {
        if (tasks.isEmpty()) {
            Text(
                text = stringResource(R.string.feature_applications_impl_workspace_tasks_none),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
        tasks.forEach { task ->
            WorkspacePrepTaskRow(
                task = task,
                onToggle = { onToggle(task.id) },
                onReport = { onReport(task.id) },
            )
        }
    }
}

@Composable
internal fun WorkspacePrepTaskRow(
    task: WorkspacePrepTask,
    onToggle: () -> Unit,
    onReport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateLabel = stringResource(
        id = if (task.isDone) {
            R.string.feature_applications_impl_workspace_task_done_description
        } else {
            R.string.feature_applications_impl_workspace_task_open_description
        },
        task.requirementText,
    )
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .toggleable(value = task.isDone, role = Role.Checkbox, onValueChange = { onToggle() })
                    .semantics(mergeDescendants = true) { contentDescription = stateLabel },
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HhCheckbox(checked = task.isDone, onCheckedChange = { onToggle() })
                Text(
                    text = task.requirementText,
                    style = HhTheme.typography.bodyM,
                    color = if (task.isDone) HhTheme.colors.onSurfaceVariant else HhTheme.colors.onSurface,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f),
                )
            }
            if (!task.isReported) {
                HhIconButton(
                    icon = HhIcons.Flag,
                    contentDescription = stringResource(
                        id = R.string.feature_applications_impl_workspace_task_report,
                        task.requirementText,
                    ),
                    onClick = onReport,
                    tint = HhTheme.colors.onSurfaceVariant,
                    containerColor = Color.Transparent,
                    borderColor = Color.Transparent,
                )
            }
        }
        if (task.isReported) {
            Text(
                text = stringResource(R.string.feature_applications_impl_workspace_task_reported),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.primary,
                modifier = Modifier.padding(start = HhTheme.spacing.touch + HhTheme.spacing.sm),
            )
        }
    }
}

@Composable
internal fun WorkspaceNotesSection(
    notes: String,
    notesState: WorkspaceNotesState,
    isFocused: Boolean,
    onNotesChange: (String) -> Unit,
    onFocusChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.feature_applications_impl_workspace_notes_label)
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isFocused) {
        if (isFocused) focusRequester.requestFocus()
    }
    WorkspaceSection(
        title = stringResource(R.string.feature_applications_impl_workspace_notes_heading),
        modifier = modifier,
    ) {
        HhTextField(
            value = notes,
            onValueChange = onNotesChange,
            placeholder = stringResource(R.string.feature_applications_impl_workspace_notes_placeholder),
            singleLine = false,
            minLines = NOTES_MIN_LINES,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { change -> onFocusChange(change.isFocused) }
                .semantics { contentDescription = label },
        )
        when (notesState) {
            WorkspaceNotesState.Idle -> Text(
                text = stringResource(R.string.feature_applications_impl_workspace_notes_idle),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
            WorkspaceNotesState.Saving -> Text(
                text = stringResource(R.string.feature_applications_impl_workspace_notes_saving),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
            WorkspaceNotesState.SavedJustNow -> Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics(mergeDescendants = true) {},
            ) {
                Icon(
                    imageVector = HhIcons.Check,
                    contentDescription = null,
                    tint = HhTheme.colors.primary,
                    modifier = Modifier.size(HhTheme.spacing.lg),
                )
                Text(
                    text = stringResource(R.string.feature_applications_impl_workspace_notes_saved),
                    style = HhTheme.typography.labelM,
                    color = HhTheme.colors.primary,
                )
            }
        }
    }
}

@Composable
internal fun WorkspaceJobDescriptionSection(
    text: String,
    wordCount: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WorkspaceSection(
        title = stringResource(R.string.feature_applications_impl_workspace_jd_heading),
        modifier = modifier,
        trailing = pluralStringResource(
            id = R.plurals.feature_applications_impl_workspace_jd_words,
            count = wordCount,
            wordCount,
        ),
    ) {
        Text(
            text = text,
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.body,
            maxLines = if (isExpanded) Int.MAX_VALUE else JD_COLLAPSED_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        HhTextButton(
            label = stringResource(
                if (isExpanded) {
                    R.string.feature_applications_impl_workspace_jd_hide
                } else {
                    R.string.feature_applications_impl_workspace_jd_show
                },
            ),
            onClick = onToggle,
            trailingIcon = if (isExpanded) null else HhIcons.ArrowForward,
        )
    }
}

@Composable
internal fun WorkspaceEntrySection(
    title: String,
    summary: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    WorkspaceSection(title = title, modifier = modifier, trailing = trailing) {
        Text(text = summary, style = HhTheme.typography.bodyM, color = HhTheme.colors.body)
        HhTextButton(label = actionLabel, onClick = onClick, trailingIcon = HhIcons.ArrowForward)
    }
}

private const val META_SEPARATOR = " · "
private const val NOTES_MIN_LINES = 3
private const val JD_COLLAPSED_LINES = 2

@Preview(showBackground = true)
@Composable
private fun WorkspaceGapSectionPreview() {
    HhTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(HhTheme.spacing.gutter)) {
            WorkspaceGapSection(
                gapCounts = WorkspaceGapCounts(met = 3, partial = 1, gap = 4),
                coverage = KeywordCoverage(covered = 9, total = 14),
                matches = previewMatches(),
                isExpanded = false,
                onToggle = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WorkspacePrepTaskRowPreview() {
    HhTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(HhTheme.spacing.gutter)) {
            WorkspacePrepTaskRow(
                task = previewPrepTask(id = "req-agile", isDone = false, isReported = true),
                onToggle = {},
                onReport = {},
            )
        }
    }
}
