package com.hirehop.feature.onboarding.impl.importresume

import android.text.format.Formatter
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhDecoration
import com.hirehop.core.designsystem.component.HhDecorationKind
import com.hirehop.core.designsystem.component.HhIconActionBar
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhPillRow
import com.hirehop.core.designsystem.component.HhPillRowStyle
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhStatusDisc
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.illustration.HhCharacterIllustration
import com.hirehop.core.designsystem.illustration.HhIllustration
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.DisclosureCard
import com.hirehop.feature.onboarding.impl.common.OnboardingStepBar

private const val IMPORT_STEP = 4

private val CHOOSE_ICON_SIZE = 20.dp
private val NOTE_ICON_SIZE = 20.dp
private val FILE_CARD_ICON_SIZE = 48.dp
private val FILE_CARD_ICON = 22.dp
private val PROGRESS_RING_SIZE = 176.dp
private val PROGRESS_RING_STROKE = 16.dp
private val STAGE_MARK_SIZE = 24.dp
private val STOP_BADGE_ICON_SIZE = 18.dp
private val STOP_BADGE_HEIGHT = 34.dp
private val STOP_HERO_HEIGHT = 196.dp
private val STOP_HERO_DISC = 150.dp
private val STOP_ART_WIDTH = 147.dp
private val STOP_ART_HEIGHT = 196.dp
private val TRY_NUMBER_SIZE = 28.dp
private val CHOOSE_ART_HEIGHT = 216.dp
private val CHOOSE_ART_WIDTH = 162.dp
private val CHOOSE_ART_RIGHT = 44.dp
private val CHOOSE_DISC_SIZE = 200.dp
private val CHOOSE_LOOP_X = 40.dp
private val CHOOSE_LOOP_Y = 12.dp
private val CHOOSE_PLUS_X = 110.dp
private val CHOOSE_PLUS_Y = 130.dp
private val HAIRLINE = 1.dp
private val LIFTED_FACTS_SHOWN = 3

data class ImportResumeActions(
    val onBack: () -> Unit,
    val onPickFile: () -> Unit,
    val onChooseAnotherFile: () -> Unit,
    val onRetry: () -> Unit,
    val onStartGuidedForm: () -> Unit,
    val onReviewFacts: () -> Unit,
)

@Composable
fun ImportResumeScreen(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            OnboardingStepBar(
                modifier = Modifier.padding(horizontal = HhTheme.spacing.gutter),
                step = IMPORT_STEP,
                onBack = if (uiState.stage == ImportStage.Parsing) null else actions.onBack,
                backContentDescription = stringResource(R.string.feature_onboarding_impl_import_resume_back_description),
            )
        },
        bottomBar = { ImportResumeBottomBar(uiState = uiState, actions = actions) },
        bottomBarNotice = if (uiState.showsPickNotice) ({ ImportResumeBarNotice() }) else null,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter, vertical = HhTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            ImportResumeBody(uiState = uiState, actions = actions)
        }
    }
}

@Composable
private fun ImportResumeBody(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
) {
    when {
        uiState.isQueued -> QueuedContent(uiState = uiState)
        uiState.isParsing || uiState.isSuccess -> ReadingContent(uiState = uiState)
        uiState.isStop || uiState.stage == ImportStage.Unsupported -> StopContent(uiState = uiState)
        else -> ChooseContent(uiState = uiState, actions = actions)
    }
}

@Composable
private fun ChooseContent(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
) {
    HhOfflineBanner(
        message = stringResource(R.string.feature_onboarding_impl_import_resume_offline_banner),
        visible = uiState.isOffline,
    )
    ScreenHeading(
        title = stringResource(R.string.feature_onboarding_impl_import_resume_heading),
        body = stringResource(R.string.feature_onboarding_impl_import_resume_intro),
    )
    OptionRows(actions = actions)
    DeleteNote()
    ChooseIllustration()
}

