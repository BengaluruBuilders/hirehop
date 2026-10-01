package com.hirehop.feature.onboarding.impl.consent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhConsentRow
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R

private val HH_TOUCH_TARGET: Dp = 48.dp

@Composable
internal fun ConsentScreen(
    uiState: ConsentUiState,
    actions: ConsentActions,
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
                    R.string.feature_onboarding_impl_consent_navigation_back_content_description,
                ),
                onNavigationClick = actions.onNotNow,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (uiState.isDeclined) {
                ConsentDeclinedContent(actions = actions, onSkipToJobDescription = onSkipToJobDescription)
            } else {
                ConsentLedgerContent(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun ConsentLedgerContent(
    uiState: ConsentUiState,
    actions: ConsentActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_title),
            style = HhTheme.typography.headlineSmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_subtitle),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhOfflineBanner(
            message = stringResource(R.string.feature_onboarding_impl_consent_offline_message),
            supportingText = stringResource(R.string.feature_onboarding_impl_consent_offline_supporting),
            visible = uiState.isOffline,
        )
        LedgerLabel(text = stringResource(R.string.feature_onboarding_impl_consent_purposes_label))
        ConsentPurpose.entries.forEach { purpose ->
            ConsentPurposeRow(
                purpose = purpose,
                isAcknowledged = uiState.isAcknowledged(purpose),
                actions = actions,
            )
        }
        HhDivider()
        LedgerLabel(text = stringResource(R.string.feature_onboarding_impl_consent_commitments_label))
        CommitmentRow(
            marker = stringResource(R.string.feature_onboarding_impl_consent_commitment_never_asks_marker),
            text = stringResource(R.string.feature_onboarding_impl_consent_commitment_never_asks),
        )
        CommitmentRow(
            marker = stringResource(R.string.feature_onboarding_impl_consent_commitment_edit_or_remove_marker),
            text = stringResource(R.string.feature_onboarding_impl_consent_commitment_edit_or_remove),
        )
        HhDivider()
        FooterRow(
            label = stringResource(R.string.feature_onboarding_impl_consent_policy_label),
            value = stringResource(R.string.feature_onboarding_impl_consent_policy_value),
        )
        FooterRow(
            label = stringResource(R.string.feature_onboarding_impl_consent_grievance_label),
            value = stringResource(R.string.feature_onboarding_impl_consent_grievance_value),
        )
        Spacer(Modifier.height(HhTheme.spacing.sm))
        AgreementReason(uiState = uiState)
        HhButton(
            onClick = actions.onAgree,
            enabled = uiState.canAgree,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_onboarding_impl_consent_action_agree))
        }
        HhOutlinedButton(
            onClick = actions.onNotNow,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(
                text = stringResource(R.string.feature_onboarding_impl_consent_action_not_now),
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConsentPurposeRow(
    purpose: ConsentPurpose,
    isAcknowledged: Boolean,
    actions: ConsentActions,
) {
    val label = stringResource(purpose.labelResource())
    val supporting = stringResource(purpose.supportingResource())
    val marker = stringResource(purpose.markerResource())
    val state = stringResource(
        if (isAcknowledged) {
            R.string.feature_onboarding_impl_consent_purpose_talkback_on
        } else {
            R.string.feature_onboarding_impl_consent_purpose_talkback_off
        },
    )
    val talkBack = "$label. $state"
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Text(
            text = marker,
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhConsentRow(
            label = label,
            supportingText = supporting,
            checked = isAcknowledged,
            onCheckedChange = { actions.onPurposeToggle(purpose) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET)
                .semantics(mergeDescendants = true) { contentDescription = talkBack },
        )
    }
}

@Composable
private fun CommitmentRow(marker: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = marker,
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = text,
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun FooterRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        Text(
            text = label,
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = value,
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun LedgerLabel(text: String) {
    Text(
        text = text,
        style = HhTheme.typography.monoSmall,
        color = HhTheme.colors.onSurfaceVariant,
    )
}

@Composable
private fun AgreementReason(uiState: ConsentUiState) {
    val reason = when {
        uiState.isSaving -> R.string.feature_onboarding_impl_consent_reason_saving
        uiState.isEveryPurposeAcknowledged -> R.string.feature_onboarding_impl_consent_reason_complete
        else -> R.string.feature_onboarding_impl_consent_reason_incomplete
    }
    Text(
        text = stringResource(reason),
        style = HhTheme.typography.titleMedium,
        color = HhTheme.colors.onSurface,
    )
}

@Composable
private fun ConsentDeclinedContent(
    actions: ConsentActions,
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
            text = stringResource(R.string.feature_onboarding_impl_consent_declined_title),
            style = HhTheme.typography.headlineSmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_onboarding_impl_consent_declined_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Spacer(Modifier.weight(1f))
        HhButton(
            onClick = actions.onReadAgain,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_onboarding_impl_consent_declined_action_read_again))
        }
        HhOutlinedButton(
            onClick = onSkipToJobDescription,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HH_TOUCH_TARGET),
        ) {
            Text(text = stringResource(R.string.feature_onboarding_impl_consent_declined_action_back))
        }
    }
}

private fun ConsentPurpose.labelResource(): Int = when (this) {
    ConsentPurpose.READ_AND_BUILD -> R.string.feature_onboarding_impl_consent_purpose_read_label
    ConsentPurpose.ANALYSE_ON_DEVICE -> R.string.feature_onboarding_impl_consent_purpose_analyse_label
    ConsentPurpose.KEEP_CONFIRMED_FACTS -> R.string.feature_onboarding_impl_consent_purpose_facts_label
}

private fun ConsentPurpose.supportingResource(): Int = when (this) {
    ConsentPurpose.READ_AND_BUILD -> R.string.feature_onboarding_impl_consent_purpose_read_supporting
    ConsentPurpose.ANALYSE_ON_DEVICE -> R.string.feature_onboarding_impl_consent_purpose_analyse_supporting
    ConsentPurpose.KEEP_CONFIRMED_FACTS -> R.string.feature_onboarding_impl_consent_purpose_facts_supporting
}

private fun ConsentPurpose.markerResource(): Int = when (this) {
    ConsentPurpose.READ_AND_BUILD -> R.string.feature_onboarding_impl_consent_purpose_read_marker
    ConsentPurpose.ANALYSE_ON_DEVICE -> R.string.feature_onboarding_impl_consent_purpose_analyse_marker
    ConsentPurpose.KEEP_CONFIRMED_FACTS -> R.string.feature_onboarding_impl_consent_purpose_facts_marker
}
