package com.hirehop.feature.tailor.impl

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.EntryCategory
import com.hirehop.feature.tailor.impl.diff.WordDiff

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
            start = HhTheme.spacing.gutter,
            end = HhTheme.spacing.gutter,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2),
    ) {
        if (state.isOffline) {
            item(key = "offline") {
                NoticeStrip(text = stringResource(R.string.feature_tailor_impl_offline_message), icon = HhIcons.Offline)
            }
        }
        if (state.regenerationsLeft == 0) {
            item(key = "regenerations-used") {
                NoticeStrip(
                    text = stringResource(R.string.feature_tailor_impl_regenerations_used),
                    icon = HhIcons.Edit,
                )
            }
        }
        item(key = "progress") {
            ProgressCard(
                reviewed = state.reviewedCount,
                total = state.totalCount,
                flagged = state.flaggedCount,
                showAllReviewedNote = true,
            )
        }
        item(key = "document") {
            DocumentCard(state, onOpenChange, onOpenSource, onRegenerate, onReportSection)
        }
        if (state.notAdded.isNotEmpty()) {
            item(key = "not-added") { NotAddedPanel(state.notAdded) }
        }
    }
}

@Composable
internal fun NoticeStrip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    val colors = HhTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.neutralContainer, HhTheme.shapes.banner)
            .padding(HhTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md - HhTheme.spacing.d2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onNeutralContainer,
            modifier = Modifier.size(HhTheme.spacing.d20),
        )
        Text(text = text, style = HhTheme.typography.bodyM, color = colors.onSurface, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProgressCard(
    reviewed: Int,
    total: Int,
    flagged: Int,
    showAllReviewedNote: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val hop = remember { Animatable(1f) }
    val allReviewed = total > 0 && reviewed >= total
    var wasAllReviewed by remember { mutableStateOf(allReviewed) }
    val hopSpec = HhTheme.motion.hopSpecs.scale
    LaunchedEffect(allReviewed) {
        if (allReviewed && !wasAllReviewed) {
            hop.snapTo(HOP_START_SCALE)
            hop.animateTo(1f, hopSpec)
        }
        wasAllReviewed = allReviewed
    }
    val description = if (total == 0) {
        stringResource(R.string.feature_tailor_impl_no_changes)
    } else {
        stringResource(R.string.feature_tailor_impl_progress_description, reviewed, total)
    }
    HhHeroCard(
        modifier = modifier.graphicsLayer {
            scaleX = hop.value
            scaleY = hop.value
        },
        contentPadding = PaddingValues(HhTheme.spacing.md),
    ) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            if (total == 0) {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_no_changes),
                    style = HhTheme.typography.bodyM,
                    color = colors.onSurface,
                )
                return@Column
            }
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_progress_value, reviewed, total),
                        style = HhTheme.typography.numeralM,
                        color = colors.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_changes_reviewed),
                        style = HhTheme.typography.labelM,
                        color = colors.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = HhTheme.spacing.d2),
                    )
                }
                if (flagged > 0) {
                    HhStatusChip(
                        kind = HhStatusKind.Partial,
                        label = pluralStringResource(R.plurals.feature_tailor_impl_flagged, flagged, flagged),
                    )
                }
            }
            SegmentBar(done = reviewed, total = total)
            if (allReviewed && showAllReviewedNote) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.metContainer, HhTheme.shapes.banner)
                        .padding(horizontal = HhTheme.spacing.d12 + HhTheme.spacing.d2, vertical = HhTheme.spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md - HhTheme.spacing.d2),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = HhIcons.CheckCircle,
                        contentDescription = null,
                        tint = colors.onMetContainer,
                        modifier = Modifier.size(HhTheme.spacing.d20),
                    )
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_all_reviewed, reviewed, total),
                        style = HhTheme.typography.bodyM,
                        color = colors.onMetContainer,
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentBar(done: Int, total: Int) {
    if (total <= 0) return
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {},
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d2 + 1.dp),
    ) {
        repeat(total) { position ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(HhTheme.spacing.d4 + HhTheme.spacing.d2)
                    .background(
                        if (position < done) HhTheme.colors.primary else HhTheme.colors.outlineVariant,
                        HhTheme.shapes.pill,
                    ),
            )
        }
    }
}

@Composable
private fun DocumentCard(
    state: TailorUiState.Success,
    onOpenChange: (String) -> Unit,
    onOpenSource: (String) -> Unit,
    onRegenerate: (EntryCategory) -> Unit,
    onReportSection: (String) -> Unit,
) {
    HhHeroCard {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2)) {
            state.sections.forEach { section ->
                when (section) {
                    is ReviewSection.Entries ->
                        EntriesSection(section, state, onOpenChange, onOpenSource, onRegenerate, onReportSection)
                    is ReviewSection.Skills -> SkillsSection(section, state, onReportSection)
                }
            }
        }
    }
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
    val heading = stringResource(section.category.headingRes())
    SectionHeader(
        heading = heading,
        canRegenerate = state.regenerationsLeft > 0 && section.changeCount > 0,
        regenerationsLeft = state.regenerationsLeft,
        isReported = sectionReportId(section.key) in state.reportedIds,
        onRegenerate = { onRegenerate(section.category) },
        onReport = { onReportSection(section.key) },
    )
    section.entries.forEach { entry ->
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            EntryHeader(entry)
            entry.bullets.forEach { bullet ->
                ChangeLine(bullet, onOpenChange = { onOpenChange(bullet.bullet.id) }, onOpenSource = { onOpenSource(bullet.bullet.id) })
            }
        }
    }
}

