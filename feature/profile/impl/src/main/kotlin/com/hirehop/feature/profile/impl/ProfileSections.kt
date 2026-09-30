package com.hirehop.feature.profile.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhHeroNumeral
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.ui.FactSourceProvenance

private const val HH_STACK_FONT_SCALE = 1.5f

@StringRes
internal fun ProfileSectionKind.titleRes(): Int = when (this) {
    ProfileSectionKind.Education -> R.string.feature_profile_impl_section_education
    ProfileSectionKind.Experience -> R.string.feature_profile_impl_section_experience
    ProfileSectionKind.Projects -> R.string.feature_profile_impl_section_projects
    ProfileSectionKind.Skills -> R.string.feature_profile_impl_section_skills
    ProfileSectionKind.Certifications -> R.string.feature_profile_impl_section_certifications
    ProfileSectionKind.Extras -> R.string.feature_profile_impl_section_extras
}

@StringRes
internal fun EntryCategory.factKindRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_profile_impl_kind_education
    EntryCategory.EXPERIENCE -> R.string.feature_profile_impl_kind_experience
    EntryCategory.PROJECT -> R.string.feature_profile_impl_kind_project
    EntryCategory.CERTIFICATION -> R.string.feature_profile_impl_kind_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_profile_impl_kind_achievement
}

internal fun FactSource.provenanceKind(
    isConfirmed: Boolean,
    provenance: FactSourceProvenance = FactSourceProvenance(),
): HhProvenanceKind = provenance.confirmedKindOf(this, isConfirmed)

@Composable
internal fun ProfileHeader(
    overview: ProfileOverviewState,
    isOffline: Boolean,
    onEditContact: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        if (isOffline) {
            HhOfflineBanner(
                message = stringResource(R.string.feature_profile_impl_offline_message),
                supportingText = stringResource(R.string.feature_profile_impl_offline_supporting),
            )
        }
        Text(
            text = overview.headlineLine,
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        val factCountAnnouncement = pluralStringResource(
            R.plurals.feature_profile_impl_fact_count_accessibility,
            overview.factCount,
            overview.factCount,
        )
        HhHeroNumeral(
            value = overview.factCount.toString(),
            caption = stringResource(R.string.feature_profile_impl_hero_caption),
            modifier = Modifier.clearAndSetSemantics {
                contentDescription = factCountAnnouncement
            },
        )
        FactCountChips(overview = overview)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactCountChips(
    overview: ProfileOverviewState,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        HhProvenanceChip(
            kind = HhProvenanceKind.Confirmed,
            label = pluralStringResource(
                R.plurals.feature_profile_impl_confirmed_count,
                overview.confirmedCount,
                overview.confirmedCount,
            ),
        )
        HhProvenanceChip(
            kind = HhProvenanceKind.UserStated,
            label = pluralStringResource(
                R.plurals.feature_profile_impl_user_stated_count,
                overview.userStatedCount,
                overview.userStatedCount,
            ),
        )
        if (overview.unconfirmedCount > 0) {
            HhStatusChip(
                kind = HhStatusKind.Partial,
                label = pluralStringResource(
                    R.plurals.feature_profile_impl_not_confirmed_count,
                    overview.unconfirmedCount,
                    overview.unconfirmedCount,
                ),
            )
        }
    }
}

@Composable
internal fun OpenItemsBanner(
    unconfirmedCount: Int,
    onReview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = HhTheme.spacing.lg,
            end = HhTheme.spacing.xs,
            top = HhTheme.spacing.sm,
            bottom = HhTheme.spacing.sm,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = pluralStringResource(
                    R.plurals.feature_profile_impl_open_items_banner,
                    unconfirmedCount,
                    unconfirmedCount,
                ),
                modifier = Modifier.weight(1f),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurface,
            )
            HhOutlinedButton(
                onClick = onReview,
                modifier = Modifier.heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_profile_impl_open_items_review),
                        style = HhTheme.typography.labelLarge,
                        color = HhTheme.colors.primary,
                    )
                },
            )
        }
    }
}

@Composable
internal fun FileRetentionNote(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.feature_profile_impl_file_deleted_note),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = HhTheme.spacing.xxs),
        style = HhTheme.typography.bodySmall,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
internal fun OverviewActions(
    onAddEvidence: () -> Unit,
    onAddFact: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stacked = LocalDensity.current.fontScale > HH_STACK_FONT_SCALE
    val spacing = Arrangement.spacedBy(HhTheme.spacing.sm)
    if (stacked) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = spacing,
        ) {
            AddEvidenceButton(
                onClick = onAddEvidence,
                modifier = Modifier.fillMaxWidth(),
            )
            AddFactButton(
                onClick = onAddFact,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = spacing,
        ) {
            AddEvidenceButton(
                onClick = onAddEvidence,
                modifier = Modifier.weight(1f),
            )
            AddFactButton(
                onClick = onAddFact,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AddEvidenceButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = HhTheme.spacing.d48),
        text = {
            Text(
                text = stringResource(R.string.feature_profile_impl_add_evidence),
                style = HhTheme.typography.labelLarge,
            )
        },
    )
}

@Composable
private fun AddFactButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhOutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = HhTheme.spacing.d48),
        text = {
            Text(
                text = stringResource(R.string.feature_profile_impl_add_fact),
                style = HhTheme.typography.labelLarge,
            )
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProfileSectionCard(
    section: ProfileSection,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(section.kind.titleRes())
    val announcement = stringResource(
        R.string.feature_profile_impl_section_accessibility,
        title,
        pluralStringResource(
            R.plurals.feature_profile_impl_section_facts_accessibility,
            section.count,
            section.count,
        ),
        stringResource(
            R.string.feature_profile_impl_section_mix_accessibility,
            section.confirmedCount,
            section.userStatedCount,
        ),
    )
    HhSectionCard(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = announcement }
            .clickable(onClick = onOpen),
    ) {
        SectionCardContent(section = section)
    }
}

@Composable
private fun ColumnScope.SectionCardContent(section: ProfileSection) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(section.kind.titleRes()),
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = section.count.toString(),
            style = HhTheme.typography.titleLarge,
            color = HhTheme.colors.onSurface,
        )
    }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        section.facts.forEach { fact ->
            HhProvenanceChip(
                kind = fact.source.provenanceKind(isConfirmed = fact.isConfirmed),
                label = fact.id,
            )
        }
    }
}
