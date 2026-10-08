package com.tailormyresume.feature.profile.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.rememberTmrListEnterState
import com.tailormyresume.core.designsystem.component.tmrListEnter
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.fact.FactDisplayIds
import com.tailormyresume.feature.profile.impl.common.FactCard
import com.tailormyresume.feature.profile.impl.common.FactStatus
import com.tailormyresume.feature.profile.impl.common.ToConfirmChip

private val ListGap = 10.dp
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
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(section.kind.titleRes())
    val description = stringResource(R.string.feature_profile_impl_section_description, title, sectionSubtitle(section))
    TmrCard(
        onClick = onOpen,
        contentPadding = PaddingValues(TmrTheme.spacing.md),
        modifier = modifier.clearAndSetSemantics {
            contentDescription = description
            role = Role.Button
            onClick {
                onOpen()
                true
            }
        },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(SectionTile).background(TmrTheme.colors.primaryContainer, RoundedCornerShape(SectionTile / 3)),
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
        }
    }
}

@Composable
internal fun ExpandedSectionScreen(
    state: ProfileUiState.Success,
    section: ProfileSection,
    actions: ProfileActions,
    navigation: ProfileNavigation,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(section.kind.titleRes())
    val subtitle = sectionSubtitle(section)
    val category = section.kind.entryCategory()
    var showSkillSheet by rememberSaveable { mutableStateOf(false) }
    val listEnter = rememberTmrListEnterState()
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = title,
                subtitle = subtitle,
                onBack = onClose,
                backContentDescription = stringResource(R.string.feature_profile_impl_back),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = TmrTheme.spacing.gutter,
                end = TmrTheme.spacing.gutter,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(ListGap),
        ) {
            if (state.isOffline) {
                item(key = "offline") {
                    TmrOfflineBanner(message = stringResource(R.string.feature_profile_impl_offline_message))
                }
            }
            if (category != null) {
                itemsIndexed(
                    items = state.profile.entries.filter { it.category == category },
                    key = { _, entry -> entry.id },
                ) { index, entry ->
                    FactCard(
                        entry = entry,
                        modifier = Modifier.tmrListEnter(state = listEnter, index = index),
                        displayId = FactDisplayIds.of(entry, state.profile.entries),
                        onEdit = { navigation.onOpenFact(entry.id) },
                        onConfirm = if (entry.isConfirmed) null else ({ actions.onConfirmEntry(entry.id) }),
                    )
                }
            } else {
                items(items = state.profile.skills.withIndex().toList(), key = { "skill-${it.index}" }) { (index, skill) ->
                    FactCard(
                        id = skillId(index),
                        status = FactStatus.Confirmed,
                        kind = stringResource(R.string.feature_profile_impl_kind_skill),
                        summary = skill,
                        modifier = Modifier.tmrListEnter(state = listEnter, index = index),
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
            item(key = "add") {
                TmrOutlineButton(
                    label = stringResource(section.kind.addLabelRes()),
                    onClick = {
                        if (category == null) showSkillSheet = true else navigation.onAddFact(category.name)
                    },
                    trailingIcon = TmrIcons.Add,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    if (showSkillSheet) {
        AddSkillSheet(
            onAdd = {
                actions.onAddSkill(it)
                showSkillSheet = false
            },
            onDismiss = { showSkillSheet = false },
        )
    }
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
