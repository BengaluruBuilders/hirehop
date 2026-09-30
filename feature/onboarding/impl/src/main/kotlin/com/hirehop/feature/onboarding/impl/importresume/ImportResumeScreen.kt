package com.hirehop.feature.onboarding.impl.importresume

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.ui.FactIdTag
import com.hirehop.feature.onboarding.impl.R

private val HH_TOUCH_TARGET: Dp = 48.dp
private val HH_ICON_SIZE: Dp = 18.dp

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
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_onboarding_impl_import_resume_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_onboarding_impl_import_resume_back_description,
                ),
                onNavigationClick = actions.onBack,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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
    when (uiState.stage) {
        ImportStage.Parsing -> ImportReadingBody(uiState = uiState)
        ImportStage.Success, ImportStage.NoFactsFound -> ImportSuccessBody(uiState = uiState, actions = actions)
        ImportStage.ScannedNoText,
        ImportStage.Empty,
        ImportStage.TooLarge,
        ImportStage.Failed,
        -> ImportStopBody(uiState = uiState, actions = actions)
        ImportStage.Idle,
        ImportStage.Picking,
        ImportStage.Unsupported,
        -> ImportIdleBody(uiState = uiState, actions = actions)
    }
}

@Composable
private fun ImportIdleBody(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d20, vertical = HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        ImportHero()
        if (uiState.isQueued) {
            ImportQueuedCard(uiState = uiState)
        }
        if (uiState.stage == ImportStage.Unsupported) {
            ImportUnsupportedNote(fileName = uiState.fileName)
        }
        if (uiState.isPicking) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_import_resume_waiting_picker),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { liveRegion = LiveRegionMode.Polite },
            )
        }
        HhButton(
            onClick = actions.onPickFile,
            enabled = uiState.canPick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(
                text = stringResource(
                    if (uiState.stage == ImportStage.Unsupported) {
                        R.string.feature_onboarding_impl_import_resume_choose_another
                    } else {
                        R.string.feature_onboarding_impl_import_resume_choose
                    },
                ),
            )
        }
        ImportDisclosures()
        ImportGuidedFormLink(onClick = actions.onStartGuidedForm)
    }
}

@Composable
private fun ImportHero() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_heading),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_promise),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun ImportDisclosures() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_no_storage_permission),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_picker_note),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ImportGuidedFormLink(onClick: () -> Unit) {
    val label = stringResource(R.string.feature_onboarding_impl_import_resume_guided_form_link)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HH_TOUCH_TARGET)
            .clickable(onClick = onClick)
            .clearAndSetSemantics { contentDescription = label },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = HhTheme.typography.labelLarge,
            color = HhTheme.colors.primary,
            textAlign = TextAlign.Center,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = HhTheme.colors.primary,
            modifier = Modifier.sizeIn(maxWidth = HH_ICON_SIZE, maxHeight = HH_ICON_SIZE),
        )
    }
}

@Composable
private fun ImportQueuedCard(uiState: ImportResumeUiState) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = uiState.fileName,
            style = HhTheme.typography.mono,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(
                R.string.feature_onboarding_impl_import_resume_queued_waiting,
                fileSizeLabel(uiState.byteSize),
                fileTypeLabel(uiState.fileName),
            ),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_queued_offline),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun ImportUnsupportedNote(fileName: String) {
    Text(
        text = stringResource(R.string.feature_onboarding_impl_import_resume_unsupported_note, fileName),
        style = HhTheme.typography.bodyMedium,
        color = HhTheme.colors.onSpotContainer,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = HhTheme.colors.spotContainer,
                shape = RoundedCornerShape(HhTheme.shapes.sm),
            )
            .padding(HhTheme.spacing.md),
    )
}

@Composable
private fun ImportReadingBody(uiState: ImportResumeUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d20, vertical = HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhStepProgress(
            stepNames = readStepNames(),
            currentStepIndex = uiState.readStepIndex,
            ordinalLabel = stringResource(R.string.feature_onboarding_impl_import_resume_reading_ordinal),
        )
        if (uiState.facts.isNotEmpty()) {
            ImportLiftedFacts(facts = uiState.facts)
        }
    }
}

