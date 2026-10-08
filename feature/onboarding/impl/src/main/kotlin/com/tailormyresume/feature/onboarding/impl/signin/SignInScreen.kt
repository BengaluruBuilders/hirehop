package com.tailormyresume.feature.onboarding.impl.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrCheckbox
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.onboarding.impl.R
import com.tailormyresume.feature.onboarding.impl.common.DisclosureCard
import com.tailormyresume.feature.onboarding.impl.common.NoticeTone
import com.tailormyresume.feature.onboarding.impl.common.OnboardingNotice
import com.tailormyresume.feature.onboarding.impl.common.OnboardingStepBar
import com.tailormyresume.feature.onboarding.impl.common.ReasonText
import com.tailormyresume.feature.onboarding.impl.common.StateCard

private val GOOGLE_MARK_SIZE = 24.dp

@Composable
internal fun SignInScreen(
    uiState: SignInUiState,
    actions: SignInActions,
    modifier: Modifier = Modifier,
) {
    if (uiState.stage == SignInStage.UNDER_18) {
        UnderEighteenScreen(actions = actions, modifier = modifier)
    } else {
        SignInFormScreen(uiState = uiState, actions = actions, modifier = modifier)
    }
}

@Composable
private fun SignInFormScreen(
    uiState: SignInUiState,
    actions: SignInActions,
    modifier: Modifier = Modifier,
) {
    TmrScreen(
        modifier = modifier,
        sheet = false,
        bottomBar = { SignInBottomBar(uiState = uiState, actions = actions) },
        bottomBarNotice = { SignInReason(uiState = uiState) },
    ) { padding ->
        SignInContent(uiState = uiState, actions = actions, modifier = Modifier.padding(padding))
    }
}

@Composable
private fun SignInBottomBar(uiState: SignInUiState, actions: SignInActions) {
    TmrBottomActionBar {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            GoogleSignInButton(uiState = uiState, onContinue = actions.onContinue)
            TmrTextButton(
                label = stringResource(R.string.feature_onboarding_impl_sign_in_action_under_18),
                onClick = actions.onUnderEighteen,
            )
        }
    }
}

@Composable
private fun SignInContent(
    uiState: SignInUiState,
    actions: SignInActions,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        OnboardingStepBar(
            onBack = actions.onBack,
            backContentDescription = stringResource(
                R.string.feature_onboarding_impl_sign_in_navigation_back_content_description,
            ),
        )
        TmrHeadline(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_heading),
            style = TmrTheme.typography.displayM,
            color = TmrTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_intro),
            style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            color = TmrTheme.colors.onSurfaceVariant,
        )
        SignInAgeCard(uiState = uiState, onChange = actions.onAdultConfirmationChange)
        DisclosureCard(
            text = androidx.compose.ui.text.AnnotatedString(
                stringResource(R.string.feature_onboarding_impl_sign_in_age_support),
            ),
            icon = TmrIcons.Lock,
        )
        SignInNotices(uiState = uiState)
        SignInLegalLine()
    }
}

@Composable
private fun SignInNotices(uiState: SignInUiState) {
    TmrOfflineBanner(
        message = stringResource(R.string.feature_onboarding_impl_sign_in_offline_message),
        visible = uiState.isOffline,
    )
    when (uiState.stage) {
        SignInStage.FAILED -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_failure_message),
            icon = TmrIcons.Error,
            tone = NoticeTone.Error,
        )

        SignInStage.CANCELLED -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_cancelled_message),
            icon = TmrIcons.Block,
        )

        else -> Unit
    }
}

@Composable
private fun SignInAgeCard(uiState: SignInUiState, onChange: (Boolean) -> Unit) {
    TmrCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(value = uiState.isAdultConfirmed, role = Role.Checkbox, onValueChange = onChange),
            horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TmrCheckbox(
                checked = uiState.isAdultConfirmed,
                onCheckedChange = onChange,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_age_label),
                style = TmrTheme.typography.titleS,
                color = TmrTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun GoogleSignInButton(uiState: SignInUiState, onContinue: () -> Unit) {
    TmrPrimaryButton(
        label = stringResource(R.string.feature_onboarding_impl_sign_in_action_continue),
        onClick = onContinue,
        enabled = uiState.canContinue && uiState.isAdultConfirmed,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun SignInReason(uiState: SignInUiState) {
    when {
        uiState.isBusy -> ReasonText(text = stringResource(R.string.feature_onboarding_impl_sign_in_reason_in_progress))
        !uiState.isAdultConfirmed -> ReasonText(text = stringResource(R.string.feature_onboarding_impl_sign_in_reason_age))
    }
}

@Composable
private fun SignInLegalLine() {
    val strong = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)
    Text(
        text = buildAnnotatedString {
            append(stringResource(R.string.feature_onboarding_impl_sign_in_legal_prefix))
            withStyle(strong) { append(stringResource(R.string.feature_onboarding_impl_sign_in_legal_terms)) }
            append(stringResource(R.string.feature_onboarding_impl_sign_in_legal_and))
            withStyle(strong) { append(stringResource(R.string.feature_onboarding_impl_sign_in_legal_privacy)) }
            append(".")
        },
        modifier = Modifier.fillMaxWidth(),
        style = TmrTheme.typography.bodyS,
        color = TmrTheme.colors.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun UnderEighteenScreen(actions: SignInActions, modifier: Modifier = Modifier) {
    TmrScreen(
        modifier = modifier,
        sheet = false,
        bottomBar = {
            TmrBottomActionBar {
                TmrPrimaryButton(
                    label = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_action_back),
                    onClick = actions.onBackFromUnderEighteen,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = TmrTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            OnboardingStepBar(
                onBack = actions.onBackFromUnderEighteen,
                backContentDescription = stringResource(
                    R.string.feature_onboarding_impl_sign_in_under_18_close_description,
                ),
            )
            StateCard(
                icon = TmrIcons.Block,
                title = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_title),
                body = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_body),
            )
        }
    }
}
