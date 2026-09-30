package com.hirehop.feature.tailor.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhEvidenceMark
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhStatusDisc
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
import com.hirehop.core.model.FactSource
import com.hirehop.core.ui.FactIdTag
import com.hirehop.core.ui.FactProvenanceChip
import com.hirehop.feature.tailor.impl.diff.DiffSegment
import com.hirehop.feature.tailor.impl.diff.WordDiff

@Composable
internal fun BulletCard(
    item: TailorBulletUi,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (item.kind) {
        BulletReviewKind.STALE -> StaleBulletCard(item, modifier)
        BulletReviewKind.VIOLATION -> ViolationBulletCard(item, modifier)
        BulletReviewKind.UNCHANGED -> UnchangedBulletCard(item, modifier)
        BulletReviewKind.REVIEWABLE -> ReviewableBulletCard(item, onAccept, onReject, modifier)
    }
}

@Composable
private fun ReviewableBulletCard(
    item: TailorBulletUi,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bullet = item.bullet
    val announcement = item.cardAnnouncement()
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhCard(modifier = Modifier.clearAndSetSemantics { contentDescription = announcement }) {
            if (bullet.originalText.trim() == bullet.proposedText.trim()) {
                PositionOnlyChange(bullet.originalText)
            } else {
                TextChange(bullet.originalText, bullet.proposedText)
            }
            EditTypeChips(bullet.editTypes)
            KeywordChips(bullet.keywordsUsed)
            SourceFacts(item)
        }
        DecisionSection(bullet.proposedText, bullet.decision, onAccept, onReject)
    }
}

@Composable
private fun TextChange(original: String, proposed: String) {
    val diff = remember(original, proposed) { WordDiff.diff(original, proposed) }
    LabeledDiff(
        labelRes = R.string.feature_tailor_impl_original,
        segments = diff.original,
        highlight = HhTheme.colors.surface3,
        changeDecoration = TextDecoration.LineThrough,
        changedWordsRes = R.string.feature_tailor_impl_removed_words,
    )
    LabeledDiff(
        labelRes = R.string.feature_tailor_impl_proposed,
        segments = diff.proposed,
        highlight = HhTheme.colors.evidence,
        changeDecoration = TextDecoration.None,
        changedWordsRes = R.string.feature_tailor_impl_changed_words,
    )
}

@Composable
private fun PositionOnlyChange(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_original),
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = text,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_position_only),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun StaleBulletCard(item: TailorBulletUi, modifier: Modifier = Modifier) {
    HhCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_stale_title),
            style = HhTheme.typography.titleSmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_stale_body),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhDivider()
        Text(
            text = item.bullet.originalText,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurface,
        )
        SourceFacts(item)
    }
}

@Composable
private fun UnchangedBulletCard(item: TailorBulletUi, modifier: Modifier = Modifier) {
    HhCard(modifier = modifier) {
        Text(
            text = item.bullet.originalText,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurface,
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            HhStatusDisc(kind = HhStatusKind.Met, size = HhTheme.spacing.d12)
            Text(
                text = stringResource(R.string.feature_tailor_impl_no_change),
                style = HhTheme.typography.labelLarge,
                color = HhTheme.colors.success,
            )
        }
        SourceFacts(item)
    }
}

@Composable
private fun ViolationBulletCard(item: TailorBulletUi, modifier: Modifier = Modifier) {
    HhCard(modifier = modifier) {
        NeutralNote(stringResource(R.string.feature_tailor_impl_violation_warning))
        item.bullet.violations.forEach { violation ->
            NeutralNote(violation.description())
        }
        HhDivider()
        Text(
            text = item.bullet.originalText,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurface,
        )
        SourceFacts(item)
    }
}

@Composable
private fun NeutralNote(text: String) {
    val shape = RoundedCornerShape(HhTheme.shapes.sm)
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .background(color = HhTheme.colors.surfaceContainerLow, shape = shape)
            .border(width = 1.dp, color = HhTheme.colors.hairlineStrong, shape = shape)
            .padding(HhTheme.spacing.md),
        style = HhTheme.typography.bodyMedium,
        color = HhTheme.colors.onSurface,
    )
}

