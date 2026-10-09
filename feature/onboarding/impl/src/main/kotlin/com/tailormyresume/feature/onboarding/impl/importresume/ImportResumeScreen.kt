package com.tailormyresume.feature.onboarding.impl.importresume

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
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.onboarding.impl.R
import com.tailormyresume.feature.onboarding.impl.common.DisclosureCard
import com.tailormyresume.feature.onboarding.impl.common.NoticeTone
import com.tailormyresume.feature.onboarding.impl.common.OnboardingNotice
import com.tailormyresume.feature.onboarding.impl.common.OnboardingStepBar

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
    TmrScreen(
        modifier = modifier,
        sheet = false,
        lightTop = true,
        header = {
            OnboardingStepBar(
                modifier = Modifier.statusBarsPadding().padding(horizontal = TmrTheme.spacing.gutter),
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
                .padding(horizontal = TmrTheme.spacing.gutter, vertical = TmrTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
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
    TmrHeadline(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_heading),
        style = TmrTheme.typography.displayM,
        color = TmrTheme.colors.onSurface,
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
        style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
        color = TmrTheme.colors.onSurfaceVariant,
    )
    if (uiState.isOffline) {
        OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_offline_banner),
            icon = TmrIcons.Offline,
        )
    }
    ChoosePicker(onPickFile = actions.onPickFile)
    TmrCard(onClick = actions.onStartGuidedForm) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(icon = TmrIcons.Add)
            Text(
                text = stringResource(R.string.feature_onboarding_impl_import_resume_option_none_title),
                modifier = Modifier.weight(1f),
                style = TmrTheme.typography.titleS,
                color = TmrTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun ChoosePicker(onPickFile: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TmrTheme.shapes.card)
            .clickable(role = Role.Button, onClick = onPickFile)
            .padding(vertical = TmrTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        Box(
            modifier = Modifier.size(CHOOSE_CIRCLE_SIZE).background(TmrTheme.colors.brand, TmrTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = TmrIcons.Description,
                contentDescription = null,
                tint = TmrTheme.colors.onBrand,
                modifier = Modifier.size(CHOOSE_ICON_SIZE),
            )
        }
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_choose),
            style = TmrTheme.typography.titleM,
            color = TmrTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_choose_hint),
            style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
            color = TmrTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun IconTile(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier.size(FILE_TILE_SIZE).background(TmrTheme.colors.primaryContainer, TmrTheme.shapes.tag),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TmrTheme.colors.onSurface,
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
    TmrCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconTile(icon = TmrIcons.Description)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .semantics(mergeDescendants = true) {
                        contentDescription = uiState.fileName
                        liveRegion = LiveRegionMode.Polite
                    },
            ) {
                Text(text = uiState.fileName, style = TmrTheme.typography.titleS, color = TmrTheme.colors.onSurface)
                Text(
                    text = meta,
                    style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
                    color = TmrTheme.colors.onSurfaceVariant,
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
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(PROGRESS_HEIGHT)
                .background(TmrTheme.colors.primaryContainer, TmrTheme.shapes.pill)
                .semantics(mergeDescendants = true) {
                    contentDescription = description
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .background(TmrTheme.colors.brand, TmrTheme.shapes.pill),
            )
        }
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_reading_time),
            style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold),
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ReadingContent(uiState: ImportResumeUiState) {
    val fraction = uiState.readStepIndex.coerceIn(0, READ_STEP_COUNT).toFloat() / READ_STEP_COUNT
    FileCard(uiState = uiState, progress = fraction)
    TmrSectionLabel(text = stringResource(R.string.feature_onboarding_impl_import_resume_facts_so_far, uiState.factCount))
    uiState.facts.forEach { fact -> FactRow(fact = fact) }
    Text(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_reading_footnote),
        style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
        color = TmrTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun FactRow(fact: ImportedFactUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TmrTheme.colors.card, TmrTheme.shapes.statusRow)
            .padding(horizontal = TmrTheme.spacing.md + TmrTheme.spacing.xxs, vertical = TmrTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrFactId(id = fact.id)
        Text(
            text = fact.line,
            modifier = Modifier.weight(1f),
            style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold),
            color = TmrTheme.colors.onSurface,
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
            style = TmrTheme.typography.titleM,
            color = TmrTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_success_body),
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.onSurfaceVariant,
        )
        return
    }
    OnboardingNotice(
        text = outcomeText(uiState),
        icon = if (uiState.isQueued) TmrIcons.Offline else TmrIcons.Error,
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
        else -> stringResource(
            when (uiState.failureCause) {
                ImportFailureCause.RateLimited -> R.string.feature_onboarding_impl_import_resume_rate_limited_body
                ImportFailureCause.QuotaReached -> R.string.feature_onboarding_impl_import_resume_quota_body
                ImportFailureCause.SignInRequired -> R.string.feature_onboarding_impl_import_resume_sign_in_body
                ImportFailureCause.Generic -> R.string.feature_onboarding_impl_import_resume_failed_body
            },
        )
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
    TmrBottomActionBar(stacked = true) {
        TmrPrimaryButton(label = primaryLabel, onClick = onPrimary, modifier = Modifier.fillMaxWidth())
        if (secondaryLabel != null) {
            TmrSecondaryButton(label = secondaryLabel, onClick = onSecondary, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ImportResumeBarNotice() {
    DisclosureCard(
        text = AnnotatedString(stringResource(R.string.feature_onboarding_impl_import_resume_no_storage_permission)),
        icon = TmrIcons.Lock,
    )
}
