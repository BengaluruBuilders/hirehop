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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.hirehop.core.designsystem.component.HhAccent
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhEvidenceText
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhInkButton
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhMonogram
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOnColorChip
import com.hirehop.core.designsystem.component.HhOnColorChipStyle
import com.hirehop.core.designsystem.component.HhPillRow
import com.hirehop.core.designsystem.component.HhPillRowStyle
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusDisc
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.component.HhToastHost
import com.hirehop.core.designsystem.component.evidenceMarkSpanStyle
import com.hirehop.core.designsystem.component.rememberHhToastState
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.FactSource
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.SourceFactSheetContent
import com.hirehop.feature.tailor.impl.StatusCard
import com.hirehop.feature.tailor.impl.TailoredBulletSource
import com.hirehop.feature.tailor.impl.coverletter.CoverLetterFactRef
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PrepQuestionsScreen(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    modifier: Modifier = Modifier,
) {
    var openCard by remember { mutableStateOf<PrepQuestionCard?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
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
        header = { PrepQuestionsHeader(uiState, actions) },
        bottomBar = prepQuestionsBottomBar(uiState, actions),
        snackbarHost = { HhToastHost(toastState) },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = HhTheme.spacing.gutter,
                end = HhTheme.spacing.gutter,
                top = padding.calculateTopPadding() + HhTheme.spacing.sm,
                bottom = padding.calculateBottomPadding(),
            ),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2),
        ) {
            prepItems(uiState, actions, listState, scope) { openCard = it }
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

@Composable
private fun PrepQuestionsHeader(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
) {
    val largeFont = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
    HhInnerHeader(
        title = uiState.jobTitle.ifBlank {
            stringResource(R.string.feature_tailor_impl_role_not_set)
        },
        subtitle = uiState.subtitle(),
        onBack = actions.onNavigateBack,
        backContentDescription = stringResource(R.string.feature_tailor_impl_prep_questions_back),
        trailing = if (largeFont) null else ({ PrepQuestionsPill() }),
        belowTitle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                if (largeFont) {
                    PrepQuestionsPill()
                } else {
                    HhMonogram(
                        text = uiState.jobCompany.ifBlank {
                            stringResource(R.string.feature_tailor_impl_company_not_set)
                        },
                    )
                }
                if (uiState.stage == PrepQuestionsStage.READY) {
                    ReadyHeaderChips(uiState)
                }
            }
        },
    )
}

@Composable
private fun PrepQuestionsPill() {
    Row(
        modifier = Modifier
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.inverseSurface)
            .defaultMinSize(minHeight = HhTheme.spacing.d32)
            .padding(horizontal = HhTheme.spacing.md + HhTheme.spacing.d2),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_title),
            style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
            color = HhTheme.colors.inverseOnSurface,
        )
    }
}

@Composable
private fun ReadyHeaderChips(uiState: PrepQuestionsUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HhOnColorChip(
            label = pluralStringResource(
                R.plurals.feature_tailor_impl_prep_questions_count_header,
                uiState.questionCount,
                uiState.questionCount,
            ),
            style = HhOnColorChipStyle.White,
            accent = HhAccent.Jade,
        )
        if (uiState.gapCards.isNotEmpty()) {
            HhOnColorChip(
                label = pluralStringResource(
                    R.plurals.feature_tailor_impl_prep_questions_gaps_header,
                    uiState.gapCards.size,
                    uiState.gapCards.size,
                ),
                style = HhOnColorChipStyle.White,
                accent = HhAccent.Jade,
            )
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
                HhInkButton(
                    label = stringResource(R.string.feature_tailor_impl_prep_questions_notify),
                    onClick = actions.onNavigateBack,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
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
private fun PrepQuestionsUiState.subtitle(): String? = jobCompany.trim().ifBlank {
    stringResource(R.string.feature_tailor_impl_company_not_set)
}

private fun PrepQuestionsUiState.gapsListIndex(showOffline: Boolean): Int =
    (if (showOffline) 1 else 0) + (if (gapCards.isEmpty()) 0 else 1) + factCards.size

private fun LazyListScope.prepItems(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    listState: androidx.compose.foundation.lazy.LazyListState,
    scope: kotlinx.coroutines.CoroutineScope,
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
            item(key = "loading-title") {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_prep_questions_loading_title),
                    style = HhTheme.typography.headlineM,
                    color = HhTheme.colors.onSurface,
                )
            }
            item(key = "loading-steps") { GeneratingSteps() }
        }
        PrepQuestionsStage.READY -> readyItems(uiState, actions, showOffline, listState, scope, onFact)
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
private fun GeneratingSteps() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2)) {
        val readTitle = stringResource(R.string.feature_tailor_impl_prep_questions_step_read)
        val readSubtitle = stringResource(R.string.feature_tailor_impl_status_done)
        HhPillRow(
            title = readTitle,
            subtitle = readSubtitle,
            onClick = {},
            style = HhPillRowStyle.Jade,
            icon = HhIcons.Check,
            trailingIcon = null,
            modifier = Modifier.clearAndSetSemantics { contentDescription = "$readTitle. $readSubtitle" },
        )
        val matchTitle = stringResource(R.string.feature_tailor_impl_prep_questions_step_match)
        val matchSubtitle = stringResource(R.string.feature_tailor_impl_status_in_progress)
        HhPillRow(
            title = matchTitle,
            subtitle = matchSubtitle,
            onClick = {},
            style = HhPillRowStyle.Marigold,
            icon = HhIcons.Clock,
            trailingIcon = null,
            modifier = Modifier.clearAndSetSemantics { contentDescription = "$matchTitle. $matchSubtitle" },
        )
        val gapsTitle = stringResource(R.string.feature_tailor_impl_prep_questions_step_gaps)
        val gapsSubtitle = stringResource(R.string.feature_tailor_impl_status_up_next)
        HhPillRow(
            title = gapsTitle,
            subtitle = gapsSubtitle,
            onClick = {},
            style = HhPillRowStyle.Neutral,
            trailingIcon = null,
            modifier = Modifier.clearAndSetSemantics { contentDescription = "$gapsTitle. $gapsSubtitle" },
        )
    }
}

