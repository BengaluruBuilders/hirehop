package com.hirehop.feature.tailor.impl

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhDecoration
import com.hirehop.core.designsystem.component.HhDecorationKind
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhFactId
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
            top = contentPadding.calculateTopPadding() + HhTheme.spacing.d32 + HhTheme.spacing.sm,
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        if (state.isOffline) {
            item(key = "offline") {
                NoticeStrip(text = stringResource(R.string.feature_tailor_impl_offline_message), icon = HhIcons.Offline)
            }
        }
        item(key = "hint") {
            Text(
                text = stringResource(R.string.feature_tailor_impl_ready_hint),
                style = HhTheme.typography.bodyS,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        if (state.notAdded.isNotEmpty()) {
            item(key = "not-added") { LeftOutNotice(joinNotAdded(state.notAdded)) }
        }
        if (state.regenerationsLeft == 0) {
            item(key = "regenerations-used") {
                RegenUsedNotice(stringResource(R.string.feature_tailor_impl_regenerations_used))
            }
        }
        if (state.isAllReviewed) {
            item(key = "all-reviewed") { AllReviewedNotice(state) }
        }
        items(items = state.sections, key = { it.key }) { section ->
            when (section) {
                is ReviewSection.Entries ->
                    EntriesCard(section, state, onOpenChange, onOpenSource, onRegenerate, onReportSection)
                is ReviewSection.Skills ->
                    SkillsCard(section, onReportSection)
            }
        }
    }
}

@Composable
internal fun NoticeStrip(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(HhTheme.colors.neutralContainer, HhTheme.shapes.banner)
            .padding(HhTheme.spacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md - HhTheme.spacing.d2),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HhTheme.colors.onNeutralContainer,
            modifier = Modifier.size(HhTheme.spacing.d20),
        )
        Text(
            text = text,
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurface,
        )
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
    HhCard(
        modifier = modifier.graphicsLayer {
            scaleX = hop.value
            scaleY = hop.value
        },
    ) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            if (total == 0) {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_no_changes),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.onSurface,
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
                        color = HhTheme.colors.onSurface,
                    )
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_changes_reviewed),
                        style = HhTheme.typography.labelM,
                        color = HhTheme.colors.onSurfaceVariant,
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
                        .background(HhTheme.colors.metContainer, HhTheme.shapes.banner)
                        .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        imageVector = HhIcons.CheckCircle,
                        contentDescription = null,
                        tint = HhTheme.colors.onMetContainer,
                        modifier = Modifier.size(HhTheme.spacing.d20),
                    )
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_all_reviewed, reviewed, total),
                        style = HhTheme.typography.bodyM,
                        color = HhTheme.colors.onMetContainer,
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
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d2 + HhTheme.spacing.d2 / 2),
    ) {
        repeat(total) { position ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = HhTheme.spacing.d8)
                    .background(
                        if (position < done) HhTheme.colors.primary else HhTheme.colors.outlineVariant,
                        HhTheme.shapes.pill,
                    ),
            )
        }
    }
}

@Composable
private fun LeftOutNotice(requirements: String) {
    val colors = HhTheme.colors
    val label = stringResource(R.string.feature_tailor_impl_left_out_label)
    val body = stringResource(R.string.feature_tailor_impl_left_out_body, requirements)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .dashedBorder(colors.gap, HhTheme.spacing.d2 * 0.75f, HhTheme.spacing.d24)
            .padding(horizontal = HhTheme.spacing.md + HhTheme.spacing.d2, vertical = HhTheme.spacing.d8 + HhTheme.spacing.d2),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d8 + HhTheme.spacing.d2),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Info,
            contentDescription = null,
            tint = colors.gap,
            modifier = Modifier.size(HhTheme.spacing.d20),
        )
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) { append(label) }
                append(" ")
                append(body)
            },
            style = HhTheme.typography.bodyS,
            color = colors.gap,
        )
    }
}

@Composable
private fun RegenUsedNotice(body: String) {
    val colors = HhTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, HhTheme.shapes.card)
            .dashedBorder(colors.gap, HhTheme.spacing.d2 * 0.75f, HhTheme.spacing.d24)
            .padding(HhTheme.spacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhRegenUsedDot()
            HhRegenUsedDot()
        }
        Text(
            text = stringResource(R.string.feature_tailor_impl_regen_used_title),
            style = HhTheme.typography.titleM,
            color = colors.onSurface,
        )
        Text(
            text = body,
            style = HhTheme.typography.bodyS,
            color = colors.gap,
        )
    }
}

