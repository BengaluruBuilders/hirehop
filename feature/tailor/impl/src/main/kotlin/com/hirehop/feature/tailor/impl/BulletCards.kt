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
    val diff = remember(bullet.originalText, bullet.proposedText) {
        WordDiff.diff(bullet.originalText, bullet.proposedText)
    }
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LabeledDiff(R.string.feature_tailor_impl_original, diff.original, MaterialTheme.colorScheme.errorContainer)
            LabeledDiff(R.string.feature_tailor_impl_proposed, diff.proposed, MaterialTheme.colorScheme.tertiaryContainer)
            EditTypeChips(bullet.editTypes)
            SourceLines(item.sourceTexts)
            DecisionSection(bullet.decision, onAccept, onReject)
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
private fun LabeledDiff(@StringRes labelRes: Int, segments: List<DiffSegment>, highlight: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DiffText(segments = segments, highlight = highlight)
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
private fun DecisionSection(decision: BulletDecision, onAccept: () -> Unit, onReject: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(decision.statusRes()),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DecisionButton(
                labelRes = R.string.feature_tailor_impl_accept,
                icon = HhIcons.Check,
                selected = decision == BulletDecision.ACCEPTED,
                onClick = onAccept,
            )
            DecisionButton(
                labelRes = R.string.feature_tailor_impl_reject,
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
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val text: @Composable () -> Unit = { Text(stringResource(labelRes)) }
    val leadingIcon: @Composable () -> Unit = { Icon(imageVector = icon, contentDescription = null) }
    if (selected) {
        HhButton(onClick = onClick, text = text, leadingIcon = leadingIcon)
    } else {
        HhOutlinedButton(onClick = onClick, text = text, leadingIcon = leadingIcon)
    }
}
