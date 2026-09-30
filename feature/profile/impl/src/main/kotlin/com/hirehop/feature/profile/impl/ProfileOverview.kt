package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry

@Composable
internal fun ProfileOverview(
    state: ProfileUiState.Success,
    actions: ProfileActions,
    onOpenSheet: (ProfileSheet) -> Unit,
    modifier: Modifier = Modifier,
) {
    val profile = state.profile
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            ProfileHeader(
                profile = profile,
                onEdit = { onOpenSheet(ProfileSheet.EditContact) },
            )
        }
        if (state.unconfirmedCount > 0) {
            item {
                ConfirmationBanner(
                    unconfirmedCount = state.unconfirmedCount,
                    onConfirmAll = actions.onConfirmAll,
                )
            }
        }
        item {
            SkillsSection(
                skills = profile.skills,
                onAddSkill = { onOpenSheet(ProfileSheet.AddSkill) },
                onRemoveSkill = actions.onRemoveSkill,
            )
        }
        entryGroups(
            entries = profile.entries,
            actions = actions,
            onEdit = { onOpenSheet(ProfileSheet.EditEntry(it.id, it.category)) },
        )
        item {
            AddEntryMenu(
                onAddEntry = { onOpenSheet(ProfileSheet.EditEntry(null, it.category)) },
                onPasteResume = { onOpenSheet(ProfileSheet.PasteResume) },
            )
        }
        item {
            Text(
                text = stringResource(R.string.feature_profile_impl_trust_copy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun LazyListScope.entryGroups(
    entries: List<ProfileEntry>,
    actions: ProfileActions,
    onEdit: (ProfileEntry) -> Unit,
) {
    EntryCategory.entries.forEach { category ->
        val group = entries.filter { it.category == category }
        if (group.isNotEmpty()) {
            item(key = "header-${category.name}") {
                Text(
                    text = stringResource(category.labelRes()),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(items = group, key = { it.id }) { entry ->
                EntryCard(
                    entry = entry,
                    onConfirm = { actions.onConfirmEntry(entry.id) },
                    onEdit = { onEdit(entry) },
                    onDelete = { actions.onDeleteEntry(entry.id) },
                )
            }
        }
    }
}