@Composable
private fun SkillsSection(
    section: ReviewSection.Skills,
    state: TailorUiState.Success,
    onReportSection: (String) -> Unit,
) {
    SectionHeader(
        heading = stringResource(R.string.feature_tailor_impl_skills_heading),
        canRegenerate = null,
        regenerationsLeft = 0,
        isReported = sectionReportId(section.key) in state.reportedIds,
        onRegenerate = {},
        onReport = { onReportSection(section.key) },
    )
    Text(
        text = section.skills.joinToString(" · "),
        style = HhTheme.typography.bodyM,
        color = HhTheme.colors.onSurface,
    )
}

@Composable
private fun SectionHeader(
    heading: String,
    canRegenerate: Boolean?,
    regenerationsLeft: Int,
    isReported: Boolean,
    onRegenerate: () -> Unit,
    onReport: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = heading.uppercase(),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Box {
                HhIconButton(
                    icon = HhIcons.More,
                    contentDescription = stringResource(R.string.feature_tailor_impl_section_options, heading),
                    onClick = { menuOpen = true },
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    borderColor = androidx.compose.ui.graphics.Color.Transparent,
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
        HhDivider()
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
                .widthIn(min = HhTheme.spacing.d64 * MENU_MIN_WIDTH_UNITS)
                .background(HhTheme.colors.surface, HhTheme.shapes.banner)
                .padding(vertical = HhTheme.spacing.sm),
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
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = HhTheme.spacing.lg, vertical = HhTheme.spacing.md),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = HhTheme.typography.bodyM,
            color = if (enabled) HhTheme.colors.onSurface else HhTheme.colors.onSurfaceVariant,
        )
        if (subtitle != null) {
            Text(text = subtitle, style = HhTheme.typography.labelM, color = HhTheme.colors.onSurfaceVariant)
        }
    }
}

@Composable
private fun EntryHeader(entry: TailorEntryUi) {
    Column {
        Text(
            text = listOf(entry.title, entry.organization).filter { it.isNotEmpty() }.joinToString(" · "),
            style = HhTheme.typography.titleS,
            color = HhTheme.colors.onSurface,
        )
        if (entry.dateRange.isNotEmpty()) {
            Text(
                text = entry.dateRange,
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChangeLine(item: TailorBulletUi, onOpenChange: () -> Unit, onOpenSource: () -> Unit) {
    val markStyle = evidenceMarkSpanStyle()
    val text = remember(item, markStyle) { item.lineText(markStyle) }
    val state = item.state
    val canOpen = item.isChange
    val description = if (canOpen) {
        stringResource(
            R.string.feature_tailor_impl_line_description,
            item.displayText(),
            state.labelRes()?.let { stringResource(it) }.orEmpty(),
            item.sources.map { it.displayId }.distinct().joinToString(", "),
        )
    } else {
        item.displayText()
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (canOpen) {
                        Modifier
                            .clickable(role = Role.Button, onClick = onOpenChange)
                            .semantics(mergeDescendants = true) { contentDescription = description }
                    } else {
                        Modifier
                    },
                ),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            HhEvidenceText(text = text, style = HhTheme.typography.bodyM)
            DecisionChip(state)
        }
        SourceBadge(factIds = item.sources.map { it.displayId }.distinct(), onClick = onOpenSource)
    }
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

private fun TailorBulletUi.lineText(markStyle: androidx.compose.ui.text.SpanStyle): AnnotatedString {
    val text = displayText()
    if (!showsProposal() || state == BulletReviewState.USER_EDITED) return AnnotatedString(text)
    val segments = WordDiff.diff(bullet.originalText, bullet.proposedText).proposed
    return buildAnnotatedString {
        segments.forEachIndexed { index, segment ->
            if (index > 0) append(" ")
            if (segment.changed) withStyle(markStyle) { append(segment.text) } else append(segment.text)
        }
    }
}

@Composable
private fun NotAddedPanel(requirements: List<String>) {
    HhCard {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_not_added_title),
                style = HhTheme.typography.titleS,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = pluralStringResource(
                    R.plurals.feature_tailor_impl_not_added_body,
                    requirements.size,
                    requirements.size,
                    requirements.joinToString(", ") { it.replaceFirstChar { char -> char.lowercaseChar() } },
                ),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
            requirements.forEach { requirement ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    HhStatusChip(kind = HhStatusKind.Gap, label = stringResource(R.string.feature_tailor_impl_to_prepare))
                    Text(text = requirement, style = HhTheme.typography.titleS, color = HhTheme.colors.onSurface)
                }
            }
        }
    }
}

private const val HOP_START_SCALE = 0.96f
private const val MENU_MIN_WIDTH_UNITS = 4
