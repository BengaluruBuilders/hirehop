package com.hirehop.feature.profile.impl.guidedform

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
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.profile.impl.R
import com.hirehop.feature.profile.impl.common.Note
import com.hirehop.feature.profile.impl.common.NoteTone

@Composable
internal fun GuidedFormScreen(
    uiState: GuidedFormUiState,
    actions: GuidedFormActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val saved = uiState.saved
    val goBack = if (saved == null && !uiState.isFirstStep && !uiState.showIntro) actions.onBack else onBack
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
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
                HhLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_guided_form_loading))
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
            .padding(horizontal = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        if (uiState.message == GuidedMessage.LOAD_FAILED) {
            HhErrorCallout(title = stringResource(R.string.feature_profile_impl_guided_form_load_failed))
        }
        if (uiState.message == GuidedMessage.SAVE_FAILED) {
            HhErrorCallout(title = stringResource(R.string.feature_profile_impl_guided_form_save_failed))
        }
        if (uiState.showIntro) {
            Note(
                text = stringResource(R.string.feature_profile_impl_guided_form_scanned_arrival),
                tone = NoteTone.Warning,
                icon = HhIcons.Scan,
            )
            IntroContent()
        } else {
            if (uiState.isOffline) {
                HhOfflineBanner(message = stringResource(R.string.feature_profile_impl_guided_form_offline_message))
            }
            StepProgress(stepIndex = uiState.stepIndex)
            FiledEntries(uiState = uiState)
            Text(
                text = stringResource(stepTitleRes(uiState.step)),
                style = HhTheme.typography.headlineM,
                color = HhTheme.colors.onSurface,
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
    HhBottomActionBar(stacked = true, primaryLast = false) {
        if (uiState.saved != null) {
            HhPrimaryButton(
                label = stringResource(R.string.feature_profile_impl_guided_form_back_to_profile),
                onClick = actions.onFinishSaved,
                modifier = Modifier.weight(1f),
            )
        } else {
            HhPrimaryButton(
                label = primaryLabel(uiState),
                onClick = if (uiState.showIntro) actions.onStartForm else actions.onNext,
                enabled = !uiState.isSaving,
                trailingIcon = HhIcons.ArrowForward,
                modifier = Modifier.weight(1f),
            )
            HhTextButton(
                label = stringResource(R.string.feature_profile_impl_guided_form_save_and_finish_later),
                onClick = actions.onSaveAndFinishLater,
                enabled = !uiState.isSaving,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun primaryLabel(uiState: GuidedFormUiState): String = when {
    uiState.showIntro -> stringResource(R.string.feature_profile_impl_guided_form_start_with_contact)
    uiState.isLastStep -> stringResource(R.string.feature_profile_impl_guided_form_next_evidence)
    else -> stringResource(
        R.string.feature_profile_impl_guided_form_next,
        stringResource(stepTitleRes(guidedStepAt(uiState.stepIndex + 1))),
    )
}
