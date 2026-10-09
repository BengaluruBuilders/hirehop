package com.tailormyresume.feature.profile.impl.guidedform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrErrorCallout
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.profile.impl.R
import com.tailormyresume.feature.profile.impl.common.Note
import com.tailormyresume.feature.profile.impl.common.NoteTone

@Composable
internal fun GuidedFormScreen(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val saved = uiState.saved
    val goBack = if (saved == null && !uiState.isFirstStep && !uiState.showIntro) actions.onBack else onBack
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = stringResource(R.string.feature_profile_impl_guided_form_title),
                onBack = goBack,
                backContentDescription = stringResource(R.string.feature_profile_impl_guided_form_back),
            )
        },
        bottomBar = if (uiState.isLoading) null else ({ GuidedActionBar(uiState = uiState, actions = actions) }),
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                TmrLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_guided_form_loading))
            }

            saved != null -> SavedBody(saved = saved, padding = padding)

            else -> FormBody(uiState = uiState, actions = actions, padding = padding)
        }
    }
}

@Composable
private fun FormBody(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(horizontal = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        if (uiState.message == GuidedMessage.LOAD_FAILED) {
            TmrErrorCallout(title = stringResource(R.string.feature_profile_impl_guided_form_load_failed))
        }
        if (uiState.message == GuidedMessage.SAVE_FAILED) {
            TmrErrorCallout(title = stringResource(R.string.feature_profile_impl_guided_form_save_failed))
        }
        if (uiState.showIntro) {
            Note(
                text = stringResource(R.string.feature_profile_impl_guided_form_scanned_arrival),
                tone = NoteTone.Warning,
                icon = TmrIcons.Scan,
            )
            IntroContent()
        } else {
            if (uiState.isOffline) {
                TmrOfflineBanner(message = stringResource(R.string.feature_profile_impl_guided_form_offline_message))
            }
            StepProgress(
                number = uiState.stepIndex + 1,
                stepName = stringResource(stepTitleRes(uiState.step)),
                filledBars = uiState.stepIndex + 1,
                doneSteps = uiState.completedSteps,
                currentStep = uiState.step,
            )
            FiledEntries(uiState = uiState, onEditFact = actions.onEditFact)
            Text(
                text = stringResource(stepTitleRes(uiState.step)),
                style = TmrTheme.typography.headlineM,
                color = TmrTheme.colors.onSurface,
            )
            StepContent(uiState = uiState, actions = actions)
        }
    }
}

@Composable
private fun GuidedActionBar(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
) {
    TmrBottomActionBar(stacked = true, primaryLast = false) {
        if (uiState.saved != null) {
            TmrPrimaryButton(
                label = stringResource(R.string.feature_profile_impl_guided_form_back_to_profile),
                onClick = actions.onFinishSaved,
                modifier = Modifier.weight(1f),
            )
        } else {
            TmrPrimaryButton(
                label = primaryLabel(uiState),
                onClick = primaryAction(uiState, actions),
                enabled = !uiState.isSaving,
                trailingIcon = TmrIcons.ArrowForward,
                modifier = Modifier.weight(1f),
            )
            TmrTextButton(
                label = stringResource(R.string.feature_profile_impl_guided_form_save_and_finish_later),
                onClick = actions.onSaveAndFinishLater,
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun primaryAction(uiState: GuidedFormUiState, actions: GuidedFormActions): () -> Unit = when {
    uiState.showIntro -> actions.onStartForm
    else -> actions.onNext
}

@Composable
private fun primaryLabel(uiState: GuidedFormUiState): String = when {
    uiState.showIntro -> stringResource(R.string.feature_profile_impl_guided_form_start_with_contact)
    uiState.isLastStep -> stringResource(R.string.feature_profile_impl_guided_form_continue_to_projects)
    else -> stringResource(
        R.string.feature_profile_impl_guided_form_next,
        stringResource(stepTitleRes(guidedStepAt(uiState.stepIndex + 1))),
    )
}
