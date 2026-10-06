package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.feature.profile.impl.common.Note
import com.hirehop.feature.profile.impl.common.NoteTone

@Composable
internal fun ProfileOverviewScreen(
    state: ProfileUiState.Success,
    actions: ProfileActions,
    navigation: ProfileNavigation,
    onOpenSection: (ProfileSectionKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sheet by rememberSaveable { mutableStateOf<ProfileSheet?>(null) }
    val overview = state.overview
    ProfileFrame(
        header = ProfileHeaderState(
            headerLine = overview.headlineLine,
            factCount = overview.factCount,
            confirmedCount = overview.confirmedCount,
            userStatedCount = overview.userStatedCount,
            toConfirmCount = overview.unconfirmedCount,
        ),
        onAddEvidence = navigation.onAddEvidence,
        onEditContact = { sheet = ProfileSheet.Contact },
        modifier = modifier,
    ) {
        if (state.isOffline) {
            item(key = "offline") {
                HhOfflineBanner(message = stringResource(R.string.feature_profile_impl_offline_message))
            }
        }
        if (overview.unconfirmedCount > 0) {
            item(key = "to-confirm") {
                Note(
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
        itemsIndexed(items = overview.sections, key = { _, it -> "section-${it.kind.name}" }) { index, section ->
            SectionCard(section = section, accent = sectionAccent(index), onOpen = { onOpenSection(section.kind) })
        }
        item(key = "add-fact") {
            HhOutlineButton(
                label = stringResource(R.string.feature_profile_impl_add_fact),
                onClick = { sheet = ProfileSheet.AddFact },
                trailingIcon = HhIcons.Add,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item(key = "file-note") {
            Note(
                text = stringResource(R.string.feature_profile_impl_file_deleted_note),
                tone = NoteTone.Positive,
                icon = HhIcons.Delete,
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
