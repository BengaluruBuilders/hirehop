package com.tailormyresume.feature.tailor.impl.prepquestions

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
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrBottomSheet
import com.tailormyresume.core.designsystem.component.TmrButtonSize
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrMonogram
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.component.TmrSpotKind
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.component.TmrToastHost
import com.tailormyresume.core.designsystem.component.rememberTmrToastState
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.feature.tailor.impl.GenerationStep
import com.tailormyresume.feature.tailor.impl.GenerationSteps
import com.tailormyresume.feature.tailor.impl.NoteLine
import com.tailormyresume.feature.tailor.impl.R
import com.tailormyresume.feature.tailor.impl.SourceFactSheetContent
import com.tailormyresume.feature.tailor.impl.StatusCard
import com.tailormyresume.feature.tailor.impl.StatusPill
import com.tailormyresume.feature.tailor.impl.StepMark
import com.tailormyresume.feature.tailor.impl.TailoredBulletSource
import com.tailormyresume.feature.tailor.impl.coverletter.CoverLetterFactRef

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrepQuestionsScreen(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    modifier: Modifier = Modifier,
) {
    var openCard by remember { mutableStateOf<PrepQuestionCard?>(null) }
    var gapsChosen by remember { mutableStateOf<Boolean?>(null) }
    val showGaps = gapsChosen ?: uiState.factCards.isEmpty()
    val toastState = rememberTmrToastState()
    val message = uiState.message
    val messageText = message?.let { stringResource(R.string.feature_tailor_impl_report_thanks) }
    LaunchedEffect(message) {
        if (messageText == null) return@LaunchedEffect
        toastState.show(messageText)
        actions.onDismissMessage()
    }
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = stringResource(R.string.feature_tailor_impl_prep_questions_title),
                onBack = actions.onNavigateBack,
                backContentDescription = stringResource(R.string.feature_tailor_impl_prep_questions_back),
            )
        },
        bottomBar = prepQuestionsBottomBar(uiState, actions),
        snackbarHost = { TmrToastHost(toastState) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = TmrTheme.spacing.gutter,
                end = TmrTheme.spacing.gutter,
                top = padding.calculateTopPadding() + TmrTheme.spacing.md,
                bottom = padding.calculateBottomPadding() + TmrTheme.spacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            prepItems(uiState, actions, showGaps, { gapsChosen = it }) { openCard = it }
        }
    }
    openCard?.let { card ->
        val fact = card.fact
        if (fact != null) {
            TmrBottomSheet(onDismissRequest = { openCard = null }) {
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
            TmrBottomActionBar {
                TmrSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_notify),
                    onClick = actions.onNavigateBack,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
    PrepQuestionsStage.ERROR -> {
        {
            TmrBottomActionBar(stacked = true) {
                TmrSecondaryButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_back_application),
                    onClick = actions.onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                )
                TmrPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_retry),
                    onClick = actions.onRetry,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
    PrepQuestionsStage.READY -> {
        {
            TmrBottomActionBar {
                TmrPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_open_plan),
                    onClick = actions.onOpenPrepPlan,
                    trailingIcon = TmrIcons.ArrowForward,
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
            TmrOfflineBanner(message = stringResource(R.string.feature_tailor_impl_prep_questions_offline))
        }
    }
    when (uiState.stage) {
        PrepQuestionsStage.GENERATING -> {
            item(key = "loading-job") { JobCard(uiState) }
            item(key = "loading-steps") { GeneratingSteps() }
            item(key = "loading-caption") {
                NoteLine(
                    text = stringResource(R.string.feature_tailor_impl_prep_questions_caption),
                    icon = TmrIcons.Clock,
                )
            }
        }
        PrepQuestionsStage.READY -> readyItems(uiState, actions, showGaps, onShowGaps, onFact)
        PrepQuestionsStage.EMPTY_ANALYSIS -> item(key = "empty-analysis") {
            StatusCard(
                kind = TmrSpotKind.Empty,
                title = stringResource(R.string.feature_tailor_impl_prep_questions_empty_analysis_heading),
                body = stringResource(R.string.feature_tailor_impl_prep_questions_empty_analysis_body),
            )
        }
        PrepQuestionsStage.EMPTY_PROFILE -> item(key = "empty-profile") {
            StatusCard(
                kind = TmrSpotKind.Empty,
                title = stringResource(R.string.feature_tailor_impl_prep_questions_empty_profile_heading),
                body = stringResource(R.string.feature_tailor_impl_prep_questions_empty_profile_body),
            )
        }
        PrepQuestionsStage.ERROR -> item(key = "error") {
            StatusCard(
                kind = TmrSpotKind.Error,
                title = stringResource(R.string.feature_tailor_impl_prep_questions_error_title),
                body = stringResource(R.string.feature_tailor_impl_prep_questions_error_body),
            )
        }
    }
}

@Composable
private fun JobCard(uiState: PrepQuestionsUiState) {
    val company = uiState.jobCompany.trim().ifBlank { stringResource(R.string.feature_tailor_impl_company_not_set) }
    TmrCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TmrMonogram(text = company, size = 44.dp)
            Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs)) {
                Text(
                    text = uiState.jobTitle.ifBlank { stringResource(R.string.feature_tailor_impl_role_not_set) },
                    style = TmrTheme.typography.titleM,
                    color = TmrTheme.colors.onSurface,
                )
                Text(text = company, style = TmrTheme.typography.bodyS, color = TmrTheme.colors.onSurfaceVariant)
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
                style = TmrTheme.typography.bodyS,
                color = TmrTheme.colors.onSurfaceVariant,
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
            .background(TmrTheme.colors.card, TmrTheme.shapes.pillRow)
            .padding(TmrTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm - TmrTheme.spacing.xxs),
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
    val colors = TmrTheme.colors
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = TmrTheme.spacing.touch)
            .clip(TmrTheme.shapes.pillRow)
            .background(if (selected) colors.brand else Color.Transparent)
            .selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(horizontal = TmrTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm - TmrTheme.spacing.xxs, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val content = if (selected) colors.onBrand else colors.onSurface
        Text(text = label, style = TmrTheme.typography.labelL, color = content)
        Text(text = count.toString(), style = TmrTheme.typography.labelL, color = content)
    }
}

