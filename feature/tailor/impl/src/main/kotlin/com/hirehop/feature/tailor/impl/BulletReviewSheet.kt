package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.hirehop.core.designsystem.component.HhEditTypeTag
import com.hirehop.core.designsystem.component.HhEvidenceMark
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
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
    val markStyle = evidenceMarkSpanStyle()
    val description = bulletDescription(item, position, total)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        PagerRow(position, total, actions)
        TagRow(item)
        BulletTexts(item, markStyle)
        if (item.bullet.keywordsUsed.isNotEmpty() && state.showsNewText()) {
            KeywordRow(item.bullet.keywordsUsed)
        }
        BulletNotice(item)
        item.sources.distinctBy { it.displayId }.forEach { source ->
            SourceRow(source, onClick = actions.onOpenSource)
        }
        BulletButtons(state, actions)
        FooterRow(item, position, actions, isReported)
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

@Composable
private fun PagerRow(position: Int, total: Int, actions: BulletSheetActions) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PagerButton(HhIcons.ArrowBack, stringResource(R.string.feature_tailor_impl_bullet_previous), actions.onPrevious)
        Text(
            text = stringResource(R.string.feature_tailor_impl_bullet_position, position, total),
            style = HhTheme.typography.titleS,
            color = HhTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
        PagerButton(HhIcons.ArrowForward, stringResource(R.string.feature_tailor_impl_bullet_next), actions.onNext)
    }
}

