package com.hirehop.feature.onboarding.impl.importresume

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPaperColors
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.illustration.HhIllustration
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.DisclosureCard
import com.hirehop.feature.onboarding.impl.common.StateCard

private val CHOOSE_ICON_SIZE = 32.dp
private val THUMBNAIL_WIDTH = 112.dp
private val THUMBNAIL_HEIGHT = 150.dp
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
            HhInnerHeader(
                title = stringResource(R.string.feature_onboarding_impl_import_resume_title),
                subtitle = stringResource(R.string.feature_onboarding_impl_import_resume_subtitle),
                onBack = actions.onBack,
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
                .padding(horizontal = HhTheme.spacing.gutter),
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
        uiState.isQueued -> StateCard(
            illustration = HhIllustration.Offline,
            illustrationDescription = stringResource(R.string.feature_onboarding_impl_import_resume_spot_offline_description),
            title = stringResource(R.string.feature_onboarding_impl_import_resume_queued_title),
            body = stringResource(R.string.feature_onboarding_impl_import_resume_queued_body),
            extra = { WaitingFileChip(fileName = uiState.fileName) },
        )

        uiState.stage == ImportStage.Parsing || uiState.isSuccess -> ReadingContent(uiState)
        uiState.isStop || uiState.stage == ImportStage.Unsupported -> StopContent(uiState)
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
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.d16 + HhTheme.spacing.xxs)) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 + HhTheme.spacing.xxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HhIcons.Facts,
                    contentDescription = null,
                    tint = HhTheme.colors.primary,
                    modifier = Modifier.size(CHOOSE_ICON_SIZE),
                )
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_import_resume_choose_title),
                    style = HhTheme.typography.titleM,
                    color = HhTheme.colors.onSurface,
                )
            }
            Text(
                text = stringResource(R.string.feature_onboarding_impl_import_resume_choose_body),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
    }
    HhTextButton(
        label = stringResource(R.string.feature_onboarding_impl_import_resume_guided_form_link),
        onClick = actions.onStartGuidedForm,
    )
}

@Composable
private fun ReadingContent(uiState: ImportResumeUiState) {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.gutter)) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = uiState.fileName
                liveRegion = LiveRegionMode.Polite
            },
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12),
        ) {
            Text(text = uiState.fileName, style = HhTheme.typography.titleS, color = HhTheme.colors.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs)) {
                ResumeThumbnail(readStepIndex = uiState.readStepIndex)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4 + HhTheme.spacing.xxs),
                ) {
                    uiState.facts.take(LIFTED_FACTS_SHOWN).forEach { fact -> LiftedFactChip(fact) }
                }
            }
            if (uiState.isSuccess) {
                Text(
                    text = pluralStringResource(
                        R.plurals.feature_onboarding_impl_import_resume_success_heading,
                        uiState.factCount,
                        uiState.factCount,
                    ),
                    style = HhTheme.typography.titleS,
                    color = HhTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_import_resume_success_body),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                )
            }
        }
    }
    if (!uiState.isSuccess) {
        HhStepProgress(
            stepNames = listOf(
                stringResource(R.string.feature_onboarding_impl_import_resume_step_read),
                stringResource(R.string.feature_onboarding_impl_import_resume_step_sections),
                stringResource(R.string.feature_onboarding_impl_import_resume_step_strip),
            ),
            currentStepIndex = uiState.readStepIndex,
            ordinalLabel = stringResource(R.string.feature_onboarding_impl_import_resume_reading_ordinal),
            footnote = stringResource(R.string.feature_onboarding_impl_import_resume_reading_footnote),
        )
    }
}

@Composable
private fun ResumeThumbnail(readStepIndex: Int) {
    val lineColor = HhPaperColors.Rule
    val lineWidths = listOf(0.86f, 0.7f, 0.78f, 0.62f, 0.8f, 0.54f)
    Column(
        modifier = Modifier
            .size(width = THUMBNAIL_WIDTH, height = THUMBNAIL_HEIGHT)
            .clip(HhTheme.shapes.tag)
            .background(HhPaperColors.Page)
            .border(HhTheme.spacing.d2 / 2, HhTheme.colors.outlineVariant, HhTheme.shapes.tag)
            .padding(horizontal = HhTheme.spacing.d12, vertical = HhTheme.spacing.d12 + HhTheme.spacing.xxs),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.d2 / 2),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(HhTheme.spacing.sm)
                .background(HhPaperColors.Ink, HhTheme.shapes.tag),
        )
        lineWidths.forEachIndexed { index, fraction ->
            val read = index < readStepIndex * 2 + 1
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(HhTheme.spacing.d4 + HhTheme.spacing.xxs)
                    .background(if (read) lineColor else lineColor.copy(alpha = 0.25f), HhTheme.shapes.tag),
            )
        }
    }
}

