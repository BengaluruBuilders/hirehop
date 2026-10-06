package com.hirehop.feature.tailor.impl.prepquestions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.FactSource
import com.hirehop.feature.tailor.impl.NoticeStrip
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.SourceFactSheetContent
import com.hirehop.feature.tailor.impl.StatusCard
import com.hirehop.feature.tailor.impl.TailoredBulletSource
import com.hirehop.feature.tailor.impl.coverletter.CoverLetterFactRef
import com.hirehop.feature.tailor.impl.jobLine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrepQuestionsScreen(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    modifier: Modifier = Modifier,
) {
    var openCard by remember { mutableStateOf<PrepQuestionCard?>(null) }
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
                subtitle = uiState.subtitle(),
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
                top = padding.calculateTopPadding() + HhTheme.spacing.sm,
                bottom = padding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2),
        ) {
            prepItems(uiState, actions) { openCard = it }
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
    PrepQuestionsStage.ERROR -> {
        {
            HhBottomActionBar {
                HhPrimaryButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_retry),
                    onClick = actions.onRetry,
                    modifier = Modifier.weight(1f),
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

@Composable
private fun PrepQuestionsUiState.subtitle(): String? = jobLine(jobTitle, jobCompany)

private fun LazyListScope.prepItems(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    onFact: (PrepQuestionCard) -> Unit,
) {
    if (uiState.isOffline && uiState.stage != PrepQuestionsStage.GENERATING) {
        item(key = "offline") {
            NoticeStrip(text = stringResource(R.string.feature_tailor_impl_prep_questions_offline), icon = HhIcons.Offline)
        }
    }
    when (uiState.stage) {
        PrepQuestionsStage.GENERATING -> item(key = "generating") {
            HhStepProgress(
                stepNames = listOf(
                    stringResource(R.string.feature_tailor_impl_prep_questions_step_read),
                    stringResource(R.string.feature_tailor_impl_prep_questions_step_match),
                    stringResource(R.string.feature_tailor_impl_prep_questions_step_gaps),
                ),
                currentStepIndex = 1,
                ordinalLabel = stringResource(R.string.feature_tailor_impl_prep_questions_caption),
                footnote = stringResource(R.string.feature_tailor_impl_prep_questions_footnote),
            )
        }
        PrepQuestionsStage.READY -> readyItems(uiState, actions, onFact)
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

private fun LazyListScope.readyItems(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    onFact: (PrepQuestionCard) -> Unit,
) {
    item(key = "summary") { SummaryCard(uiState.questionCount) }
    if (uiState.factCards.isNotEmpty()) {
        item(key = "facts-heading") {
            GroupHeading(stringResource(R.string.feature_tailor_impl_prep_questions_group_facts))
        }
        items(uiState.factCards.size, key = { uiState.factCards[it].id }) { index ->
            val card = uiState.factCards[index]
            QuestionCard(card, marked = !uiState.isOffline, isReported = card.id in uiState.reportedIds, actions = actions, onFact = onFact)
        }
    }
    if (uiState.gapCards.isNotEmpty()) {
        item(key = "gaps-heading") {
            GroupHeading(stringResource(R.string.feature_tailor_impl_prep_questions_group_gaps))
        }
        items(uiState.gapCards.size, key = { uiState.gapCards[it].id }) { index ->
            GapCard(uiState.gapCards[index], isReported = uiState.gapCards[index].id in uiState.reportedIds, actions = actions, onFact = onFact)
        }
    }
}

@Composable
private fun SummaryCard(count: Int) {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.cardPadding)) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
            Row(
                modifier = Modifier.semantics(mergeDescendants = true) {
                    contentDescription = ""
                },
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(text = count.toString(), style = HhTheme.typography.numeralM, color = HhTheme.colors.onSurface)
                Text(
                    text = pluralStringResource(R.plurals.feature_tailor_impl_prep_questions_count_label, count),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(R.string.feature_tailor_impl_prep_questions_intro),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GroupHeading(text: String) {
    Text(
        text = text,
        style = HhTheme.typography.titleS,
        color = HhTheme.colors.onSurfaceVariant,
        modifier = Modifier.padding(top = HhTheme.spacing.sm),
    )
}

@Composable
private fun QuestionCard(
    card: PrepQuestionCard,
    marked: Boolean,
    isReported: Boolean,
    actions: PrepQuestionsActions,
    onFact: (PrepQuestionCard) -> Unit,
) {
    HhCard {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_prep_questions_ordinal, card.ordinal),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
            Text(text = card.prompt, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
            HhEvidenceText(
                text = whyText(card.requirementText, marked, evidenceMarkSpanStyle()),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
            FactsRow(card, onFact)
            ReportButton(
                description = stringResource(R.string.feature_tailor_impl_prep_questions_report_description, card.ordinal),
                isReported = isReported,
                onClick = { actions.onReportInaccurate(card.id) },
            )
        }
    }
}

@Composable
private fun whyText(requirement: String, marked: Boolean, style: SpanStyle): AnnotatedString {
    val template = stringResource(R.string.feature_tailor_impl_prep_questions_why, "\u0000")
    val split = template.indexOf('\u0000')
    val prefix = template.substring(0, split)
    val suffix = template.substring(split + 1)
    return buildAnnotatedString {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(prefix) }
        if (marked) withStyle(style) { append(requirement) } else append(requirement)
        append(suffix)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactsRow(card: PrepQuestionCard, onFact: (PrepQuestionCard) -> Unit) {
    val fact = card.fact ?: return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_facts_label),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        val description = stringResource(R.string.feature_tailor_impl_prep_questions_fact_description, fact.displayId)
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = HhTheme.spacing.touch)
                .clickable(role = Role.Button) { onFact(card) }
                .semantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fact.displayId,
                style = HhTheme.typography.factId,
                color = HhTheme.colors.onSurface,
                modifier = Modifier
                    .background(HhTheme.colors.evidence, HhTheme.shapes.pill)
                    .padding(horizontal = HhTheme.spacing.md - HhTheme.spacing.d2, vertical = HhTheme.spacing.xs + HhTheme.spacing.xxs),
            )
        }
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
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HhStatusChip(
                    kind = HhStatusKind.Gap,
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_to_prepare),
                )
                Text(text = card.requirementText, style = HhTheme.typography.titleS, color = HhTheme.colors.onSurface)
            }
            Text(text = card.prompt, style = HhTheme.typography.bodyM, color = HhTheme.colors.body)
            FactsRow(card, onFact)
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
}

@Composable
private fun ReportButton(description: String, isReported: Boolean, onClick: () -> Unit) {
    HhTextButton(
        label = stringResource(
            if (isReported) R.string.feature_tailor_impl_menu_reported else R.string.feature_tailor_impl_prep_questions_report,
        ),
        onClick = onClick,
        enabled = !isReported,
        leadingIcon = HhIcons.Flag,
        modifier = Modifier.semantics { contentDescription = description },
    )
}
