package com.hirehop.feature.tailor.impl.credits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import com.hirehop.core.designsystem.component.HhAccent
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhErrorCallout
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOnColorChip
import com.hirehop.core.designsystem.component.HhOnColorChipStyle
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPillRow
import com.hirehop.core.designsystem.component.HhPillRowStyle
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSolidCard
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
    val largeFont = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
    val loading = stringResource(R.string.feature_tailor_impl_credits_loading)
    if (largeFont) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
            HhLoadingWheel(contentDesc = loading)
            Text(text = loading, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
        }
        return
    }
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
    CreditsHero(uiState = uiState)
    HhPillRow(
        title = stringResource(R.string.feature_tailor_impl_credits_get_pack),
        onClick = { if (uiState.canBuy) actions.onGetPack() },
        style = HhPillRowStyle.Marigold,
        icon = HhIcons.Download,
        modifier = if (uiState.canBuy) {
            Modifier
        } else {
            Modifier
                .alpha(DISABLED_ALPHA)
                .semantics { disabled() }
        },
    )
    CreditsSectionTitle(text = stringResource(R.string.feature_tailor_impl_credits_purchases_title))
    CreditsPurchases(uiState = uiState)
    CreditsSectionTitle(text = stringResource(R.string.feature_tailor_impl_credits_help_title))
    CreditsHelp(actions = actions)
}

@Composable
private fun CreditsHero(uiState: CreditsUiState) {
    val left = uiState.totalCredits
    val expiry = stringResource(
        if (uiState.creditsNeverExpire) {
            R.string.feature_tailor_impl_credits_never_expire
        } else {
            R.string.feature_tailor_impl_credits_may_expire
        },
    )
    val freeNote = if (uiState.showsFreeNote) stringResource(R.string.feature_tailor_impl_credits_free_note) else null
    val description = listOfNotNull(
        pluralStringResource(R.plurals.feature_tailor_impl_credits_left_description, left, left),
        expiry,
        freeNote,
    ).joinToString(" ")
    val chips: (@Composable RowScope.() -> Unit)? = if (uiState.showsFreeNote) {
        { HhOnColorChip(label = stringResource(R.string.feature_tailor_impl_credits_free_note)) }
    } else {
        null
    }
    HhSolidCard(
        accent = HhAccent.Marigold,
        monogram = left.toString(),
        title = pluralStringResource(R.plurals.feature_tailor_impl_credits_left, left, left),
        subtitle = expiry,
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
        chips = chips,
    )
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
    uiState.purchases.forEach { entry -> CreditsPurchaseRow(entry = entry) }
}

@Composable
private fun CreditsPurchaseRow(entry: CreditsPurchaseEntry) {
    if (entry.isPending) {
        val pending = stringResource(R.string.feature_tailor_impl_credits_pending)
        Column(
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs + HhTheme.spacing.xxs),
            modifier = Modifier.semantics(mergeDescendants = true) {},
        ) {
            HhSolidCard(
                accent = HhAccent.Marigold,
                monogram = "₹",
                title = pluralStringResource(
                    R.plurals.feature_tailor_impl_credits_purchase_title,
                    entry.credits,
                    entry.credits,
                    entry.formattedPrice,
                ),
                subtitle = pluralStringResource(
                    R.plurals.feature_tailor_impl_credits_pending_note,
                    entry.credits,
                    entry.credits,
                ),
                chips = {
                    Icon(
                        imageVector = HhIcons.Clock,
                        contentDescription = null,
                        tint = HhTheme.colors.onSpecial,
                        modifier = Modifier.heightIn(min = HhTheme.spacing.d20),
                    )
                    HhOnColorChip(label = pending, style = HhOnColorChipStyle.Ink)
                },
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_purchase_meta, entry.formattedDate),
                style = HhTheme.typography.labelM,
                color = HhTheme.colors.onSurfaceVariant,
            )
            CreditsPurchaseOrder(entry = entry)
        }
        return
    }
    HhCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
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
        Text(
            text = stringResource(R.string.feature_tailor_impl_credits_purchase_meta, entry.formattedDate),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
        CreditsPurchaseOrder(entry = entry)
    }
}

@Composable
private fun CreditsPurchaseOrder(entry: CreditsPurchaseEntry) {
    Text(
        text = entry.orderId,
        style = HhTheme.typography.factId,
        color = HhTheme.colors.onSurface,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CreditsHelp(actions: CreditsActions) {
    val largeFont = LocalDensity.current.fontScale >= LARGE_FONT_SCALE
    HhCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_refund_summary),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
            if (largeFont) {
                Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
                    HhOutlineButton(
                        label = stringResource(R.string.feature_tailor_impl_credits_ask_refund),
                        onClick = actions.onAskRefund,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HhOutlineButton(
                        label = stringResource(R.string.feature_tailor_impl_credits_contact_help),
                        onClick = actions.onContactHelp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            } else {
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
            }
            Text(
                text = stringResource(R.string.feature_tailor_impl_credits_help_summary),
                style = HhTheme.typography.bodyM,
                color = HhTheme.colors.body,
            )
        }
    }
}

private const val LARGE_FONT_SCALE = 1.5f
private const val DISABLED_ALPHA = 0.38f