@Composable
private fun ScreenHeading(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(text = title, style = HhTheme.typography.headlineL, color = HhTheme.colors.onSurface)
        Text(text = body, style = HhTheme.typography.bodyL, color = HhTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun OptionRows(actions: ImportResumeActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        HhPillRow(
            title = stringResource(R.string.feature_onboarding_impl_import_resume_option_pdf_title),
            subtitle = stringResource(R.string.feature_onboarding_impl_import_resume_option_pdf_body),
            onClick = actions.onPickFile,
            style = HhPillRowStyle.Coral,
            icon = HhIcons.Description,
        )
        HhPillRow(
            title = stringResource(R.string.feature_onboarding_impl_import_resume_option_docx_title),
            subtitle = stringResource(R.string.feature_onboarding_impl_import_resume_option_docx_body),
            onClick = actions.onPickFile,
            style = HhPillRowStyle.Jade,
            icon = HhIcons.Description,
        )
        HhPillRow(
            title = stringResource(R.string.feature_onboarding_impl_import_resume_option_none_title),
            subtitle = stringResource(R.string.feature_onboarding_impl_import_resume_option_none_body),
            onClick = actions.onStartGuidedForm,
            style = HhPillRowStyle.Marigold,
            icon = HhIcons.Add,
        )
    }
}

@Composable
private fun DeleteNote() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = HhTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Lock,
            contentDescription = null,
            tint = HhTheme.colors.primary,
            modifier = Modifier.size(NOTE_ICON_SIZE),
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_delete_note),
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.labelL,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ChooseIllustration() {
    Box(
        modifier = Modifier.fillMaxWidth().height(CHOOSE_ART_HEIGHT),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = HhTheme.spacing.xl)
                .size(CHOOSE_DISC_SIZE)
                .background(HhTheme.colors.primaryContainer, HhTheme.shapes.pill),
        )
        HhDecoration(
            kind = HhDecorationKind.Loop,
            color = HhTheme.colors.coral,
            modifier = Modifier.align(Alignment.TopStart).padding(start = CHOOSE_LOOP_X, top = CHOOSE_LOOP_Y),
        )
        HhDecoration(
            kind = HhDecorationKind.Plus,
            color = HhTheme.colors.special,
            modifier = Modifier.align(Alignment.TopStart).padding(start = CHOOSE_PLUS_X, top = CHOOSE_PLUS_Y),
        )
        HhCharacterIllustration(
            illustration = HhIllustration.Hero,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = CHOOSE_ART_RIGHT)
                .size(width = CHOOSE_ART_WIDTH, height = CHOOSE_ART_HEIGHT),
        )
    }
}

@Composable
private fun ReadingContent(uiState: ImportResumeUiState) {
    Text(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_reading_heading),
        style = HhTheme.typography.headlineL,
        color = HhTheme.colors.onSurface,
    )
    FileCard(uiState = uiState)
    ReadingProgress(readStepIndex = uiState.readStepIndex)
    StageList(readStepIndex = uiState.readStepIndex, factCount = uiState.factCount)
    if (uiState.facts.isNotEmpty()) {
        LiftedFactChips(facts = uiState.facts)
    }
    ReadingOutcome(uiState = uiState)
}

@Composable
private fun FileCard(uiState: ImportResumeUiState) {
    val context = LocalContext.current
    val meta = stringResource(
        R.string.feature_onboarding_impl_import_resume_file_meta,
        uiState.fileName.substringAfterLast('.').uppercase(),
        Formatter.formatShortFileSize(context, uiState.byteSize),
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HhTheme.shapes.pillRow)
            .background(HhTheme.colors.card, HhTheme.shapes.pillRow)
            .border(HAIRLINE, HhTheme.colors.outlineVariant, HhTheme.shapes.pillRow),
    ) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = HhTheme.spacing.d40 + HhTheme.spacing.d32)
                .padding(horizontal = HhTheme.spacing.md),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(FILE_CARD_ICON_SIZE).background(HhTheme.colors.coral, HhTheme.shapes.pill),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = HhIcons.Description,
                    contentDescription = null,
                    tint = HhTheme.colors.onCoral,
                    modifier = Modifier.size(FILE_CARD_ICON),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        contentDescription = uiState.fileName
                        liveRegion = LiveRegionMode.Polite
                    },
            ) {
                Text(text = uiState.fileName, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
                Text(text = meta, style = HhTheme.typography.labelM, color = HhTheme.colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReadingProgress(readStepIndex: Int) {
    val colors = HhTheme.colors
    val fraction = readStepIndex.coerceIn(0, READ_STEP_COUNT).toFloat() / READ_STEP_COUNT
    val percent = (fraction * 100).toInt()
    val description = stringResource(R.string.feature_onboarding_impl_import_resume_progress_description)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(PROGRESS_RING_SIZE)) {
            drawReadingRing(track = colors.primaryContainer, arc = colors.brand, fraction = fraction)
        }
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_progress_percent, percent),
            style = HhTheme.typography.displayL,
            color = HhTheme.colors.onSurface,
        )
    }
}