@Composable
private fun LiftedFactChip(fact: ImportedFactUi) {
    val shape = HhTheme.shapes.field
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(HhTheme.colors.card, shape)
            .border(HhTheme.spacing.d2 / 2, HhTheme.colors.outlineVariant, shape)
            .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.d4 + HhTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.d4 + HhTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhFactId(id = fact.id)
        Text(
            text = fact.line,
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun WaitingFileChip(fileName: String) {
    Row(
        modifier = Modifier
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
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun StopContent(uiState: ImportResumeUiState) {
    val context = LocalContext.current
    val copy = when (uiState.stage) {
        ImportStage.ScannedNoText -> StopCopy(
            illustration = HhIllustration.Scanned,
            description = R.string.feature_onboarding_impl_import_resume_spot_scanned_description,
            title = stringResource(R.string.feature_onboarding_impl_import_resume_scanned_heading),
            body = stringResource(R.string.feature_onboarding_impl_import_resume_scanned_body),
        )

        ImportStage.Unsupported -> StopCopy(
            illustration = HhIllustration.Error,
            description = R.string.feature_onboarding_impl_import_resume_spot_error_description,
            title = stringResource(R.string.feature_onboarding_impl_import_resume_unsupported_heading),
            body = stringResource(R.string.feature_onboarding_impl_import_resume_unsupported_body, uiState.fileName),
        )

        ImportStage.NoFactsFound -> StopCopy(
            illustration = HhIllustration.Empty,
            description = R.string.feature_onboarding_impl_import_resume_spot_empty_description,
            title = stringResource(R.string.feature_onboarding_impl_import_resume_no_facts_heading),
            body = stringResource(R.string.feature_onboarding_impl_import_resume_no_facts_body),
        )

        ImportStage.Empty -> StopCopy(
            illustration = HhIllustration.Empty,
            description = R.string.feature_onboarding_impl_import_resume_spot_empty_description,
            title = stringResource(R.string.feature_onboarding_impl_import_resume_empty_heading),
            body = stringResource(R.string.feature_onboarding_impl_import_resume_empty_body),
        )

        ImportStage.TooLarge -> StopCopy(
            illustration = HhIllustration.Error,
            description = R.string.feature_onboarding_impl_import_resume_spot_error_description,
            title = stringResource(R.string.feature_onboarding_impl_import_resume_too_large_heading),
            body = stringResource(
                R.string.feature_onboarding_impl_import_resume_too_large_body,
                Formatter.formatShortFileSize(context, RESUME_READ_LIMIT_BYTES),
            ),
        )

        else -> StopCopy(
            illustration = HhIllustration.Error,
            description = R.string.feature_onboarding_impl_import_resume_spot_error_description,
            title = stringResource(R.string.feature_onboarding_impl_import_resume_failed_heading),
            body = stringResource(R.string.feature_onboarding_impl_import_resume_failed_body),
        )
    }
    StateCard(
        illustration = copy.illustration,
        illustrationDescription = stringResource(copy.description),
        title = copy.title,
        body = copy.body,
    )
}

private class StopCopy(
    val illustration: HhIllustration,
    val description: Int,
    val title: String,
    val body: String,
)

@Composable
private fun ImportResumeBottomBar(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
) {
    val chooseAnother = stringResource(R.string.feature_onboarding_impl_import_resume_choose_another)
    val guidedForm = stringResource(R.string.feature_onboarding_impl_import_resume_start_guided_form)
    when {
        uiState.isQueued -> HhBottomActionBar {
            HhOutlineButton(label = chooseAnother, onClick = actions.onChooseAnotherFile, modifier = Modifier.weight(1f))
        }

        uiState.isSuccess -> HhBottomActionBar {
            HhOutlineButton(label = chooseAnother, onClick = actions.onChooseAnotherFile, modifier = Modifier.weight(1f))
            HhPrimaryButton(
                label = stringResource(R.string.feature_onboarding_impl_import_resume_review_facts),
                onClick = actions.onReviewFacts,
                trailingIcon = HhIcons.ArrowForward,
                modifier = Modifier.weight(1f),
            )
        }

        uiState.stage == ImportStage.Parsing -> Unit

        uiState.stage == ImportStage.Failed -> HhBottomActionBar {
            HhOutlineButton(label = chooseAnother, onClick = actions.onChooseAnotherFile, modifier = Modifier.weight(1f))
            HhPrimaryButton(
                label = stringResource(R.string.feature_onboarding_impl_import_resume_try_again),
                onClick = actions.onRetry,
                modifier = Modifier.weight(1f),
            )
        }

        uiState.stage == ImportStage.Unsupported -> HhBottomActionBar {
            HhOutlineButton(label = guidedForm, onClick = actions.onStartGuidedForm, modifier = Modifier.weight(1f))
            HhPrimaryButton(label = chooseAnother, onClick = actions.onChooseAnotherFile, modifier = Modifier.weight(1f))
        }

        uiState.isStop -> HhBottomActionBar {
            HhOutlineButton(label = chooseAnother, onClick = actions.onChooseAnotherFile, modifier = Modifier.weight(1f))
            HhPrimaryButton(label = guidedForm, onClick = actions.onStartGuidedForm, modifier = Modifier.weight(1f))
        }

        else -> HhBottomActionBar {
            HhPrimaryButton(
                label = stringResource(R.string.feature_onboarding_impl_import_resume_choose),
                onClick = actions.onPickFile,
                enabled = uiState.canPick,
                trailingIcon = HhIcons.ArrowForward,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ImportResumeBarNotice() {
    DisclosureCard(
        text = AnnotatedString(stringResource(R.string.feature_onboarding_impl_import_resume_no_storage_permission)),
        icon = HhIcons.Lock,
    )
}
