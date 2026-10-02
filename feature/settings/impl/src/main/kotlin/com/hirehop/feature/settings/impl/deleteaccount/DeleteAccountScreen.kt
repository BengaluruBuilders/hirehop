package com.hirehop.feature.settings.impl.deleteaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhContentSwitch
import com.hirehop.core.designsystem.component.HhDestructiveButton
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhStepProgress
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.illustration.HhCharacterIllustration
import com.hirehop.core.designsystem.illustration.HhIllustration
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.account.AccountDeletionCounts
import com.hirehop.core.domain.account.AccountDeletionStep
import com.hirehop.feature.settings.impl.R
import com.hirehop.feature.settings.impl.common.SettingsAddressSlot
import com.hirehop.feature.settings.impl.common.SettingsNeutralCallout

@Composable
internal fun DeleteAccountScreen(
    uiState: DeleteAccountUiState,
    actions: DeleteAccountActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = { DeleteAccountHeader(uiState = uiState, onBack = actions.onBack) },
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
private fun DeleteAccountHeader(uiState: DeleteAccountUiState, onBack: () -> Unit) {
    when (uiState) {
        is DeleteAccountUiState.Deleting -> HhInnerHeader(
            title = stringResource(R.string.feature_settings_impl_delete_account_title),
            subtitle = uiState.accountEmail,
        )

        is DeleteAccountUiState.Ready -> HhInnerHeader(
            title = stringResource(R.string.feature_settings_impl_delete_account_title),
            subtitle = uiState.accountEmail,
            onBack = onBack,
            backContentDescription = stringResource(R.string.feature_settings_impl_delete_account_back),
        )

        DeleteAccountUiState.Loading -> HhInnerHeader(
            title = stringResource(R.string.feature_settings_impl_delete_account_title),
            onBack = onBack,
            backContentDescription = stringResource(R.string.feature_settings_impl_delete_account_back),
        )
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
        if (uiState.isOffline) {
            OfflineCard()
        }
        uiState.failure?.let { failure ->
            SettingsNeutralCallout(
                text = stringResource(
                    when (failure) {
                        DeleteAccountFailure.DATA_INTACT -> R.string.feature_settings_impl_delete_account_error_intact
                        DeleteAccountFailure.PARTLY_DELETED -> R.string.feature_settings_impl_delete_account_error_partial
                    },
                ),
            )
        }
        HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.lg)) {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
                Text(
                    text = stringResource(R.string.feature_settings_impl_delete_account_list_title),
                    style = HhTheme.typography.titleL,
                    color = HhTheme.colors.onSurface,
                )
                CountsList(counts = uiState.counts)
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
    }
}

@Composable
private fun OfflineCard() {
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.cardPadding)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.cardPadding),
        ) {
            IllustrationCircle(illustration = HhIllustration.Offline, size = OFFLINE_CIRCLE_SIZE)
            Text(
                text = stringResource(R.string.feature_settings_impl_delete_account_offline_message),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
internal fun IllustrationCircle(illustration: HhIllustration, size: Dp, description: String? = null) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.primaryContainer),
        contentAlignment = Alignment.BottomCenter,
    ) {
        HhCharacterIllustration(
            illustration = illustration,
            modifier = Modifier.size(size),
            contentDescription = description,
        )
    }
}

@Composable
private fun CountsList(counts: AccountDeletionCounts) {
    CountLine(
        count = counts.profileFacts,
        label = pluralStringResource(
            R.plurals.feature_settings_impl_delete_account_row_facts,
            counts.profileFacts,
        ),
    )
    CountLine(
        count = counts.applications,
        label = pluralStringResource(
            R.plurals.feature_settings_impl_delete_account_row_applications,
            counts.applications,
        ),
    )
    CountLine(
        count = counts.unusedCredits,
        label = pluralStringResource(
            R.plurals.feature_settings_impl_delete_account_row_credits,
            counts.unusedCredits,
        ),
    )
}

@Composable
private fun CountLine(count: Int, label: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = HhTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            Text(
                text = count.toString(),
                style = HhTheme.typography.headlineM,
                color = HhTheme.colors.onSurface,
                modifier = Modifier
                    .widthIn(min = COUNT_MIN_WIDTH)
                    .alignByBaseline(),
            )
            Text(
                text = label,
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .alignByBaseline(),
            )
        }
        HhDivider()
    }
}

@Composable
private fun ReadyBar(uiState: DeleteAccountUiState.Ready, actions: DeleteAccountActions) {
    HhBottomActionBar {
        HhDestructiveButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_confirm),
            onClick = actions.onDeleteAccount,
            modifier = Modifier.weight(1f),
            enabled = !uiState.isOffline,
        )
        HhOutlineButton(
            label = stringResource(R.string.feature_settings_impl_delete_account_keep),
            onClick = actions.onKeepAccount,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DeletingContent(uiState: DeleteAccountUiState.Deleting, padding: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = HhTheme.spacing.gutter),
    ) {
        HhStepProgress(
            stepNames = AccountDeletionStep.entries.map { step -> stepLabel(step = step, counts = uiState.counts) },
            currentStepIndex = uiState.step.ordinal,
            ordinalLabel = stringResource(R.string.feature_settings_impl_delete_account_deleting_title),
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

    AccountDeletionStep.CLOSING_ACCOUNT -> pluralStringResource(
        R.plurals.feature_settings_impl_delete_account_step_closing,
        counts.unusedCredits,
        counts.unusedCredits,
    )
}

private val COUNT_MIN_WIDTH = 32.dp
private val OFFLINE_CIRCLE_SIZE = 96.dp
