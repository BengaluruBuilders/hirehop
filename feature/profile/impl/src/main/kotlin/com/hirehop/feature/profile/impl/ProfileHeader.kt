package com.hirehop.feature.profile.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhHeaderButton
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSheet
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.profile.impl.common.ToConfirmChip

internal data class ProfileHeaderState(
    val headerLine: String,
    val factCount: Int,
    val confirmedCount: Int,
    val userStatedCount: Int,
    val toConfirmCount: Int,
)

private val HeaderHeight = 220.dp
private val HeaderTop = 12.dp
private val HeaderBottom = 24.dp
private val RowGap = 10.dp
private val ChipGap = 6.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProfileFrame(
    header: ProfileHeaderState,
    onAddEvidence: () -> Unit,
    modifier: Modifier = Modifier,
    onEditContact: (() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    HhScreen(modifier = modifier, sheet = false, lightTop = false) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeaderBlock(
                state = header,
                onAddEvidence = onAddEvidence,
                onEditContact = onEditContact,
            )
            HhSheet(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = HhTheme.spacing.gutter,
                        end = HhTheme.spacing.gutter,
                        top = HhTheme.spacing.xl,
                        bottom = padding.calculateBottomPadding(),
                    ),
                    verticalArrangement = Arrangement.spacedBy(RowGap),
                    content = content,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileHeaderBlock(
    state: ProfileHeaderState,
    onAddEvidence: () -> Unit,
    onEditContact: (() -> Unit)?,
) {
    val colors = HhTheme.colors
    val largeText = LocalDensity.current.fontScale >= 1.5f
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val factsLabel = pluralStringResource(R.plurals.feature_profile_impl_fact_count_accessibility, state.factCount, state.factCount)
    val summary = stringResource(
        R.string.feature_profile_impl_header_summary_description,
        factsLabel,
        pluralStringResource(R.plurals.feature_profile_impl_confirmed_count, state.confirmedCount, state.confirmedCount),
        pluralStringResource(R.plurals.feature_profile_impl_user_stated_count, state.userStatedCount, state.userStatedCount),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = statusTop + HeaderHeight)
            .background(colors.header),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = HhTheme.spacing.gutter,
                    end = HhTheme.spacing.gutter,
                    top = statusTop + HeaderTop,
                    bottom = HeaderBottom,
                ),
            verticalArrangement = Arrangement.spacedBy(RowGap),
        ) {
            NameLine(headerLine = state.headerLine, onEditContact = onEditContact)
            ProfileSummaryRow(
                factCount = state.factCount,
                summary = summary,
                largeText = largeText,
                onAddEvidence = onAddEvidence,
            )
            FlowRow(
                modifier = Modifier.clearAndSetSemantics { },
                horizontalArrangement = Arrangement.spacedBy(ChipGap),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            ) {
                HhProvenanceChip(
                    kind = HhProvenanceKind.Confirmed,
                    label = pluralStringResource(
                        R.plurals.feature_profile_impl_confirmed_count,
                        state.confirmedCount,
                        state.confirmedCount,
                    ),
                )
                HhProvenanceChip(
                    kind = HhProvenanceKind.UserStated,
                    label = pluralStringResource(
                        R.plurals.feature_profile_impl_user_stated_count,
                        state.userStatedCount,
                        state.userStatedCount,
                    ),
                )
                if (state.toConfirmCount > 0) {
                    ToConfirmChip(
                        label = stringResource(R.string.feature_profile_impl_to_confirm_count, state.toConfirmCount),
                        onHeader = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileSummaryRow(
    factCount: Int,
    summary: String,
    largeText: Boolean,
    onAddEvidence: () -> Unit,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .semantics(mergeDescendants = true) { contentDescription = summary }
    if (largeText) {
        Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(RowGap)) {
            FactCountLabel(factCount)
            HhHeaderButton(
                label = stringResource(R.string.feature_profile_impl_add_evidence),
                onClick = onAddEvidence,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = HhIcons.Add,
            )
        }
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            FactCountLabel(factCount)
            HhHeaderButton(
                label = stringResource(R.string.feature_profile_impl_add_evidence),
                onClick = onAddEvidence,
                trailingIcon = HhIcons.Add,
            )
        }
    }
}

@Composable
private fun FactCountLabel(factCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(RowGap)) {
        Text(
            text = factCount.toString(),
            modifier = Modifier.alignByBaseline(),
            style = HhTheme.typography.numeralHero,
            color = HhTheme.colors.onHeader,
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_hero_caption),
            modifier = Modifier.alignByBaseline(),
            style = HhTheme.typography.headlineM,
            color = HhTheme.colors.onHeader,
        )
    }
}

@Composable
private fun NameLine(
    headerLine: String,
    onEditContact: (() -> Unit)?,
) {
    val text = headerLine.ifBlank { stringResource(R.string.feature_profile_impl_header_name_placeholder) }
    val editDescription = stringResource(R.string.feature_profile_impl_edit_contact)
    val clickable = if (onEditContact != null) {
        Modifier
            .clickable(role = Role.Button, onClick = onEditContact)
            .semantics { contentDescription = "$text. $editDescription" }
    } else {
        Modifier
    }
    Row(
        modifier = Modifier
            .heightIn(min = HhTheme.spacing.touch)
            .then(clickable),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = text, style = HhTheme.typography.titleS, color = HhTheme.colors.onHeader)
    }
}
