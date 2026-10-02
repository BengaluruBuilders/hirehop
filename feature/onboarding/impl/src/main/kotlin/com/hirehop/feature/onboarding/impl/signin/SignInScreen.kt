package com.hirehop.feature.onboarding.impl.signin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.text.AnnotatedString
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCheckbox
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R
import com.hirehop.feature.onboarding.impl.common.DisclosureCard
import com.hirehop.feature.onboarding.impl.common.MessageCard
import com.hirehop.feature.onboarding.impl.common.NoticeTone
import com.hirehop.feature.onboarding.impl.common.OnboardingNotice
import com.hirehop.feature.onboarding.impl.common.ReasonText

@Composable
internal fun SignInScreen(
    uiState: SignInUiState,
    actions: SignInActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_onboarding_impl_sign_in_title),
                subtitle = stringResource(R.string.feature_onboarding_impl_sign_in_subtitle),
                onBack = actions.onBack,
                backContentDescription = stringResource(
                    R.string.feature_onboarding_impl_sign_in_navigation_back_content_description,
                ),
            )
        },
        bottomBar = {
            if (uiState.stage == SignInStage.UNDER_18) {
                UnderEighteenBar(actions = actions)
            } else {
                SignInBottomBar(uiState = uiState, actions = actions)
            }
        },
        bottomBarNotice = if (uiState.stage == SignInStage.UNDER_18) null else ({ SignInBarNotice(uiState = uiState) }),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            if (uiState.stage == SignInStage.UNDER_18) {
                MessageCard(
                    title = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_title),
                    body = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_body),
                )
            } else {
                SignInNotices(uiState = uiState)
                SignInFormCard(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun SignInNotices(uiState: SignInUiState) {
    HhOfflineBanner(
        message = stringResource(R.string.feature_onboarding_impl_sign_in_offline_message),
        visible = uiState.isOffline,
    )
    when (uiState.stage) {
        SignInStage.FAILED -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_failure_message),
            icon = HhIcons.Error,
            tone = NoticeTone.Error,
        )

        SignInStage.CANCELLED -> OnboardingNotice(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_cancelled_message),
            icon = HhIcons.Block,
        )

        else -> Unit
    }
}

@Composable
private fun SignInFormCard(
    uiState: SignInUiState,
    actions: SignInActions,
) {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.gutter)) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_intro),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
            AdultRow(uiState = uiState, onChange = actions.onAdultConfirmationChange)
            HhTextField(
                value = uiState.referralCode,
                onValueChange = actions.onReferralCodeChange,
                label = stringResource(R.string.feature_onboarding_impl_sign_in_code_label),
                placeholder = stringResource(R.string.feature_onboarding_impl_sign_in_code_placeholder),
            )
        }
    }
}

@Composable
private fun AdultRow(
    uiState: SignInUiState,
    onChange: (Boolean) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = HhTheme.spacing.touch)
                .toggleable(value = uiState.isAdultConfirmed, role = Role.Checkbox, onValueChange = onChange),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HhCheckbox(
                checked = uiState.isAdultConfirmed,
                onCheckedChange = onChange,
                modifier = Modifier.clearAndSetSemantics {},
            )
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_age_label),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurface,
            )
        }
        if (uiState.isAdultNudged && !uiState.isAdultConfirmed) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_age_nudge),
                modifier = Modifier.padding(start = HhTheme.spacing.touch + HhTheme.spacing.sm),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun SignInBarNotice(uiState: SignInUiState) {
    val reason = when {
        uiState.isBusy -> stringResource(R.string.feature_onboarding_impl_sign_in_reason_in_progress)
        uiState.isOffline -> stringResource(R.string.feature_onboarding_impl_sign_in_reason_offline)
        !uiState.isAdultConfirmed -> stringResource(R.string.feature_onboarding_impl_sign_in_reason_needs_tick)
        else -> null
    }
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        DisclosureCard(
            text = AnnotatedString(stringResource(R.string.feature_onboarding_impl_sign_in_disclosure)),
            icon = HhIcons.Lock,
        )
        if (reason != null) {
            ReasonText(text = reason)
        }
    }
}

@Composable
private fun SignInBottomBar(
    uiState: SignInUiState,
    actions: SignInActions,
) {
    HhBottomActionBar {
        HhOutlineButton(
            label = stringResource(R.string.feature_onboarding_impl_sign_in_action_under_18),
            onClick = actions.onUnderEighteen,
            modifier = Modifier.weight(1f),
        )
        HhPrimaryButton(
            label = stringResource(R.string.feature_onboarding_impl_sign_in_action_continue),
            onClick = actions.onContinue,
            enabled = uiState.canContinue,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun UnderEighteenBar(actions: SignInActions) {
    HhBottomActionBar {
        HhOutlineButton(
            label = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_action_back),
            onClick = actions.onBackFromUnderEighteen,
            modifier = Modifier.weight(1f),
        )
    }
}
