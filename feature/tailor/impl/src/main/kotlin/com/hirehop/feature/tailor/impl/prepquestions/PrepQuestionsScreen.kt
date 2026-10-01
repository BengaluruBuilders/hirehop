package com.hirehop.feature.tailor.impl.prepquestions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextDecoration
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.prep.PrepQuestionKind
import com.hirehop.core.ui.FactIdTag
import com.hirehop.feature.tailor.impl.R

private const val HH_STACK_FONT_SCALE = 1.5f

@Composable
internal fun PrepQuestionsScreen(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_tailor_impl_prep_questions_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_tailor_impl_prep_questions_navigation_back_description,
                ),
                onNavigationClick = actions.onNavigateBack,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState.stage) {
                PrepQuestionsStage.GENERATING -> PrepQuestionsGenerating()
                PrepQuestionsStage.ERROR -> PrepQuestionsError(actions = actions)
                PrepQuestionsStage.EMPTY_ANALYSIS -> PrepQuestionsEmptyAnalysis()
                PrepQuestionsStage.EMPTY_PROFILE -> PrepQuestionsEmptyProfile()
                PrepQuestionsStage.READY,
                PrepQuestionsStage.OFFLINE,
                -> PrepQuestionsBody(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun PrepQuestionsGenerating() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        HhStepProgress(
            stepNames = listOf(
                stringResource(R.string.feature_tailor_impl_prep_questions_step_pick),
                stringResource(R.string.feature_tailor_impl_prep_questions_step_write),
                stringResource(R.string.feature_tailor_impl_prep_questions_step_check),
            ),
            currentStepIndex = 1,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_generating_note),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrepQuestionsError(actions: PrepQuestionsActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Error)
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_error_heading),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_error_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhButton(
            onClick = actions.onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_prep_questions_error_retry),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun PrepQuestionsEmptyAnalysis() {
    PrepQuestionsSpot(
        heading = stringResource(R.string.feature_tailor_impl_prep_questions_empty_analysis_heading),
        body = stringResource(R.string.feature_tailor_impl_prep_questions_empty_analysis_body),
    )
}

@Composable
private fun PrepQuestionsEmptyProfile() {
    PrepQuestionsSpot(
        heading = stringResource(R.string.feature_tailor_impl_prep_questions_empty_profile_heading),
        body = stringResource(R.string.feature_tailor_impl_prep_questions_empty_profile_body),
    )
}

@Composable
private fun PrepQuestionsSpot(
    heading: String,
    body: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = heading,
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = body,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PrepQuestionsBody(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_tailor_impl_prep_questions_offline_message),
            supportingText = stringResource(R.string.feature_tailor_impl_prep_questions_offline_supporting),
            visible = uiState.isOffline,
        )
        PrepQuestionsMessageNote(uiState = uiState, actions = actions)
        PrepQuestionsHeader(uiState = uiState)
        PrepQuestionFilterRow(uiState = uiState, onFilter = actions.onFilterChosen)
        uiState.visibleGroups.forEach { group ->
            PrepQuestionGroupSection(group = group, actions = actions)
        }
    }
}

@Composable
private fun PrepQuestionsMessageNote(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
) {
    val message = uiState.message ?: return
    HhCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            Text(
                text = stringResource(message.labelRes()),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            PrepQuestionsTextLink(
                label = stringResource(R.string.feature_tailor_impl_prep_questions_dismiss),
                onClick = actions.onDismissMessage,
            )
        }
    }
}

@Composable
private fun PrepQuestionsHeader(uiState: PrepQuestionsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        Text(
            text = uiState.totalCount.toString(),
            style = HhTheme.typography.heroNumeral,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = pluralStringResource(
                R.plurals.feature_tailor_impl_prep_questions_count,
                uiState.totalCount,
                uiState.totalCount,
            ),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_intro),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = pluralStringResource(
                R.plurals.feature_tailor_impl_prep_questions_practised_count,
                uiState.practisedCount,
                uiState.practisedCount,
                uiState.totalCount,
            ),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PrepQuestionFilterRow(
    uiState: PrepQuestionsUiState,
    onFilter: (PrepQuestionFilter) -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        PrepQuestionFilter.entries.forEach { filter ->
            val label = stringResource(filter.labelRes())
            PrepQuestionFilterChip(
                label = stringResource(
                    R.string.feature_tailor_impl_prep_questions_filter_count,
                    label,
                    uiState.countOf(filter),
                ),
                selected = uiState.filter == filter,
                onClick = { onFilter(filter) },
            )
        }
    }
}

