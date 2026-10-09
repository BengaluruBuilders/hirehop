package com.tailormyresume.feature.tailor.impl.packpurchase

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrBottomActionBar
import com.tailormyresume.core.designsystem.component.TmrDivider
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrInnerHeader
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrOutlineButton
import com.tailormyresume.core.designsystem.component.TmrPrimaryButton
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSecondaryButton
import com.tailormyresume.core.designsystem.component.TmrSectionLabel
import com.tailormyresume.core.designsystem.component.TmrSpotKind
import com.tailormyresume.core.designsystem.component.TmrTextButton
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.ApplicationPack
import com.tailormyresume.core.domain.PurchaseFailureReason
import com.tailormyresume.core.model.ExportFormat
import com.tailormyresume.feature.tailor.impl.NoteLine
import com.tailormyresume.feature.tailor.impl.R
import com.tailormyresume.feature.tailor.impl.StatusCard
import com.tailormyresume.feature.tailor.impl.StatusPill
import com.tailormyresume.feature.tailor.impl.credits.CreditCounter
import com.tailormyresume.feature.tailor.impl.credits.formattedPrice
import com.tailormyresume.feature.tailor.impl.exportpreview.NoticeBanner
import com.tailormyresume.feature.tailor.impl.exportpreview.NoticeTone

@Composable
internal fun PackPurchaseScreen(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
    modifier: Modifier = Modifier,
) {
    TmrScreen(
        modifier = modifier,
        sheet = false,
        header = {
            TmrInnerHeader(
                title = "",
                onBack = actions.onNavigateBack,
                backContentDescription = stringResource(
                    R.string.feature_tailor_impl_pack_purchase_navigation_back_description,
                ),
            )
        },
        bottomBar = packBottomBar(uiState = uiState, actions = actions),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = TmrTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        ) {
            PackContent(uiState = uiState, actions = actions)
        }
    }
}

private val PackPurchaseUiState.showsOffer: Boolean
    get() = hasCatalogue && stage in OFFER_STAGES

private val OFFER_STAGES = setOf(
    PackPurchaseStage.READY,
    PackPurchaseStage.PURCHASING,
    PackPurchaseStage.CANCELLED,
    PackPurchaseStage.FAILED,
)

private val PackPurchaseUiState.canBuyNow: Boolean
    get() = canBuy || (stage == PackPurchaseStage.CANCELLED && !isOffline && selectedPack != null)

@Composable
private fun PackContent(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    when {
        uiState.stage == PackPurchaseStage.LOADING -> PackLoading()
        uiState.isCatalogueFailure -> PackCatalogueFailure(actions = actions)
        !uiState.hasCatalogue -> PackNoPacks(actions = actions)
        uiState.showsOffer -> PackOffer(uiState = uiState, actions = actions)
        uiState.stage == PackPurchaseStage.PENDING -> PackPending(actions = actions)
        else -> PackSuccess(uiState = uiState)
    }
}

