package com.hirehop.feature.profile.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.layout
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

private val HeaderHeight = 284.dp
private val SheetOverlap = 48.dp
private val HeaderTop = 12.dp
private val RowGap = 10.dp
private val ChipGap = 6.dp
private val LargeCircle = 260.dp
private val SmallCircle = 180.dp
private val LargeCircleInset = 70.dp
private val LargeCircleRise = 90.dp
private val SmallCircleInset = 60.dp
private val SmallCircleTop = 134.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ProfileFrame(
    header: ProfileHeaderState,
    onAddEvidence: () -> Unit,
    modifier: Modifier = Modifier,
    onEditContact: (() -> Unit)? = null,
    content: LazyListScope.() -> Unit,
) {
    HhScreen(modifier = modifier, sheet = false) { padding ->
        Column(modifier = Modifier.fillMaxSize()) {
            ProfileHeaderBlock(
                state = header,
                onAddEvidence = onAddEvidence,
                onEditContact = onEditContact,
            )
            HhSheet(
                modifier = Modifier
                    .weight(1f)
                    .layout { measurable, constraints ->
                        val overlap = SheetOverlap.roundToPx()
                        val placeable = measurable.measure(
                            constraints.copy(
                                minHeight = constraints.maxHeight + overlap,
                                maxHeight = constraints.maxHeight + overlap,
                            ),
                        )
                        layout(placeable.width, constraints.maxHeight) { placeable.place(0, -overlap) }
                    },
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
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val circles = colors.headerShape
    val factsLabel = pluralStringResource(R.plurals.feature_profile_impl_fact_count_accessibility, state.factCount, state.factCount)
    val summary = stringResource(
        R.string.feature_profile_impl_header_summary_description,
        factsLabel,
        pluralStringResource(R.plurals.feature_profile_impl_confirmed_count, state.confirmedCount, state.confirmedCount),
        pluralStringResource(R.plurals.feature_profile_impl_user_stated_count, state.userStatedCount, state.userStatedCount),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = statusTop + HeaderHeight)
            .background(colors.header)
            .drawBehind {
                val large = LargeCircle.toPx()
                val small = SmallCircle.toPx()
                drawCircle(
                    color = circles,
                    radius = large / 2,
                    center = Offset(size.width + LargeCircleInset.toPx() - large / 2, -LargeCircleRise.toPx() + large / 2),
                )
                drawCircle(
                    color = circles,
                    radius = small / 2,
                    center = Offset(-SmallCircleInset.toPx() + small / 2, statusTop.toPx() + SmallCircleTop.toPx() + small / 2),
                )
            }
            .padding(start = HhTheme.spacing.gutter, end = HhTheme.spacing.gutter, top = statusTop + HeaderTop),
        verticalArrangement = Arrangement.spacedBy(RowGap),
    ) {
        NameLine(headerLine = state.headerLine, onEditContact = onEditContact)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .semantics(mergeDescendants = true) { contentDescription = summary },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(RowGap)) {
                Text(
                    text = state.factCount.toString(),
                    modifier = Modifier.alignByBaseline(),
                    style = HhTheme.typography.numeralHero,
                    color = colors.onHeader,
                )
                Text(
                    text = stringResource(R.string.feature_profile_impl_hero_caption),
                    modifier = Modifier.alignByBaseline(),
                    style = HhTheme.typography.headlineM,
                    color = colors.onHeader,
                )
            }
            HhHeaderButton(
                label = stringResource(R.string.feature_profile_impl_add_evidence),
                onClick = onAddEvidence,
                trailingIcon = HhIcons.Add,
            )
        }
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
