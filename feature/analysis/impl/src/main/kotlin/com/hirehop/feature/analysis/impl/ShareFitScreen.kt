package com.hirehop.feature.analysis.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhStatusDisc
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.MatchStatus

@Composable
internal fun ShareFitScreen(
    state: AnalysisUiState.Result,
    actions: AnalysisActions,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = actions.onDismissOverlay)
    val role = state.job.title.withoutCompany(state.job.company)
        .ifBlank { stringResource(R.string.feature_analysis_impl_role_not_set) }
    val met = state.items.count { it.status == MatchStatus.MET }
    val partial = state.items.count { it.status == MatchStatus.PARTIAL }
    val gap = state.items.count { it.status == MatchStatus.GAP }
    val shareText = stringResource(R.string.feature_analysis_impl_share_text, role, met, partial, gap)
    HhScreen(
        modifier = modifier.fillMaxSize(),
        sheet = false,
        bottomBar = {
            HhBottomActionBar {
                HhPrimaryButton(
                    label = stringResource(R.string.feature_analysis_impl_share_whatsapp),
                    onClick = { actions.onShareText(shareText) },
                    modifier = Modifier.weight(1f),
                    leadingIcon = HhIcons.Share,
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ShareFitTopRow(onBack = actions.onDismissOverlay)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.feature_analysis_impl_share_my_fit),
                    style = HhTheme.typography.headlineL,
                    color = HhTheme.colors.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
                Text(
                    text = stringResource(R.string.feature_analysis_impl_share_intro),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
            ShareFitCard(
                role = role,
                met = met,
                partial = partial,
                gap = gap,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ShareInclusionRow(
                    icon = HhIcons.Check,
                    tint = HhTheme.colors.brand,
                    label = stringResource(R.string.feature_analysis_impl_share_included),
                    labelColor = HhTheme.colors.onSurface,
                )
                ShareInclusionRow(
                    icon = HhIcons.Close,
                    tint = HhTheme.colors.onSurfaceVariant,
                    label = stringResource(R.string.feature_analysis_impl_share_excluded),
                    labelColor = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

internal fun String.withoutCompany(company: String): String {
    val name = Regex.escape(company.trim())
    if (company.isBlank()) return this
    val option = RegexOption.IGNORE_CASE
    val atEnd = Regex("""(?:\s+at\s+|\s*[@\-\u2013|,(]\s*)$name\)?\s*$""", option)
    val atStart = Regex("""^\s*$name\s*[\-|:]\s*""", option)
    return when {
        atEnd.containsMatchIn(this) -> replace(atEnd, "")
        atStart.containsMatchIn(this) -> replace(atStart, "")
        else -> this
    }.trim()
}

@Composable
private fun ShareFitTopRow(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhIconButton(
            icon = HhIcons.ArrowBack,
            contentDescription = stringResource(R.string.feature_analysis_impl_back),
            onClick = onBack,
            borderColor = HhTheme.colors.outlineVariant,
        )
        Box(
            modifier = Modifier
                .height(32.dp)
                .clip(HhTheme.shapes.pill)
                .background(HhTheme.colors.card)
                .border(1.dp, HhTheme.colors.outlineVariant, HhTheme.shapes.pill)
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.feature_analysis_impl_share_size_chip),
                style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun ShareInclusionRow(
    icon: ImageVector,
    tint: Color,
    label: String,
    labelColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = label,
            style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
            color = labelColor,
        )
    }
}

@Composable
private fun ShareFitCard(
    role: String,
    met: Int,
    partial: Int,
    gap: Int,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val description = stringResource(R.string.feature_analysis_impl_share_card_description, role, met, partial, gap)
    Column(
        modifier = modifier
            .width(216.dp)
            .heightIn(min = 384.dp)
            .clip(HhTheme.shapes.card)
            .background(colors.brand)
            .padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 16.dp)
            .semantics(mergeDescendants = true) { contentDescription = description },
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(colors.onBrand),
            )
            Text(
                text = stringResource(R.string.feature_analysis_impl_share_brand),
                style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.onBrand,
            )
        }
        Spacer(modifier = Modifier.height(44.dp))
        Text(
            text = stringResource(R.string.feature_analysis_impl_share_card_label),
            style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
            color = colors.onHeaderVariant,
        )
        Text(
            text = role,
            style = HhTheme.typography.headlineL,
            color = colors.onBrand,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShareCountPill(
                kind = HhStatusKind.Met,
                count = met,
                unit = stringResource(R.string.feature_analysis_impl_share_unit_met),
            )
            ShareCountPill(
                kind = HhStatusKind.Partial,
                count = partial,
                unit = stringResource(R.string.feature_analysis_impl_share_unit_partial),
            )
            ShareCountPill(
                kind = HhStatusKind.Gap,
                count = gap,
                unit = stringResource(R.string.feature_analysis_impl_share_unit_gap),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.feature_analysis_impl_share_card_footer),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = colors.onHeaderVariant,
        )
    }
}

@Composable
private fun ShareCountPill(kind: HhStatusKind, count: Int, unit: String) {
    Row(
        modifier = Modifier
            .heightIn(min = 38.dp)
            .fillMaxWidth()
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.surface)
            .padding(start = 8.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhStatusDisc(kind = kind, size = 22.dp)
        Text(text = count.toString(), style = HhTheme.typography.numeralM, color = HhTheme.colors.onSurface)
        Text(
            text = unit,
            style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
            color = HhTheme.colors.onSurface,
        )
    }
}
