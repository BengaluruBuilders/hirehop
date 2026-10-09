package com.tailormyresume.feature.tailor.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrIconButton
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.EntryCategory

@Composable
internal fun ReviewContent(
    state: TailorUiState.Success,
    onOpenChange: (String) -> Unit,
    onOpenSource: (String) -> Unit,
    onRegenerate: (EntryCategory) -> Unit,
    onReportSection: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = TmrTheme.spacing.gutter,
            end = TmrTheme.spacing.gutter,
            top = contentPadding.calculateTopPadding() + TmrTheme.spacing.md,
            bottom = contentPadding.calculateBottomPadding() + TmrTheme.spacing.md,
        ),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        if (state.isOffline) {
            item(key = "offline") {
                NoticeStrip(text = stringResource(R.string.feature_tailor_impl_offline_message), icon = TmrIcons.Offline)
            }
        }
        item(key = "progress") { ReviewProgress(state) }
        item(key = "hint") {
            NoteLine(text = stringResource(R.string.feature_tailor_impl_ready_hint), icon = TmrIcons.Info)
        }
        if (state.notAdded.isNotEmpty()) {
            item(key = "not-added") { LeftOutNotice(joinNotAdded(state.notAdded)) }
        }
        item(key = "resume") {
            TmrCard {
                state.sections.forEach { section ->
                    when (section) {
                        is ReviewSection.Entries ->
                            EntriesSection(section, state, onOpenChange, onOpenSource, onRegenerate, onReportSection)
                        is ReviewSection.Skills -> SkillsSection(section, onReportSection)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewProgress(state: TailorUiState.Success) {
    when {
        state.totalCount == 0 -> NoteLine(
            text = stringResource(R.string.feature_tailor_impl_no_changes),
            icon = TmrIcons.Verified,
        )
        state.isAllReviewed && !state.isOffline -> NoticeStrip(
            text = pluralStringResource(
                R.plurals.feature_tailor_impl_all_reviewed_banner,
                state.totalCount,
                state.reviewedCount,
                state.totalCount,
            ),
            icon = TmrIcons.CheckCircle,
            tone = BannerTone.Ok,
        )
        else -> Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            Text(
                text = progressText(state),
                style = TmrTheme.typography.titleM,
                color = TmrTheme.colors.onSurface,
            )
            ProgressBar(fraction = state.reviewedCount.toFloat() / state.totalCount)
        }
    }
}

@Composable
internal fun progressText(state: TailorUiState.Success): String {
    val progress = stringResource(
        R.string.feature_tailor_impl_progress_description,
        state.reviewedCount,
        state.totalCount,
    )
    if (state.flaggedCount == 0) return progress
    val flagged = pluralStringResource(
        R.plurals.feature_tailor_impl_progress_flagged,
        state.flaggedCount,
        state.flaggedCount,
    )
    return "$progress, $flagged"
}

@Composable
private fun LeftOutNotice(requirements: String) {
    val label = stringResource(R.string.feature_tailor_impl_left_out_label)
    val body = stringResource(R.string.feature_tailor_impl_left_out_body, requirements)
    NoteLine(text = "$label $body", icon = TmrIcons.Info)
}

@Composable
private fun EntriesSection(
    section: ReviewSection.Entries,
    state: TailorUiState.Success,
    onOpenChange: (String) -> Unit,
    onOpenSource: (String) -> Unit,
    onRegenerate: (EntryCategory) -> Unit,
    onReportSection: (String) -> Unit,
) {
    val changed = section.entries.flatMap { it.bullets }.filter { it.isChange }
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm + TmrTheme.spacing.d2)) {
        SectionHeader(
            heading = stringResource(section.category.headingRes()),
            changeCount = changed.size,
            reviewedCount = changed.count { it.isReviewed },
            canRegenerate = state.regenerationsLeft > 0 && section.changeCount > 0,
            regenerationsLeft = state.regenerationsLeft,
            isReported = sectionReportId(section.key) in state.reportedIds,
            onRegenerate = { onRegenerate(section.category) },
            onReport = { onReportSection(section.key) },
        )
        section.entries.forEach { entry ->
            Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.d8 - TmrTheme.spacing.d2)) {
                EntryHeader(entry)
                entry.bullets.forEach { bullet ->
                    ChangeLine(
                        item = bullet,
                        onOpenChange = { onOpenChange(bullet.bullet.id) },
                        onOpenSource = { onOpenSource(bullet.bullet.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SkillsSection(
    section: ReviewSection.Skills,
    onReportSection: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm + TmrTheme.spacing.d2)) {
        SectionHeader(
            heading = stringResource(R.string.feature_tailor_impl_skills_heading),
            changeCount = 0,
            reviewedCount = 0,
            canRegenerate = null,
            regenerationsLeft = 0,
            isReported = sectionReportId(section.key) in emptyReportedIds(),
            onRegenerate = {},
            onReport = { onReportSection(section.key) },
        )
        Text(
            text = section.skills.joinToString(" · "),
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.onSurface,
        )
    }
}

@Composable
private fun emptyReportedIds(): Set<String> = emptySet()

@Composable
private fun SectionHeader(
    heading: String,
    changeCount: Int,
    reviewedCount: Int,
    canRegenerate: Boolean?,
    regenerationsLeft: Int,
    isReported: Boolean,
    onRegenerate: () -> Unit,
    onReport: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrSectionLabel(text = heading, modifier = Modifier.weight(1f))
        SectionCountLabel(changeCount = changeCount, reviewedCount = reviewedCount, canRegenerate = canRegenerate)
        Box {
            TmrIconButton(
                icon = TmrIcons.More,
                contentDescription = stringResource(R.string.feature_tailor_impl_section_options, heading),
                onClick = { menuOpen = true },
                containerColor = Color.Transparent,
                borderColor = Color.Transparent,
            )
            if (menuOpen) {
                SectionMenu(
                    canRegenerate = canRegenerate,
                    regenerationsLeft = regenerationsLeft,
                    isReported = isReported,
                    onDismiss = { menuOpen = false },
                    onRegenerate = {
                        menuOpen = false
                        onRegenerate()
                    },
                    onReport = {
                        menuOpen = false
                        onReport()
                    },
                )
            }
        }
    }
}

@Composable
private fun SectionCountLabel(changeCount: Int, reviewedCount: Int, canRegenerate: Boolean?) {
    val colors = TmrTheme.colors
    when {
        canRegenerate == null || changeCount == 0 -> Unit
        reviewedCount == changeCount -> Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = TmrIcons.CheckCircle,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(TmrTheme.spacing.d16),
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_section_reviewed),
                style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
            )
        }
        reviewedCount > 0 -> Text(
            text = pluralStringResource(
                R.plurals.feature_tailor_impl_section_left,
                changeCount - reviewedCount,
                changeCount - reviewedCount,
            ),
            style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurfaceVariant,
        )
        else -> Text(
            text = pluralStringResource(R.plurals.feature_tailor_impl_section_changes, changeCount, changeCount),
            style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun SectionMenu(
    canRegenerate: Boolean?,
    regenerationsLeft: Int,
    isReported: Boolean,
    onDismiss: () -> Unit,
    onRegenerate: () -> Unit,
    onReport: () -> Unit,
) {
    Popup(
        alignment = Alignment.TopEnd,
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true),
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = TmrTheme.spacing.d64 * MENU_MIN_WIDTH_UNITS)
                .background(TmrTheme.colors.surface, TmrTheme.shapes.statusRow)
                .padding(TmrTheme.spacing.d4),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.d4),
        ) {
            if (canRegenerate != null) {
                MenuRow(
                    title = stringResource(R.string.feature_tailor_impl_menu_regenerate),
                    subtitle = if (regenerationsLeft > 0) {
                        stringResource(R.string.feature_tailor_impl_menu_regenerate_left, regenerationsLeft, MAX_REGENERATIONS)
                    } else {
                        stringResource(R.string.feature_tailor_impl_menu_regenerate_none)
                    },
                    enabled = canRegenerate,
                    onClick = onRegenerate,
                )
            }
            MenuRow(
                title = stringResource(
                    if (isReported) R.string.feature_tailor_impl_menu_reported else R.string.feature_tailor_impl_menu_report,
                ),
                subtitle = null,
                enabled = !isReported,
                onClick = onReport,
            )
        }
    }
}