private fun DrawScope.drawReadingRing(track: Color, arc: Color, fraction: Float) {
    val stroke = PROGRESS_RING_STROKE.toPx()
    val inset = stroke / 2f
    val box = Size(size.minDimension - stroke, size.minDimension - stroke)
    val topLeft = Offset(inset, inset)
    drawArc(
        color = track,
        startAngle = FULL_TURN_START,
        sweepAngle = FULL_TURN_SWEEP,
        useCenter = false,
        topLeft = topLeft,
        size = box,
        style = Stroke(width = stroke),
    )
    if (fraction <= 0f) return
    drawArc(
        color = arc,
        startAngle = FULL_TURN_START,
        sweepAngle = FULL_TURN_SWEEP * fraction,
        useCenter = false,
        topLeft = topLeft,
        size = box,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}

private const val FULL_TURN_START = -90f
private const val FULL_TURN_SWEEP = 360f

@Composable
private fun StageList(readStepIndex: Int, factCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md + HhTheme.spacing.xxs)) {
        StageRow(
            kind = stageKind(position = 0, current = readStepIndex),
            label = stringResource(R.string.feature_onboarding_impl_import_resume_stage_opened),
            status = stageStatus(position = 0, current = readStepIndex, factCount = factCount),
        )
        StageRow(
            kind = stageKind(position = 1, current = readStepIndex),
            label = stringResource(R.string.feature_onboarding_impl_import_resume_stage_finding),
            status = stageStatus(position = 1, current = readStepIndex, factCount = factCount),
        )
        StageRow(
            kind = stageKind(position = 2, current = readStepIndex),
            label = stringResource(R.string.feature_onboarding_impl_import_resume_stage_getting_ready),
            status = stageStatus(position = 2, current = readStepIndex, factCount = factCount),
        )
    }
}

private fun stageKind(position: Int, current: Int): HhStatusKind = when {
    position < current -> HhStatusKind.Met
    position == current -> HhStatusKind.Partial
    else -> HhStatusKind.Gap
}

@Composable
private fun stageStatus(position: Int, current: Int, factCount: Int): String? = when (stageKind(position, current)) {
    HhStatusKind.Met -> stringResource(R.string.feature_onboarding_impl_import_resume_stage_done)
    HhStatusKind.Partial -> stringResource(R.string.feature_onboarding_impl_import_resume_stage_so_far, factCount)
    HhStatusKind.Gap -> null
}

@Composable
private fun StageRow(kind: HhStatusKind, label: String, status: String?) {
    Row(
        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = HhTheme.spacing.touch),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhStatusDisc(kind = kind, size = STAGE_MARK_SIZE)
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = if (kind == HhStatusKind.Partial) HhTheme.typography.titleM else HhTheme.typography.titleS,
            color = if (kind == HhStatusKind.Gap) HhTheme.colors.onSurfaceVariant else HhTheme.colors.onSurface,
        )
        if (status != null) {
            Text(
                text = status,
                style = HhTheme.typography.labelL,
                color = if (kind == HhStatusKind.Met) HhTheme.colors.brand else HhTheme.colors.onPrimaryContainer,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LiftedFactChips(facts: List<ImportedFactUi>) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xs),
    ) {
        facts.take(LIFTED_FACTS_SHOWN).forEach { fact -> LiftedFactChip(term = factTerm(fact)) }
        val hidden = facts.size - LIFTED_FACTS_SHOWN
        if (hidden > 0) {
            LiftedFactChip(
                term = stringResource(R.string.feature_onboarding_impl_import_resume_more_facts, hidden),
                background = HhTheme.colors.special,
                borderless = true,
            )
        }
    }
}

private fun factTerm(fact: ImportedFactUi): String = fact.line.substringBefore(',').substringBefore(" · ").trim()

