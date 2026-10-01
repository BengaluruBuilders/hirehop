package com.hirehop.feature.profile.impl.guidedform

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSegmentedCounter
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.FactSource
import com.hirehop.core.ui.FactIdTag
import com.hirehop.core.ui.FactProvenanceChip
import com.hirehop.feature.profile.impl.R

private val HH_TOUCH_TARGET: Dp = 48.dp

private const val HH_FACT_SEPARATOR = " · "

@Composable
internal fun GuidedFormScreen(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_profile_impl_guided_form_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_profile_impl_guided_form_navigation_back_content_description,
                ),
                onNavigationClick = actions.onBack,
            )
        },
        bottomBar = { GuidedFormBottomBar(uiState = uiState, actions = actions) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isLoading) {
                GuidedFormLoading()
            } else {
                GuidedFormContent(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun GuidedFormLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_profile_impl_guided_form_loading),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun GuidedFormContent(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_profile_impl_guided_form_offline_message),
            visible = uiState.isOffline,
        )
        if (uiState.arrival == GuidedArrival.FROM_SCANNED_PDF) {
            GuidedArrivalCard()
        }
        if (uiState.message == GuidedMessage.LOAD_FAILED) {
            HhErrorCallout(
                title = stringResource(R.string.feature_profile_impl_guided_form_load_failed),
                actionLabel = stringResource(R.string.feature_profile_impl_guided_form_continue_now),
                onAction = actions.onContinueNow,
            )
        }
        if (uiState.isSaveRejected) {
            HhErrorCallout(
                title = stringResource(R.string.feature_profile_impl_guided_form_save_rejected_title),
                supportingText = stringResource(
                    R.string.feature_profile_impl_guided_form_save_rejected_body,
                ),
                actionLabel = stringResource(R.string.feature_profile_impl_guided_form_continue_now),
                onAction = actions.onContinueNow,
            )
        }
        GuidedStepHeader(uiState)
        val proofMs = HhTheme.motion.proof
        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = {
                fadeIn(animationSpec = tween(proofMs)) togetherWith
                    fadeOut(animationSpec = tween(proofMs))
            },
            label = "guidedFormStep",
        ) { step ->
            GuidedStepBody(
                uiState = uiState,
                step = step,
                onValueChange = actions.onValueChange,
            )
        }
        GuidedSaveLaterAction(actions = actions)
        if (uiState.previews.isNotEmpty()) {
            GuidedFoldedFacts(uiState = uiState)
        }
        GuidedTrustNotes()
        uiState.saved?.let { saved ->
            GuidedSavedCard(saved = saved, actions = actions)
        }
        if (uiState.isLastStep) {
            GuidedHandoffCard(actions = actions)
        }
    }
}

