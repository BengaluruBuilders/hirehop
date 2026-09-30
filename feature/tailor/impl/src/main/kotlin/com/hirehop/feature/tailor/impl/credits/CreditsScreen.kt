package com.hirehop.feature.tailor.impl.credits

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhDividerStyle
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhHeroNumeral
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSectionCard
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.tailor.impl.R

@Composable
internal fun CreditsScreen(
    uiState: CreditsUiState,
    actions: CreditsActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_tailor_impl_credits_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_tailor_impl_credits_navigation_back_description,
                ),
                onNavigationClick = actions.onNavigateBack,
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState.stage) {
                CreditsStage.LOADING,
                CreditsStage.IDLE,
                -> CreditsLoading()
                CreditsStage.ERROR -> CreditsError(actions = actions)
                else -> CreditsBody(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun CreditsLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        HhLoadingWheel(contentDesc = stringResource(R.string.feature_tailor_impl_credits_loading))
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_loading),
            style = HhTheme.typography.titleLarge,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun CreditsError(actions: CreditsActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Error)
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_error_heading),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_error_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhDivider(style = HhDividerStyle.Dashed)
        HhOutlinedButton(
            onClick = actions.onRestore,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_credits_error_retry),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun CreditsBody(
    uiState: CreditsUiState,
    actions: CreditsActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_tailor_impl_credits_offline_message),
            supportingText = stringResource(R.string.feature_tailor_impl_credits_offline_supporting),
            visible = uiState.isOffline,
        )
        CreditsStageNote(uiState = uiState, actions = actions)
        CreditsBalance(uiState = uiState)
        CreditsPurchases(uiState = uiState)
        CreditsRefundsNote(actions = actions)
    }
}

@Composable
private fun CreditsStageNote(
    uiState: CreditsUiState,
    actions: CreditsActions,
) {
    when (uiState.stage) {
        CreditsStage.RESTORING -> HhCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_restoring),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            HhLoadingWheel(contentDesc = stringResource(R.string.feature_tailor_impl_credits_restoring))
        }
        CreditsStage.RESTORED -> HhCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_restored_heading),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_restored_body),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        CreditsStage.ERROR -> HhErrorCallout(
            title = stringResource(R.string.feature_tailor_impl_credits_error_heading),
            supportingText = stringResource(R.string.feature_tailor_impl_credits_error_body),
            actionLabel = stringResource(R.string.feature_tailor_impl_credits_error_retry),
            onAction = actions.onDismiss,
        )
        CreditsStage.ZERO -> HhCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_zero_heading),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_zero_body),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        else -> Unit
    }
}

@Composable
private fun CreditsBalance(uiState: CreditsUiState) {
    HhSectionCard {
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_separate_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhDivider(style = HhDividerStyle.Dashed)
        CreditsBalanceRow(
            eyebrow = stringResource(R.string.feature_tailor_impl_credits_free_eyebrow),
            credits = uiState.freeCredits,
            caption = stringResource(R.string.feature_tailor_impl_credits_free_caption),
            description = pluralStringResource(
                R.plurals.feature_tailor_impl_credits_free_value_description,
                uiState.freeCredits,
                uiState.freeCredits,
            ),
            note = stringResource(R.string.feature_tailor_impl_credits_free_note),
        )
        HhDivider(style = HhDividerStyle.Dashed)
        CreditsBalanceRow(
            eyebrow = stringResource(R.string.feature_tailor_impl_credits_purchased_eyebrow),
            credits = uiState.purchasedCredits,
            caption = stringResource(R.string.feature_tailor_impl_credits_purchased_caption),
            description = pluralStringResource(
                R.plurals.feature_tailor_impl_credits_purchased_value_description,
                uiState.purchasedCredits,
                uiState.purchasedCredits,
            ),
            note = expiryNoteFor(uiState = uiState),
        )
    }
}

@Composable
private fun CreditsBalanceRow(
    eyebrow: String,
    credits: Int,
    caption: String,
    description: String,
    note: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        Text(
            text = eyebrow,
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhHeroNumeral(
            value = credits.toString(),
            caption = caption,
            contentDescription = description,
        )
        Text(
            text = note,
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun expiryNoteFor(uiState: CreditsUiState): String = when {
    !uiState.hasPurchasedCredits -> stringResource(R.string.feature_tailor_impl_credits_purchased_expiry_unknown)
    uiState.purchasedCreditsNeverExpire -> stringResource(R.string.feature_tailor_impl_credits_purchased_never_expire)
    uiState.purchasedCreditsMayExpire -> stringResource(R.string.feature_tailor_impl_credits_purchased_may_expire)
    else -> stringResource(R.string.feature_tailor_impl_credits_purchased_expiry_unknown)
}

@Composable
private fun CreditsPurchases(uiState: CreditsUiState) {
    HhSectionCard {
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_purchases_eyebrow),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        when {
            uiState.hasPendingPurchase -> {
                uiState.purchases.forEach { entry ->
                    CreditsPendingRow(entry = entry)
                }
                Text(
                    text = stringResource(R.string.feature_tailor_impl_credits_pending_body),
                    style = HhTheme.typography.bodyMedium,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
            uiState.hasPurchasedCredits -> Text(
                text = stringResource(
                    R.string.feature_tailor_impl_credits_purchased_recorded_body,
                    pluralStringResource(
                        R.plurals.feature_tailor_impl_credits_purchased_count,
                        uiState.purchasedCredits,
                        uiState.purchasedCredits,
                    ),
                ),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurface,
            )
            uiState.hasFreeCredits -> Text(
                text = stringResource(R.string.feature_tailor_impl_credits_no_purchases_free_body),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            else -> Text(
                text = stringResource(R.string.feature_tailor_impl_credits_no_purchases_body),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CreditsPendingRow(entry: CreditsPurchaseEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        Text(
            text = entry.packName,
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = pluralStringResource(
                R.plurals.feature_tailor_impl_credits_pending_pack_line,
                entry.credits,
                entry.formattedPrice,
                entry.credits,
            ),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_pending_status),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun CreditsRefundsNote(actions: CreditsActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhDivider(style = HhDividerStyle.Dashed)
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_refunds_eyebrow),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_refunds_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_no_guarantee),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            CreditsTextLink(
                label = stringResource(R.string.feature_tailor_impl_credits_restore_action),
                onClick = actions.onRestore,
            )
        }
    }
}

@Composable
private fun CreditsTextLink(
    label: String,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        style = HhTheme.typography.labelLarge,
        color = HhTheme.colors.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.d48)
            .clickable(onClick = onClick)
            .padding(vertical = HhTheme.spacing.sm),
    )
}
