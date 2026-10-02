package com.hirehop.feature.tailor.impl.credits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhApplicationStatusKind
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.tailor.impl.R

@Composable
internal fun CreditsScreen(
    uiState: CreditsUiState,
    actions: CreditsActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
                title = stringResource(R.string.feature_tailor_impl_credits_title),
                subtitle = stringResource(R.string.feature_tailor_impl_credits_subtitle),
                onBack = actions.onNavigateBack,
                backContentDescription = stringResource(R.string.feature_tailor_impl_credits_navigation_back_description),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            when (uiState.stage) {
                CreditsStage.LOADING -> CreditsLoading()
                CreditsStage.ERROR -> HhErrorCallout(
                    title = stringResource(R.string.feature_tailor_impl_credits_error_title),
                    supportingText = stringResource(R.string.feature_tailor_impl_credits_error_body),
                    actionLabel = stringResource(R.string.feature_tailor_impl_credits_error_retry),
                    onAction = actions.onRetry,
                )

                CreditsStage.READY -> CreditsReady(uiState = uiState, actions = actions)
            }
        }
    }
}

@Composable
private fun CreditsLoading() {
    val loading = stringResource(R.string.feature_tailor_impl_credits_loading)
    Row(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhLoadingWheel(contentDesc = loading)
        Text(text = loading, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
    }
}

@Composable
private fun CreditsReady(
    uiState: CreditsUiState,
    actions: CreditsActions,
) {
    HhOfflineBanner(
        message = stringResource(R.string.feature_tailor_impl_credits_offline_banner),
        visible = uiState.isOffline,
    )
    CreditsHero(uiState = uiState, onGetPack = actions.onGetPack)
    CreditsSectionTitle(text = stringResource(R.string.feature_tailor_impl_credits_purchases_title))
    CreditsPurchases(uiState = uiState)
    CreditsSectionTitle(text = stringResource(R.string.feature_tailor_impl_credits_help_title))
    CreditsHelp(actions = actions)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreditsHero(
    uiState: CreditsUiState,
    onGetPack: () -> Unit,
) {
    val left = uiState.totalCredits
    val description = pluralStringResource(R.plurals.feature_tailor_impl_credits_left_description, left, left)
    HhHeroCard(contentPadding = PaddingValues(HhTheme.spacing.gutter)) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs)) {
            FlowRow(
                modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
                itemVerticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = pluralStringResource(R.plurals.feature_tailor_impl_credits_left, left, left),
                    style = HhTheme.typography.numeralHero,
                    color = HhTheme.colors.onSurface,
                )
                Text(
                    text = stringResource(
                        if (uiState.creditsNeverExpire) {
                            R.string.feature_tailor_impl_credits_never_expire
                        } else {
                            R.string.feature_tailor_impl_credits_may_expire
                        },
                    ),
                    style = HhTheme.typography.titleM,
                    color = HhTheme.colors.onSurface,
                )
            }
            if (uiState.showsFreeNote) {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_credits_free_note),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.onSurfaceVariant,
                )
            }
            HhSecondaryButton(
                label = stringResource(R.string.feature_tailor_impl_credits_get_pack),
                onClick = onGetPack,
                enabled = uiState.canBuy,
                trailingIcon = HhIcons.ArrowForward,
            )
        }
    }
}

@Composable
private fun CreditsSectionTitle(text: String) {
    Text(
        text = text,
        style = HhTheme.typography.titleS,
        color = HhTheme.colors.onSurfaceVariant,
        modifier = Modifier.padding(top = HhTheme.spacing.xs),
    )
}

@Composable
private fun CreditsPurchases(uiState: CreditsUiState) {
    if (uiState.purchases.isEmpty()) {
        HhCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_no_purchases),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        return
    }
    uiState.purchases.forEach { entry -> CreditsPurchaseCard(entry = entry) }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreditsPurchaseCard(entry: CreditsPurchaseEntry) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.feature_tailor_impl_credits_purchase_title,
                        entry.credits,
                        entry.credits,
                        entry.formattedPrice,
                    ),
                    style = HhTheme.typography.titleS,
                    color = HhTheme.colors.onSurface,
                )
                if (entry.isPending) {
                    HhApplicationStatusChip(
                        kind = HhApplicationStatusKind.Saved,
                        label = stringResource(R.string.feature_tailor_impl_credits_pending),
                    )
                }
            }
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_purchase_meta, entry.formattedDate),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
            Text(text = entry.orderId, style = HhTheme.typography.factId, color = HhTheme.colors.onSurface)
            if (entry.isPending) {
                Text(
                    text = pluralStringResource(
                        R.plurals.feature_tailor_impl_credits_pending_note,
                        entry.credits,
                        entry.credits,
                    ),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreditsHelp(actions: CreditsActions) {
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_refund_summary),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                HhOutlineButton(
                    label = stringResource(R.string.feature_tailor_impl_credits_ask_refund),
                    onClick = actions.onAskRefund,
                )
                HhOutlineButton(
                    label = stringResource(R.string.feature_tailor_impl_credits_contact_help),
                    onClick = actions.onContactHelp,
                )
            }
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_help_summary),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
    }
}
