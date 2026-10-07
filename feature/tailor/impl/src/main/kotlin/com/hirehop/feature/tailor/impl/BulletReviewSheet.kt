package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.hirehop.core.designsystem.component.HhButtonSize
import com.hirehop.core.designsystem.component.HhEvidenceMark
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.feature.tailor.impl.diff.DiffSegment
import com.hirehop.feature.tailor.impl.diff.WordDiff

internal data class BulletSheetActions(
    val onPrevious: (() -> Unit)?,
    val onNext: (() -> Unit)?,
    val onAccept: () -> Unit,
    val onKeepOriginal: () -> Unit,
    val onUndo: () -> Unit,
    val onEditByHand: () -> Unit,
    val onNextChange: () -> Unit,
    val onOpenSource: () -> Unit,
    val onReport: () -> Unit,
)

@Composable
internal fun BulletReviewSheetContent(
    item: TailorBulletUi,
    position: Int,
    total: Int,
    actions: BulletSheetActions,
    modifier: Modifier = Modifier,
    isReported: Boolean = false,
) {
    val state = item.state
    val description = bulletDescription(item, position, total)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        SheetHeader(item, position, total, actions)
        OriginalBlock(item)
        ChangedBlock(item)
        BulletNotice(item)
        VerbKeptCard(item)
        if (item.bullet.keywordsUsed.isNotEmpty() && state.showsNewText()) {
            KeywordRow(item.bullet.keywordsUsed)
        }
        SourceBlock(item.sources, actions.onOpenSource)
        BulletButtons(state, actions)
        FooterRow(position, actions, isReported)
    }
}

private fun BulletReviewState.showsNewText(): Boolean = when (this) {
    BulletReviewState.TO_REVIEW, BulletReviewState.FLAGGED, BulletReviewState.ACCEPTED -> true
    else -> false
}

@Composable
private fun bulletDescription(item: TailorBulletUi, position: Int, total: Int): String {
    val sources = item.sources.map { it.displayId }.distinct().joinToString(", ")
    return when (item.state) {
        BulletReviewState.USER_EDITED -> stringResource(
            R.string.feature_tailor_impl_bullet_description_edited,
            position,
            total,
            item.bullet.proposedText,
        )
        BulletReviewState.ORIGINAL_KEPT, BulletReviewState.REPAIR_FAILED -> stringResource(
            R.string.feature_tailor_impl_bullet_description_kept,
            position,
            total,
            item.bullet.originalText,
            sources,
        )
        else -> stringResource(
            R.string.feature_tailor_impl_bullet_description,
            position,
            total,
            item.bullet.originalText,
            item.bullet.proposedText,
            sources,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SheetHeader(item: TailorBulletUi, position: Int, total: Int, actions: BulletSheetActions) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f).padding(top = HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_bullet_position, position, total),
                style = HhTheme.typography.labelL,
                color = HhTheme.colors.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                if (item.state != BulletReviewState.TO_REVIEW) DecisionChip(item.state)
                item.bullet.editTypes.forEach { type ->
                    StatusPill(
                        label = stringResource(type.labelRes()),
                        icon = HhIcons.Edit,
                        color = HhTheme.colors.onSurface,
                    )
                }
            }
        }
        PagerButton(
            HhIcons.ArrowBack,
            stringResource(R.string.feature_tailor_impl_bullet_previous),
            actions.onPrevious,
        )
        PagerButton(
            HhIcons.ArrowForward,
            stringResource(R.string.feature_tailor_impl_bullet_next),
            actions.onNext,
        )
    }
}

@Composable
private fun PagerButton(icon: ImageVector, description: String, onClick: (() -> Unit)?) {
    HhIconButton(
        icon = icon,
        contentDescription = description,
        onClick = onClick ?: {},
        enabled = onClick != null,
    )
}

@Composable
private fun LabeledText(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        HhSectionLabel(text = label)
        content()
    }
}

