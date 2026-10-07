package com.hirehop.feature.tailor.impl.prepquestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhButtonSize
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhMonogram
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.FactSource
import com.hirehop.feature.tailor.impl.GenerationStep
import com.hirehop.feature.tailor.impl.GenerationSteps
import com.hirehop.feature.tailor.impl.NoteLine
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.SourceFactSheetContent
import com.hirehop.feature.tailor.impl.StatusCard
import com.hirehop.feature.tailor.impl.StatusPill
import com.hirehop.feature.tailor.impl.StepMark
import com.hirehop.feature.tailor.impl.TailoredBulletSource
import com.hirehop.feature.tailor.impl.coverletter.CoverLetterFactRef

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrepQuestionsScreen(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    modifier: Modifier = Modifier,
) {
    var openCard by remember { mutableStateOf<PrepQuestionCard?>(null) }
    var showGaps by remember { mutableStateOf(uiState.factCards.isEmpty()) }
    val toastState = rememberHhToastState()
    val message = uiState.message
    val messageText = message?.let { stringResource(R.string.feature_tailor_impl_report_thanks) }
    LaunchedEffect(message) {
        if (messageText == null) return@LaunchedEffect
        toastState.show(messageText)
        actions.onDismissMessage()
    }
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_tailor_impl_prep_questions_title),
                onBack = actions.onNavigateBack,
                backContentDescription = stringResource(R.string.feature_tailor_impl_prep_questions_back),
            )
        },
        bottomBar = prepQuestionsBottomBar(uiState, actions),
        snackbarHost = { HhToastHost(toastState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = HhTheme.spacing.gutter,
                end = HhTheme.spacing.gutter,
                top = padding.calculateTopPadding() + HhTheme.spacing.md,
                bottom = padding.calculateBottomPadding() + HhTheme.spacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            prepItems(uiState, actions, showGaps, { showGaps = it }) { openCard = it }
        }
    }
    openCard?.let { card ->
        val fact = card.fact
        if (fact != null) {
            HhBottomSheet(onDismissRequest = { openCard = null }) {
                SourceFactSheetContent(
                    sources = listOf(fact.asSource()),
                    onEditFact = { source ->
                        openCard = null
                        actions.onEditFact(source.entryId, source.category.name.lowercase())
                    },
                    onReport = {
                        actions.onReportInaccurate(card.id)
                        openCard = null
                    },
                    isReported = card.id in uiState.reportedIds,
                )
            }
        }
    }
}

