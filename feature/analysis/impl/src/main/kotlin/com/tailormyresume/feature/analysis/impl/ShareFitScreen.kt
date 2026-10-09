package com.tailormyresume.feature.analysis.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.component.TmrStatusChip
import com.tailormyresume.core.designsystem.component.TmrStatusKind
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.KeywordCoverage

@Composable
internal fun ShareFitScreen(
    state: AnalysisUiState.Result,
    actions: AnalysisActions,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = actions.onDismissOverlay)
    val role = state.job.title.withoutCompany(state.job.company)
        .ifBlank { stringResource(R.string.feature_analysis_impl_role_not_set) }
    val met = state.keywordCoverage.covered
    val toPrepare = (state.keywordCoverage.total - met).coerceAtLeast(0)
    val shareText = stringResource(R.string.feature_analysis_impl_share_text, role, met, toPrepare)
    TmrScreen(
        modifier = modifier.fillMaxSize(),
        header = {
            TmrInnerHeader(
                title = stringResource(R.string.feature_analysis_impl_share_title),
                onBack = actions.onDismissOverlay,
                backContentDescription = stringResource(R.string.feature_analysis_impl_back),
            )
        },
        sheet = false,
        bottomBar = {
            TmrBottomActionBar(stacked = true, primaryLast = false) {
                TmrPrimaryButton(
                    label = stringResource(R.string.feature_analysis_impl_share_my_fit),
                    onClick = { actions.onShareText(shareText) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = TmrIcons.Share,
                )
                TmrSecondaryButton(
                    label = stringResource(R.string.feature_analysis_impl_share_cancel),
                    onClick = actions.onDismissOverlay,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(top = TmrTheme.spacing.sm)
                .padding(horizontal = TmrTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            ShareFitCard(
                role = role,
                company = state.job.company.trim(),
                coverage = state.keywordCoverage,
                met = met,
                gap = toPrepare,
            )
            NoteCard(icon = TmrIcons.Info) {
                Text(
                    text = stringResource(R.string.feature_analysis_impl_share_excluded),
                    style = TmrTheme.typography.labelL,
                    color = TmrTheme.colors.onSurface,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShareFitCard(
    role: String,
    company: String,
    coverage: KeywordCoverage,
    met: Int,
    gap: Int,
    modifier: Modifier = Modifier,
) {
    val colors = TmrTheme.colors
    val covered = coverage.covered.toString()
    val total = coverage.total.toString()
    val description = stringResource(
        R.string.feature_analysis_impl_share_card_description,
        role,
        covered,
        total,
        met,
        gap,
    )
    TmrCard(
        modifier = modifier.semantics(mergeDescendants = true) { contentDescription = description },
        contentPadding = PaddingValues(TmrTheme.spacing.lg),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(colors.brand, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(TmrIcons.Check, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(20.dp))
            }
            Text(
                text = stringResource(R.string.feature_analysis_impl_share_brand),
                style = TmrTheme.typography.titleL,
                color = colors.onSurface,
            )
        }
        TmrSectionLabel(text = stringResource(R.string.feature_analysis_impl_share_label))
        Text(text = role, style = TmrTheme.typography.titleM, color = colors.onSurface)
        if (company.isNotBlank()) {
            Text(text = company, style = TmrTheme.typography.bodyM, color = colors.onSurfaceVariant)
        }
        Box(modifier = Modifier.clearAndSetSemantics {}) {
            TmrHeadline(
                text = stringResource(R.string.feature_analysis_impl_share_covers, covered, total),
                style = TmrTheme.typography.displayM,
            )
        }
        Text(
            text = stringResource(R.string.feature_analysis_impl_share_not_score),
            style = TmrTheme.typography.titleS,
            color = colors.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            TmrStatusChip(TmrStatusKind.Met, label = stringResource(R.string.feature_analysis_impl_summary_met, met))
            TmrStatusChip(TmrStatusKind.Gap, label = stringResource(R.string.feature_analysis_impl_summary_gap, gap))
        }
    }
}
