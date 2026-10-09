package com.tailormyresume.feature.applications.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrCheckbox
import com.tailormyresume.core.designsystem.component.TmrDivider
import com.tailormyresume.core.designsystem.component.TmrExpandable
import com.tailormyresume.core.designsystem.component.TmrIconButton
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrStatusChip
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.component.TmrTextField
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.model.MatchStatus
import com.tailormyresume.core.ui.MatchStatusKindMapper
import kotlin.time.Instant

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun WorkspaceIdentityCard(
    role: String,
    company: String,
    status: ApplicationStatus,
    updatedAt: Instant,
    now: Instant,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = status.label()
    val description = stringResource(R.string.feature_applications_impl_status_chip_description, label)
    TmrCard(modifier = modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
            LogoTile(company = company, size = IDENTITY_TILE)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = roleOrFallback(role),
                    style = TmrTheme.typography.headlineM,
                    color = TmrTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(
                        R.string.feature_applications_impl_row_company_updated,
                        companyOrFallback(company),
                        updatedSentence(updatedAt = updatedAt, now = now),
                    ),
                    style = TmrTheme.typography.labelM,
                    color = TmrTheme.colors.onSurfaceVariant,
                )
            }
        }
        Row(modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description }) {
            StatusChip(status = status, label = label, onClick = onStatusClick)
        }
        TmrOutlineButton(
            label = stringResource(R.string.feature_applications_impl_row_change_status),
            onClick = onStatusClick,
            modifier = Modifier.fillMaxWidth(),
            size = TmrButtonSize.Compact,
        )
    }
}

private val IDENTITY_TILE = 56.dp
private val SECTION_TILE = 44.dp
private val SECTION_TILE_SHAPE = RoundedCornerShape(14.dp)