@Composable
private fun OriginalBlock(item: TailorBulletUi) {
    val kept = item.state == BulletReviewState.ORIGINAL_KEPT || item.state == BulletReviewState.REPAIR_FAILED
    LabeledText(label = stringResource(R.string.feature_tailor_impl_bullet_chip_original)) {
        Text(
            text = item.bullet.originalText,
            style = if (kept) HhTheme.typography.titleM else HhTheme.typography.bodyM,
            color = if (kept) HhTheme.colors.onSurface else HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChangedBlock(item: TailorBulletUi) {
    val state = item.state
    val bullet = item.bullet
    if (state == BulletReviewState.USER_EDITED) {
        LabeledText(label = stringResource(R.string.feature_tailor_impl_bullet_your_edit)) {
            Text(text = bullet.proposedText, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
        }
        return
    }
    if (!state.showsNewText()) return
    val diff = remember(bullet.originalText, bullet.proposedText) {
        WordDiff.diff(bullet.originalText, bullet.proposedText)
    }
    val showsDiff = bullet.originalText.trim() != bullet.proposedText.trim()
    LabeledText(label = stringResource(R.string.feature_tailor_impl_bullet_new)) {
        if (showsDiff) {
            HhEvidenceText(
                text = diff.proposed.annotated(evidenceMarkSpanStyle()),
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onSurface,
            )
            AddedWordsLegend()
        } else {
            Text(
                text = stringResource(R.string.feature_tailor_impl_bullet_position_only),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddedWordsLegend() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_bullet_added_words),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhEvidenceMark(text = stringResource(R.string.feature_tailor_impl_bullet_added_sample), style = HhTheme.typography.labelM)
    }
}

private fun List<DiffSegment>.annotated(changed: SpanStyle): AnnotatedString =
    buildAnnotatedString {
        this@annotated.forEachIndexed { index, segment ->
            if (index > 0) append(" ")
            if (segment.changed) withStyle(changed) { append(segment.text) } else append(segment.text)
        }
    }

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordRow(keywords: List<String>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_bullet_keywords),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        keywords.forEach { keyword -> HhEvidenceMark(text = keyword, style = HhTheme.typography.labelM) }
    }
}

@Composable
private fun BulletNotice(item: TailorBulletUi) {
    val verbKept = item.flagViolation is GuardrailViolation.VerbEscalation
    when (item.state) {
        BulletReviewState.ACCEPTED -> NoticeStrip(
            text = stringResource(R.string.feature_tailor_impl_bullet_accepted_note),
            icon = HhIcons.CheckCircle,
            tone = BannerTone.Ok,
        )
        BulletReviewState.ORIGINAL_KEPT -> NoticeStrip(
            text = stringResource(R.string.feature_tailor_impl_bullet_kept_note),
            icon = HhIcons.Info,
        )
        BulletReviewState.REPAIR_FAILED -> NoticeStrip(
            text = stringResource(R.string.feature_tailor_impl_bullet_repair_failed),
            icon = HhIcons.Flag,
            tone = BannerTone.Warn,
        )
        BulletReviewState.USER_EDITED -> NoteLine(
            text = stringResource(R.string.feature_tailor_impl_bullet_user_edited_note),
            icon = HhIcons.Info,
        )
        BulletReviewState.FLAGGED -> if (!verbKept) {
            NoticeStrip(
                text = item.flagViolation?.flagNote(item.sources.firstOrNull()?.displayId.orEmpty()).orEmpty(),
                icon = HhIcons.Flag,
                tone = BannerTone.Warn,
            )
        }
        else -> Unit
    }
}

@Composable
private fun VerbKeptCard(item: TailorBulletUi) {
    val violation = item.flagViolation as? GuardrailViolation.VerbEscalation ?: return
    val colors = HhTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.partialContainer, HhTheme.shapes.banner)
            .padding(horizontal = HhTheme.spacing.lg, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Flag,
            contentDescription = null,
            tint = colors.partial,
            modifier = Modifier.size(22.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_bullet_verb_kept_title),
                style = HhTheme.typography.titleS,
                color = colors.onSurface,
            )
            Text(text = verbKeptText(violation), style = HhTheme.typography.bodyS, color = colors.onSurface)
        }
    }
}

@Composable
private fun verbKeptText(violation: GuardrailViolation.VerbEscalation): AnnotatedString = buildAnnotatedString {
    append(stringResource(R.string.feature_tailor_impl_bullet_verb_kept_said))
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(violation.to) }
    append(stringResource(R.string.feature_tailor_impl_bullet_verb_kept_fact))
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(violation.from) }
    append(stringResource(R.string.feature_tailor_impl_bullet_verb_kept_rule))
}