private fun LazyListScope.readyItems(
    uiState: PrepQuestionsUiState,
    actions: PrepQuestionsActions,
    showOffline: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    scope: kotlinx.coroutines.CoroutineScope,
    onFact: (PrepQuestionCard) -> Unit,
) {
    if (uiState.gapCards.isNotEmpty()) {
        item(key = "gaps-row") {
            HhPillRow(
                title = stringResource(R.string.feature_tailor_impl_prep_questions_group_gaps),
                subtitle = stringResource(R.string.feature_tailor_impl_prep_questions_gaps_subtitle),
                onClick = { scope.launch { listState.animateScrollToItem(uiState.gapsListIndex(showOffline)) } },
                style = HhPillRowStyle.Marigold,
                trailingIcon = HhIcons.ArrowForward,
                leading = { HhStatusDisc(kind = HhStatusKind.Gap) },
            )
        }
    }
    items(uiState.factCards.size, key = { uiState.factCards[it].id }) { index ->
        val card = uiState.factCards[index]
        QuestionCard(
            card = card,
            marked = !uiState.isOffline,
            isReported = card.id in uiState.reportedIds,
            actions = actions,
            onFact = onFact,
        )
    }
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

@Composable
private fun QuestionCard(
    card: PrepQuestionCard,
    marked: Boolean,
    isReported: Boolean,
    actions: PrepQuestionsActions,
    onFact: (PrepQuestionCard) -> Unit,
) {
    HhCard {
        QuestionNumber(card.ordinal)
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

@Composable
private fun QuestionNumber(ordinal: Int) {
    val colors = HhTheme.colors
    val fill = when (ordinal % 3) {
        1 -> colors.brand
        2 -> colors.coral
        else -> colors.special
    }
    val content = if (ordinal % 3 == 0) colors.onSpecial else colors.onBrand
    Box(
        modifier = Modifier.size(HhTheme.spacing.d32).background(fill, HhTheme.shapes.pill),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = ordinal.toString(),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = content,
        )
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
        val trimmed = requirement.trimEnd('.')
        if (marked) withStyle(style) { append(trimmed) } else append(trimmed)
        append(suffix)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FactsRow(card: PrepQuestionCard, onFact: (PrepQuestionCard) -> Unit) {
    val fact = card.fact ?: return
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
        itemVerticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_prep_questions_facts_label),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.brand,
        )
        FactChip(id = fact.displayId, card = card, onFact = onFact)
    }
}

@Composable
private fun FactChip(id: String, card: PrepQuestionCard, onFact: (PrepQuestionCard) -> Unit) {
    val description = stringResource(R.string.feature_tailor_impl_prep_questions_fact_description, id)
    Box(
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.touch)
            .clickable(role = Role.Button) { onFact(card) }
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HhTheme.shapes.card)
            .background(HhTheme.colors.special)
            .padding(HhTheme.spacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2),
    ) {
        HhStatusChip(kind = HhStatusKind.Gap, label = gapLabel())
        Text(
            text = card.requirementText,
            style = HhTheme.typography.titleL,
            color = HhTheme.colors.onSpecial,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(HhTheme.shapes.field)
                .background(HhTheme.colors.card)
                .padding(horizontal = HhTheme.spacing.md + HhTheme.spacing.d2, vertical = HhTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
        ) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_prep_questions_honest_way),
                style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
                color = HhTheme.colors.onSurfaceVariant,
            )
            Text(
                text = card.prompt,
                style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                color = HhTheme.colors.onSurface,
            )
        }
        HhInkButton(
            label = stringResource(R.string.feature_tailor_impl_prep_questions_open_prep_plan),
            onClick = actions.onOpenPrepPlan,
            modifier = Modifier.fillMaxWidth(),
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

private const val LARGE_FONT_SCALE = 1.5f