@Composable
private fun QuestionCard(
    card: PrepQuestionCard,
    isReported: Boolean,
    actions: PrepQuestionsActions,
    onFact: (PrepQuestionCard) -> Unit,
) {
    TmrCard {
        TmrSectionLabel(text = stringResource(R.string.feature_tailor_impl_prep_questions_question_label, card.ordinal))
        Text(text = card.prompt, style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_why, card.requirementText.trimEnd('.')),
            style = TmrTheme.typography.bodyS,
            color = TmrTheme.colors.onSurfaceVariant,
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
            .defaultMinSize(minHeight = TmrTheme.spacing.touch)
            .clickable(role = Role.Button) { onFact(card) }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.CenterStart,
    ) {
        TmrFactId(id = id)
    }
}

@Composable
private fun GapCard(
    card: PrepQuestionCard,
    isReported: Boolean,
    actions: PrepQuestionsActions,
    onFact: (PrepQuestionCard) -> Unit,
) {
    TmrCard {
        Text(text = card.requirementText, style = TmrTheme.typography.titleL, color = TmrTheme.colors.onSurface)
        StatusPill(label = gapLabel(), icon = TmrIcons.Flag, color = TmrTheme.colors.onSurface)
        Text(text = card.prompt, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurface)
        if (card.fact != null) {
            TmrSectionLabel(text = stringResource(R.string.feature_tailor_impl_prep_questions_facts_label))
            FactsRow(card, onFact)
        }
        TmrOutlineButton(
            label = stringResource(R.string.feature_tailor_impl_prep_questions_open_prep_plan),
            onClick = actions.onOpenPrepPlan,
            modifier = Modifier.fillMaxWidth(),
            size = TmrButtonSize.Compact,
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
    TmrTextButton(
        label = stringResource(
            if (isReported) R.string.feature_tailor_impl_menu_reported else R.string.feature_tailor_impl_prep_questions_report,
        ),
        onClick = onClick,
        enabled = !isReported,
        leadingIcon = TmrIcons.Flag,
        modifier = Modifier
            .heightIn(min = TmrTheme.spacing.touch)
            .semantics { contentDescription = description },
    )
}