@Composable
private fun SourceBlock(sources: List<TailoredBulletSource>, onClick: () -> Unit) {
    val distinct = sources.distinctBy { it.displayId }
    if (distinct.isEmpty()) return
    LabeledText(
        label = pluralStringResource(R.plurals.feature_tailor_impl_bullet_source_label, distinct.size),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            distinct.forEach { source -> SourceCard(source, onClick) }
        }
    }
}

@Composable
private fun SourceCard(source: TailoredBulletSource, onClick: () -> Unit) {
    val colors = HhTheme.colors
    val description = stringResource(R.string.feature_tailor_impl_bullet_open_source, source.displayId)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .background(colors.card, HhTheme.shapes.statusRow)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = HhTheme.spacing.md + HhTheme.spacing.d2, vertical = HhTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhFactId(id = source.displayId)
            HhProvenanceChip(
                kind = source.source.provenanceKind(),
                label = stringResource(source.source.provenanceRes()),
            )
        }
        Text(text = sourceTitle(source), style = HhTheme.typography.titleM, color = colors.onSurface)
    }
}

@Composable
private fun sourceTitle(source: TailoredBulletSource): String =
    source.entryTitle.ifEmpty { stringResource(source.category.headingRes()) }
        .let { title -> listOf(title, source.organization).filter { it.isNotEmpty() }.joinToString(", ") }

@Composable
private fun BulletButtons(state: BulletReviewState, actions: BulletSheetActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        when (state) {
            BulletReviewState.TO_REVIEW, BulletReviewState.FLAGGED -> {
                HhPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_accept),
                    onClick = actions.onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = HhIcons.Check,
                )
                SecondaryRow {
                    CompactOutline(R.string.feature_tailor_impl_bullet_keep_original, actions.onKeepOriginal, null)
                    CompactOutline(R.string.feature_tailor_impl_bullet_edit_by_hand, actions.onEditByHand, HhIcons.Edit)
                }
            }
            BulletReviewState.USER_EDITED -> {
                NextChangeButton(actions)
                SecondaryRow {
                    CompactOutline(R.string.feature_tailor_impl_bullet_undo, actions.onUndo, HhIcons.ArrowBack)
                    CompactOutline(R.string.feature_tailor_impl_bullet_edit_again, actions.onEditByHand, HhIcons.Edit)
                }
            }
            BulletReviewState.REPAIR_FAILED -> {
                NextChangeButton(actions)
                SecondaryRow {
                    CompactOutline(R.string.feature_tailor_impl_bullet_edit_by_hand, actions.onEditByHand, HhIcons.Edit)
                }
            }
            else -> {
                NextChangeButton(actions)
                SecondaryRow {
                    CompactOutline(R.string.feature_tailor_impl_bullet_undo, actions.onUndo, HhIcons.ArrowBack)
                }
            }
        }
    }
}

@Composable
private fun NextChangeButton(actions: BulletSheetActions) {
    HhPrimaryButton(
        label = stringResource(R.string.feature_tailor_impl_bullet_next_change),
        onClick = actions.onNextChange,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SecondaryRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        content = content,
    )
}

@Composable
private fun RowScope.CompactOutline(
    labelRes: Int,
    onClick: () -> Unit,
    icon: ImageVector?,
) {
    HhOutlineButton(
        label = stringResource(labelRes),
        onClick = onClick,
        modifier = Modifier.weight(1f),
        leadingIcon = icon,
        size = HhButtonSize.Compact,
    )
}

