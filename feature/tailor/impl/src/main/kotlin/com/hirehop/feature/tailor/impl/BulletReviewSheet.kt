package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.hirehop.core.designsystem.component.HhDecoration
import com.hirehop.core.designsystem.component.HhDecorationKind
import com.hirehop.core.designsystem.component.HhEvidenceMark
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhIconActionBar
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.GuardrailViolation
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
        SheetHeader(item, position, total, actions)
        if (state == BulletReviewState.ACCEPTED) {
            AcceptedBanner(left = total - position)
        }
        OriginalBlock(item, markStyle)
        ChangedBlock(item, markStyle)
        if (item.bullet.keywordsUsed.isNotEmpty() && state.showsNewText()) {
            KeywordRow(item.bullet.keywordsUsed)
        }
        BulletNotice(item)
        VerbKeptCard(item)
        if (state.showsNewText() && state != BulletReviewState.USER_EDITED) {
            FactChipRow(item.sources, onClick = actions.onOpenSource)
        }
        item.sources.distinctBy { it.displayId }.forEach { source ->
            SourceCard(source, onClick = actions.onOpenSource)
        }
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

@Composable
private fun SheetHeader(item: TailorBulletUi, position: Int, total: Int, actions: BulletSheetActions) {
    val entry = bulletEntryLine(item)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
        ) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_bullet_position, position, total),
                style = HhTheme.typography.titleL,
                color = HhTheme.colors.onSurface,
            )
            if (entry.isNotEmpty()) {
                Text(
                    text = entry,
                    style = HhTheme.typography.labelM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
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
        containerColor = Color.Transparent,
        borderColor = Color.Transparent,
    )
}

@Composable
private fun bulletEntryLine(item: TailorBulletUi): String {
    val source = item.sources.firstOrNull() ?: return ""
    val head = source.entryTitle.ifEmpty { stringResource(source.category.headingRes()) }
    return listOf(head, source.organization)
        .filter { it.isNotEmpty() }
        .joinToString(" · ")
}

@Composable
private fun AcceptedBanner(left: Int) {
    val colors = HhTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.brand, HhTheme.shapes.statusRow),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = HhTheme.spacing.d48 + HhTheme.spacing.d8)
                .padding(start = HhTheme.spacing.d8, end = HhTheme.spacing.cardPadding, top = HhTheme.spacing.sm, bottom = HhTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = HhIcons.CheckCircle,
                contentDescription = null,
                tint = colors.onBrand,
                modifier = Modifier.size(HhTheme.spacing.d32 + HhTheme.spacing.xs),
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_bullet_accepted_note, left),
                style = HhTheme.typography.titleM,
                color = colors.onBrand,
            )
        }
        HhDecoration(
            kind = HhDecorationKind.Squiggle,
            color = colors.special,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = -HhTheme.spacing.md, y = HhTheme.spacing.sm),
        )
    }
}

@Composable
private fun OriginalBlock(item: TailorBulletUi, markStyle: SpanStyle) {
    val bullet = item.bullet
    val diff = remember(bullet.originalText, bullet.proposedText) {
        WordDiff.diff(bullet.originalText, bullet.proposedText)
    }
    val showsDiff = item.state.showsNewText() && bullet.originalText.trim() != bullet.proposedText.trim()
    LabeledText(
        label = stringResource(R.string.feature_tailor_impl_bullet_chip_original),
        labelColor = HhTheme.colors.onSurfaceVariant,
    ) {
        val struck = SpanStyle(
            background = HhTheme.colors.neutralContainer,
            textDecoration = TextDecoration.LineThrough,
        )
        HhEvidenceText(
            text = if (showsDiff) diff.original.annotated(struck) else AnnotatedString(bullet.originalText),
            style = HhTheme.typography.bodyM,
            color = if (showsDiff) HhTheme.colors.onSurfaceVariant else HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun ChangedBlock(item: TailorBulletUi, markStyle: SpanStyle) {
    val state = item.state
    if (state == BulletReviewState.USER_EDITED) {
        UserEditBlock(item)
        return
    }
    val bullet = item.bullet
    val diff = remember(bullet.originalText, bullet.proposedText) {
        WordDiff.diff(bullet.originalText, bullet.proposedText)
    }
    val showsDiff = state.showsNewText() && bullet.originalText.trim() != bullet.proposedText.trim()
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        DecisionChip(state)
        if (showsDiff) {
            HhEvidenceText(
                text = diff.proposed.annotated(markStyle),
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onSurface,
            )
        } else if (state.showsNewText()) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_bullet_position_only),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UserEditBlock(item: TailorBulletUi) {
    val colors = HhTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(HhTheme.spacing.d2, colors.onSurface), HhTheme.shapes.statusRow)
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        InkChip(stringResource(R.string.feature_tailor_impl_bullet_edited_by_you))
        Text(
            text = item.bullet.proposedText,
            style = HhTheme.typography.titleM,
            color = colors.onSurface,
        )
        BackedByRow(
            item.sources,
            onClick = BulletSheetActions(
                onPrevious = null,
                onNext = null,
                onAccept = {},
                onKeepOriginal = {},
                onUndo = {},
                onEditByHand = {},
                onNextChange = {},
                onOpenSource = {},
                onReport = {},
            ).onOpenSource,
        )
    }
}

@Composable
private fun InkChip(label: String) {
    val colors = HhTheme.colors
    Row(
        modifier = Modifier
            .background(colors.inverseSurface, HhTheme.shapes.pill)
            .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = HhIcons.Edit,
            contentDescription = null,
            tint = colors.inverseOnSurface,
            modifier = Modifier.size(HhTheme.spacing.d12),
        )
        Text(
            text = label,
            style = HhTheme.typography.labelM,
            color = colors.inverseOnSurface,
        )
    }
}

