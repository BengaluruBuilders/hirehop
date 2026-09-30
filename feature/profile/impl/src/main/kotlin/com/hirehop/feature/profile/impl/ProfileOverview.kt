package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.EntryCategory

@Composable
internal fun ProfileOverview(
    state: ProfileUiState.Success,
    actions: ProfileActions,
    onOpenSheet: (ProfileSheet) -> Unit,
    onOpenFact: (String?) -> Unit,
    onAddEvidence: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val overview = state.overview
    val openEntries = state.profile.entries.filterNot { it.isConfirmed }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        item(key = "header") {
            ProfileHeader(
                overview = overview,
                isOffline = state.isOffline,
                onEditContact = { onOpenSheet(ProfileSheet.EditContact) },
            )
        }
        if (overview.unconfirmedCount > 0) {
            item(key = "open-items") {
                OpenItemsBanner(
                    unconfirmedCount = overview.unconfirmedCount,
                    onReview = { onOpenFact(overview.firstUnconfirmedId) },
                )
            }
        }
        item(key = "retention") {
            FileRetentionNote()
        }
        item(key = "actions") {
            OverviewActions(
                onAddEvidence = onAddEvidence,
                onAddFact = { onOpenFact(null) },
            )
        }
        items(items = overview.sections, key = { "section-${it.kind.name}" }) { section ->
            ProfileSectionCard(
                section = section,
                onOpen = { onOpenSheet(section.kind.destinationSheet()) },
            )
        }
        if (overview.sections.any { it.kind == ProfileSectionKind.Skills }) {
            item(key = "skills") {
                SkillsEditor(
                    skills = state.profile.skills,
                    onAddSkill = { onOpenSheet(ProfileSheet.AddSkill) },
                    onRemoveSkill = actions.onRemoveSkill,
                )
            }
        }
        item(key = "add-entry") {
            AddEntryMenu(
                onAddEntry = { onOpenSheet(ProfileSheet.EditEntry(null, it.category)) },
                onPasteResume = { onOpenSheet(ProfileSheet.PasteResume) },
            )
        }
        if (openEntries.isNotEmpty()) {
            item(key = "open-facts-title") {
                Text(
                    text = stringResource(R.string.feature_profile_impl_open_facts_title),
                    style = HhTheme.typography.titleMedium,
                    color = HhTheme.colors.onSurface,
                )
            }
            items(items = openEntries, key = { "open-${it.id}" }) { entry ->
                EntryCard(
                    entry = entry,
                    onConfirm = { actions.onConfirmEntry(entry.id) },
                    onEdit = { onOpenSheet(ProfileSheet.EditEntry(entry.id, entry.category)) },
                    onDelete = { onOpenSheet(ProfileSheet.DeleteEntry(entry.id)) },
                )
            }
        }
    }
}

private fun ProfileSectionKind.destinationSheet(): ProfileSheet = when (this) {
    ProfileSectionKind.Skills -> ProfileSheet.AddSkill
    else -> ProfileSheet.EditEntry(null, entryCategory())
}

private fun ProfileSectionKind.entryCategory(): EntryCategory = when (this) {
    ProfileSectionKind.Education -> EntryCategory.EDUCATION
    ProfileSectionKind.Experience -> EntryCategory.EXPERIENCE
    ProfileSectionKind.Projects -> EntryCategory.PROJECT
    ProfileSectionKind.Skills -> EntryCategory.PROJECT
    ProfileSectionKind.Certifications -> EntryCategory.CERTIFICATION
    ProfileSectionKind.Extras -> EntryCategory.ACHIEVEMENT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillsEditor(
    skills: List<String>,
    onAddSkill: () -> Unit,
    onRemoveSkill: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        skills.forEach { skill ->
            HhOutlinedButton(
                onClick = { onRemoveSkill(skill) },
                text = {
                    Text(
                        text = skill,
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
        }
        HhOutlinedButton(
            onClick = onAddSkill,
            text = {
                Text(
                    text = stringResource(R.string.feature_profile_impl_add_skill),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}
