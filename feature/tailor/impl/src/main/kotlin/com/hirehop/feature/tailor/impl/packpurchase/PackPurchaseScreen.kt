package com.hirehop.feature.tailor.impl.packpurchase

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhInnerHeader
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhOfflineBanner
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
import com.hirehop.core.designsystem.component.HhScreen
import com.hirehop.core.designsystem.component.HhSheet
import com.hirehop.core.designsystem.component.HhSpecialButton
import com.hirehop.core.designsystem.component.HhSpecialDeclineButton
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTextButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.credits.CreditCounter
import com.hirehop.feature.tailor.impl.credits.formattedPrice
import com.hirehop.feature.tailor.impl.jobLine

private const val LARGE_TEXT_SCALE = 1.5f

@Composable
internal fun PackPurchaseScreen(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        HhScreen(
            sheet = false,
            header = {
                HhInnerHeader(
                    title = stringResource(
                        if (uiState.hasApplication) {
                            R.string.feature_tailor_impl_export_preview_title
                        } else {
                            R.string.feature_tailor_impl_credits_title
                        },
                    ),
                    subtitle = if (uiState.hasApplication) {
                        jobLine(uiState.jobTitle, uiState.jobCompany)
                    } else {
                        stringResource(R.string.feature_tailor_impl_credits_subtitle)
                    },
                    onBack = actions.onNavigateBack,
                    backContentDescription = stringResource(
                        R.string.feature_tailor_impl_pack_purchase_navigation_back_description,
                    ),
                )
            },
        ) { Box(Modifier.fillMaxSize()) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HhTheme.colors.scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = actions.onNotNow,
                )
                .clearAndSetSemantics {},
        )
        BoxWithConstraints(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            HhSheet(
                modifier = Modifier.heightIn(max = maxHeight - (HhTheme.spacing.d64 + HhTheme.spacing.d32)),
                contentPadding = PaddingValues(
                    start = HhTheme.spacing.gutter,
                    top = HhTheme.spacing.sm + HhTheme.spacing.xxs,
                    end = HhTheme.spacing.gutter,
                    bottom = HhTheme.spacing.xxl,
                ),
            ) {
                PackSheetHandle()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(top = HhTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
                ) {
                    PackSheetContent(uiState = uiState, actions = actions)
                }
            }
        }
    }
}

@Composable
private fun PackSheetHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = HhTheme.spacing.d32 + HhTheme.spacing.xs, height = HhTheme.spacing.xs)
                .clip(HhTheme.shapes.pill)
                .background(HhTheme.colors.outline),
        )
    }
}

