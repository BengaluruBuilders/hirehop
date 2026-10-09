package com.tailormyresume.feature.tailor.impl.credits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrErrorCallout
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.tailor.impl.R
import com.tailormyresume.feature.tailor.impl.StatusPill
import com.tailormyresume.feature.tailor.impl.exportpreview.IconTile
import com.tailormyresume.feature.tailor.impl.exportpreview.NoticeBanner
import com.tailormyresume.feature.tailor.impl.exportpreview.NoticeTone

@Composable
internal fun CreditsScreen(
    uiState: CreditsUiState,
    actions: CreditsActions,
    modifier: Modifier = Modifier,
) {
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = stringResource(R.string.feature_tailor_impl_credits_title),
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
                .padding(horizontal = TmrTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            when (uiState.stage) {
                CreditsStage.LOADING -> CreditsLoading()
                CreditsStage.ERROR -> TmrErrorCallout(
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
    val rowModifier = Modifier.fillMaxWidth()
    if (LocalDensity.current.fontScale >= LARGE_FONT_SCALE) {
        Column(modifier = rowModifier, verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
            TmrLoadingWheel(contentDesc = loading)
            Text(text = loading, style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
        }
        return
    }
    Row(
        modifier = rowModifier,
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrLoadingWheel(contentDesc = loading)
        Text(text = loading, style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
    }
}

@Composable
private fun CreditsReady(
    uiState: CreditsUiState,
    actions: CreditsActions,
) {
    if (uiState.isOffline) {
        NoticeBanner(
            message = stringResource(R.string.feature_tailor_impl_credits_offline_banner),
            tone = NoticeTone.Offline,
        )
    } else if (uiState.purchases.any { entry -> entry.isPending }) {
        NoticeBanner(
            message = stringResource(R.string.feature_tailor_impl_credits_pending_banner),
            tone = NoticeTone.Warning,
        )
    }
    CreditsHero(uiState = uiState)
    TmrPrimaryButton(
        label = stringResource(R.string.feature_tailor_impl_credits_get_pack),
        onClick = actions.onGetPack,
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.canBuy,
        leadingIcon = TmrIcons.Download,
    )
    TmrSectionLabel(text = stringResource(R.string.feature_tailor_impl_credits_purchases_title))
    CreditsPurchases(uiState = uiState)
    TmrSectionLabel(text = stringResource(R.string.feature_tailor_impl_credits_help_title))
    CreditsHelp(actions = actions, hasPurchases = uiState.offersRefund)
}

@Composable
private fun CreditsHero(uiState: CreditsUiState) {
    val left = uiState.totalCredits
    val caption = stringResource(
        when {
            uiState.showsFreeNote -> R.string.feature_tailor_impl_credits_free_note
            uiState.creditsNeverExpire -> R.string.feature_tailor_impl_credits_never_expire
            else -> R.string.feature_tailor_impl_credits_may_expire
        },
    )
    val description = pluralStringResource(R.plurals.feature_tailor_impl_credits_left_description, left, left)
    TmrCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = "$description $caption" },
    ) {
        Text(
            text = left.toString(),
            style = TmrTheme.typography.numeralHero,
            color = TmrTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_left_caption, caption),
            style = TmrTheme.typography.titleS,
            color = TmrTheme.colors.onSurface,
        )
    }
}

@Composable
private fun CreditsPurchases(uiState: CreditsUiState) {
    if (uiState.purchases.isEmpty()) {
        Text(
            text = stringResource(
                if (uiState.purchasesKnown) {
                    R.string.feature_tailor_impl_credits_no_purchases
                } else {
                    R.string.feature_tailor_impl_credits_purchases_unavailable
                },
            ),
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.onSurfaceVariant,
        )
        return
    }
    uiState.purchases.forEach { entry -> CreditsPurchaseRow(entry = entry) }
}

@Composable
private fun CreditsPurchaseRow(entry: CreditsPurchaseEntry) {
    val colors = TmrTheme.colors
    TmrCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
            Text(
                text = pluralStringResource(
                    R.plurals.feature_tailor_impl_credits_purchase_title,
                    entry.credits,
                    entry.credits,
                ),
                style = TmrTheme.typography.titleM,
                color = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(text = entry.formattedPrice, style = TmrTheme.typography.titleM, color = colors.onSurface)
        }
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_purchase_meta, entry.formattedDate),
            style = TmrTheme.typography.bodyS,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_order_id, entry.orderId),
            style = TmrTheme.typography.bodyS,
            color = colors.onSurfaceVariant,
        )
        if (entry.isPending) {
            StatusPill(
                label = stringResource(R.string.feature_tailor_impl_credits_pending),
                icon = TmrIcons.Clock,
                color = colors.onSurface,
            )
        } else {
            StatusPill(
                label = stringResource(R.string.feature_tailor_impl_credits_paid),
                icon = TmrIcons.CheckCircle,
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun CreditsHelp(actions: CreditsActions, hasPurchases: Boolean) {
    HelpCard(
        icon = TmrIcons.Info,
        title = R.string.feature_tailor_impl_credits_help_refunds_title,
        body = if (hasPurchases) {
            R.string.feature_tailor_impl_credits_refund_summary
        } else {
            R.string.feature_tailor_impl_credits_refund_summary_none
        },
    ) {
        if (hasPurchases) {
            TmrOutlineButton(
                label = stringResource(R.string.feature_tailor_impl_credits_ask_refund),
                onClick = actions.onAskRefund,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
    HelpCard(
        icon = TmrIcons.Chat,
        title = R.string.feature_tailor_impl_credits_help_credit_title,
        body = R.string.feature_tailor_impl_credits_help_credit_body,
    )
    HelpCard(
        icon = TmrIcons.Send,
        title = R.string.feature_tailor_impl_credits_help_contact_title,
        body = R.string.feature_tailor_impl_credits_help_summary,
    ) {
        TmrOutlineButton(
            label = stringResource(R.string.feature_tailor_impl_credits_contact_help),
            onClick = actions.onContactHelp,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun HelpCard(
    icon: ImageVector,
    title: Int,
    body: Int,
    action: @Composable () -> Unit = {},
) {
    TmrCard(modifier = Modifier.fillMaxWidth()) {
        IconTile(icon = icon)
        Text(text = stringResource(title), style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
        Text(
            text = stringResource(body),
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.onSurface,
        )
        action()
    }
}

private const val LARGE_FONT_SCALE = 1.5f
