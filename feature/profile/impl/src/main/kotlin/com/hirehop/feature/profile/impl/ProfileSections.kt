package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.model.CandidateProfile

@Composable
internal fun ProfileHeader(
    profile: CandidateProfile,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contactLine = listOf(profile.email, profile.phone)
        .filter { it.isNotBlank() }
        .joinToString(separator = "  |  ")
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = profile.fullName.ifBlank {
                    stringResource(R.string.feature_profile_impl_header_name_placeholder)
                },
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = profile.headline.ifBlank {
                    stringResource(R.string.feature_profile_impl_header_headline_placeholder)
                },
                style = MaterialTheme.typography.bodyLarge,
            )
            if (contactLine.isNotEmpty()) {
                Text(
                    text = contactLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = HhIcons.Edit,
                contentDescription = stringResource(R.string.feature_profile_impl_edit_contact),
            )
        }
    }
}

@Composable
internal fun ConfirmationBanner(
    unconfirmedCount: Int,
    onConfirmAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = pluralStringResource(
                    R.plurals.feature_profile_impl_banner_unconfirmed,
                    unconfirmedCount,
                    unconfirmedCount,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            HhButton(
                onClick = onConfirmAll,
                text = { Text(stringResource(R.string.feature_profile_impl_confirm_all)) },
                leadingIcon = { Icon(imageVector = HhIcons.Check, contentDescription = null) },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SkillsSection(
    skills: List<String>,
    onAddSkill: () -> Unit,
    onRemoveSkill: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_profile_impl_skills_title),
            style = MaterialTheme.typography.titleMedium,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            skills.forEach { skill ->
                SkillChip(skill = skill, onRemove = { onRemoveSkill(skill) })
            }
            AssistChip(
                onClick = onAddSkill,
                label = { Text(stringResource(R.string.feature_profile_impl_add_skill)) },
                leadingIcon = { Icon(imageVector = HhIcons.Add, contentDescription = null) },
            )
        }
    }
}

@Composable
private fun SkillChip(
    skill: String,
    onRemove: () -> Unit,
) {
    InputChip(
        selected = false,
        onClick = onRemove,
        label = { Text(skill) },
        trailingIcon = {
            Icon(
                imageVector = HhIcons.Close,
                contentDescription = stringResource(R.string.feature_profile_impl_remove_skill, skill),
            )
        },
    )
}