@Composable
private fun BackedByRow(sources: List<TailoredBulletSource>, onClick: () -> Unit) {
    val colors = HhTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.CheckCircle,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier
                .padding(top = HhTheme.spacing.xxs)
                .size(HhTheme.spacing.lg),
        )
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_bullet_backed_by),
                style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
                color = colors.primary,
            )
            FactChipRow(sources, onClick)
        }
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
    val verbKept = item.flagViolation is GuardrailViolation.VerbEscalation
    val note = when (item.state) {
        BulletReviewState.FLAGGED -> if (verbKept) {
            null
        } else {
            item.flagViolation?.flagNote(item.sources.firstOrNull()?.displayId.orEmpty())
        }
        BulletReviewState.REPAIR_FAILED -> stringResource(R.string.feature_tailor_impl_bullet_repair_failed)
        BulletReviewState.USER_EDITED -> stringResource(R.string.feature_tailor_impl_bullet_user_edited_note)
        else -> null
    } ?: return
    val icon = if (item.state == BulletReviewState.FLAGGED) HhIcons.Flag else HhIcons.Verified
    NoticeStrip(text = note, icon = icon)
}

@Composable
private fun VerbKeptCard(item: TailorBulletUi) {
    val violation = item.flagViolation as? GuardrailViolation.VerbEscalation ?: return
    val colors = HhTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(HhTheme.spacing.d2, colors.error), HhTheme.shapes.statusRow)
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Flag,
            contentDescription = null,
            tint = colors.error,
            modifier = Modifier.size(HhTheme.spacing.d20),
        )
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_bullet_verb_kept_title),
                style = HhTheme.typography.titleM,
                color = colors.error,
            )
            Text(
                text = verbKeptText(violation),
                style = HhTheme.typography.bodyM,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
private fun verbKeptText(violation: GuardrailViolation.VerbEscalation): AnnotatedString = buildAnnotatedString {
    append(stringResource(R.string.feature_tailor_impl_bullet_verb_kept_said))
    withStyle(
        SpanStyle(
            color = HhTheme.colors.error,
            fontWeight = FontWeight.Bold,
            textDecoration = TextDecoration.LineThrough,
        ),
    ) { append(violation.to) }
    append(stringResource(R.string.feature_tailor_impl_bullet_verb_kept_fact))
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(violation.from) }
    append(stringResource(R.string.feature_tailor_impl_bullet_verb_kept_rule))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactChipRow(sources: List<TailoredBulletSource>, onClick: () -> Unit) {
    val distinct = sources.distinctBy { it.displayId }
    if (distinct.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        distinct.forEach { source -> FactChip(source, onClick) }
    }
}

@Composable
private fun FactChip(source: TailoredBulletSource, onClick: () -> Unit) {
    val description = stringResource(R.string.feature_tailor_impl_bullet_open_source, source.displayId)
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        HhFactId(id = source.displayId)
    }
}