@Composable
private fun PackSheetContent(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    when {
        uiState.stage == PackPurchaseStage.LOADING -> PackLoading()
        uiState.isCatalogueFailure -> PackCatalogueFailure(actions = actions)
        !uiState.hasCatalogue -> PackNoPacks(actions = actions)
        else -> when (uiState.stage) {
            PackPurchaseStage.READY,
            PackPurchaseStage.PURCHASING,
            -> PackOffer(uiState = uiState, actions = actions)

            PackPurchaseStage.PENDING -> PackPending(uiState = uiState, actions = actions)
            PackPurchaseStage.SUCCESS -> PackSuccess(uiState = uiState, actions = actions)
            PackPurchaseStage.CANCELLED -> PackCancelled(uiState = uiState, actions = actions)
            PackPurchaseStage.FAILED -> PackFailed(uiState = uiState, actions = actions)
            PackPurchaseStage.LOADING -> PackLoading()
        }
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
    PackTitle(text = stringResource(R.string.feature_tailor_impl_pack_purchase_catalogue_error_title))
    PackBody(text = stringResource(R.string.feature_tailor_impl_pack_purchase_catalogue_error_body))
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
    PackTitle(text = stringResource(R.string.feature_tailor_impl_pack_purchase_no_packs_title))
    PackBody(text = stringResource(R.string.feature_tailor_impl_pack_purchase_no_packs_body))
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
    val compact = LocalDensity.current.fontScale >= LARGE_TEXT_SCALE
    HhOfflineBanner(
        message = stringResource(R.string.feature_tailor_impl_pack_purchase_offline_banner),
        visible = uiState.isOffline,
    )
    Text(
        text = packHeadline(uiState = uiState, compact = compact),
        style = if (compact) HhTheme.typography.titleM else HhTheme.typography.titleL,
        color = HhTheme.colors.onSurface,
    )
    PackPrice(pack = pack, compact = compact)
    if (!compact) {
        PackLine(text = stringResource(R.string.feature_tailor_impl_pack_purchase_line_one_time))
        PackLine(text = stringResource(R.string.feature_tailor_impl_pack_purchase_line_no_subscription))
        PackLine(text = stringResource(R.string.feature_tailor_impl_pack_purchase_line_each_application))
    }
    if (uiState.stage == PackPurchaseStage.PURCHASING) {
        PackWaiting()
    }
    val buyLabel = packButtonLabel(pack)
    val caption = packButtonCaption(uiState)
    val description = packButtonDescription(pack = pack, creditsNeverExpire = uiState.creditsNeverExpire)
    val buyModifier = Modifier
        .alpha(if (uiState.canBuy) 1f else DISABLED_ALPHA)
        .semantics {
            contentDescription = description
            if (!uiState.canBuy) disabled()
        }
    HhSpecialButton(
        label = buyLabel,
        onClick = { actions.onBuy(pack.id) },
        modifier = buyModifier,
        caption = caption,
    )
    uiState.otherPacks.forEach { other ->
        HhOutlineButton(
            label = packButtonLabel(other),
            onClick = { actions.onBuy(other.id) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canBuy,
        )
    }
    HhSpecialDeclineButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now),
        onClick = actions.onNotNow,
    )
    if (!compact) {
        RefundsLink(onClick = actions.onOpenCredits)
    }
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
private fun PackPrice(pack: ApplicationPack, compact: Boolean) {
    val price = pack.formattedPrice()
    if (compact) {
        Text(text = price, style = HhTheme.typography.numeralHero, color = HhTheme.colors.onSurface)
        return
    }
    PackBaselineRow {
        Text(text = price, style = HhTheme.typography.numeralHero, color = HhTheme.colors.onSurface)
        Text(
            text = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_price_caption,
                pack.credits,
                pack.credits,
            ),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PackBaselineRow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs),
        itemVerticalAlignment = Alignment.Bottom,
    ) {
        content()
    }
}

@Composable
private fun PackLine(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = HhIcons.Check,
            contentDescription = null,
            tint = HhTheme.colors.primary,
            modifier = Modifier.size(HhTheme.spacing.xl),
        )
        Text(text = text, style = HhTheme.typography.bodyM, color = HhTheme.colors.body)
    }
}

@Composable
private fun RefundsLink(onClick: () -> Unit) {
    HhTextButton(
        label = stringResource(R.string.feature_tailor_impl_pack_purchase_refunds_link),
        onClick = onClick,
        leadingIcon = HhIcons.Info,
    )
}

@Composable
private fun PackPending(uiState: PackPurchaseUiState, actions: PackPurchaseActions) {
    PackTitleRow(
        icon = HhIcons.Clock,
        tint = HhTheme.colors.primary,
        text = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_title),
    )
    PackBody(text = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_body))
    Text(
        text = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_note),
        style = HhTheme.typography.bodyM,
        color = HhTheme.colors.onSurfaceVariant,
    )
    HhOutlineButton(
        label = backLabel(uiState),
        onClick = actions.onBackToPreview,
        modifier = Modifier.fillMaxWidth(),
    )
    RefundsLink(onClick = actions.onOpenCredits)
}