@Composable
private fun packBottomBar(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
): (@Composable () -> Unit)? = when {
    uiState.stage == PackPurchaseStage.LOADING || uiState.isCatalogueFailure || !uiState.hasCatalogue -> null
    uiState.showsOffer -> {
        { TmrBottomActionBar { PackOfferAction(uiState = uiState, actions = actions) } }
    }

    uiState.stage == PackPurchaseStage.PENDING -> {
        {
            TmrBottomActionBar {
                TmrSecondaryButton(
                    label = backLabel(uiState),
                    onClick = actions.onBackToPreview,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    else -> {
        { TmrBottomActionBar { PackSuccessAction(uiState = uiState, actions = actions) } }
    }
}

@Composable
private fun RowScope.PackOfferAction(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    val pack = uiState.selectedPack ?: return
    if (uiState.stage == PackPurchaseStage.FAILED) {
        TmrPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_try_again),
            onClick = actions.onRetryBuy,
            modifier = Modifier.weight(1f),
            enabled = !uiState.isOffline,
        )
        return
    }
    val description = packButtonDescription(pack = pack, creditsNeverExpire = uiState.creditsNeverExpire)
    TmrPrimaryButton(
        label = pluralStringResource(
            R.plurals.feature_tailor_impl_pack_purchase_button_buy,
            pack.credits,
            pack.credits,
        ),
        onClick = { if (uiState.canBuyNow) actions.onBuy(pack.id) },
        modifier = Modifier
            .weight(1f)
            .semantics { contentDescription = description },
        enabled = uiState.canBuyNow,
    )
}

@Composable
private fun RowScope.PackSuccessAction(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    if (uiState.hasApplication) {
        TmrPrimaryButton(
            label = stringResource(
                if (uiState.format == ExportFormat.PDF) {
                    R.string.feature_tailor_impl_export_preview_download_pdf
                } else {
                    R.string.feature_tailor_impl_export_preview_download_docx
                },
            ),
            onClick = actions.onDownloadAfterPurchase,
            modifier = Modifier.weight(1f),
            trailingIcon = TmrIcons.Download,
        )
    } else {
        TmrPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_done),
            onClick = actions.onBackToPreview,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PackLoading() {
    val loading = stringResource(R.string.feature_tailor_impl_pack_purchase_loading)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrLoadingWheel(contentDesc = loading)
        Text(text = loading, style = TmrTheme.typography.titleM, color = TmrTheme.colors.onSurface)
    }
}

@Composable
private fun PackCatalogueFailure(actions: PackPurchaseActions) {
    StatusCard(
        kind = TmrSpotKind.Error,
        title = stringResource(R.string.feature_tailor_impl_pack_purchase_catalogue_error_title),
        body = stringResource(R.string.feature_tailor_impl_pack_purchase_catalogue_error_body),
    )
    TmrPrimaryButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_try_again),
        onClick = actions.onReloadPacks,
        modifier = Modifier.fillMaxWidth(),
    )
    TmrOutlineButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now),
        onClick = actions.onNotNow,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PackNoPacks(actions: PackPurchaseActions) {
    StatusCard(
        kind = TmrSpotKind.Empty,
        title = stringResource(R.string.feature_tailor_impl_pack_purchase_no_packs_title),
        body = stringResource(R.string.feature_tailor_impl_pack_purchase_no_packs_body),
    )
    TmrOutlineButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now),
        onClick = actions.onNotNow,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PackOffer(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    val pack = uiState.selectedPack ?: return
    TmrHeadline(
        text = stringResource(R.string.feature_tailor_impl_pack_purchase_title),
        style = TmrTheme.typography.displayM,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(packHeadlineRes(uiState), uiState.jobCompany),
        style = TmrTheme.typography.bodyL,
        color = TmrTheme.colors.onSurfaceVariant,
    )
    PackOfferBanner(uiState = uiState)
    PackCard(pack = pack, creditsNeverExpire = uiState.creditsNeverExpire)
    if (uiState.stage == PackPurchaseStage.PURCHASING) {
        PackWaiting()
    }
    uiState.otherPacks.forEach { other ->
        TmrOutlineButton(
            label = packButtonLabel(other),
            onClick = { actions.onBuy(other.id) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canBuyNow,
        )
    }
    TmrTextButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now),
        onClick = actions.onNotNow,
        modifier = Modifier.fillMaxWidth(),
    )
    TmrTextButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_refunds_link),
        onClick = actions.onOpenCredits,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PackOfferBanner(uiState: PackPurchaseUiState) {
    when {
        uiState.stage == PackPurchaseStage.FAILED -> NoticeBanner(
            message = stringResource(failureBodyRes(uiState.failureReason)),
            tone = NoticeTone.Error,
        )

        uiState.isOffline -> NoticeBanner(
            message = stringResource(R.string.feature_tailor_impl_pack_purchase_offline_banner),
            tone = NoticeTone.Offline,
        )

        uiState.stage == PackPurchaseStage.CANCELLED -> NoticeBanner(
            message = stringResource(
                if (uiState.hasApplication) {
                    R.string.feature_tailor_impl_pack_purchase_cancelled_body
                } else {
                    R.string.feature_tailor_impl_pack_purchase_cancelled_body_credits
                },
            ),
            tone = NoticeTone.Quiet,
        )
    }
}

private fun failureBodyRes(reason: PurchaseFailureReason?): Int = when (reason) {
    PurchaseFailureReason.PaymentUnavailable -> R.string.feature_tailor_impl_pack_purchase_failed_body_unavailable
    PurchaseFailureReason.PurchaseUnavailable ->
        R.string.feature_tailor_impl_pack_purchase_failed_body_pack_unavailable

    PurchaseFailureReason.PaymentUnconfirmed -> R.string.feature_tailor_impl_pack_purchase_failed_body_unconfirmed

    else -> R.string.feature_tailor_impl_pack_purchase_failed_body
}

@Composable
private fun PackCard(pack: ApplicationPack, creditsNeverExpire: Boolean) {
    val colors = TmrTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, TmrTheme.shapes.card)
            .border(2.dp, colors.brand, TmrTheme.shapes.card)
            .padding(TmrTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.d12 - TmrTheme.spacing.d2),
    ) {
        TmrSectionLabel(text = stringResource(R.string.feature_tailor_impl_pack_purchase_title))
        Text(
            text = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_price_caption,
                pack.credits,
                pack.credits,
            ),
            style = TmrTheme.typography.headlineM,
            color = colors.onSurface,
        )
        Text(
            text = pack.formattedPrice(),
            style = TmrTheme.typography.numeralHero,
            color = colors.onSurface,
        )
        TmrDivider()
        PackLine(text = stringResource(R.string.feature_tailor_impl_pack_purchase_line_one_time))
        PackLine(text = stringResource(R.string.feature_tailor_impl_pack_purchase_line_no_subscription))
        PackLine(
            text = stringResource(
                if (creditsNeverExpire) {
                    R.string.feature_tailor_impl_pack_purchase_line_never_expire
                } else {
                    R.string.feature_tailor_impl_pack_purchase_line_may_expire
                },
            ),
        )
        PackLine(text = stringResource(R.string.feature_tailor_impl_pack_purchase_line_each_application))
    }
}

