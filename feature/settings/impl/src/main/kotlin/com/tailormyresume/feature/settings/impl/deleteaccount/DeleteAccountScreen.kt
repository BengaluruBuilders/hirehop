package com.tailormyresume.feature.settings.impl.deleteaccount

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
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrConfirmDialog
import com.tailormyresume.core.designsystem.component.TmrContentSwitch
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrOfflineBanner
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrOutlinedButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.component.TmrStepProgress
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.account.AccountDeletionCounts
import com.tailormyresume.core.domain.account.AccountDeletionStep
import com.tailormyresume.feature.settings.impl.R
import com.tailormyresume.feature.settings.impl.common.SettingsAddressSlot
import com.tailormyresume.feature.settings.impl.common.SettingsErrorNotice
import com.tailormyresume.feature.settings.impl.common.SettingsTopBar
import com.tailormyresume.feature.settings.impl.common.stepStatusWords

@Composable
internal fun DeleteAccountScreen(
    uiState: DeleteAccountUiState,
    actions: DeleteAccountActions,
    modifier: Modifier = Modifier,
) {
    TmrScreen(
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
        TmrContentSwitch(targetState = uiState, contentKey = { it::class }) { state ->
            when (state) {
                DeleteAccountUiState.Loading -> Unit
                is DeleteAccountUiState.Ready -> ReadyContent(uiState = state, actions = actions, padding = padding)
                is DeleteAccountUiState.Deleting -> DeletingContent(uiState = state, padding = padding)
            }
        }
    }
    if (uiState is DeleteAccountUiState.Ready && uiState.isConfirmVisible) {
        DeleteConfirmDialog(counts = uiState.counts, actions = actions)
    }
}

@Composable
private fun DeleteConfirmDialog(counts: AccountDeletionCounts, actions: DeleteAccountActions) {
    TmrConfirmDialog(
        title = stringResource(R.string.feature_settings_impl_delete_account_dialog_title),
        message = stringResource(
            R.string.feature_settings_impl_delete_account_dialog_body,
            pluralStringResource(
                R.plurals.feature_settings_impl_delete_account_dialog_facts,
                counts.profileFacts,
                counts.profileFacts,
            ),
            pluralStringResource(
                R.plurals.feature_settings_impl_delete_account_dialog_applications,
                counts.applications,
                counts.applications,
            ),
            pluralStringResource(
                R.plurals.feature_settings_impl_delete_account_dialog_credits,
                counts.unusedCredits,
                counts.unusedCredits,
            ),
        ),
        confirmLabel = stringResource(R.string.feature_settings_impl_delete_account_dialog_confirm),
        cancelLabel = stringResource(R.string.feature_settings_impl_delete_account_dialog_cancel),
        onConfirm = actions.onDeleteConfirmed,
        onCancel = actions.onDeleteDismissed,
        destructive = true,
    )
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
            .padding(horizontal = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            TmrHeadline(text = stringResource(R.string.feature_settings_impl_delete_account_title))
            uiState.accountEmail?.let { email ->
                Text(
                    text = email,
                    style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.Bold),
                    color = TmrTheme.colors.onSurface,
                )
            }
        }
        TmrOfflineBanner(
            message = stringResource(R.string.feature_settings_impl_delete_account_offline_message),
            visible = uiState.isOffline,
        )
        uiState.failure?.let { failure ->
            SettingsErrorNotice(
                text = stringResource(
                    when (failure) {
                        DeleteAccountFailure.DATA_INTACT -> R.string.feature_settings_impl_delete_account_error_intact
                        DeleteAccountFailure.PARTLY_DELETED -> R.string.feature_settings_impl_delete_account_error_partial
                        DeleteAccountFailure.LOCAL_WIPE_PENDING ->
                            R.string.feature_settings_impl_delete_account_error_local_wipe_pending
                    },
                ),
            )
        }
        TmrCard(contentPadding = PaddingValues(TmrTheme.spacing.lg)) {
            TmrSectionLabel(text = stringResource(R.string.feature_settings_impl_delete_account_list_title))
            CountsList(counts = uiState.counts)
        }
        TmrTextButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_download_first),
            onClick = actions.onDownloadData,
            leadingIcon = TmrIcons.Download,
        )
        Text(
            text = stringResource(R.string.feature_settings_impl_delete_account_web_lead),
            style = TmrTheme.typography.bodyS,
            color = TmrTheme.colors.onSurfaceVariant,
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
        style = TmrTheme.typography.titleS,
        color = TmrTheme.colors.onSurface,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun ReadyBar(uiState: DeleteAccountUiState.Ready, actions: DeleteAccountActions) {
    TmrBottomActionBar(stacked = true, primaryLast = false) {
        TmrOutlineButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_keep),
            onClick = actions.onKeepAccount,
            modifier = Modifier.fillMaxWidth(),
        )
        TmrOutlinedButton(
            onClick = actions.onDeleteAccount,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isOffline,
        ) {
            Icon(
                imageVector = TmrIcons.Delete,
                contentDescription = null,
                tint = TmrTheme.colors.error,
                modifier = Modifier.size(BUTTON_ICON_SIZE),
            )
            Text(
                text = stringResource(R.string.feature_settings_impl_delete_account_confirm),
                color = TmrTheme.colors.error,
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
            .padding(horizontal = TmrTheme.spacing.gutter),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        TmrHeadline(
            text = stringResource(R.string.feature_settings_impl_delete_account_deleting_title),
            style = TmrTheme.typography.headlineL,
        )
        TmrStepProgress(
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