@Composable
private fun MenuRow(title: String, subtitle: String?, enabled: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmrTheme.colors.sheet, TmrTheme.shapes.banner)
            .border(BorderStroke(TmrTheme.spacing.d2, TmrTheme.colors.outlineVariant), TmrTheme.shapes.banner)
            .heightIn(min = TmrTheme.spacing.touch)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = TmrTheme.spacing.md, vertical = TmrTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs),
    ) {
        Text(
            text = title,
            style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = if (enabled) TmrTheme.colors.onSurface else TmrTheme.colors.onSurfaceVariant,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = TmrTheme.typography.bodyS,
                color = TmrTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EntryHeader(entry: TailorEntryUi) {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs)) {
        Text(
            text = listOf(entry.title, entry.organization).filter { it.isNotEmpty() }.joinToString(", "),
            style = TmrTheme.typography.titleM,
            color = TmrTheme.colors.onSurface,
        )
        if (entry.dateRange.isNotEmpty()) {
            Text(
                text = entry.dateRange,
                style = TmrTheme.typography.bodyS,
                color = TmrTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChangeLine(item: TailorBulletUi, onOpenChange: () -> Unit, onOpenSource: () -> Unit) {
    val text = item.displayText()
    if (!item.isChange) {
        Text(text = text, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurface)
        return
    }
    val factIds = item.sources.map { it.displayId }.distinct()
    val description = stringResource(
        R.string.feature_tailor_impl_line_description,
        text,
        stringResource(item.state.labelChipRes()),
        factIds.joinToString(", "),
    )
    val outline = if (item.isFlagged) TmrTheme.colors.partial else TmrTheme.colors.outlineVariant
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmrTheme.colors.background, TmrTheme.shapes.statusRow)
            .border(BorderStroke(1.5.dp, outline), TmrTheme.shapes.statusRow)
            .clickable(role = Role.Button, onClick = onOpenChange)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = TmrTheme.spacing.md, vertical = TmrTheme.spacing.sm + TmrTheme.spacing.d2),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        Text(text = text, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurface)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm - TmrTheme.spacing.d2),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
            itemVerticalAlignment = Alignment.CenterVertically,
        ) {
            LineStateChip(item.state)
            factIds.forEach { factId ->
                val sourceDescription = stringResource(R.string.feature_tailor_impl_source_description, factId)
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = TmrTheme.spacing.touch, minHeight = TmrTheme.spacing.touch)
                        .clickable(role = Role.Button, onClick = onOpenSource)
                        .semantics { contentDescription = sourceDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    TmrFactId(id = factId)
                }
            }
        }
    }
}