private fun prepQuestionsBottomBar(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
): (@Composable () -> Unit)? = when (uiState.stage) {
    PrepQuestionsStage.GENERATING -> {
        {
            HhBottomActionBar {
                HhSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_notify),
                    onClick = actions.onNavigateBack,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    PrepQuestionsStage.ERROR -> {
        {
            HhBottomActionBar(stacked = true) {
                HhSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_back_application),
                    onClick = actions.onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                )
                HhPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_retry),
                    onClick = actions.onRetry,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    PrepQuestionsStage.READY -> {
        {
            HhBottomActionBar {
                HhPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_open_plan),
                    onClick = actions.onOpenPrepPlan,
                    trailingIcon = HhIcons.ArrowForward,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    else -> null
}

private fun CoverLetterFactRef.asSource(): TailoredBulletSource = TailoredBulletSource(
    id = factId,
    displayId = displayId,
    entryId = entryId,
    category = category,
    text = text,
    source = FactSource.IMPORTED,
    entryTitle = entryTitle,
    organization = "",
    dateRange = "",
)

private fun LazyListScope.prepItems(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    showGaps: Boolean,
    onShowGaps: (Boolean) -> Unit,
    onFact: (PrepQuestionCard) -> Unit,
) {
    val showOffline = uiState.isOffline && uiState.stage != PrepQuestionsStage.GENERATING
    if (showOffline) {
        item(key = "offline") {
            HhOfflineBanner(message = stringResource(R.string.feature_tailor_impl_prep_questions_offline))
        }
    }
    when (uiState.stage) {
        PrepQuestionsStage.GENERATING -> {
            item(key = "loading-job") { JobCard(uiState) }
            item(key = "loading-steps") { GeneratingSteps() }
            item(key = "loading-caption") {
                NoteLine(
                    text = stringResource(R.string.feature_tailor_impl_prep_questions_caption),
                    icon = HhIcons.Clock,
                )
            }
        }
        PrepQuestionsStage.READY -> readyItems(uiState, actions, showGaps, onShowGaps, onFact)
        PrepQuestionsStage.EMPTY_ANALYSIS -> item(key = "empty-analysis") {
            StatusCard(
                kind = HhSpotKind.Empty,
                title = stringResource(R.string.feature_tailor_impl_prep_questions_empty_analysis_heading),
                body = stringResource(R.string.feature_tailor_impl_prep_questions_empty_analysis_body),
            )
        }
        PrepQuestionsStage.EMPTY_PROFILE -> item(key = "empty-profile") {
            StatusCard(
                kind = HhSpotKind.Empty,
                title = stringResource(R.string.feature_tailor_impl_prep_questions_empty_profile_heading),
                body = stringResource(R.string.feature_tailor_impl_prep_questions_empty_profile_body),
            )
        }
        PrepQuestionsStage.ERROR -> item(key = "error") {
            StatusCard(
                kind = HhSpotKind.Error,
                title = stringResource(R.string.feature_tailor_impl_prep_questions_error_title),
                body = stringResource(R.string.feature_tailor_impl_prep_questions_error_body),
            )
        }
    }
}

@Composable
private fun JobCard(uiState: PrepQuestionsUiState) {
    val company = uiState.jobCompany.trim().ifBlank { stringResource(R.string.feature_tailor_impl_company_not_set) }
    HhCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhMonogram(text = company, size = 44.dp)
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
                Text(
                    text = uiState.jobTitle.ifBlank { stringResource(R.string.feature_tailor_impl_role_not_set) },
                    style = HhTheme.typography.titleM,
                    color = HhTheme.colors.onSurface,
                )
                Text(text = company, style = HhTheme.typography.bodyS, color = HhTheme.colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun GeneratingSteps() {
    GenerationSteps(
        listOf(
            GenerationStep(
                title = stringResource(R.string.feature_tailor_impl_prep_questions_step_read),
                detail = null,
                status = stringResource(R.string.feature_tailor_impl_status_done),
                mark = StepMark.Done,
            ),
            GenerationStep(
                title = stringResource(R.string.feature_tailor_impl_prep_questions_step_match),
                detail = null,
                status = stringResource(R.string.feature_tailor_impl_status_in_progress),
                mark = StepMark.Now,
            ),
            GenerationStep(
                title = stringResource(R.string.feature_tailor_impl_prep_questions_step_gaps),
                detail = null,
                status = stringResource(R.string.feature_tailor_impl_status_up_next),
                mark = StepMark.Waiting,
            ),
        ),
    )
}

private fun LazyListScope.readyItems(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    showGaps: Boolean,
    onShowGaps: (Boolean) -> Unit,
    onFact: (PrepQuestionCard) -> Unit,
) {
    val hasBoth = uiState.gapCards.isNotEmpty() && uiState.factCards.isNotEmpty()
    val gapsVisible = if (hasBoth) showGaps else uiState.factCards.isEmpty()
    if (hasBoth) {
        item(key = "tabs") {
            PrepTabs(
                factCount = uiState.factCards.size,
                gapCount = uiState.gapCards.size,
                gapsSelected = gapsVisible,
                onSelect = onShowGaps,
            )
        }
    }
    if (!gapsVisible) {
        item(key = "intro") {
            val count = uiState.questionCount
            Text(
                text = "$count ${pluralStringResource(R.plurals.feature_tailor_impl_prep_questions_count_label, count)}. " +
                    stringResource(R.string.feature_tailor_impl_prep_questions_intro),
                style = HhTheme.typography.bodyS,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        items(uiState.factCards.size, key = { uiState.factCards[it].id }) { index ->
            val card = uiState.factCards[index]
            QuestionCard(
                card = card,
                isReported = card.id in uiState.reportedIds,
                actions = actions,
                onFact = onFact,
            )
        }
    } else {
        items(uiState.gapCards.size, key = { uiState.gapCards[it].id }) { index ->
            val card = uiState.gapCards[index]
            GapCard(
                card = card,
                isReported = card.id in uiState.reportedIds,
                actions = actions,
                onFact = onFact,
            )
        }
    }
}

@Composable
private fun PrepTabs(factCount: Int, gapCount: Int, gapsSelected: Boolean, onSelect: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HhTheme.colors.card, HhTheme.shapes.pillRow)
            .padding(HhTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
    ) {
        PrepTab(
            label = stringResource(R.string.feature_tailor_impl_prep_questions_group_facts),
            count = factCount,
            selected = !gapsSelected,
            onClick = { onSelect(false) },
            modifier = Modifier.weight(1f),
        )
        PrepTab(
            label = stringResource(R.string.feature_tailor_impl_prep_questions_group_gaps),
            count = gapCount,
            selected = gapsSelected,
            onClick = { onSelect(true) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PrepTab(label: String, count: Int, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val colors = HhTheme.colors
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .clip(HhTheme.shapes.pillRow)
            .background(if (selected) colors.brand else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = HhTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val content = if (selected) colors.onBrand else colors.onSurface
        Text(text = label, style = HhTheme.typography.labelL, color = content)
        Text(text = count.toString(), style = HhTheme.typography.labelL, color = content)
    }
}

@Composable
private fun QuestionCard(
    card: PrepQuestionCard,
    isReported: Boolean,
    actions: PrepQuestionsActions,
    onFact: (PrepQuestionCard) -> Unit,
) {
    HhCard {
        HhSectionLabel(text = stringResource(R.string.feature_tailor_impl_prep_questions_question_label, card.ordinal))
        Text(text = card.prompt, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_why, card.requirementText.trimEnd('.')),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
        )
        FactsRow(card, onFact)
        ReportButton(
            description = stringResource(R.string.feature_tailor_impl_prep_questions_report_description, card.ordinal),
            isReported = isReported,
            onClick = { actions.onReportInaccurate(card.id) },
        )
    }
}

@Composable
private fun FactsRow(card: PrepQuestionCard, onFact: (PrepQuestionCard) -> Unit) {
    val fact = card.fact ?: return
    FactChip(id = fact.displayId, card = card, onFact = onFact)
}

@Composable
private fun FactChip(id: String, card: PrepQuestionCard, onFact: (PrepQuestionCard) -> Unit) {
    val description = stringResource(R.string.feature_tailor_impl_prep_questions_fact_description, id)
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .clickable(role = Role.Button) { onFact(card) }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.CenterStart,
    ) {
        HhFactId(id = id)
    }
}

@Composable
private fun GapCard(
    card: PrepQuestionCard,
    isReported: Boolean,
    actions: PrepQuestionsActions,
    onFact: (PrepQuestionCard) -> Unit,
) {
    HhCard {
        Text(text = card.requirementText, style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
        StatusPill(label = gapLabel(), icon = HhIcons.Flag, color = HhTheme.colors.onSurface)
        Text(text = card.prompt, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurface)
        if (card.fact != null) {
            HhSectionLabel(text = stringResource(R.string.feature_tailor_impl_prep_questions_facts_label))
            FactsRow(card, onFact)
        }
        HhOutlineButton(
            label = stringResource(R.string.feature_tailor_impl_prep_questions_open_prep_plan),
            onClick = actions.onOpenPrepPlan,
            modifier = Modifier.fillMaxWidth(),
            size = HhButtonSize.Compact,
        )
        ReportButton(
            description = stringResource(
                R.string.feature_tailor_impl_prep_questions_report_gap_description,
                card.requirementText,
            ),
            isReported = isReported,
            onClick = { actions.onReportInaccurate(card.id) },
        )
    }
}

@Composable
private fun gapLabel(): String =
    stringResource(R.string.feature_tailor_impl_prep_questions_gap_label)

@Composable
private fun ReportButton(description: String, isReported: Boolean, onClick: () -> Unit) {
    HhTextButton(
        label = stringResource(
            if (isReported) R.string.feature_tailor_impl_menu_reported else R.string.feature_tailor_impl_prep_questions_report,
        ),
        onClick = onClick,
        enabled = !isReported,
        leadingIcon = HhIcons.Flag,
        modifier = Modifier
            .heightIn(min = HhTheme.spacing.touch)
            .semantics { contentDescription = description },
    )
}