@Composable
private fun HhRegenUsedDot() {
    Box(
        modifier = Modifier
            .size(HhTheme.spacing.d20)
            .dashedBorder(HhTheme.colors.gap, HhTheme.spacing.d2, HhTheme.spacing.d32)
            .clearAndSetSemantics {},
    )
}

@Composable
private fun AllReviewedNotice(state: TailorUiState.Success) {
    val colors = HhTheme.colors
    val accepted = state.changes.count { it.state == BulletReviewState.ACCEPTED }
    val kept = state.changes.count {
        it.state == BulletReviewState.ORIGINAL_KEPT || it.state == BulletReviewState.REPAIR_FAILED
    }
    val edited = state.changes.count { it.state == BulletReviewState.USER_EDITED }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.special, HhTheme.shapes.card)
            .padding(HhTheme.spacing.cardPadding),
    ) {
        HhDecoration(
            kind = HhDecorationKind.Squiggle,
            color = colors.brand,
            modifier = Modifier.align(Alignment.TopEnd),
        )
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_all_reviewed_title, state.totalCount),
                style = HhTheme.typography.titleL,
                color = colors.onSpecial,
            )
            Text(
                text = stringResource(
                    R.string.feature_tailor_impl_all_reviewed_summary,
                    accepted,
                    kept,
                    edited,
                ),
                style = HhTheme.typography.bodyS.copy(fontWeight = FontWeight.SemiBold),
                color = colors.onSpecial,
            )
            LineChip(
                label = stringResource(R.string.feature_tailor_impl_all_reviewed_done),
                icon = HhIcons.Check,
                container = colors.document,
                content = colors.onPrimaryContainer,
                border = null,
            )
        }
    }
}

@Composable
private fun EntriesCard(
    section: ReviewSection.Entries,
    state: TailorUiState.Success,
    onOpenChange: (String) -> Unit,
    onOpenSource: (String) -> Unit,
    onRegenerate: (EntryCategory) -> Unit,
    onReportSection: (String) -> Unit,
) {
    val changed = section.entries.flatMap { it.bullets }.filter { it.isChange }
    HhCard {
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
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d8 - HhTheme.spacing.d2)) {
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
private fun SkillsCard(
    section: ReviewSection.Skills,
    onReportSection: (String) -> Unit,
) {
    HhCard {
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
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurface,
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
        Text(
            text = heading,
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        SectionCountLabel(changeCount = changeCount, reviewedCount = reviewedCount, canRegenerate = canRegenerate)
        Box {
            HhIconButton(
                icon = HhIcons.More,
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
    val colors = HhTheme.colors
    when {
        canRegenerate == null || changeCount == 0 -> Unit
        reviewedCount == changeCount -> Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = HhIcons.CheckCircle,
                contentDescription = null,
                tint = colors.brand,
                modifier = Modifier.size(HhTheme.spacing.d16),
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_section_reviewed),
                style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
                color = colors.brand,
            )
        }
        reviewedCount > 0 -> Text(
            text = stringResource(R.string.feature_tailor_impl_section_left, changeCount - reviewedCount),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurfaceVariant,
        )
        else -> Text(
            text = pluralStringResource(R.plurals.feature_tailor_impl_section_changes, changeCount, changeCount),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
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
                .widthIn(min = HhTheme.spacing.d64 * MENU_MIN_WIDTH_UNITS)
                .background(HhTheme.colors.surface, HhTheme.shapes.statusRow)
                .padding(HhTheme.spacing.d4),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4),
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
            .background(HhTheme.colors.sheet, HhTheme.shapes.banner)
            .border(BorderStroke(HhTheme.spacing.d2, HhTheme.colors.outlineVariant), HhTheme.shapes.banner)
            .heightIn(min = HhTheme.spacing.touch)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Text(
            text = title,
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = if (enabled) HhTheme.colors.onSurface else HhTheme.colors.onSurfaceVariant,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = HhTheme.typography.bodyS,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EntryHeader(entry: TailorEntryUi) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChangeLine(item: TailorBulletUi, onOpenChange: () -> Unit, onOpenSource: () -> Unit) {
    val text = item.displayText()
    if (!item.isChange) {
        Text(text = text, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurface)
        return
    }
    val markStyle = evidenceMarkSpanStyle()
    val marked = remember(item, markStyle) { item.lineText(markStyle) }
    val factIds = item.sources.map { it.displayId }.distinct()
    val description = stringResource(
        R.string.feature_tailor_impl_line_description,
        text,
        stringResource(item.state.labelChipRes()),
        factIds.joinToString(", "),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(HhTheme.colors.document, HhTheme.shapes.field)
            .border(BorderStroke(HhTheme.spacing.d2, HhTheme.colors.outlineVariant), HhTheme.shapes.field)
            .clickable(role = Role.Button, onClick = onOpenChange)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.md - HhTheme.spacing.d2),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d8 - HhTheme.spacing.d2)) {
            HhEvidenceText(text = marked, style = HhTheme.typography.bodyM)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d8 - HhTheme.spacing.d2),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d8 - HhTheme.spacing.d2),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                LineStateChip(item.state)
                if (item.isFlagged) {
                    LineChip(
                        label = stringResource(R.string.feature_tailor_impl_chip_verb_kept),
                        icon = HhIcons.Flag,
                        container = HhTheme.colors.document,
                        content = HhTheme.colors.coral,
                        border = BorderStroke(HhTheme.spacing.d2 * 0.75f, HhTheme.colors.coral),
                    )
                }
                factIds.forEach { factId ->
                    val sourceDescription = stringResource(R.string.feature_tailor_impl_source_description, factId)
                    Box(
                        modifier = Modifier
                            .defaultMinSize(minWidth = HhTheme.spacing.touch, minHeight = HhTheme.spacing.touch)
                            .clickable(role = Role.Button, onClick = onOpenSource)
                            .semantics { contentDescription = sourceDescription },
                        contentAlignment = Alignment.Center,
                    ) {
                        HhFactId(id = factId)
                    }
                }
            }
        }
    }
}