@Composable
private fun readStepNames(): List<String> = listOf(
    stringResource(R.string.feature_onboarding_impl_import_resume_step_read),
    stringResource(R.string.feature_onboarding_impl_import_resume_step_sections),
    stringResource(R.string.feature_onboarding_impl_import_resume_step_strip),
)

@Composable
private fun ImportLiftedFacts(facts: List<ImportedFactUi>) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_import_resume_lifted_heading),
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        facts.forEach { fact -> ImportLiftedFactCard(fact = fact) }
    }
}

@Composable
private fun ImportLiftedFactCard(fact: ImportedFactUi) {
    val section = stringResource(fact.category.sectionRes())
    val description = stringResource(
        R.string.feature_onboarding_impl_import_resume_lifted_card_description,
        section,
        fact.line,
    )
    val shape = RoundedCornerShape(HhTheme.shapes.md)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = HhTheme.colors.surfaceContainer, shape = shape)
            .border(width = HhTheme.spacing.d2, color = HhTheme.colors.hairline, shape = shape)
            .padding(HhTheme.spacing.md)
            .clearAndSetSemantics { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FactIdTag(factId = fact.id)
            Text(
                text = section,
                style = HhTheme.typography.monoSmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        Text(
            text = fact.line,
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun ImportSuccessBody(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d20, vertical = HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        ImportHero()
        if (uiState.isSuccess) {
            Text(
                text = pluralStringResource(
                    R.plurals.feature_onboarding_impl_import_resume_success_heading,
                    uiState.factCount,
                    uiState.factCount,
                ),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_onboarding_impl_import_resume_success_body),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            if (uiState.facts.isNotEmpty()) {
                ImportLiftedFacts(facts = uiState.facts)
            }
            HhButton(
                onClick = actions.onReviewFacts,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HH_TOUCH_TARGET),
            ) {
                Text(text = stringResource(R.string.feature_onboarding_impl_import_resume_review_facts))
            }
        } else {
            ImportNoFactsBody(actions = actions)
        }
        ImportDisclosures()
    }
}

@Composable
private fun ImportNoFactsBody(actions: ImportResumeActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg)) {
        HhSpotIllustration(
            kind = HhSpotKind.Empty,
            contentDescription = stringResource(
                R.string.feature_onboarding_impl_import_resume_spot_empty_description,
            ),
        )
        ImportStopCopy(
            heading = stringResource(R.string.feature_onboarding_impl_import_resume_no_facts_heading),
            body = stringResource(R.string.feature_onboarding_impl_import_resume_no_facts_body),
        )
        ImportStopActions(
            primaryLabel = stringResource(R.string.feature_onboarding_impl_import_resume_start_guided_form),
            onPrimary = actions.onStartGuidedForm,
            secondaryLabel = stringResource(R.string.feature_onboarding_impl_import_resume_choose_another),
            onSecondary = actions.onChooseAnotherFile,
        )
    }
}

