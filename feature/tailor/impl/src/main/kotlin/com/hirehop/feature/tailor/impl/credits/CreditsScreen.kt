package com.hirehop.feature.tailor.impl.credits

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
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.StatusPill
import com.hirehop.feature.tailor.impl.exportpreview.IconTile
import com.hirehop.feature.tailor.impl.exportpreview.NoticeBanner
import com.hirehop.feature.tailor.impl.exportpreview.NoticeTone

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
    val rowModifier = Modifier.fillMaxWidth()
    if (LocalDensity.current.fontScale >= LARGE_FONT_SCALE) {
        Column(modifier = rowModifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhLoadingWheel(contentDesc = loading)
            Text(text = loading, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
        }
        return
    }
    Row(
        modifier = rowModifier,
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
    HhPrimaryButton(
        label = stringResource(R.string.feature_tailor_impl_credits_get_pack),
        onClick = actions.onGetPack,
        modifier = Modifier.fillMaxWidth(),
        enabled = uiState.canBuy,
        leadingIcon = HhIcons.Download,
    )
    HhSectionLabel(text = stringResource(R.string.feature_tailor_impl_credits_purchases_title))
    CreditsPurchases(uiState = uiState)
    HhSectionLabel(text = stringResource(R.string.feature_tailor_impl_credits_help_title))
    CreditsHelp(actions = actions)
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
    HhCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) { contentDescription = "$description $caption" },
    ) {
        Text(
            text = left.toString(),
            style = HhTheme.typography.numeralHero,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_left_caption, caption),
            style = HhTheme.typography.titleS,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun CreditsPurchases(uiState: CreditsUiState) {
    if (uiState.purchases.isEmpty()) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_no_purchases),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        return
    }
    uiState.purchases.forEach { entry -> CreditsPurchaseRow(entry = entry) }
}

@Composable
private fun CreditsPurchaseRow(entry: CreditsPurchaseEntry) {
    val colors = HhTheme.colors
    HhCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            Text(
                text = pluralStringResource(
                    R.plurals.feature_tailor_impl_credits_purchase_title,
                    entry.credits,
                    entry.credits,
                ),
                style = HhTheme.typography.titleM,
                color = colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(text = entry.formattedPrice, style = HhTheme.typography.titleM, color = colors.onSurface)
        }
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_purchase_meta, entry.formattedDate),
            style = HhTheme.typography.bodyS,
            color = colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_order_id, entry.orderId),
            style = HhTheme.typography.bodyS,
            color = colors.onSurfaceVariant,
        )
        if (entry.isPending) {
            StatusPill(
                label = stringResource(R.string.feature_tailor_impl_credits_pending),
                icon = HhIcons.Clock,
                color = colors.onSurface,
            )
        } else {
            StatusPill(
                label = stringResource(R.string.feature_tailor_impl_credits_paid),
                icon = HhIcons.CheckCircle,
                color = colors.primary,
            )
        }
    }
}

@Composable
private fun CreditsHelp(actions: CreditsActions) {
    HelpCard(
        icon = HhIcons.Info,
        title = R.string.feature_tailor_impl_credits_help_refunds_title,
        body = R.string.feature_tailor_impl_credits_refund_summary,
    ) {
        HhOutlineButton(
            label = stringResource(R.string.feature_tailor_impl_credits_ask_refund),
            onClick = actions.onAskRefund,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    HelpCard(
        icon = HhIcons.Chat,
        title = R.string.feature_tailor_impl_credits_help_credit_title,
        body = R.string.feature_tailor_impl_credits_help_credit_body,
    )
    HelpCard(
        icon = HhIcons.Send,
        title = R.string.feature_tailor_impl_credits_help_contact_title,
        body = R.string.feature_tailor_impl_credits_help_summary,
    ) {
        HhOutlineButton(
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
    HhCard(modifier = Modifier.fillMaxWidth()) {
        IconTile(icon = icon)
        Text(text = stringResource(title), style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
        Text(
            text = stringResource(body),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurface,
        )
        action()
    }
}

private const val LARGE_FONT_SCALE = 1.5f