@Composable
private fun GuidedArrivalCard() {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhSpotIllustration(kind = HhSpotKind.Scanned)
            Text(
                text = stringResource(R.string.feature_profile_impl_guided_form_scanned_arrival),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun GuidedStepHeader(uiState: GuidedFormUiState) {
    val ordinal = stringResource(
        R.string.feature_profile_impl_guided_form_step_counter_description,
        uiState.stepIndex + 1,
        GUIDED_STEPS.size,
    )
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        HhSegmentedCounter(
            current = uiState.stepIndex + 1,
            total = GUIDED_STEPS.size,
            modifier = Modifier.clearAndSetSemantics { contentDescription = ordinal },
        )
        HhStepProgress(
            stepNames = GUIDED_STEPS.map { stringResource(stepTitleRes(it)) },
            currentStepIndex = uiState.stepIndex,
            ordinalLabel = ordinal,
        )
    }
}

@Composable
private fun GuidedStepBody(
    uiState: GuidedFormUiState,
    step: GuidedStep,
    onValueChange: (GuidedField, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = stringResource(stepSubtitleRes(step)),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (step == GuidedStep.SKILLS) {
            GuidedSkillsBody(uiState = uiState, onValueChange = onValueChange)
        } else {
            step.fields().forEach { field ->
                GuidedFieldInput(uiState = uiState, field = field, onValueChange = onValueChange)
            }
        }
        if (step == GuidedStep.EXPERIENCE) {
            Text(
                text = stringResource(R.string.feature_profile_impl_guided_form_skips_reassurance),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun GuidedSkillsBody(
    uiState: GuidedFormUiState,
    onValueChange: (GuidedField, String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        HhTextField(
            value = uiState.values[GuidedField.SKILL].orEmpty(),
            onValueChange = { onValueChange(GuidedField.SKILL, it) },
            label = stringResource(R.string.feature_profile_impl_guided_form_skills_title),
            placeholder = stringResource(R.string.feature_profile_impl_guided_form_skills_placeholder),
            supportingText = {
                Text(
                    text = stringResource(R.string.feature_profile_impl_guided_form_skills_hint),
                    style = HhTheme.typography.bodySmall,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            },
            singleLine = false,
            minLines = 3,
        )
        if (uiState.skills.isNotEmpty()) {
            Text(
                text = uiState.skills.joinToString(HH_FACT_SEPARATOR),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun GuidedFieldInput(
    uiState: GuidedFormUiState,
    field: GuidedField,
    onValueChange: (GuidedField, String) -> Unit,
) {
    HhTextField(
        value = uiState.values[field].orEmpty(),
        onValueChange = { onValueChange(field, it) },
        label = stringResource(fieldLabelRes(field)),
        placeholder = if (field.isDate()) {
            stringResource(R.string.feature_profile_impl_guided_form_field_date_hint)
        } else {
            null
        },
        errorText = uiState.fieldProblems[field]?.let { problemText(it) },
        singleLine = !field.isDate() && field != GuidedField.SKILL,
        modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
    )
}

@Composable
private fun GuidedSaveLaterAction(actions: GuidedFormActions) {
    HhButton(
        onClick = actions.onSaveAndFinishLater,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HH_TOUCH_TARGET),
    ) {
        Text(text = stringResource(R.string.feature_profile_impl_guided_form_save_and_finish_later))
    }
}

@Composable
private fun GuidedFoldedFacts(uiState: GuidedFormUiState) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feature_profile_impl_guided_form_folded_caption),
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        uiState.previews.forEach { preview ->
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(sectionLabelRes(preview.category)),
                        style = HhTheme.typography.labelSmall,
                        color = HhTheme.colors.onSurfaceVariant,
                    )
                    FactProvenanceChip(source = FactSource.USER_STATED, isConfirmed = false)
                    preview.entry?.let { FactIdTag(factId = it.id) }
                }
                Text(
                    text = preview.line,
                    style = HhTheme.typography.bodyMedium,
                    color = HhTheme.colors.onSurface,
                )
            }
        }
    }
}

@Composable
private fun GuidedTrustNotes() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        Text(
            text = stringResource(R.string.feature_profile_impl_guided_form_user_stated_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_guided_form_never_asks),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun GuidedSavedCard(
    saved: GuidedSaved,
    actions: GuidedFormActions,
) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        HhSpotIllustration(kind = HhSpotKind.Done)
        Text(
            text = stringResource(
                R.string.feature_profile_impl_guided_form_saved_summary,
                saved.completedSteps,
                saved.totalSteps,
            ),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
        )
        HhOutlinedButton(
            onClick = actions.onContinueNow,
            modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_profile_impl_guided_form_continue_now))
        }
    }
}

@Composable
private fun GuidedHandoffCard(actions: GuidedFormActions) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.feature_profile_impl_guided_form_handoff_title),
            style = HhTheme.typography.titleSmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_guided_form_handoff_body),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhButton(
            onClick = actions.onStartHandoff,
            modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_profile_impl_guided_form_handoff_continue))
        }
    }
}

@Composable
private fun GuidedFormBottomBar(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    HhBottomActionBar(
        contentPadding = PaddingValues(
            start = HhTheme.spacing.d20,
            end = HhTheme.spacing.d20,
            top = HhTheme.spacing.md,
            bottom = HhTheme.spacing.md,
        ),
        actions = {
            HhOutlinedButton(
                onClick = actions.onBack,
                enabled = !uiState.isFirstStep,
                modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
            ) {
                Text(text = stringResource(R.string.feature_profile_impl_guided_form_back))
            }
            HhButton(
                onClick = actions.onNext,
                enabled = !uiState.isSaving,
                modifier = Modifier.heightIn(min = HH_TOUCH_TARGET),
            ) {
                Text(text = stringResource(R.string.feature_profile_impl_guided_form_next))
            }
        },
    )
}

internal fun GuidedField.isDate(): Boolean = this == GuidedField.EDUCATION_START ||
    this == GuidedField.EDUCATION_END ||
    this == GuidedField.EXPERIENCE_START ||
    this == GuidedField.EXPERIENCE_END
