package com.hirehop.feature.profile.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.profile.impl.common.ToConfirmChip

private val MonogramSize = 56.dp
private const val INITIALS = 2

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProfileCard(
    state: ProfileHeaderState,
    onAddEvidence: () -> Unit,
    onEditContact: (() -> Unit)?,
) {
    val summary = stringResource(
        R.string.feature_profile_impl_header_summary_description,
        pluralStringResource(R.plurals.feature_profile_impl_fact_count_accessibility, state.factCount, state.factCount),
        pluralStringResource(R.plurals.feature_profile_impl_confirmed_count, state.confirmedCount, state.confirmedCount),
        pluralStringResource(R.plurals.feature_profile_impl_user_stated_count, state.userStatedCount, state.userStatedCount),
    )
    HhCard {
        NameLine(state = state, onEditContact = onEditContact)
        Text(
            text = pluralStringResource(R.plurals.feature_profile_impl_fact_count_accessibility, state.factCount, state.factCount),
            style = HhTheme.typography.headlineL,
            color = HhTheme.colors.onSurface,
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = summary },
        )
        FlowRow(
            modifier = Modifier.clearAndSetSemantics { },
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            val confirmed = pluralStringResource(R.plurals.feature_profile_impl_confirmed_count, state.confirmedCount, state.confirmedCount)
            val userStated = pluralStringResource(R.plurals.feature_profile_impl_user_stated_count, state.userStatedCount, state.userStatedCount)
            HhProvenanceChip(kind = HhProvenanceKind.Confirmed, label = confirmed)
            HhProvenanceChip(kind = HhProvenanceKind.UserStated, label = userStated)
            if (state.toConfirmCount > 0) {
                ToConfirmChip(label = pluralStringResource(R.plurals.feature_profile_impl_to_confirm_count, state.toConfirmCount, state.toConfirmCount))
            }
        }
        HhPrimaryButton(
            label = stringResource(R.string.feature_profile_impl_add_evidence),
            onClick = onAddEvidence,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = HhIcons.Add,
        )
    }
}

@Composable
private fun NameLine(state: ProfileHeaderState, onEditContact: (() -> Unit)?) {
    val name = state.name.ifBlank { stringResource(R.string.feature_profile_impl_header_name_placeholder) }
    val editDescription = stringResource(R.string.feature_profile_impl_edit_contact)
    val clickable = if (onEditContact != null) {
        Modifier
            .clickable(role = Role.Button, onClick = onEditContact)
            .semantics { contentDescription = "$name. $editDescription" }
    } else {
        Modifier
    }
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = HhTheme.spacing.touch).then(clickable),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(MonogramSize).background(HhTheme.colors.primaryContainer, HhTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = initials(state.name), style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
            if (state.role.isNotBlank()) {
                Text(
                    text = state.role,
                    style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.SemiBold),
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

private fun initials(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(INITIALS).joinToString("") { it.first().uppercase() }