@Composable
private fun SourceCard(source: TailoredBulletSource, onClick: () -> Unit) {
    val colors = HhTheme.colors
    val markStyle = evidenceMarkSpanStyle()
    val description = stringResource(R.string.feature_tailor_impl_bullet_open_source, source.displayId)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .background(colors.primaryContainer, HhTheme.shapes.statusRow)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description }
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhFactId(id = source.displayId)
            Text(
                text = sourceEntryLine(source),
                style = HhTheme.typography.labelM,
                color = colors.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            HhProvenanceChip(
                kind = source.source.provenanceKind(),
                label = stringResource(source.source.provenanceRes()),
            )
        }
        HhEvidenceText(
            text = markedFact(source.text, markStyle),
            style = HhTheme.typography.bodyM,
            color = colors.onPrimaryContainer,
        )
    }
}

@Composable
private fun BulletButtons(state: BulletReviewState, actions: BulletSheetActions) {
    when (state) {
        BulletReviewState.TO_REVIEW, BulletReviewState.FLAGGED -> {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                HhPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_accept),
                    onClick = actions.onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = HhIcons.Check,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                ) {
                    HhOutlineButton(
                        label = stringResource(R.string.feature_tailor_impl_bullet_keep_original),
                        onClick = actions.onKeepOriginal,
                        modifier = Modifier.weight(1f),
                    )
                    HhOutlineButton(
                        label = stringResource(R.string.feature_tailor_impl_bullet_edit_by_hand),
                        onClick = actions.onEditByHand,
                        modifier = Modifier.weight(1f),
                        leadingIcon = HhIcons.Edit,
                    )
                }
            }
        }
        BulletReviewState.USER_EDITED -> {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                HhTextButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_go_back_suggestion),
                    onClick = actions.onUndo,
                    leadingIcon = HhIcons.ArrowBack,
                )
                HhIconActionBar(
                    secondaryIcon = HhIcons.Edit,
                    secondaryContentDescription = stringResource(R.string.feature_tailor_impl_bullet_edit_again),
                    onSecondaryClick = actions.onEditByHand,
                    primaryLabel = stringResource(R.string.feature_tailor_impl_bullet_next_change),
                    onPrimaryClick = actions.onNextChange,
                    primaryTrailingIcon = HhIcons.ArrowForward,
                )
            }
        }
        BulletReviewState.REPAIR_FAILED -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                HhOutlineButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_edit_by_hand),
                    onClick = actions.onEditByHand,
                    modifier = Modifier.weight(1f),
                    leadingIcon = HhIcons.Edit,
                )
                HhPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_next_change),
                    onClick = actions.onNextChange,
                    modifier = Modifier.weight(1f),
                    trailingIcon = HhIcons.ArrowForward,
                )
            }
        }
        else -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                HhOutlineButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_undo),
                    onClick = actions.onUndo,
                    modifier = Modifier.weight(1f),
                    leadingIcon = HhIcons.ArrowBack,
                )
                HhPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_bullet_next_change),
                    onClick = actions.onNextChange,
                    modifier = Modifier.weight(1f),
                    trailingIcon = HhIcons.ArrowForward,
                )
            }
        }
    }
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
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_edit_hand_title),
                style = HhTheme.typography.headlineM,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_edit_sheet_title, position),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
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
        NewFactNote()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
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
private fun NewFactNote() {
    val colors = HhTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Info,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(HhTheme.spacing.d20),
        )
        Text(
            text = buildAnnotatedString {
                append(stringResource(R.string.feature_tailor_impl_edit_hand_new_fact_a))
                append(" ")
                withStyle(SpanStyle(color = colors.primary, fontWeight = FontWeight.Bold)) {
                    append(stringResource(R.string.feature_tailor_impl_edit_hand_new_fact_b))
                }
                append(stringResource(R.string.feature_tailor_impl_edit_hand_new_fact_c))
            },
            style = HhTheme.typography.bodyM,
            color = colors.onSurfaceVariant,
        )
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
    val markStyle = evidenceMarkSpanStyle()
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        Text(
            text = stringResource(
                if (sources.size > 1) {
                    R.string.feature_tailor_impl_source_sheet_title_many
                } else {
                    R.string.feature_tailor_impl_source_sheet_title
                },
            ),
            style = HhTheme.typography.titleL,
            color = colors.onSurface,
        )
        sources.forEach { source ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.primaryContainer, HhTheme.shapes.statusRow)
                    .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
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
                Text(
                    text = sourceEntryLine(source),
                    style = HhTheme.typography.titleS,
                    color = colors.onPrimaryContainer,
                )
                HhEvidenceText(
                    text = markedFact(source.text, markStyle),
                    style = HhTheme.typography.bodyM,
                    color = colors.onPrimaryContainer,
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

private val WHITESPACE = Regex("\\s+")