@Composable
private fun PrepQuestionGroupSection(
    group: PrepQuestionGroup,
    actions: PrepQuestionsActions,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhStatusChip(
            kind = group.kind.statusKind(),
            label = stringResource(group.kind.headingRes()),
        )
        group.kind.groupNoteRes()?.let { noteRes ->
            Text(
                text = stringResource(noteRes),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        group.cards.forEach { card ->
            PrepQuestionCardRow(
                card = card,
                onPractise = actions.onPractiseToggled,
                onReport = actions.onReportInaccurate,
            )
        }
    }
}

@Composable
private fun PrepQuestionCardRow(
    card: PrepQuestionCard,
    onPractise: (String) -> Unit,
    onReport: (String) -> Unit,
) {
    val stacked = LocalDensity.current.fontScale > HH_STACK_FONT_SCALE
    val description = stringResource(
        R.string.feature_tailor_impl_prep_questions_card_description,
        stringResource(card.kind.kindLabelRes()),
        card.prompt,
        card.requirementText,
        stringResource(card.practiseActionRes()),
    )
    HhCard {
        Column(
            modifier = Modifier.clearAndSetSemantics { contentDescription = description },
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        ) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_prep_questions_ordinal, card.ordinal),
                style = HhTheme.typography.monoSmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
            Text(
                text = card.prompt,
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_prep_questions_requirement_heading),
                style = HhTheme.typography.labelMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_prep_questions_requirement, card.requirementText),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurface,
            )
            PrepQuestionFactLine(card = card)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (stacked) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                ) {
                    PrepQuestionPractiseControl(card = card, onPractise = onPractise)
                    PrepQuestionsTextLink(
                        label = stringResource(R.string.feature_tailor_impl_prep_questions_report_action),
                        onClick = { onReport(card.id) },
                    )
                }
            } else {
                PrepQuestionPractiseControl(card = card, onPractise = onPractise)
                PrepQuestionsTextLink(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_report_action),
                    onClick = { onReport(card.id) },
                )
            }
        }
    }
}

@Composable
private fun PrepQuestionPractiseControl(
    card: PrepQuestionCard,
    onPractise: (String) -> Unit,
) {
    if (card.isPractised) {
        Row(
            modifier = Modifier.defaultMinSize(minHeight = HhTheme.spacing.d48),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            HhStatusChip(
                kind = HhStatusKind.Met,
                label = stringResource(R.string.feature_tailor_impl_prep_questions_practised_label),
            )
            PrepQuestionsTextLink(
                label = stringResource(R.string.feature_tailor_impl_prep_questions_unpractise_action),
                onClick = { onPractise(card.id) },
            )
        }
    } else {
        HhOutlinedButton(
            onClick = { onPractise(card.id) },
            modifier = Modifier.heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_prep_questions_practise_action),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun PrepQuestionFactLine(card: PrepQuestionCard) {
    val factId = card.backingFactId
    if (factId == null) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_fact_none),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        return
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_fact_heading),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        FactIdTag(factId = factId)
    }
}

@Composable
private fun PrepQuestionFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = HhTheme.colors
    val shape = RoundedCornerShape(HhTheme.shapes.sm)
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.d48)
            .background(color = if (selected) colors.primaryContainer else colors.surface, shape = shape)
            .border(
                width = HhTheme.spacing.d2,
                color = if (selected) colors.primaryContainer else colors.hairline,
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.sm),
    ) {
        Text(
            text = label,
            style = HhTheme.typography.labelLarge,
            color = if (selected) colors.onPrimaryContainer else colors.onSurface,
        )
    }
}

@Composable
private fun PrepQuestionsTextLink(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = HhTheme.typography.labelLarge,
        color = HhTheme.colors.onSurfaceVariant,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.d48)
            .clickable(onClick = onClick)
            .padding(vertical = HhTheme.spacing.sm),
    )
}

private fun PrepQuestionsMessage.labelRes(): Int = when (this) {
    PrepQuestionsMessage.PRACTISED -> R.string.feature_tailor_impl_prep_questions_practised
    PrepQuestionsMessage.UNPRACTISED -> R.string.feature_tailor_impl_prep_questions_unpractised
    PrepQuestionsMessage.REPORT_UNAVAILABLE -> R.string.feature_tailor_impl_prep_questions_report_unavailable
}

private fun PrepQuestionCard.practiseActionRes(): Int = if (isPractised) {
    R.string.feature_tailor_impl_prep_questions_unpractise_action
} else {
    R.string.feature_tailor_impl_prep_questions_practise_action
}

private fun PrepQuestionKind.statusKind(): HhStatusKind = when (this) {
    PrepQuestionKind.STRENGTH -> HhStatusKind.Met
    PrepQuestionKind.CLARIFY -> HhStatusKind.Partial
    PrepQuestionKind.GAP -> HhStatusKind.Gap
}

private fun PrepQuestionFilter.labelRes(): Int = when (this) {
    PrepQuestionFilter.ALL -> R.string.feature_tailor_impl_prep_questions_filter_all
    PrepQuestionFilter.STRENGTH -> R.string.feature_tailor_impl_prep_questions_filter_strength
    PrepQuestionFilter.CLARIFY -> R.string.feature_tailor_impl_prep_questions_filter_clarify
    PrepQuestionFilter.GAP -> R.string.feature_tailor_impl_prep_questions_filter_gap
}
