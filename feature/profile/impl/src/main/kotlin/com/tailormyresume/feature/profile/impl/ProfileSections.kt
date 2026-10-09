package com.tailormyresume.feature.profile.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.fact.FactDisplayIds
import com.tailormyresume.core.model.exceedsLimits
import com.tailormyresume.feature.profile.impl.common.FactCard
import com.tailormyresume.feature.profile.impl.common.ToConfirmChip

private val SectionTile = 44.dp

internal fun ProfileSectionKind.icon(): ImageVector = when (this) {
    ProfileSectionKind.Education -> TmrIcons.Description
    ProfileSectionKind.Experience -> TmrIcons.Applications
    ProfileSectionKind.Projects -> TmrIcons.Facts
    ProfileSectionKind.Skills -> TmrIcons.Check
    ProfileSectionKind.Certifications -> TmrIcons.Verified
    ProfileSectionKind.Extras -> TmrIcons.Flag
}

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
internal fun ProfileSectionKind.addLabelRes(): Int = when (this) {
    ProfileSectionKind.Education -> R.string.feature_profile_impl_add_education
    ProfileSectionKind.Experience -> R.string.feature_profile_impl_add_experience
    ProfileSectionKind.Projects -> R.string.feature_profile_impl_add_project
    ProfileSectionKind.Skills -> R.string.feature_profile_impl_add_skill
    ProfileSectionKind.Certifications -> R.string.feature_profile_impl_add_certification
    ProfileSectionKind.Extras -> R.string.feature_profile_impl_add_achievement
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SectionCard(
    section: ProfileSection,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val title = stringResource(section.kind.titleRes())
    val description = stringResource(R.string.feature_profile_impl_section_description, title, sectionSubtitle(section))
    val stateText = stringResource(
        if (expanded) R.string.feature_profile_impl_section_state_expanded else R.string.feature_profile_impl_section_state_collapsed,
    )
    TmrCard(modifier = modifier, contentPadding = PaddingValues(TmrTheme.spacing.md)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TmrTheme.spacing.touch)
                .clickable(onClick = onToggle)
                .clearAndSetSemantics {
                    contentDescription = description
                    stateDescription = stateText
                    role = Role.Button
                    onClick {
                        onToggle()
                        true
                    }
                },
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(SectionTile)
                    .background(TmrTheme.colors.primaryContainer, RoundedCornerShape(SectionTile / 3)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(section.kind.icon(), contentDescription = null, tint = TmrTheme.colors.onSurface)
            }
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = title, style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
                if (section.toConfirmCount > 0) {
                    ToConfirmChip(label = pluralStringResource(R.plurals.feature_profile_impl_to_confirm_count, section.toConfirmCount, section.toConfirmCount))
                }
            }
            Text(
                text = section.count.toString(),
                style = TmrTheme.typography.titleS.copy(fontWeight = FontWeight.ExtraBold),
                color = TmrTheme.colors.onSurfaceVariant,
            )
            Icon(
                imageVector = if (expanded) TmrIcons.ExpandLess else TmrIcons.ExpandMore,
                contentDescription = null,
                tint = TmrTheme.colors.onSurfaceVariant,
            )
        }
        if (content != null) content()
    }
}

@Composable
internal fun SectionFacts(
    state: ProfileUiState.Success,
    section: ProfileSection,
    actions: ProfileActions,
    navigation: ProfileNavigation,
    onAddSkill: () -> Unit,
) {
    val category = section.kind.entryCategory()
    if (category != null) {
        state.profile.entries.filter { it.category == category }.forEach { entry ->
            key("fact-${section.kind.name}-${entry.id}") {
                FactCard(
                    entry = entry,
                    displayId = FactDisplayIds.of(entry, state.profile.entries),
                    onEdit = { navigation.onOpenFact(entry.id) },
                    onConfirm = if (entry.isConfirmed || entry.exceedsLimits) null else ({ actions.onConfirmEntry(entry.id) }),
                    embedded = true,
                )
            }
        }
    } else {
        state.profile.skills.forEachIndexed { index, skill ->
            key("fact-skill-$index") {
                FactCard(
                    id = skillId(index),
                    status = state.profile.skillStatus(skill),
                    kind = stringResource(R.string.feature_profile_impl_kind_skill),
                    summary = skill,
                    embedded = true,
                ) {
                    TmrOutlineButton(
                        label = stringResource(R.string.feature_profile_impl_skill_remove),
                        onClick = { actions.onRemoveSkill(skill) },
                        trailingIcon = TmrIcons.Delete,
                        size = TmrButtonSize.Compact,
                    )
                }
            }
        }
    }
    TmrOutlineButton(
        label = stringResource(section.kind.addLabelRes()),
        onClick = {
            if (category == null) onAddSkill() else navigation.onAddFact(category.name)
        },
        trailingIcon = TmrIcons.Add,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun sectionSubtitle(section: ProfileSection): String {
    val base = stringResource(
        R.string.feature_profile_impl_section_subtitle,
        pluralStringResource(R.plurals.feature_profile_impl_fact_count_accessibility, section.count, section.count),
        pluralStringResource(R.plurals.feature_profile_impl_confirmed_count, section.confirmedCount, section.confirmedCount),
        pluralStringResource(R.plurals.feature_profile_impl_user_stated_count, section.userStatedCount, section.userStatedCount),
    )
    return if (section.toConfirmCount > 0) {
        base + pluralStringResource(R.plurals.feature_profile_impl_section_subtitle_to_confirm, section.toConfirmCount, section.toConfirmCount)
    } else {
        base
    }
}