@Composable
private fun WorkspaceSection(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    content: @Composable () -> Unit,
) {
    TmrCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md - TmrTheme.spacing.xxs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(SECTION_TILE)
                    .clip(SECTION_TILE_SHAPE)
                    .background(TmrTheme.colors.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TmrTheme.colors.onSurface,
                    modifier = Modifier.size(TmrTheme.spacing.xl),
                )
            }
            Text(
                text = title,
                style = TmrTheme.typography.titleM,
                color = TmrTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (trailing != null) {
                Text(text = trailing, style = TmrTheme.typography.labelM, color = TmrTheme.colors.onSurfaceVariant)
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
        icon = TmrIcons.Description,
        modifier = modifier,
    ) {
        when (resume) {
            WorkspaceResume.Absent -> Text(
                text = stringResource(R.string.feature_applications_impl_workspace_resume_absent),
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.body,
            )
            WorkspaceResume.NotExported -> Text(
                text = stringResource(R.string.feature_applications_impl_workspace_resume_ready),
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.body,
            )
            is WorkspaceResume.Exported -> ExportedFile(resume = resume)
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            when (resume) {
                WorkspaceResume.Absent -> Unit
                WorkspaceResume.NotExported -> TmrPrimaryButton(
                    label = stringResource(R.string.feature_applications_impl_workspace_resume_preview),
                    onClick = onPreview,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = TmrIcons.ArrowForward,
                )
                is WorkspaceResume.Exported -> TmrSecondaryButton(
                    label = stringResource(R.string.feature_applications_impl_workspace_resume_share_again),
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    trailingIcon = TmrIcons.Share,
                    size = TmrButtonSize.Compact,
                )
            }
            if (canReview) {
                val reviewDescription = stringResource(R.string.feature_applications_impl_workspace_resume_review_description)
                TmrSecondaryButton(
                    label = stringResource(R.string.feature_applications_impl_workspace_resume_review),
                    onClick = onReview,
                    modifier = Modifier
                        .weight(1f)
                        .semantics { contentDescription = reviewDescription },
                    trailingIcon = TmrIcons.OpenInNew,
                    size = TmrButtonSize.Compact,
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
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md - TmrTheme.spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = TmrIcons.Description,
            contentDescription = null,
            tint = TmrTheme.colors.primary,
            modifier = Modifier.size(TmrTheme.spacing.xxl),
        )
        Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs)) {
            Text(
                text = resume.fileName,
                style = TmrTheme.typography.factId,
                color = TmrTheme.colors.onSurface,
            )
            Text(
                text = stringResource(
                    id = R.string.feature_applications_impl_workspace_resume_file_meta,
                    details,
                    dayMonthLabel(resume.exportedAt),
                ),
                style = TmrTheme.typography.bodyS,
                color = TmrTheme.colors.onSurfaceVariant,
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
        icon = TmrIcons.CheckCircle,
        modifier = modifier,
    ) {
        if (gapCounts == null) {
            Text(
                text = stringResource(R.string.feature_applications_impl_workspace_gap_unavailable),
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.body,
            )
            return@WorkspaceSection
        }
        if (coverage != null) {
            Text(
                text = stringResource(
                    R.string.feature_applications_impl_workspace_gap_summary,
                    coverageLine(coverage),
                ),
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.onSurface,
            )
        }
        TmrExpandable(expanded = isExpanded) {
            Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
                val requirementTotal = gapCounts.met + gapCounts.partial + gapCounts.gap
                Text(
                    text = pluralStringResource(
                        R.plurals.feature_applications_impl_workspace_gap_legend,
                        requirementTotal,
                        requirementTotal,
                    ),
                    style = TmrTheme.typography.bodyS,
                    color = TmrTheme.colors.onSurfaceVariant,
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
                ) {
                    TmrStatusChip(
                        kind = MatchStatusKindMapper().kindOf(MatchStatus.MET),
                        label = pluralStringResource(R.plurals.feature_applications_impl_workspace_gap_met, gapCounts.met, gapCounts.met),
                    )
                    TmrStatusChip(
                        kind = MatchStatusKindMapper().kindOf(MatchStatus.PARTIAL),
                        label = pluralStringResource(R.plurals.feature_applications_impl_workspace_gap_partial, gapCounts.partial, gapCounts.partial),
                    )
                    TmrStatusChip(
                        kind = MatchStatusKindMapper().kindOf(MatchStatus.GAP),
                        label = pluralStringResource(R.plurals.feature_applications_impl_workspace_gap_gap, gapCounts.gap, gapCounts.gap),
                    )
                }
                matches.forEach { match -> WorkspaceMatchRow(match = match) }
            }
        }
        TmrOutlineButton(
            label = stringResource(
                if (isExpanded) {
                    R.string.feature_applications_impl_workspace_gap_close
                } else {
                    R.string.feature_applications_impl_workspace_gap_open
                },
            ),
            onClick = onToggle,
            size = TmrButtonSize.Compact,
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
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
    ) {
        TmrDivider()
        Text(
            text = match.requirementText,
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.onSurface,
        )
        TmrStatusChip(kind = MatchStatusKindMapper().kindOf(match.status), label = statusLabel)
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
        icon = TmrIcons.Check,
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
                style = TmrTheme.typography.bodyM,
                color = TmrTheme.colors.body,
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
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .toggleable(value = task.isDone, role = Role.Checkbox, onValueChange = { onToggle() })
                    .semantics(mergeDescendants = true) { contentDescription = stateLabel },
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TmrCheckbox(checked = task.isDone, onCheckedChange = { onToggle() })
                Text(
                    text = task.requirementText,
                    style = TmrTheme.typography.bodyM,
                    color = if (task.isDone) TmrTheme.colors.onSurfaceVariant else TmrTheme.colors.onSurface,
                    textDecoration = if (task.isDone) TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f),
                )
            }
            if (!task.isReported) {
                TmrIconButton(
                    icon = TmrIcons.Flag,
                    contentDescription = stringResource(
                        id = R.string.feature_applications_impl_workspace_task_report,
                        task.requirementText,
                    ),
                    onClick = onReport,
                    tint = TmrTheme.colors.onSurfaceVariant,
                    containerColor = Color.Transparent,
                    borderColor = Color.Transparent,
                )
            }
        }
        if (task.isReported) {
            Text(
                text = stringResource(R.string.feature_applications_impl_workspace_task_reported),
                style = TmrTheme.typography.labelM,
                color = TmrTheme.colors.primary,
                modifier = Modifier.padding(start = TmrTheme.spacing.touch + TmrTheme.spacing.sm),
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
        icon = TmrIcons.Edit,
        modifier = modifier,
    ) {
        TmrTextField(
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
                style = TmrTheme.typography.labelM,
                color = TmrTheme.colors.onSurfaceVariant,
            )
            WorkspaceNotesState.Saving -> Text(
                text = stringResource(R.string.feature_applications_impl_workspace_notes_saving),
                style = TmrTheme.typography.labelM,
                color = TmrTheme.colors.onSurfaceVariant,
            )
            WorkspaceNotesState.SavedJustNow -> Row(
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.semantics(mergeDescendants = true) {},
            ) {
                Icon(
                    imageVector = TmrIcons.Check,
                    contentDescription = null,
                    tint = TmrTheme.colors.primary,
                    modifier = Modifier.size(TmrTheme.spacing.lg),
                )
                Text(
                    text = stringResource(R.string.feature_applications_impl_workspace_notes_saved),
                    style = TmrTheme.typography.labelM,
                    color = TmrTheme.colors.primary,
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
        icon = TmrIcons.Description,
        modifier = modifier,
        trailing = pluralStringResource(
            id = R.plurals.feature_applications_impl_workspace_jd_words,
            count = wordCount,
            wordCount,
        ),
    ) {
        Text(
            text = text,
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.body,
            maxLines = if (isExpanded) Int.MAX_VALUE else JD_COLLAPSED_LINES,
            overflow = TextOverflow.Ellipsis,
        )
        TmrTextButton(
            label = stringResource(
                if (isExpanded) {
                    R.string.feature_applications_impl_workspace_jd_hide
                } else {
                    R.string.feature_applications_impl_workspace_jd_show
                },
            ),
            onClick = onToggle,
            trailingIcon = if (isExpanded) null else TmrIcons.ArrowForward,
        )
    }
}

@Composable
internal fun WorkspaceEntrySection(
    title: String,
    icon: ImageVector,
    summary: String,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: String? = null,
) {
    WorkspaceSection(title = title, icon = icon, modifier = modifier, trailing = trailing) {
        Text(text = summary, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.body)
        TmrOutlineButton(label = actionLabel, onClick = onClick, size = TmrButtonSize.Compact)
    }
}

private const val META_SEPARATOR = " · "
private const val NOTES_MIN_LINES = 3
private const val JD_COLLAPSED_LINES = 2

@Preview(showBackground = true)
@Composable
private fun WorkspaceGapSectionPreview() {
    TmrTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(TmrTheme.spacing.gutter)) {
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
    TmrTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(TmrTheme.spacing.gutter)) {
            WorkspacePrepTaskRow(
                task = previewPrepTask(id = "req-agile", isDone = false, isReported = true),
                onToggle = {},
                onReport = {},
            )
        }
    }
}
