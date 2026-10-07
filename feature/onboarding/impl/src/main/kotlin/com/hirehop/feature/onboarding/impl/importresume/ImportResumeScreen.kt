package com.hirehop.feature.onboarding.impl.importresume

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.DisclosureCard
import com.hirehop.feature.onboarding.impl.common.NoticeTone
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.OnboardingStepBar

private val CHOOSE_CIRCLE_SIZE = 64.dp
private val CHOOSE_ICON_SIZE = 28.dp
private val FILE_TILE_SIZE = 44.dp
private val FILE_TILE_ICON = 22.dp
private val PROGRESS_HEIGHT = 8.dp

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
        lightTop = true,
        header = {
            OnboardingStepBar(
                modifier = Modifier.statusBarsPadding().padding(horizontal = HhTheme.spacing.gutter),
                onBack = actions.onBack,
                backContentDescription = stringResource(R.string.feature_onboarding_impl_import_resume_back_description),
                title = if (uiState.isParsing) stringResource(R.string.feature_onboarding_impl_import_resume_reading_heading) else null,
            )
        },
        bottomBar = if (uiState.isParsing) null else ({ ImportResumeBottomBar(uiState = uiState, actions = actions) }),
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
        uiState.isParsing -> ReadingContent(uiState = uiState)
        uiState.isQueued || uiState.isSuccess || uiState.isStop || uiState.stage == ImportStage.Unsupported ->
            FileOutcomeContent(uiState = uiState)
        else -> ChooseContent(uiState = uiState, actions = actions)
    }
}

@Composable
private fun ScreenHeading() {
    HhHeadline(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_heading),
        style = HhTheme.typography.displayM,
        color = HhTheme.colors.onSurface,
    )
}

@Composable
private fun ChooseContent(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
) {
    ScreenHeading()
    Text(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_intro),
        style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
        color = HhTheme.colors.onSurfaceVariant,
    )
    if (uiState.isOffline) {
        OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_offline_banner),
            icon = HhIcons.Offline,
        )
    }
    ChoosePicker(onPickFile = actions.onPickFile)
    HhCard(onClick = actions.onStartGuidedForm) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(icon = HhIcons.Add)
            Text(
                text = stringResource(R.string.feature_onboarding_impl_import_resume_option_none_title),
                modifier = Modifier.weight(1f),
                style = HhTheme.typography.titleS,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun ChoosePicker(onPickFile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HhTheme.shapes.card)
            .clickable(role = Role.Button, onClick = onPickFile)
            .padding(vertical = HhTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Box(
            modifier = Modifier.size(CHOOSE_CIRCLE_SIZE).background(HhTheme.colors.brand, HhTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = HhIcons.Description,
                contentDescription = null,
                tint = HhTheme.colors.onBrand,
                modifier = Modifier.size(CHOOSE_ICON_SIZE),
            )
        }
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_choose),
            style = HhTheme.typography.titleM,
            color = HhTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_choose_hint),
            style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun IconTile(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier.size(FILE_TILE_SIZE).background(HhTheme.colors.primaryContainer, HhTheme.shapes.tag),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HhTheme.colors.onSurface,
            modifier = Modifier.size(FILE_TILE_ICON),
        )
    }
}

@Composable
private fun FileCard(uiState: ImportResumeUiState, progress: Float? = null) {
    val context = LocalContext.current
    val meta = if (uiState.isQueued) {
        stringResource(R.string.feature_onboarding_impl_import_resume_queued_waiting)
    } else {
        stringResource(
            R.string.feature_onboarding_impl_import_resume_file_meta,
            uiState.fileName.substringAfterLast('.').uppercase(),
            Formatter.formatShortFileSize(context, uiState.byteSize),
        )
    }
    HhCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(icon = HhIcons.Description)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        contentDescription = uiState.fileName
                        liveRegion = LiveRegionMode.Polite
                    },
            ) {
                Text(text = uiState.fileName, style = HhTheme.typography.titleS, color = HhTheme.colors.onSurface)
                Text(
                    text = meta,
                    style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
        }
        if (progress != null) {
            ReadingProgress(fraction = progress)
        }
    }
}

