package com.hirehop.feature.profile.impl.common

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButtonSize
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.fact.FactLineRenderer
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry
import com.hirehop.feature.profile.impl.R

private val HighlightWidth = 2.dp

@Composable
internal fun FactCard(
    entry: ProfileEntry,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    displayId: String = entry.id,
    highlighted: Boolean = false,
    onConfirm: (() -> Unit)? = null,
) {
    FactCard(
        id = displayId,
        status = entry.status(),
        kind = stringResource(entry.category.kindRes()),
        summary = FactLineRenderer.render(entry),
        modifier = modifier,
        highlighted = highlighted,
    ) {
        HhOutlineButton(
            label = stringResource(R.string.feature_profile_impl_fact_edit),
            onClick = onEdit,
            trailingIcon = HhIcons.Edit,
            size = HhButtonSize.Compact,
        )
        if (onConfirm != null) {
            HhSecondaryButton(
                label = stringResource(R.string.feature_profile_impl_fact_confirm),
                onClick = onConfirm,
                trailingIcon = HhIcons.Check,
                size = HhButtonSize.Compact,
            )
        }
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
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val statusLabel = stringResource(status.labelRes())
    val description = stringResource(
        R.string.feature_profile_impl_fact_card_description,
        kind,
        summary,
        statusLabel,
        id,
    )
    val shape = HhTheme.shapes.card
    HhSectionCard(
        contentPadding = PaddingValues(HhTheme.spacing.cardPadding),
        modifier = modifier
            .semantics(mergeDescendants = true) { contentDescription = description }
            .then(
                if (highlighted) Modifier.border(HighlightWidth, HhTheme.colors.primary, shape) else Modifier,
            ),
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            HhFactId(id = id)
            val provenance = status.provenanceKind()
            if (provenance != null) {
                HhProvenanceChip(kind = provenance, label = statusLabel)
            } else {
                ToConfirmChip(label = statusLabel)
            }
        }
        Text(text = summary, style = HhTheme.typography.bodyM, color = HhTheme.colors.body)
        if (actions != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm), content = actions)
        }
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