@Composable
private fun PackSuccess(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    PackTitleRow(
        icon = HhIcons.CheckCircle,
        tint = HhTheme.colors.primary,
        text = stringResource(R.string.feature_tailor_impl_pack_purchase_success_title),
    )
    val left = uiState.totalCredits
    PackBaselineRow {
        CreditCounter(
            credits = left,
            previousCredits = uiState.creditsBefore,
            suffix = stringResource(R.string.feature_tailor_impl_pack_purchase_success_left),
            contentDescription = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_success_left_description,
                left,
                left,
            ),
        )
        Text(
            text = stringResource(
                if (uiState.creditsNeverExpire) {
                    R.string.feature_tailor_impl_pack_purchase_success_was_never_expire
                } else {
                    R.string.feature_tailor_impl_pack_purchase_success_was
                },
                uiState.creditsBefore,
            ),
            style = HhTheme.typography.labelM,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
    val receipt = uiState.receipt
    if (receipt != null) {
        Text(
            text = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_success_receipt,
                receipt.credits,
                receipt.credits,
                receipt.formattedPrice,
                receipt.formattedDate,
            ),
            style = HhTheme.typography.bodyM,
            color = HhTheme.colors.body,
        )
    }
    if (uiState.hasApplication) {
        HhCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = HhIcons.Download,
                    contentDescription = null,
                    tint = HhTheme.colors.primary,
                    modifier = Modifier.size(HhTheme.spacing.xl),
                )
                Text(
                    text = pluralStringResource(
                        R.plurals.feature_tailor_impl_pack_purchase_success_uses_line,
                        left,
                        left,
                    ),
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.onSurface,
                )
            }
        }
        HhPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_export_preview_download_pdf),
            onClick = actions.onDownloadAfterPurchase,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = HhIcons.Download,
        )
    } else {
        HhPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_done),
            onClick = actions.onBackToPreview,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PackCancelled(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    PackTitle(text = stringResource(R.string.feature_tailor_impl_pack_purchase_cancelled_title))
    PackBody(
        text = if (!uiState.hasApplication) {
            stringResource(R.string.feature_tailor_impl_pack_purchase_cancelled_body_credits)
        } else if (uiState.jobCompany.isBlank()) {
            stringResource(R.string.feature_tailor_impl_pack_purchase_cancelled_body_generic)
        } else {
            stringResource(R.string.feature_tailor_impl_pack_purchase_cancelled_body, uiState.jobCompany)
        },
    )
    HhOutlineButton(
        label = backLabel(uiState),
        onClick = actions.onBackToPreview,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun backLabel(uiState: PackPurchaseUiState): String = stringResource(
    if (uiState.hasApplication) {
        R.string.feature_tailor_impl_pack_purchase_back_to_preview
    } else {
        R.string.feature_tailor_impl_pack_purchase_back_to_credits
    },
)

@Composable
private fun PackFailed(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        HhSpotIllustration(kind = HhSpotKind.Error)
    }
    PackTitleRow(
        icon = HhIcons.Error,
        tint = HhTheme.colors.error,
        text = stringResource(R.string.feature_tailor_impl_pack_purchase_failed_title),
    )
    PackBody(
        text = stringResource(
            when (uiState.failureReason) {
                PurchaseFailureReason.PaymentUnavailable ->
                    R.string.feature_tailor_impl_pack_purchase_failed_body_unavailable

                PurchaseFailureReason.PurchaseUnavailable ->
                    R.string.feature_tailor_impl_pack_purchase_failed_body_pack_unavailable

                else -> R.string.feature_tailor_impl_pack_purchase_failed_body
            },
        ),
    )
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhOutlineButton(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now),
            onClick = actions.onNotNow,
            modifier = Modifier.fillMaxWidth(),
        )
        HhPrimaryButton(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_try_again),
            onClick = actions.onRetryBuy,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isOffline,
        )
    }
    RefundsLink(onClick = actions.onOpenCredits)
}

@Composable
private fun PackTitle(text: String) {
    Text(text = text, style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
}

@Composable
private fun PackTitleRow(icon: ImageVector, tint: Color, text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(HhTheme.spacing.xxl),
        )
        PackTitle(text = text)
    }
}

@Composable
private fun PackBody(text: String) {
    Text(text = text, style = HhTheme.typography.bodyL, color = HhTheme.colors.body)
}

@Composable
private fun packHeadline(uiState: PackPurchaseUiState, compact: Boolean): String =
    stringResource(packHeadlineRes(uiState, compact), uiState.jobCompany)

internal fun packHeadlineRes(uiState: PackPurchaseUiState, compact: Boolean): Int = when {
    !uiState.hasApplication -> R.string.feature_tailor_impl_pack_purchase_headline_credits
    compact -> R.string.feature_tailor_impl_pack_purchase_headline_compact
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
private fun packButtonCaption(uiState: PackPurchaseUiState): String = stringResource(
    if (uiState.creditsNeverExpire) {
        R.string.feature_tailor_impl_pack_purchase_credits_never_expire
    } else {
        R.string.feature_tailor_impl_pack_purchase_credits_may_expire
    },
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

private const val DISABLED_ALPHA = 0.38f