@Composable
private fun LineStateChip(state: BulletReviewState) {
    val colors = HhTheme.colors
    val (container, content, framed) = when (state) {
        BulletReviewState.TO_REVIEW, BulletReviewState.FLAGGED ->
            Triple(colors.special, colors.onSpecial, false)
        BulletReviewState.ACCEPTED ->
            Triple(colors.metContainer, colors.onMetContainer, false)
        BulletReviewState.ORIGINAL_KEPT, BulletReviewState.REPAIR_FAILED ->
            Triple(colors.document, colors.onSurfaceVariant, true)
        BulletReviewState.USER_EDITED ->
            Triple(colors.inverseSurface, colors.inverseOnSurface, false)
        BulletReviewState.STALE, BulletReviewState.UNCHANGED ->
            Triple(colors.card, colors.onSurfaceVariant, false)
    }
    LineChip(
        label = stringResource(state.labelChipRes()),
        icon = state.chipIcon(),
        container = container,
        content = content,
        border = if (framed) BorderStroke(HhTheme.spacing.d2, colors.outlineVariant) else null,
    )
}

@Composable
private fun LineChip(label: String, icon: ImageVector?, container: Color, content: Color, border: BorderStroke?) {
    Row(
        modifier = Modifier
            .background(container, HhTheme.shapes.pill)
            .then(if (border != null) Modifier.border(border, HhTheme.shapes.pill) else Modifier)
            .heightIn(min = HhTheme.spacing.d24)
            .padding(start = if (icon != null) HhTheme.spacing.d8 - HhTheme.spacing.d2 else HhTheme.spacing.d8, end = HhTheme.spacing.d8),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = content, modifier = Modifier.size(HhTheme.spacing.d12))
        }
        Text(
            text = label,
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = content,
        )
    }
}

private fun BulletReviewState.labelChipRes(): Int = when (this) {
    BulletReviewState.TO_REVIEW, BulletReviewState.FLAGGED -> R.string.feature_tailor_impl_chip_changed
    BulletReviewState.ACCEPTED -> R.string.feature_tailor_impl_state_accepted
    BulletReviewState.ORIGINAL_KEPT, BulletReviewState.REPAIR_FAILED -> R.string.feature_tailor_impl_state_original_kept
    BulletReviewState.USER_EDITED -> R.string.feature_tailor_impl_state_user_edited
    BulletReviewState.STALE -> R.string.feature_tailor_impl_state_source_changed
    BulletReviewState.UNCHANGED -> R.string.feature_tailor_impl_chip_changed
}

private fun BulletReviewState.chipIcon(): ImageVector? = when (this) {
    BulletReviewState.TO_REVIEW,
    BulletReviewState.FLAGGED,
    BulletReviewState.USER_EDITED,
    -> HhIcons.Edit
    BulletReviewState.ACCEPTED -> HhIcons.Check
    BulletReviewState.ORIGINAL_KEPT, BulletReviewState.REPAIR_FAILED -> HhIcons.ArrowBack
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

private fun TailorBulletUi.lineText(markStyle: SpanStyle): AnnotatedString {
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

private fun joinNotAdded(requirements: List<String>): String =
    requirements.joinToString(", ") { it.replaceFirstChar { char -> char.lowercaseChar() } }

private const val HOP_START_SCALE = 0.96f
private const val MENU_MIN_WIDTH_UNITS = 4
