package com.hirehop.feature.settings.impl.deleteaccount

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhContentSwitch
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.account.AccountDeletionCounts
import com.hirehop.core.domain.account.AccountDeletionStep
import com.hirehop.feature.settings.impl.R
import com.hirehop.feature.settings.impl.common.SettingsAddressSlot
import com.hirehop.feature.settings.impl.common.SettingsErrorNotice
import com.hirehop.feature.settings.impl.common.SettingsTopBar
import com.hirehop.feature.settings.impl.common.stepStatusWords

@Composable
internal fun DeleteAccountScreen(
    uiState: DeleteAccountUiState,
    actions: DeleteAccountActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        lightTop = true,
        header = {
            SettingsTopBar(
                onBack = if (uiState is DeleteAccountUiState.Deleting) null else actions.onBack,
                backContentDescription = stringResource(R.string.feature_settings_impl_delete_account_back),
            )
        },
        bottomBar = when (uiState) {
            is DeleteAccountUiState.Ready -> {
                { ReadyBar(uiState = uiState, actions = actions) }
            }

            else -> null
        },
    ) { padding ->
        HhContentSwitch(targetState = uiState, contentKey = { it::class }) { state ->
            when (state) {
                DeleteAccountUiState.Loading -> Unit
                is DeleteAccountUiState.Ready -> ReadyContent(uiState = state, actions = actions, padding = padding)
                is DeleteAccountUiState.Deleting -> DeletingContent(uiState = state, padding = padding)
            }
        }
    }
}

@Composable
private fun ReadyContent(
    uiState: DeleteAccountUiState.Ready,
    actions: DeleteAccountActions,
    padding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhHeadline(text = stringResource(R.string.feature_settings_impl_delete_account_title))
            uiState.accountEmail?.let { email ->
                Text(
                    text = email,
                    style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.Bold),
                    color = HhTheme.colors.onSurface,
                )
            }
        }
        HhOfflineBanner(
            message = stringResource(R.string.feature_settings_impl_delete_account_offline_message),
            visible = uiState.isOffline,
        )
        uiState.failure?.let { failure ->
            SettingsErrorNotice(
                text = stringResource(
                    when (failure) {
                        DeleteAccountFailure.DATA_INTACT -> R.string.feature_settings_impl_delete_account_error_intact
                        DeleteAccountFailure.PARTLY_DELETED -> R.string.feature_settings_impl_delete_account_error_partial
                    },
                ),
            )
        }
        HhCard(contentPadding = PaddingValues(HhTheme.spacing.lg)) {
            HhSectionLabel(text = stringResource(R.string.feature_settings_impl_delete_account_list_title))
            CountsList(counts = uiState.counts)
        }
        HhTextButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_download_first),
            onClick = actions.onDownloadData,
            leadingIcon = HhIcons.Download,
        )
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_web_lead),
            style = HhTheme.typography.bodyS,
            color = HhTheme.colors.onSurfaceVariant,
        )
        SettingsAddressSlot(
            address = stringResource(R.string.feature_settings_impl_delete_account_web_address),
            pendingLabel = stringResource(R.string.feature_settings_impl_delete_account_web_pending),
        )
    }
}

@Composable
private fun CountsList(counts: AccountDeletionCounts) {
    CountLine(
        pluralStringResource(
            R.plurals.feature_settings_impl_delete_account_row_facts,
            counts.profileFacts,
            counts.profileFacts,
        ),
    )
    CountLine(
        pluralStringResource(
            R.plurals.feature_settings_impl_delete_account_row_applications,
            counts.applications,
            counts.applications,
        ),
    )
    CountLine(
        pluralStringResource(
            R.plurals.feature_settings_impl_delete_account_row_credits,
            counts.unusedCredits,
            counts.unusedCredits,
        ),
    )
}

@Composable
private fun CountLine(text: String) {
    Text(
        text = text,
        style = HhTheme.typography.titleS,
        color = HhTheme.colors.onSurface,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ReadyBar(uiState: DeleteAccountUiState.Ready, actions: DeleteAccountActions) {
    HhBottomActionBar(stacked = true, primaryLast = false) {
        HhOutlineButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_keep),
            onClick = actions.onKeepAccount,
            modifier = Modifier.fillMaxWidth(),
        )
        HhOutlinedButton(
            onClick = actions.onDeleteAccount,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isOffline,
        ) {
            Icon(
                imageVector = HhIcons.Delete,
                contentDescription = null,
                tint = HhTheme.colors.error,
                modifier = Modifier.size(BUTTON_ICON_SIZE),
            )
            Text(
                text = stringResource(R.string.feature_settings_impl_delete_account_confirm),
                color = HhTheme.colors.error,
            )
        }
    }
}

@Composable
private fun DeletingContent(uiState: DeleteAccountUiState.Deleting, padding: PaddingValues) {
    val stepNames = AccountDeletionStep.entries.map { step -> stepLabel(step = step, counts = uiState.counts) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhHeadline(
            text = stringResource(R.string.feature_settings_impl_delete_account_deleting_title),
            style = HhTheme.typography.headlineL,
        )
        HhStepProgress(
            stepNames = stepNames,
            currentStepIndex = uiState.step.ordinal,
            ordinalLabel = "",
            stepStatuses = stepStatusWords(stepNames.size, uiState.step.ordinal),
            footnote = stringResource(R.string.feature_settings_impl_delete_account_deleting_note),
        )
    }
}

@Composable
private fun stepLabel(step: AccountDeletionStep, counts: AccountDeletionCounts): String = when (step) {
    AccountDeletionStep.DELETING_APPLICATIONS -> pluralStringResource(
        R.plurals.feature_settings_impl_delete_account_step_applications,
        counts.applications,
        counts.applications,
    )

    AccountDeletionStep.DELETING_PROFILE_FACTS -> pluralStringResource(
        R.plurals.feature_settings_impl_delete_account_step_facts,
        counts.profileFacts,
        counts.profileFacts,
    )

    AccountDeletionStep.CLOSING_ACCOUNT -> stringResource(R.string.feature_settings_impl_delete_account_step_closing)
}

private val BUTTON_ICON_SIZE = 20.dp
