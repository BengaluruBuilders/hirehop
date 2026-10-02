package com.hirehop.feature.profile.impl

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.fact.FactDisplayIds
import com.hirehop.feature.profile.impl.common.FactCard
import com.hirehop.feature.profile.impl.common.FactIdChip
import com.hirehop.feature.profile.impl.common.FactStatus

private val ArrowSize = 18.dp
private val ChipGap = 6.dp
private val ListGap = 10.dp

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
    val factsLabel = pluralStringResource(
        R.plurals.feature_profile_impl_fact_count_accessibility,
        section.count,
        section.count,
    )
    val description = stringResource(R.string.feature_profile_impl_section_description, title, factsLabel)
    HhCard(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        onClick = onOpen,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = section.count.toString(),
                    style = HhTheme.typography.titleM,
                    color = HhTheme.colors.onSurface,
                )
                Icon(
                    imageVector = HhIcons.ArrowForward,
                    contentDescription = null,
                    tint = HhTheme.colors.onSurfaceVariant,
                    modifier = Modifier.size(ArrowSize),
                )
            }
        }
        FlowRow(
            modifier = Modifier.clearAndSetSemantics { },
            horizontalArrangement = Arrangement.spacedBy(ChipGap),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            section.facts.forEach { fact -> FactIdChip(id = fact.displayId, status = fact.status) }
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
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
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
                start = HhTheme.spacing.gutter,
                end = HhTheme.spacing.gutter,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(ListGap),
        ) {
            if (state.isOffline) {
                item(key = "offline") {
                    HhOfflineBanner(message = stringResource(R.string.feature_profile_impl_offline_message))
                }
            }
            if (category != null) {
                items(
                    items = state.profile.entries.filter { it.category == category },
                    key = { it.id },
                ) { entry ->
                    FactCard(
                        entry = entry,
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
                    ) {
                        HhOutlineButton(
                            label = stringResource(R.string.feature_profile_impl_skill_remove),
                            onClick = { actions.onRemoveSkill(skill) },
                            trailingIcon = HhIcons.Delete,
                        )
                    }
                }
            }
            item(key = "add") {
                HhOutlineButton(
                    label = stringResource(section.kind.addLabelRes()),
                    onClick = {
                        if (category == null) showSkillSheet = true else navigation.onAddFact(category.name)
                    },
                    trailingIcon = HhIcons.Add,
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
        base + stringResource(R.string.feature_profile_impl_section_subtitle_to_confirm, section.toConfirmCount)
    } else {
        base
    }
}