@Composable
private fun ReadingProgress(fraction: Float) {
    val description = stringResource(R.string.feature_onboarding_impl_import_resume_progress_description)
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PROGRESS_HEIGHT)
                .background(HhTheme.colors.primaryContainer, HhTheme.shapes.pill)
                .semantics(mergeDescendants = true) {
                    contentDescription = description
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(HhTheme.colors.brand, HhTheme.shapes.pill),
            )
        }
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_reading_time),
            style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReadingContent(uiState: ImportResumeUiState) {
    val fraction = uiState.readStepIndex.coerceIn(0, READ_STEP_COUNT).toFloat() / READ_STEP_COUNT
    FileCard(uiState = uiState, progress = fraction)
    HhSectionLabel(text = stringResource(R.string.feature_onboarding_impl_import_resume_facts_so_far, uiState.factCount))
    uiState.facts.forEach { fact -> FactRow(fact = fact) }
    Text(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_reading_footnote),
        style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun FactRow(fact: ImportedFactUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HhTheme.colors.card, HhTheme.shapes.statusRow)
            .padding(horizontal = HhTheme.spacing.md + HhTheme.spacing.xxs, vertical = HhTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhFactId(id = fact.id)
        Text(
            text = fact.line,
            modifier = Modifier.weight(1f),
            style = HhTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun FileOutcomeContent(uiState: ImportResumeUiState) {
    ScreenHeading()
    FileCard(uiState = uiState)
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
        return
    }
    OnboardingNotice(
        text = outcomeText(uiState),
        icon = if (uiState.isQueued) HhIcons.Offline else HhIcons.Error,
        tone = outcomeTone(uiState),
    )
}

private fun outcomeTone(uiState: ImportResumeUiState): NoticeTone = when {
    uiState.isQueued -> NoticeTone.Neutral
    uiState.stage == ImportStage.Failed || uiState.stage == ImportStage.TooLarge -> NoticeTone.Error
    else -> NoticeTone.Warning
}

@Composable
private fun outcomeText(uiState: ImportResumeUiState): String = when {
    uiState.isQueued -> stringResource(R.string.feature_onboarding_impl_import_resume_queued_body)
    else -> when (uiState.stage) {
        ImportStage.ScannedNoText -> stringResource(R.string.feature_onboarding_impl_import_resume_scanned_text)
        ImportStage.Unsupported -> stringResource(
            R.string.feature_onboarding_impl_import_resume_unsupported_body,
            uiState.fileName.substringAfterLast('.', "").uppercase(),
        )
        ImportStage.NoFactsFound -> stringResource(R.string.feature_onboarding_impl_import_resume_no_facts_body)
        ImportStage.Empty -> stringResource(R.string.feature_onboarding_impl_import_resume_empty_body)
        ImportStage.TooLarge -> stringResource(R.string.feature_onboarding_impl_import_resume_too_large_body)
        else -> stringResource(R.string.feature_onboarding_impl_import_resume_failed_body)
    }
}

@Composable
private fun ImportResumeBottomBar(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
) {
    val chooseAnother = stringResource(R.string.feature_onboarding_impl_import_resume_choose_another)
    val guided = stringResource(R.string.feature_onboarding_impl_import_resume_start_guided_form)
    when {
        uiState.isQueued -> ActionPair(chooseAnother, actions.onChooseAnotherFile)

        uiState.isSuccess -> ActionPair(
            primaryLabel = stringResource(R.string.feature_onboarding_impl_import_resume_review_facts),
            onPrimary = actions.onReviewFacts,
        )

        uiState.stage == ImportStage.Failed -> ActionPair(
            primaryLabel = stringResource(R.string.feature_onboarding_impl_import_resume_try_again),
            onPrimary = actions.onRetry,
            secondaryLabel = chooseAnother,
            onSecondary = actions.onChooseAnotherFile,
        )

        uiState.stage == ImportStage.ScannedNoText -> ActionPair(
            primaryLabel = guided,
            onPrimary = actions.onStartGuidedForm,
            secondaryLabel = chooseAnother,
            onSecondary = actions.onChooseAnotherFile,
        )

        uiState.isStop || uiState.stage == ImportStage.Unsupported -> ActionPair(
            primaryLabel = chooseAnother,
            onPrimary = actions.onChooseAnotherFile,
            secondaryLabel = guided,
            onSecondary = actions.onStartGuidedForm,
        )

        else -> Unit
    }
}

@Composable
private fun ActionPair(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String? = null,
    onSecondary: () -> Unit = {},
) {
    HhBottomActionBar(stacked = true) {
        HhPrimaryButton(label = primaryLabel, onClick = onPrimary, modifier = Modifier.fillMaxWidth())
        if (secondaryLabel != null) {
            HhSecondaryButton(label = secondaryLabel, onClick = onSecondary, modifier = Modifier.fillMaxWidth())
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