@Composable
private fun LineStateChip(state: BulletReviewState) {
    StatusPill(label = stringResource(state.labelChipRes()), icon = state.chipIcon(), color = state.chipColor())
}

@Composable
internal fun BulletReviewState.chipColor(): Color = when (this) {
    BulletReviewState.FLAGGED -> TmrTheme.colors.partial
    BulletReviewState.ACCEPTED -> TmrTheme.colors.primary
    BulletReviewState.ORIGINAL_KEPT, BulletReviewState.REPAIR_FAILED -> TmrTheme.colors.onSurfaceVariant
    else -> TmrTheme.colors.onSurface
}

internal fun BulletReviewState.labelChipRes(): Int = when (this) {
    BulletReviewState.TO_REVIEW, BulletReviewState.UNCHANGED -> R.string.feature_tailor_impl_chip_changed
    BulletReviewState.FLAGGED -> R.string.feature_tailor_impl_state_flagged
    BulletReviewState.ACCEPTED -> R.string.feature_tailor_impl_state_accepted
    BulletReviewState.ORIGINAL_KEPT, BulletReviewState.REPAIR_FAILED -> R.string.feature_tailor_impl_state_original_kept
    BulletReviewState.USER_EDITED -> R.string.feature_tailor_impl_state_user_edited
    BulletReviewState.STALE -> R.string.feature_tailor_impl_state_source_changed
}

internal fun BulletReviewState.chipIcon(): ImageVector? = when (this) {
    BulletReviewState.TO_REVIEW, BulletReviewState.USER_EDITED -> TmrIcons.Edit
    BulletReviewState.FLAGGED -> TmrIcons.Flag
    BulletReviewState.ACCEPTED -> TmrIcons.Check
    BulletReviewState.ORIGINAL_KEPT, BulletReviewState.REPAIR_FAILED -> TmrIcons.ArrowBack
    BulletReviewState.STALE, BulletReviewState.UNCHANGED -> null
}

internal fun TailorBulletUi.showsProposal(): Boolean = when (state) {
    BulletReviewState.TO_REVIEW,
    BulletReviewState.FLAGGED,
    BulletReviewState.ACCEPTED,
    BulletReviewState.USER_EDITED,
    -> true
    else -> false
}

internal fun TailorBulletUi.displayText(): String =
    if (showsProposal()) bullet.proposedText else bullet.originalText

private fun joinNotAdded(requirements: List<String>): String =
    requirements.joinToString(", ") { it.replaceFirstChar { char -> char.lowercaseChar() } }

private const val MENU_MIN_WIDTH_UNITS = 4