@Composable
private fun FooterRow(position: Int, actions: BulletSheetActions, isReported: Boolean) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            HhIconButton(
                icon = HhIcons.More,
                contentDescription = stringResource(R.string.feature_tailor_impl_bullet_more, position),
                onClick = { menuOpen = true },
                containerColor = Color.Transparent,
            )
            if (menuOpen) {
                Popup(
                    alignment = Alignment.TopEnd,
                    onDismissRequest = { menuOpen = false },
                    properties = PopupProperties(focusable = true),
                ) {
                    Text(
                        text = stringResource(
                            if (isReported) R.string.feature_tailor_impl_menu_reported else R.string.feature_tailor_impl_menu_report,
                        ),
                        style = HhTheme.typography.bodyM,
                        color = if (isReported) HhTheme.colors.onSurfaceVariant else HhTheme.colors.onSurface,
                        modifier = Modifier
                            .background(HhTheme.colors.surface, HhTheme.shapes.banner)
                            .clickable(enabled = !isReported, role = Role.Button) {
                                menuOpen = false
                                actions.onReport()
                            }
                            .defaultMinSize(minHeight = HhTheme.spacing.touch)
                            .padding(horizontal = HhTheme.spacing.lg, vertical = HhTheme.spacing.md),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditByHandContent(
    text: String,
    onTextChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    showsError: Boolean = false,
    linkedFactIds: List<String> = emptyList(),
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_edit_hand_title),
            style = HhTheme.typography.headlineM,
            color = HhTheme.colors.onSurface,
        )
        val words = remember(text) {
            text.trim().split(WHITESPACE).count { it.isNotEmpty() }
        }
        HhTextField(
            value = text,
            onValueChange = onTextChange,
            label = stringResource(R.string.feature_tailor_impl_edit_hand_field),
            singleLine = false,
            minLines = 3,
            errorText = if (showsError) {
                stringResource(R.string.feature_tailor_impl_edit_sheet_error_required)
            } else {
                null
            },
            trailingSlot = {
                Text(
                    text = pluralStringResource(R.plurals.feature_tailor_impl_edit_hand_words, words, words),
                    style = HhTheme.typography.bodyS,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        if (linkedFactIds.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_edit_hand_linked_to),
                    style = HhTheme.typography.labelM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
                linkedFactIds.forEach { id -> HhFactId(id = id) }
            }
        }
        NoteLine(text = stringResource(R.string.feature_tailor_impl_edit_hand_unchecked), icon = HhIcons.Info)
        NewFactNote()
        HhPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_edit_sheet_save),
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
        )
        HhSecondaryButton(
            label = stringResource(R.string.feature_tailor_impl_edit_sheet_cancel),
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun NewFactNote() {
    NoteLine(
        text = buildString {
            append(stringResource(R.string.feature_tailor_impl_edit_hand_new_fact_a))
            append(" ")
            append(stringResource(R.string.feature_tailor_impl_edit_hand_new_fact_b))
            append(stringResource(R.string.feature_tailor_impl_edit_hand_new_fact_c))
        },
        icon = HhIcons.Info,
    )
}

@Composable
internal fun SourceFactSheetContent(
    sources: List<TailoredBulletSource>,
    onEditFact: (TailoredBulletSource) -> Unit,
    modifier: Modifier = Modifier,
    onReport: (() -> Unit)? = null,
    isReported: Boolean = false,
    onReviewChange: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null,
) {
    val colors = HhTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_source_sheet_title),
            style = HhTheme.typography.headlineM,
            color = colors.onSurface,
        )
        HhSectionLabel(text = pluralStringResource(R.plurals.feature_tailor_impl_bullet_source_label, sources.size))
        sources.forEach { source -> SourceFactCard(source, onEditFact) }
        if (onReport != null) {
            HhTextButton(
                label = stringResource(
                    if (isReported) R.string.feature_tailor_impl_menu_reported else R.string.feature_tailor_impl_menu_report,
                ),
                onClick = onReport,
                enabled = !isReported,
                leadingIcon = HhIcons.Flag,
            )
        }
        if (onReviewChange != null) {
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_source_sheet_review_change),
                onClick = onReviewChange,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (onClose != null) {
            HhSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_source_sheet_close),
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SourceFactCard(source: TailoredBulletSource, onEditFact: (TailoredBulletSource) -> Unit) {
    val colors = HhTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, HhTheme.shapes.card)
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.d2),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhFactId(id = source.displayId)
            HhProvenanceChip(
                kind = source.source.provenanceKind(),
                label = stringResource(source.source.provenanceRes()),
            )
        }
        Text(text = sourceTitle(source), style = HhTheme.typography.titleM, color = colors.onSurface)
        Text(text = sourceDetail(source), style = HhTheme.typography.bodyS, color = colors.onSurfaceVariant)
        HhOutlineButton(
            label = stringResource(R.string.feature_tailor_impl_source_sheet_edit_fact),
            onClick = { onEditFact(source) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = HhIcons.Edit,
            size = HhButtonSize.Compact,
        )
    }
}

private fun sourceDetail(source: TailoredBulletSource): String =
    listOf(source.dateRange, source.text).filter { it.isNotEmpty() }.joinToString(" · ")

private val WHITESPACE = Regex("\\s+")
