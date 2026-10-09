package com.tailormyresume.feature.profile.impl.common

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrProvenanceChip
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.fact.FactLineRenderer
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.hasTooLongBullet
import com.tailormyresume.feature.profile.impl.R

private val HighlightWidth = 2.dp

@Composable
internal fun FactCard(
    entry: ProfileEntry,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    displayId: String = entry.id,
    highlighted: Boolean = false,
    embedded: Boolean = false,
    onConfirm: (() -> Unit)? = null,
) {
    FactCard(
        id = displayId,
        status = entry.displayStatus(),
        kind = stringResource(entry.category.kindRes()),
        summary = FactLineRenderer.render(entry),
        modifier = modifier,
        highlighted = highlighted,
        embedded = embedded,
        note = if (entry.hasTooLongBullet) stringResource(R.string.feature_profile_impl_fact_too_long_note) else null,
    ) {
        if (onConfirm != null && !entry.hasTooLongBullet) {
            TmrSecondaryButton(
                label = stringResource(R.string.feature_profile_impl_fact_confirm),
                onClick = onConfirm,
                trailingIcon = TmrIcons.Check,
                size = TmrButtonSize.Compact,
            )
        }
        TmrTextButton(label = stringResource(R.string.feature_profile_impl_fact_edit), onClick = onEdit)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun FactCard(
    id: String,
    status: FactStatus,
    kind: String,
    summary: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    embedded: Boolean = false,
    note: String? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val statusLabel = stringResource(status.labelRes())
    val description = if (note == null) {
        stringResource(R.string.feature_profile_impl_fact_card_description, kind, summary, statusLabel, id)
    } else {
        stringResource(R.string.feature_profile_impl_fact_card_description_with_note, kind, summary, statusLabel, id, note)
    }
    val outline = if (highlighted) Modifier.border(HighlightWidth, TmrTheme.colors.primary, TmrTheme.shapes.card) else Modifier
    val semanticModifier = modifier.semantics(mergeDescendants = true) { contentDescription = description }.then(outline)
    if (embedded) {
        Column(
            modifier = semanticModifier
                .fillMaxWidth()
                .background(TmrTheme.colors.background, RoundedCornerShape(TmrTheme.spacing.lg))
                .padding(TmrTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            FactCardContent(id, status, statusLabel, summary, note, actions)
        }
    } else {
        TmrCard(contentPadding = PaddingValues(TmrTheme.spacing.cardPadding), modifier = semanticModifier) {
            FactCardContent(id, status, statusLabel, summary, note, actions)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactCardContent(
    id: String,
    status: FactStatus,
    statusLabel: String,
    summary: String,
    note: String?,
    actions: (@Composable RowScope.() -> Unit)?,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
    ) {
        TmrFactId(id = id)
        val provenance = status.provenanceKind()
        if (provenance != null) {
            TmrProvenanceChip(kind = provenance, label = statusLabel)
        } else {
            ToConfirmChip(label = statusLabel)
        }
    }
    Text(
        text = summary,
        style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
        color = TmrTheme.colors.onSurface,
    )
    if (note != null) {
        Note(text = note, tone = NoteTone.Warning)
    }
    if (actions != null) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}

@StringRes
internal fun EntryCategory.kindRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_kind_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_kind_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_kind_project
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_kind_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_kind_achievement
}
