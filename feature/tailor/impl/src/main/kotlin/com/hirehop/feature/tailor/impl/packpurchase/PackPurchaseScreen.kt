package com.hirehop.feature.tailor.impl.packpurchase

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
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSecondaryButton
import com.hirehop.core.designsystem.component.HhSectionLabel
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.core.model.ExportFormat
import com.hirehop.feature.tailor.impl.NoteLine
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.StatusCard
import com.hirehop.feature.tailor.impl.StatusPill
import com.hirehop.feature.tailor.impl.credits.CreditCounter
import com.hirehop.feature.tailor.impl.credits.formattedPrice
import com.hirehop.feature.tailor.impl.exportpreview.NoticeBanner
import com.hirehop.feature.tailor.impl.exportpreview.NoticeTone

@Composable
internal fun PackPurchaseScreen(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
    modifier: Modifier = Modifier,
) {
    HhScreen(
        modifier = modifier,
        sheet = false,
        header = {
            HhInnerHeader(
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
                .padding(horizontal = HhTheme.spacing.gutter),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
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
        { HhBottomActionBar { PackOfferAction(uiState = uiState, actions = actions) } }
    }

    uiState.stage == PackPurchaseStage.PENDING -> {
        {
            HhBottomActionBar {
                HhSecondaryButton(
                    label = backLabel(uiState),
                    onClick = actions.onBackToPreview,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    else -> {
        { HhBottomActionBar { PackSuccessAction(uiState = uiState, actions = actions) } }
    }
}

@Composable
private fun RowScope.PackOfferAction(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    val pack = uiState.selectedPack ?: return
    if (uiState.stage == PackPurchaseStage.FAILED) {
        HhPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_try_again),
            onClick = actions.onRetryBuy,
            modifier = Modifier.weight(1f),
            enabled = !uiState.isOffline,
        )
        return
    }
    val description = packButtonDescription(pack = pack, creditsNeverExpire = uiState.creditsNeverExpire)
    HhPrimaryButton(
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
        HhPrimaryButton(
            label = stringResource(
                if (uiState.format == ExportFormat.PDF) {
                    R.string.feature_tailor_impl_export_preview_download_pdf
                } else {
                    R.string.feature_tailor_impl_export_preview_download_docx
                },
            ),
            onClick = actions.onDownloadAfterPurchase,
            modifier = Modifier.weight(1f),
            trailingIcon = HhIcons.Download,
        )
    } else {
        HhPrimaryButton(
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
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhLoadingWheel(contentDesc = loading)
        Text(text = loading, style = HhTheme.typography.titleM, color = HhTheme.colors.onSurface)
    }
}

@Composable
private fun PackCatalogueFailure(actions: PackPurchaseActions) {
    StatusCard(
        kind = HhSpotKind.Error,
        title = stringResource(R.string.feature_tailor_impl_pack_purchase_catalogue_error_title),
        body = stringResource(R.string.feature_tailor_impl_pack_purchase_catalogue_error_body),
    )
    HhPrimaryButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_try_again),
        onClick = actions.onReloadPacks,
        modifier = Modifier.fillMaxWidth(),
    )
    HhOutlineButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now),
        onClick = actions.onNotNow,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun PackNoPacks(actions: PackPurchaseActions) {
    StatusCard(
        kind = HhSpotKind.Empty,
        title = stringResource(R.string.feature_tailor_impl_pack_purchase_no_packs_title),
        body = stringResource(R.string.feature_tailor_impl_pack_purchase_no_packs_body),
    )
    HhOutlineButton(
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
    HhHeadline(
        text = stringResource(R.string.feature_tailor_impl_pack_purchase_title),
        style = HhTheme.typography.displayM,
        modifier = Modifier.fillMaxWidth(),
    )
    Text(
        text = stringResource(packHeadlineRes(uiState), uiState.jobCompany),
        style = HhTheme.typography.bodyL,
        color = HhTheme.colors.onSurfaceVariant,
    )
    PackOfferBanner(uiState = uiState)
    PackCard(pack = pack, creditsNeverExpire = uiState.creditsNeverExpire)
    if (uiState.stage == PackPurchaseStage.PURCHASING) {
        PackWaiting()
    }
    uiState.otherPacks.forEach { other ->
        HhOutlineButton(
            label = packButtonLabel(other),
            onClick = { actions.onBuy(other.id) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canBuyNow,
        )
    }
    HhTextButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now),
        onClick = actions.onNotNow,
        modifier = Modifier.fillMaxWidth(),
    )
    HhTextButton(
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

    else -> R.string.feature_tailor_impl_pack_purchase_failed_body
}

@Composable
private fun PackCard(pack: ApplicationPack, creditsNeverExpire: Boolean) {
    val colors = HhTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, HhTheme.shapes.card)
            .border(2.dp, colors.brand, HhTheme.shapes.card)
            .padding(HhTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 - HhTheme.spacing.d2),
    ) {
        HhSectionLabel(text = stringResource(R.string.feature_tailor_impl_pack_purchase_title))
        Text(
            text = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_price_caption,
                pack.credits,
                pack.credits,
            ),
            style = HhTheme.typography.headlineM,
            color = colors.onSurface,
        )
        Text(
            text = pack.formattedPrice(),
            style = HhTheme.typography.numeralHero,
            color = colors.onSurface,
        )
        HhDivider()
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
    Text(text = text, style = HhTheme.typography.bodyM, color = HhTheme.colors.onSurfaceVariant)
}

@Composable
private fun PackWaiting() {
    val waiting = stringResource(R.string.feature_tailor_impl_pack_purchase_waiting)
    Row(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HhLoadingWheel(contentDesc = waiting)
        Text(text = waiting, style = HhTheme.typography.labelL, color = HhTheme.colors.onSurfaceVariant)
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
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(88.dp)
                .background(circle, HhTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(40.dp))
        }
        HhHeadline(text = title, style = HhTheme.typography.headlineL, modifier = Modifier.fillMaxWidth())
        Text(text = body, style = HhTheme.typography.bodyL, color = HhTheme.colors.onSurfaceVariant)
        extra()
    }
}

@Composable
private fun PackPending(actions: PackPurchaseActions) {
    val colors = HhTheme.colors
    PackMessage(
        icon = HhIcons.Clock,
        circle = colors.partialContainer,
        tint = colors.partial,
        title = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_title),
        body = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_body),
    ) {
        StatusPill(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_waiting),
            icon = HhIcons.Clock,
            color = colors.onSurface,
        )
        HhTextButton(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_refunds_link),
            onClick = actions.onOpenCredits,
        )
    }
}

@Composable
private fun PackSuccess(uiState: PackPurchaseUiState) {
    val colors = HhTheme.colors
    val left = uiState.totalCredits
    val bought = uiState.receipt?.credits ?: left
    PackMessage(
        icon = HhIcons.Check,
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
            style = HhTheme.typography.headlineL,
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
                style = HhTheme.typography.bodyS,
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
                icon = HhIcons.Download,
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