@Composable
private fun LiftedFactChip(
    term: String,
    background: Color = HhTheme.colors.card,
    borderless: Boolean = false,
) {
    Box(
        modifier = Modifier
            .clip(HhTheme.shapes.pill)
            .background(background, HhTheme.shapes.pill)
            .then(if (borderless) Modifier else Modifier.border(HAIRLINE, HhTheme.colors.outlineVariant, HhTheme.shapes.pill)),
    ) {
        Text(
            text = term,
            modifier = Modifier.padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.xs + HhTheme.spacing.xs),
            style = HhTheme.typography.labelM.copy(fontWeight = FontWeight.Bold),
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun ReadingOutcome(uiState: ImportResumeUiState) {
    Text(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_reading_footnote),
        style = HhTheme.typography.bodyM,
        color = HhTheme.colors.onSurfaceVariant,
    )
    if (uiState.isSuccess) {
        Text(
            text = pluralStringResource(
                R.plurals.feature_onboarding_impl_import_resume_success_heading,
                uiState.factCount,
                uiState.factCount,
            ),
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_success_body),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun QueuedContent(uiState: ImportResumeUiState) {
    StopHero(
        illustration = HhIllustration.Offline,
        contentDescription = stringResource(R.string.feature_onboarding_impl_import_resume_spot_offline_description),
    )
    Text(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_queued_title),
        style = HhTheme.typography.headlineL,
        color = HhTheme.colors.onSurface,
    )
    Text(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_queued_body),
        style = HhTheme.typography.bodyL,
        color = HhTheme.colors.onSurfaceVariant,
    )
    WaitingFileChip(fileName = uiState.fileName)
}

@Composable
private fun WaitingFileChip(fileName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.neutralContainer)
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm - HhTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = HhIcons.Clock,
            contentDescription = null,
            tint = HhTheme.colors.onNeutralContainer,
            modifier = Modifier.size(HhTheme.spacing.lg),
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_queued_waiting, fileName),
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun StopContent(uiState: ImportResumeUiState) {
    val copy = stopCopy(uiState)
    StopHero(illustration = copy.illustration, contentDescription = stringResource(copy.description))
    if (copy.badge != null) {
        StopBadge(label = copy.badge)
    }
    Text(text = copy.title, style = HhTheme.typography.headlineL, color = HhTheme.colors.onSurface)
    Text(text = copy.body, style = HhTheme.typography.bodyL, color = HhTheme.colors.onSurfaceVariant)
    TrySteps()
}

@Composable
private fun StopHero(illustration: HhIllustration, contentDescription: String) {
    Box(modifier = Modifier.fillMaxWidth().height(STOP_HERO_HEIGHT)) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = HhTheme.spacing.xl)
                .size(STOP_HERO_DISC)
                .clip(HhTheme.shapes.pill)
                .background(HhTheme.colors.card)
                .border(HAIRLINE, HhTheme.colors.outlineVariant, HhTheme.shapes.pill),
        )
        HhDecoration(
            kind = HhDecorationKind.Zigzag,
            color = HhTheme.colors.coral,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = HhTheme.spacing.d32, end = HhTheme.spacing.d40),
        )
        HhDecoration(
            kind = HhDecorationKind.Ring,
            color = HhTheme.colors.special,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = HhTheme.spacing.d32 + HhTheme.spacing.lg, bottom = HhTheme.spacing.d32),
        )
        HhCharacterIllustration(
            illustration = illustration,
            contentDescription = contentDescription,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(width = STOP_ART_WIDTH, height = STOP_ART_HEIGHT),
        )
    }
}

@Composable
private fun StopBadge(label: String) {
    Box(modifier = Modifier.clip(HhTheme.shapes.pill).background(HhTheme.colors.coral, HhTheme.shapes.pill)) {
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = STOP_BADGE_HEIGHT)
                .padding(start = HhTheme.spacing.sm + HhTheme.spacing.xs, end = HhTheme.spacing.md + HhTheme.spacing.xs),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = HhIcons.Info,
                contentDescription = null,
                tint = HhTheme.colors.onCoral,
                modifier = Modifier.size(STOP_BADGE_ICON_SIZE),
            )
            Text(
                text = label,
                style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.Bold),
                color = HhTheme.colors.onCoral,
            )
        }
    }
}

@Composable
private fun TrySteps() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HhTheme.shapes.card)
            .background(HhTheme.colors.card)
            .border(HAIRLINE, HhTheme.colors.outlineVariant, HhTheme.shapes.card)
            .padding(HhTheme.spacing.cardPadding),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xs),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_try_title),
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
        )
        TryStep(ordinal = 1, label = stringResource(R.string.feature_onboarding_impl_import_resume_try_1))
        TryStep(ordinal = 2, label = stringResource(R.string.feature_onboarding_impl_import_resume_try_2))
        TryStep(ordinal = 3, label = stringResource(R.string.feature_onboarding_impl_import_resume_try_3))
    }
}

@Composable
private fun TryStep(ordinal: Int, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(TRY_NUMBER_SIZE).background(HhTheme.colors.special, HhTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = ordinal.toString(),
                style = HhTheme.typography.labelL.copy(fontWeight = FontWeight.ExtraBold),
                color = HhTheme.colors.onSpecial,
            )
        }
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.titleS,
            color = HhTheme.colors.onSurface,
        )
    }
}

private class StopCopy(
    val illustration: HhIllustration,
    val description: Int,
    val title: String,
    val body: String,
    val badge: String?,
)

