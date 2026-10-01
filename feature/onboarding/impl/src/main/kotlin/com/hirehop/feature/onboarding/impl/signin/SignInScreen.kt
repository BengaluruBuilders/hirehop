package com.hirehop.feature.onboarding.impl.signin

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhConsentRow
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhStatusChip
import com.hirehop.core.designsystem.component.HhStatusKind
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.SignInFailureReason
import com.hirehop.feature.onboarding.impl.R

private val HH_TOUCH_TARGET: Dp = 48.dp

@Composable
internal fun SignInScreen(
    uiState: SignInUiState,
    actions: SignInActions,
    onSkipToJobDescription: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = "",
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_onboarding_impl_sign_in_navigation_back_content_description,
                ),
                onNavigationClick = actions.onBackFromUnderEighteen,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState.stage) {
                SignInStage.UNDER_18 -> UnderEighteenContent(actions = actions)
                SignInStage.SKIPPED -> SkippedContent(
                    actions = actions,
                    onSkipToJobDescription = onSkipToJobDescription,
                )

                else -> SignInMainContent(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun SignInMainContent(
    uiState: SignInUiState,
    actions: SignInActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_title),
            style = HhTheme.typography.headlineSmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_subtitle),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhOfflineBanner(
            message = stringResource(R.string.feature_onboarding_impl_sign_in_offline_message),
            supportingText = stringResource(R.string.feature_onboarding_impl_sign_in_offline_supporting),
            visible = uiState.isOffline,
        )
        AdultConfirmationRow(uiState = uiState, actions = actions)
        Spacer(Modifier.height(HhTheme.spacing.md))
        WhatLeavesThisPhone()
        OutcomeBlock(uiState = uiState, actions = actions)
        HhButton(
            onClick = actions.onContinue,
            enabled = uiState.canContinue,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_onboarding_impl_sign_in_action_continue))
        }
        SignInContinueReason(uiState = uiState)
        HhOutlinedButton(
            onClick = actions.onNotNow,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_onboarding_impl_sign_in_action_not_now))
        }
        HhOutlinedButton(
            onClick = actions.onUnderEighteen,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_action_under_18),
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AdultConfirmationRow(
    uiState: SignInUiState,
    actions: SignInActions,
) {
    val shape = RoundedCornerShape(HhTheme.shapes.md)
    val nudgeFrame = if (uiState.isAdultNudged) {
        Modifier
            .border(width = 1.5.dp, color = HhTheme.colors.onSurface, shape = shape)
            .padding(horizontal = HhTheme.spacing.md, vertical = HhTheme.spacing.sm)
    } else {
        Modifier
    }
    val talkBack = if (uiState.isAdultConfirmed) {
        stringResource(R.string.feature_onboarding_impl_sign_in_age_talkback_on)
    } else {
        stringResource(R.string.feature_onboarding_impl_sign_in_age_talkback_off)
    }
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        HhConsentRow(
            label = stringResource(R.string.feature_onboarding_impl_sign_in_age_label),
            supportingText = stringResource(R.string.feature_onboarding_impl_sign_in_age_supporting),
            checked = uiState.isAdultConfirmed,
            onCheckedChange = actions.onAdultConfirmationChange,
            modifier = nudgeFrame.semantics(mergeDescendants = true) {
                contentDescription = talkBack
            },
        )
        if (uiState.isAdultNudged) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_age_nudge),
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}

@Composable
private fun WhatLeavesThisPhone() {
    HhCard {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_what_leaves_label),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_what_leaves_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_what_leaves_meta),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun OutcomeBlock(
    uiState: SignInUiState,
    actions: SignInActions,
) {
    when {
        uiState.failure == SignInFailureReason.ProviderUnavailable -> HhErrorCallout(
            title = stringResource(R.string.feature_onboarding_impl_sign_in_failure_provider_title),
            supportingText = stringResource(R.string.feature_onboarding_impl_sign_in_failure_provider_body),
            actionLabel = stringResource(R.string.feature_onboarding_impl_sign_in_failure_action_retry),
            onAction = actions.onContinue,
        )

        uiState.failure == SignInFailureReason.NetworkUnavailable -> HhOfflineBanner(
            message = stringResource(R.string.feature_onboarding_impl_sign_in_failure_network_title),
            supportingText = stringResource(R.string.feature_onboarding_impl_sign_in_failure_network_body),
        )

        uiState.stage == SignInStage.SIGNED_IN -> HhCard {
            HhStatusChip(
                kind = HhStatusKind.Met,
                label = stringResource(R.string.feature_onboarding_impl_sign_in_success_status),
            )
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_success_title),
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
            if (uiState.displayName != null) {
                Text(
                    text = uiState.displayName.orEmpty(),
                    style = HhTheme.typography.bodyLarge,
                    color = HhTheme.colors.onSurface,
                )
            }
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_success_body),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }

        uiState.stage == SignInStage.CANCELLED -> HhCard {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_cancelled_title),
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_onboarding_impl_sign_in_cancelled_body),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SignInContinueReason(uiState: SignInUiState) {
    val reason = when {
        uiState.isBusy -> R.string.feature_onboarding_impl_sign_in_reason_in_progress
        uiState.needsAdultConfirmation -> R.string.feature_onboarding_impl_sign_in_reason_needs_tick
        else -> null
    }
    if (reason == null) return
    Text(
        text = stringResource(reason),
        style = HhTheme.typography.bodyMedium,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun UnderEighteenContent(actions: SignInActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d24),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Review)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_title),
            style = HhTheme.typography.headlineSmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        HhOutlinedButton(
            onClick = actions.onBackFromUnderEighteen,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_onboarding_impl_sign_in_under_18_action_back))
        }
    }
}

@Composable
private fun SkippedContent(
    actions: SignInActions,
    onSkipToJobDescription: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d24),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_skipped_title),
            style = HhTheme.typography.headlineSmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_sign_in_skipped_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        HhButton(
            onClick = onSkipToJobDescription,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_onboarding_impl_sign_in_skipped_action_back))
        }
        HhOutlinedButton(
            onClick = actions.onRevisit,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_onboarding_impl_sign_in_skipped_action_revisit))
        }
    }
}