@Composable
private fun PackLine(text: String) {
    Text(text = text, style = TmrTheme.typography.bodyM, color = TmrTheme.colors.onSurfaceVariant)
}

@Composable
private fun PackWaiting() {
    val waiting = stringResource(R.string.feature_tailor_impl_pack_purchase_waiting)
    Row(
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TmrLoadingWheel(contentDesc = waiting)
        Text(text = waiting, style = TmrTheme.typography.labelL, color = TmrTheme.colors.onSurfaceVariant)
    }
}

@Composable
private fun PackMessage(
    icon: ImageVector,
    circle: Color,
    tint: Color,
    title: String,
    body: String,
    extra: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(88.dp)
                .background(circle, TmrTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(40.dp))
        }
        TmrHeadline(text = title, style = TmrTheme.typography.headlineL, modifier = Modifier.fillMaxWidth())
        Text(text = body, style = TmrTheme.typography.bodyL, color = TmrTheme.colors.onSurfaceVariant)
        extra()
    }
}

@Composable
private fun PackPending(actions: PackPurchaseActions) {
    val colors = TmrTheme.colors
    PackMessage(
        icon = TmrIcons.Clock,
        circle = colors.partialContainer,
        tint = colors.partial,
        title = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_title),
        body = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_body),
    ) {
        StatusPill(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_waiting),
            icon = TmrIcons.Clock,
            color = colors.onSurface,
        )
        TmrTextButton(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_refunds_link),
            onClick = actions.onOpenCredits,
        )
    }
}

@Composable
private fun PackSuccess(uiState: PackPurchaseUiState) {
    val colors = TmrTheme.colors
    val left = uiState.totalCredits
    val bought = uiState.receipt?.credits ?: left
    PackMessage(
        icon = TmrIcons.Check,
        circle = colors.metContainer,
        tint = colors.met,
        title = stringResource(R.string.feature_tailor_impl_pack_purchase_success_title),
        body = pluralStringResource(
            if (uiState.creditsNeverExpire) {
                R.plurals.feature_tailor_impl_pack_purchase_success_body_never_expire
            } else {
                R.plurals.feature_tailor_impl_pack_purchase_success_body
            },
            bought,
            bought,
        ),
    ) {
        CreditCounter(
            credits = left,
            previousCredits = uiState.creditsBefore,
            suffix = stringResource(R.string.feature_tailor_impl_pack_purchase_success_left),
            contentDescription = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_success_left_description,
                left,
                left,
            ),
            style = TmrTheme.typography.headlineL,
        )
        uiState.receipt?.let { receipt ->
            Text(
                text = pluralStringResource(
                    R.plurals.feature_tailor_impl_pack_purchase_success_receipt,
                    receipt.credits,
                    receipt.credits,
                    receipt.formattedPrice,
                    receipt.formattedDate,
                ),
                style = TmrTheme.typography.bodyS,
                color = colors.onSurfaceVariant,
            )
        }
        if (uiState.hasApplication) {
            NoteLine(
                text = pluralStringResource(
                    R.plurals.feature_tailor_impl_pack_purchase_success_uses_line,
                    left,
                    left,
                ),
                icon = TmrIcons.Download,
            )
        }
    }
}

@Composable
private fun backLabel(uiState: PackPurchaseUiState): String = stringResource(
    if (uiState.hasApplication) {
        R.string.feature_tailor_impl_pack_purchase_back_to_preview
    } else {
        R.string.feature_tailor_impl_pack_purchase_back_to_credits
    },
)

internal fun packHeadlineRes(uiState: PackPurchaseUiState): Int = when {
    !uiState.hasApplication -> R.string.feature_tailor_impl_pack_purchase_headline_credits
    uiState.jobCompany.isBlank() -> R.string.feature_tailor_impl_pack_purchase_headline_generic
    else -> R.string.feature_tailor_impl_pack_purchase_headline
}

@Composable
private fun packButtonLabel(pack: ApplicationPack): String = pluralStringResource(
    R.plurals.feature_tailor_impl_pack_purchase_button_label,
    pack.credits,
    pack.credits,
    pack.formattedPrice(),
)

@Composable
private fun packButtonDescription(pack: ApplicationPack, creditsNeverExpire: Boolean): String =
    pluralStringResource(
        if (creditsNeverExpire) {
            R.plurals.feature_tailor_impl_pack_purchase_button_description_never_expire
        } else {
            R.plurals.feature_tailor_impl_pack_purchase_button_description
        },
        pack.credits,
        pack.credits,
        pack.formattedPrice(),
    )
