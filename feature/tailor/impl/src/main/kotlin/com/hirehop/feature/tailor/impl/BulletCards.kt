package com.hirehop.feature.tailor.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
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
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (bullet.originalText.trim() == bullet.proposedText.trim()) {
                PositionOnlyChange(bullet.originalText)
            } else {
                TextChange(bullet.originalText, bullet.proposedText)
            }
            EditTypeChips(bullet.editTypes)
            SourceLines(item.sourceTexts)
            DecisionSection(bullet.proposedText, bullet.decision, onAccept, onReject)
        }
    }
}

@Composable
private fun TextChange(original: String, proposed: String) {
    val diff = remember(original, proposed) { WordDiff.diff(original, proposed) }
    val colors = MaterialTheme.colorScheme
    LabeledDiff(
        labelRes = R.string.feature_tailor_impl_original,
        segments = diff.original,
        highlight = colors.errorContainer,
        changeDecoration = TextDecoration.LineThrough,
        changedWordsRes = R.string.feature_tailor_impl_removed_words,
    )
    LabeledDiff(
        labelRes = R.string.feature_tailor_impl_proposed,
        segments = diff.proposed,
        highlight = colors.tertiaryContainer,
        changeDecoration = TextDecoration.Underline,
        changedWordsRes = R.string.feature_tailor_impl_changed_words,
    )
}

@Composable
private fun PositionOnlyChange(text: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = stringResource(R.string.feature_tailor_impl_position_only),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StaleBulletCard(item: TailorBulletUi, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_stale_title),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_stale_body),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = item.bullet.originalText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun UnchangedBulletCard(item: TailorBulletUi, modifier: Modifier = Modifier) {
    OutlinedCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = item.bullet.originalText, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = stringResource(R.string.feature_tailor_impl_no_change),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun ViolationBulletCard(item: TailorBulletUi, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.errorContainer,
            contentColor = colors.onErrorContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Rounded.Warning, contentDescription = null)
                Text(
                    text = stringResource(R.string.feature_tailor_impl_violation_warning),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            item.bullet.violations.forEach {
                Text(text = it.description(), style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = item.bullet.originalText, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun LabeledDiff(
    @StringRes labelRes: Int,
    segments: List<DiffSegment>,
    highlight: Color,
    changeDecoration: TextDecoration,
    @StringRes changedWordsRes: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DiffText(
            segments = segments,
            highlight = highlight,
            changeDecoration = changeDecoration,
            changedWordsRes = changedWordsRes,
        )
    }
}

@Composable
private fun EditTypeChips(editTypes: List<EditType>) {
    if (editTypes.isEmpty()) return
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        editTypes.forEach { editType ->
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Text(
                    text = stringResource(editType.labelRes()),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun SourceLines(sourceTexts: List<String>) {
    sourceTexts.forEach {
        Text(
            text = stringResource(R.string.feature_tailor_impl_based_on, it),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DecisionSection(
    bulletText: String,
    decision: BulletDecision,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    val firstWords = bulletText.trim().split(WHITESPACE).take(DESCRIPTION_WORDS).joinToString(" ")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(decision.statusRes()),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DecisionButton(
                labelRes = R.string.feature_tailor_impl_accept,
                description = stringResource(R.string.feature_tailor_impl_accept_description, firstWords),
                icon = HhIcons.Check,
                selected = decision == BulletDecision.ACCEPTED,
                onClick = onAccept,
            )
            DecisionButton(
                labelRes = R.string.feature_tailor_impl_reject,
                description = stringResource(R.string.feature_tailor_impl_reject_description, firstWords),
                icon = HhIcons.Close,
                selected = decision == BulletDecision.REJECTED,
                onClick = onReject,
            )
        }
    }
}

@Composable
private fun DecisionButton(
    @StringRes labelRes: Int,
    description: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val modifier = Modifier.semantics {
        contentDescription = description
        this.selected = selected
    }
    val text: @Composable () -> Unit = { Text(stringResource(labelRes)) }
    val leadingIcon: @Composable () -> Unit = { Icon(imageVector = icon, contentDescription = null) }
    if (selected) {
        HhButton(onClick = onClick, modifier = modifier, text = text, leadingIcon = leadingIcon)
    } else {
        HhOutlinedButton(onClick = onClick, modifier = modifier, text = text, leadingIcon = leadingIcon)
    }
}

private const val DESCRIPTION_WORDS = 6
private val WHITESPACE = Regex("\\s+")