@Composable
private fun PagerButton(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: (() -> Unit)?) {
    HhIconButton(
        icon = icon,
        contentDescription = description,
        onClick = onClick ?: {},
        enabled = onClick != null,
        containerColor = Color.Transparent,
        borderColor = Color.Transparent,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagRow(item: TailorBulletUi) {
    val state = item.state
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        when (state) {
            BulletReviewState.TO_REVIEW, BulletReviewState.FLAGGED ->
                item.bullet.editTypes.forEach { HhEditTypeTag(label = stringResource(it.labelRes())) }
            else -> DecisionChip(state)
        }
    }
}

@Composable
private fun BulletTexts(item: TailorBulletUi, markStyle: SpanStyle) {
    val state = item.state
    val bullet = item.bullet
    val diff = remember(bullet.originalText, bullet.proposedText) {
        WordDiff.diff(bullet.originalText, bullet.proposedText)
    }
    val showsDiff = state.showsNewText() && bullet.originalText.trim() != bullet.proposedText.trim()
    LabeledText(
        label = stringResource(R.string.feature_tailor_impl_bullet_original),
        labelColor = HhTheme.colors.onSurfaceVariant,
    ) {
        val struck = SpanStyle(background = HhTheme.colors.neutralContainer, textDecoration = TextDecoration.LineThrough)
        HhEvidenceText(
            text = if (showsDiff) diff.original.annotated(struck) else AnnotatedString(bullet.originalText),
            style = HhTheme.typography.bodyM,
            color = if (showsDiff) HhTheme.colors.onSurfaceVariant else HhTheme.colors.onSurface,
        )
    }
    when {
        state == BulletReviewState.USER_EDITED -> LabeledText(
            label = stringResource(R.string.feature_tailor_impl_bullet_your_edit),
            labelColor = HhTheme.colors.primary,
        ) {
            Text(
                text = bullet.proposedText,
                style = HhTheme.typography.bodyL,
                fontWeight = FontWeight.SemiBold,
                color = HhTheme.colors.onSurface,
            )
        }
        showsDiff -> LabeledText(
            label = stringResource(R.string.feature_tailor_impl_bullet_new),
            labelColor = HhTheme.colors.primary,
        ) {
            HhEvidenceText(
                text = diff.proposed.annotated(markStyle),
                style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            )
        }
        state.showsNewText() -> Text(
            text = stringResource(R.string.feature_tailor_impl_bullet_position_only),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

private fun List<com.hirehop.feature.tailor.impl.diff.DiffSegment>.annotated(changed: SpanStyle): AnnotatedString =
    buildAnnotatedString {
        this@annotated.forEachIndexed { index, segment ->
            if (index > 0) append(" ")
            if (segment.changed) withStyle(changed) { append(segment.text) } else append(segment.text)
        }
    }

@Composable
private fun LabeledText(label: String, labelColor: Color, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        Text(text = label, style = HhTheme.typography.labelM, color = labelColor)
        content()
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
    val note = when (item.state) {
        BulletReviewState.FLAGGED -> item.flagViolation?.flagNote(item.sources.firstOrNull()?.displayId.orEmpty())
        BulletReviewState.REPAIR_FAILED -> stringResource(R.string.feature_tailor_impl_bullet_repair_failed)
        BulletReviewState.USER_EDITED -> stringResource(R.string.feature_tailor_impl_bullet_user_edited_note)
        else -> null
    } ?: return
    val icon = if (item.state == BulletReviewState.FLAGGED) HhIcons.Flag else HhIcons.Verified
    NoticeStrip(text = note, icon = icon)
}

@Composable
private fun SourceRow(source: TailoredBulletSource, onClick: () -> Unit) {
    val colors = HhTheme.colors
    val description = stringResource(R.string.feature_tailor_impl_bullet_open_source, source.displayId)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .background(colors.card, HhTheme.shapes.banner)
            .dashedBorder(colors.outline, HhTheme.spacing.d2 / 2, HhTheme.spacing.d16)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = HhTheme.spacing.md - HhTheme.spacing.d2, vertical = HhTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = source.displayId, style = HhTheme.typography.factId, color = colors.primary)
        Text(
            text = listOf(source.entryTitle, source.organization).filter { it.isNotEmpty() }.joinToString(", "),
            style = HhTheme.typography.labelM,
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        HhProvenanceChip(kind = source.source.provenanceKind(), label = stringResource(source.source.provenanceRes()))
    }
}

@Composable
private fun BulletButtons(state: BulletReviewState, actions: BulletSheetActions) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        when (state) {
            BulletReviewState.TO_REVIEW, BulletReviewState.FLAGGED -> {
                HhSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_keep_original),
                    onClick = actions.onKeepOriginal,
                    modifier = Modifier.weight(1f),
                )
                HhSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_accept),
                    onClick = actions.onAccept,
                    modifier = Modifier.weight(1f),
                )
            }
            BulletReviewState.USER_EDITED, BulletReviewState.REPAIR_FAILED -> {
                HhSecondaryButton(
                    label = stringResource(
                        if (state == BulletReviewState.USER_EDITED) {
                            R.string.feature_tailor_impl_bullet_edit_again
                        } else {
                            R.string.feature_tailor_impl_bullet_edit_by_hand
                        },
                    ),
                    onClick = actions.onEditByHand,
                    modifier = Modifier.weight(1f),
                )
                HhSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_next),
                    onClick = actions.onNextChange,
                    modifier = Modifier.weight(1f),
                )
            }
            else -> {
                HhSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_undo),
                    onClick = actions.onUndo,
                    modifier = Modifier.weight(1f),
                )
                HhSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_next),
                    onClick = actions.onNextChange,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun FooterRow(item: TailorBulletUi, position: Int, actions: BulletSheetActions, isReported: Boolean) {
    var menuOpen by remember { mutableStateOf(false) }
    val showsEditLink = item.state == BulletReviewState.TO_REVIEW || item.state == BulletReviewState.FLAGGED
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showsEditLink) {
            HhTextButton(
                label = stringResource(R.string.feature_tailor_impl_bullet_edit_by_hand),
                onClick = actions.onEditByHand,
            )
        } else {
            Box(Modifier)
        }
        Box {
            HhIconButton(
                icon = HhIcons.More,
                contentDescription = stringResource(R.string.feature_tailor_impl_bullet_more, position),
                onClick = { menuOpen = true },
                containerColor = Color.Transparent,
                borderColor = Color.Transparent,
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

@Composable
internal fun EditByHandContent(
    position: Int,
    text: String,
    onTextChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
    showsError: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_edit_sheet_title, position),
            style = HhTheme.typography.titleL,
            color = HhTheme.colors.onSurface,
        )
        HhTextField(
            value = text,
            onValueChange = onTextChange,
            label = stringResource(R.string.feature_tailor_impl_edit_sheet_label),
            singleLine = false,
            minLines = 3,
            errorText = if (showsError) stringResource(R.string.feature_tailor_impl_edit_sheet_error_required) else null,
            modifier = Modifier.fillMaxWidth(),
        )
        NoticeStrip(text = stringResource(R.string.feature_tailor_impl_edit_sheet_note), icon = HhIcons.Verified)
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhOutlineButton(
                label = stringResource(R.string.feature_tailor_impl_edit_sheet_cancel),
                onClick = onCancel,
                modifier = Modifier.weight(1f),
            )
            HhPrimaryButton(
                label = stringResource(R.string.feature_tailor_impl_edit_sheet_save),
                onClick = onSave,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun SourceFactSheetContent(
    sources: List<TailoredBulletSource>,
    onEditFact: (TailoredBulletSource) -> Unit,
    modifier: Modifier = Modifier,
    onReport: (() -> Unit)? = null,
    isReported: Boolean = false,
) {
    val colors = HhTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Text(
            text = stringResource(
                if (sources.size > 1) {
                    R.string.feature_tailor_impl_source_sheet_title_many
                } else {
                    R.string.feature_tailor_impl_source_sheet_title
                },
            ),
            style = HhTheme.typography.labelM,
            color = colors.onSurfaceVariant,
        )
        sources.forEach { source ->
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = source.displayId, style = HhTheme.typography.factId, color = colors.primary)
                    HhProvenanceChip(
                        kind = source.source.provenanceKind(),
                        label = stringResource(source.source.provenanceRes()),
                    )
                }
                Text(
                    text = sourceEntryLine(source),
                    style = HhTheme.typography.titleM,
                    color = colors.onSurface,
                )
                HhEvidenceText(
                    text = markedFact(source.text, evidenceMarkSpanStyle()),
                    style = HhTheme.typography.bodyL,
                    color = colors.body,
                )
                HhOutlineButton(
                    label = stringResource(R.string.feature_tailor_impl_source_sheet_edit_fact),
                    onClick = { onEditFact(source) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = HhIcons.Edit,
                )
            }
        }
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
    }
}

@Composable
private fun sourceEntryLine(source: TailoredBulletSource): String = listOf(source.entryTitle, source.organization)
    .filter { it.isNotEmpty() }
    .joinToString(", ")
    .let { line ->
        if (source.dateRange.isEmpty()) line else "$line · ${source.dateRange}"
    }

private fun markedFact(text: String, style: SpanStyle): AnnotatedString =
    buildAnnotatedString { withStyle(style) { append(text) } }
