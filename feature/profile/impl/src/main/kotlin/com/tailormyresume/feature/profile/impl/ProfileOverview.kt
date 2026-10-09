package com.tailormyresume.feature.profile.impl

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.feature.profile.impl.common.Note
import com.tailormyresume.feature.profile.impl.common.NoteTone

@Composable
internal fun ProfileOverviewScreen(
    state: ProfileUiState.Success,
    actions: ProfileActions,
    navigation: ProfileNavigation,
    expanded: ProfileSectionKind?,
    onToggleSection: (ProfileSectionKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sheet by rememberSaveable { mutableStateOf<ProfileSheet?>(null) }
    val overview = state.overview
    ProfileFrame(modifier = modifier) {
        if (state.isOffline) {
            item(key = "offline") {
                TmrOfflineBanner(message = stringResource(R.string.feature_profile_impl_offline_message))
            }
        }
        if (overview.unconfirmedCount > 0) {
            item(key = "to-confirm") {
                Note(
                    tone = NoteTone.Warning,
                    text = pluralStringResource(
                        R.plurals.feature_profile_impl_open_items_banner,
                        overview.unconfirmedCount,
                        overview.unconfirmedCount,
                    ),
                    actionLabel = stringResource(R.string.feature_profile_impl_open_items_review),
                    onAction = { overview.firstUnconfirmedId?.let(navigation.onOpenFact) },
                )
            }
        }
        item(key = "profile-card") {
            ProfileCard(
                state = ProfileHeaderState(
                    name = state.profile.fullName,
                    role = state.profile.headline,
                    factCount = overview.factCount,
                    confirmedCount = overview.confirmedCount,
                    userStatedCount = overview.userStatedCount,
                    toConfirmCount = overview.unconfirmedCount,
                ),
                onAddEvidence = navigation.onAddEvidence,
                onEditContact = { sheet = ProfileSheet.Contact },
            )
        }
        overview.sections.forEach { section ->
            item(key = "section-${section.kind.name}") {
                val isExpanded = section.kind == expanded
                SectionCard(
                    section = section,
                    expanded = isExpanded,
                    onToggle = { onToggleSection(section.kind) },
                    content = if (isExpanded) {
                        {
                            SectionFacts(
                                state = state,
                                section = section,
                                actions = actions,
                                navigation = navigation,
                                onAddSkill = { sheet = ProfileSheet.AddSkill },
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
        item(key = "add-fact") {
            TmrOutlineButton(
                label = stringResource(R.string.feature_profile_impl_add_fact),
                onClick = { sheet = ProfileSheet.AddFact },
                trailingIcon = TmrIcons.Add,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item(key = "file-note") {
            Note(
                text = stringResource(R.string.feature_profile_impl_file_deleted_note),
                tone = NoteTone.Plain,
                icon = TmrIcons.Delete,
            )
        }
    }
    ProfileSheetHost(
        sheet = sheet,
        profile = state.profile,
        actions = actions,
        navigation = navigation,
        onDismiss = { sheet = null },
    )
}