@Composable
private fun LabeledDiff(
    @StringRes labelRes: Int,
    segments: List<DiffSegment>,
    highlight: Color,
    changeDecoration: TextDecoration,
    @StringRes changedWordsRes: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        Text(
            text = stringResource(labelRes),
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        DiffText(
            segments = segments,
            highlight = highlight,
            changeDecoration = changeDecoration,
            changedWordsRes = changedWordsRes,
            style = HhTheme.typography.bodyLarge,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditTypeChips(editTypes: List<EditType>) {
    if (editTypes.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        editTypes.forEach { editType ->
            val shape = RoundedCornerShape(HhTheme.shapes.xs)
            Text(
                text = stringResource(editType.labelRes()),
                modifier = Modifier
                    .background(color = HhTheme.colors.surface2, shape = shape)
                    .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.xxs),
                style = HhTheme.typography.labelMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordChips(keywords: List<String>) {
    if (keywords.isEmpty()) return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        keywords.forEach { keyword ->
            HhEvidenceMark(
                text = keyword,
                style = HhTheme.typography.labelMedium,
                tint = HhTheme.colors.evidence,
            )
        }
    }
}

@Composable
private fun SourceFacts(item: TailorBulletUi) {
    val aligned = item.sources
    val factIds = item.bullet.sourceIds
    val factTexts = item.sourceTexts
    if (factIds.isEmpty() && factTexts.isEmpty()) return
    Column(
        modifier = Modifier.padding(top = HhTheme.spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        HhDivider()
        if (aligned.isNotEmpty() && aligned.size == factIds.size) {
            aligned.forEach { source ->
                SourceFactRow(factId = source.id, text = source.text, source = source.source)
            }
        } else if (factIds.isNotEmpty() && factIds.size == factTexts.size) {
            factIds.forEachIndexed { index, factId -> SourceFactRow(factId = factId, text = factTexts[index], source = null) }
        } else {
            factIds.forEach { factId -> SourceFactRow(factId = factId, text = null, source = null) }
            factTexts.forEach { factText ->
                Text(
                    text = stringResource(R.string.feature_tailor_impl_based_on, factText),
                    style = HhTheme.typography.bodySmall,
                    color = HhTheme.colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun SourceFactRow(
    factId: String,
    text: String?,
    source: FactSource?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        FactIdTag(factId = factId)
        if (source != null) {
            FactProvenanceChip(source = source)
        }
        if (text != null) {
            Text(
                text = text,
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DecisionSection(
    bulletText: String,
    decision: BulletDecision,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    val firstWords = bulletText.trim().split(WHITESPACE).take(DESCRIPTION_WORDS).joinToString(" ")
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        DecisionState(decision)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            DecisionButton(
                labelRes = R.string.feature_tailor_impl_accept,
                description = stringResource(R.string.feature_tailor_impl_accept_description, firstWords),
                selected = decision == BulletDecision.ACCEPTED,
                onClick = onAccept,
                modifier = Modifier.weight(1f),
            )
            DecisionButton(
                labelRes = R.string.feature_tailor_impl_reject,
                description = stringResource(R.string.feature_tailor_impl_reject_description, firstWords),
                selected = decision == BulletDecision.REJECTED,
                onClick = onReject,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DecisionState(decision: BulletDecision) {
    val kind = when (decision) {
        BulletDecision.ACCEPTED -> HhStatusKind.Met
        BulletDecision.REJECTED -> HhStatusKind.Gap
        BulletDecision.PENDING -> null
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        if (kind != null) {
            HhStatusDisc(kind = kind, size = HhTheme.spacing.d12)
        }
        Text(
            text = stringResource(decision.statusRes()),
            style = HhTheme.typography.labelLarge,
            color = when (decision) {
                BulletDecision.ACCEPTED -> HhTheme.colors.success
                BulletDecision.REJECTED -> HhTheme.colors.gap
                BulletDecision.PENDING -> HhTheme.colors.onSurfaceVariant
            },
        )
    }
}

@Composable
private fun DecisionButton(
    @StringRes labelRes: Int,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonModifier = modifier
        .heightIn(min = HhTheme.spacing.d48)
        .semantics {
            contentDescription = description
            this.selected = selected
        }
    val text: @Composable () -> Unit = {
        Text(
            text = stringResource(labelRes),
            style = HhTheme.typography.labelLarge,
        )
    }
    if (selected) {
        HhButton(onClick = onClick, modifier = buttonModifier, text = text)
    } else {
        HhOutlinedButton(onClick = onClick, modifier = buttonModifier, text = text)
    }
}

@Composable
private fun TailorBulletUi.cardAnnouncement(): String {
    val originalLabel = stringResource(R.string.feature_tailor_impl_original)
    val proposedLabel = stringResource(R.string.feature_tailor_impl_proposed)
    val basedOnLabel = stringResource(
        R.string.feature_tailor_impl_based_on,
        bullet.sourceIds.joinToString(", "),
    )
    val actions = stringResource(R.string.feature_tailor_impl_accept) +
        ", " + stringResource(R.string.feature_tailor_impl_reject)
    return "$originalLabel: ${bullet.originalText}. " +
        "$proposedLabel: ${bullet.proposedText}. " +
        "$basedOnLabel. " +
        actions
}

private const val DESCRIPTION_WORDS = 6
private val WHITESPACE = Regex("\\s+")