@Composable
private fun stopCopy(uiState: ImportResumeUiState): StopCopy = when (uiState.stage) {
    ImportStage.ScannedNoText -> StopCopy(
        illustration = HhIllustration.Scanned,
        description = R.string.feature_onboarding_impl_import_resume_spot_scanned_description,
        title = stringResource(R.string.feature_onboarding_impl_import_resume_scanned_title),
        body = stringResource(R.string.feature_onboarding_impl_import_resume_scanned_text),
        badge = stringResource(R.string.feature_onboarding_impl_import_resume_scanned_badge),
    )

    ImportStage.Unsupported -> StopCopy(
        illustration = HhIllustration.Error,
        description = R.string.feature_onboarding_impl_import_resume_spot_error_description,
        title = stringResource(R.string.feature_onboarding_impl_import_resume_unsupported_heading),
        body = stringResource(R.string.feature_onboarding_impl_import_resume_unsupported_body, uiState.fileName),
        badge = null,
    )

    ImportStage.NoFactsFound -> StopCopy(
        illustration = HhIllustration.Empty,
        description = R.string.feature_onboarding_impl_import_resume_spot_empty_description,
        title = stringResource(R.string.feature_onboarding_impl_import_resume_no_facts_heading),
        body = stringResource(R.string.feature_onboarding_impl_import_resume_no_facts_body),
        badge = null,
    )

    ImportStage.Empty -> StopCopy(
        illustration = HhIllustration.Empty,
        description = R.string.feature_onboarding_impl_import_resume_spot_empty_description,
        title = stringResource(R.string.feature_onboarding_impl_import_resume_empty_heading),
        body = stringResource(R.string.feature_onboarding_impl_import_resume_empty_body),
        badge = null,
    )

    ImportStage.TooLarge -> StopCopy(
        illustration = HhIllustration.Error,
        description = R.string.feature_onboarding_impl_import_resume_spot_error_description,
        title = stringResource(R.string.feature_onboarding_impl_import_resume_too_large_heading),
        body = stringResource(R.string.feature_onboarding_impl_import_resume_failed_body),
        badge = null,
    )

    else -> StopCopy(
        illustration = HhIllustration.Error,
        description = R.string.feature_onboarding_impl_import_resume_spot_error_description,
        title = stringResource(R.string.feature_onboarding_impl_import_resume_failed_heading),
        body = stringResource(R.string.feature_onboarding_impl_import_resume_failed_body),
        badge = null,
    )
}

@Composable
private fun ImportResumeBottomBar(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
) {
    val back = stringResource(R.string.feature_onboarding_impl_import_resume_back_description)
    val chooseAnother = stringResource(R.string.feature_onboarding_impl_import_resume_choose_another)
    when {
        uiState.isQueued -> HhIconActionBar(
            secondaryIcon = HhIcons.ArrowBack,
            secondaryContentDescription = back,
            onSecondaryClick = actions.onBack,
            primaryLabel = chooseAnother,
            onPrimaryClick = actions.onChooseAnotherFile,
        )

        uiState.isParsing || uiState.isSuccess -> HhIconActionBar(
            secondaryIcon = HhIcons.Close,
            secondaryContentDescription = back,
            onSecondaryClick = actions.onBack,
            primaryLabel = stringResource(R.string.feature_onboarding_impl_import_resume_review_facts),
            onPrimaryClick = actions.onReviewFacts,
            primaryEnabled = uiState.isSuccess,
        )

        uiState.stage == ImportStage.Failed -> HhIconActionBar(
            secondaryIcon = HhIcons.ArrowBack,
            secondaryContentDescription = back,
            onSecondaryClick = actions.onBack,
            primaryLabel = stringResource(R.string.feature_onboarding_impl_import_resume_try_again),
            onPrimaryClick = actions.onRetry,
        )

        uiState.isStop || uiState.stage == ImportStage.Unsupported -> HhIconActionBar(
            secondaryIcon = HhIcons.Add,
            secondaryContentDescription = stringResource(R.string.feature_onboarding_impl_import_resume_start_guided_form),
            onSecondaryClick = actions.onStartGuidedForm,
            primaryLabel = chooseAnother,
            onPrimaryClick = actions.onChooseAnotherFile,
        )

        else -> Unit
    }
}

@Composable
private fun ImportResumeBarNotice() {
    DisclosureCard(
        text = AnnotatedString(stringResource(R.string.feature_onboarding_impl_import_resume_no_storage_permission)),
        icon = HhIcons.Lock,
    )
}