@Composable
private fun ImportStopBody(
    uiState: ImportResumeUiState,
    actions: ImportResumeActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d20, vertical = HhTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        when (uiState.stage) {
            ImportStage.ScannedNoText -> {
                HhSpotIllustration(
                    kind = HhSpotKind.Scanned,
                    contentDescription = stringResource(
                        R.string.feature_onboarding_impl_import_resume_spot_scanned_description,
                    ),
                )
                ImportStopCopy(
                    heading = stringResource(R.string.feature_onboarding_impl_import_resume_scanned_heading),
                    body = stringResource(R.string.feature_onboarding_impl_import_resume_scanned_body),
                )
                ImportStopActions(
                    primaryLabel = stringResource(
                        R.string.feature_onboarding_impl_import_resume_start_guided_form,
                    ),
                    onPrimary = actions.onStartGuidedForm,
                    secondaryLabel = stringResource(
                        R.string.feature_onboarding_impl_import_resume_choose_another,
                    ),
                    onSecondary = actions.onChooseAnotherFile,
                )
            }

            ImportStage.Empty -> {
                HhErrorCallout(
                    title = stringResource(R.string.feature_onboarding_impl_import_resume_empty_heading),
                    supportingText = stringResource(R.string.feature_onboarding_impl_import_resume_empty_body),
                )
                ImportStopActions(
                    primaryLabel = stringResource(
                        R.string.feature_onboarding_impl_import_resume_choose_another,
                    ),
                    onPrimary = actions.onChooseAnotherFile,
                    secondaryLabel = stringResource(
                        R.string.feature_onboarding_impl_import_resume_start_guided_form,
                    ),
                    onSecondary = actions.onStartGuidedForm,
                )
            }

            ImportStage.TooLarge -> {
                HhErrorCallout(
                    title = stringResource(R.string.feature_onboarding_impl_import_resume_too_large_heading),
                    supportingText = stringResource(
                        R.string.feature_onboarding_impl_import_resume_too_large_body,
                        fileSizeLabel(RESUME_READ_LIMIT_BYTES),
                    ),
                )
                ImportStopActions(
                    primaryLabel = stringResource(
                        R.string.feature_onboarding_impl_import_resume_choose_another,
                    ),
                    onPrimary = actions.onChooseAnotherFile,
                    secondaryLabel = stringResource(
                        R.string.feature_onboarding_impl_import_resume_start_guided_form,
                    ),
                    onSecondary = actions.onStartGuidedForm,
                )
            }

            else -> {
                HhSpotIllustration(
                    kind = HhSpotKind.Error,
                    tint = HhTheme.colors.error,
                    contentDescription = stringResource(
                        R.string.feature_onboarding_impl_import_resume_spot_error_description,
                    ),
                )
                ImportStopCopy(
                    heading = stringResource(R.string.feature_onboarding_impl_import_resume_failed_heading),
                    body = stringResource(R.string.feature_onboarding_impl_import_resume_failed_body),
                )
                ImportStopActions(
                    primaryLabel = stringResource(R.string.feature_onboarding_impl_import_resume_try_again),
                    onPrimary = actions.onRetry,
                    secondaryLabel = stringResource(
                        R.string.feature_onboarding_impl_import_resume_choose_another,
                    ),
                    onSecondary = actions.onChooseAnotherFile,
                )
            }
        }
    }
}

@Composable
private fun ImportStopCopy(heading: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = heading,
            style = HhTheme.typography.headlineSmall,
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
private fun ImportStopActions(
    primaryLabel: String,
    onPrimary: () -> Unit,
    secondaryLabel: String,
    onSecondary: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        HhButton(
            onClick = onPrimary,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = primaryLabel)
        }
        HhOutlinedButton(
            onClick = onSecondary,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = secondaryLabel, color = HhTheme.colors.onSurface)
        }
    }
}

@Composable
private fun fileSizeLabel(byteSize: Long): String =
    Formatter.formatFileSize(LocalContext.current, byteSize)

@Composable
private fun fileTypeLabel(fileName: String): String {
    val lower = fileName.lowercase()
    val res = when {
        lower.endsWith(".pdf") -> R.string.feature_onboarding_impl_import_resume_file_type_pdf
        lower.endsWith(".docx") -> R.string.feature_onboarding_impl_import_resume_file_type_docx
        else -> R.string.feature_onboarding_impl_import_resume_file_type_other
    }
    return stringResource(res)
}

internal fun EntryCategory.sectionRes(): Int = when (this) {
    EntryCategory.EDUCATION -> R.string.feature_onboarding_impl_import_resume_section_education
    EntryCategory.EXPERIENCE -> R.string.feature_onboarding_impl_import_resume_section_experience
    EntryCategory.PROJECT -> R.string.feature_onboarding_impl_import_resume_section_project
    EntryCategory.CERTIFICATION -> R.string.feature_onboarding_impl_import_resume_section_certification
    EntryCategory.ACHIEVEMENT -> R.string.feature_onboarding_impl_import_resume_section_achievement
}
