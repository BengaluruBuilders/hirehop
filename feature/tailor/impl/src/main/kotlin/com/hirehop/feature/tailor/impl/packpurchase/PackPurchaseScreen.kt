package com.hirehop.feature.tailor.impl.packpurchase

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import com.hirehop.core.designsystem.component.HhBottomActionBar
import com.hirehop.core.designsystem.component.HhButton
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
import com.hirehop.core.domain.ApplicationPack
import com.hirehop.core.domain.PurchaseFailureReason
import com.hirehop.feature.tailor.impl.R

private const val HH_STACK_FONT_SCALE = 1.5f

@Composable
internal fun PackPurchaseScreen(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
    modifier: Modifier = Modifier,
) {
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(R.string.feature_tailor_impl_pack_purchase_title),
                navigationIcon = Icons.AutoMirrored.Rounded.ArrowBack,
                navigationIconContentDescription = stringResource(
                    R.string.feature_tailor_impl_pack_purchase_navigation_back_description,
                ),
                onNavigationClick = actions.onNavigateBack,
            )
        },
        bottomBar = {
            if (uiState.stage.showsBottomBar) {
                PackPurchaseBottomBar(uiState = uiState, actions = actions)
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            PackPurchaseBody(uiState = uiState, actions = actions)
        }
    }
}

private val PackPurchaseStage.showsBottomBar: Boolean
    get() = this in setOf(
        PackPurchaseStage.READY,
        PackPurchaseStage.OFFLINE,
        PackPurchaseStage.CANCELLED,
        PackPurchaseStage.FAILED,
        PackPurchaseStage.RESTORING,
        PackPurchaseStage.RESTORED,
    )

@Composable
private fun PackPurchaseBody(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    when {
        uiState.stage == PackPurchaseStage.LOADING_PACKS || uiState.stage == PackPurchaseStage.IDLE -> {
            PackPurchaseLoading()
        }
        uiState.isCatalogueFailure -> PackPurchaseCatalogueError(actions = actions)
        else -> PackPurchaseOutcomeBody(uiState = uiState, actions = actions)
    }
}

@Composable
private fun PackPurchaseLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.lg),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        HhLoadingWheel(contentDesc = stringResource(R.string.feature_tailor_impl_pack_purchase_loading))
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_loading),
            style = HhTheme.typography.titleLarge,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_loading_note),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PackPurchaseCatalogueError(actions: PackPurchaseActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(HhTheme.spacing.d20),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhSpotIllustration(kind = HhSpotKind.Error)
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_catalogue_error_heading),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_catalogue_error_body),
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhDivider(style = HhDividerStyle.Dashed)
        HhButton(
            onClick = actions.onRestore,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_tailor_impl_pack_purchase_catalogue_error_retry),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun PackPurchaseOutcomeBody(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = HhTheme.spacing.d16, vertical = HhTheme.spacing.d16),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        HhOfflineBanner(
            message = stringResource(R.string.feature_tailor_impl_pack_purchase_offline_message),
            supportingText = stringResource(R.string.feature_tailor_impl_pack_purchase_offline_supporting),
            visible = uiState.isOffline,
        )
        PackPurchaseStageNote(uiState = uiState, actions = actions)
        if (uiState.hasCatalogue) {
            PackPurchaseHeadline(uiState = uiState)
            PackPurchaseBalance(uiState = uiState)
            uiState.selectedPack?.let { pack -> PackPurchaseIncludes(pack = pack) }
            PackPurchaseAlternatives(uiState = uiState, actions = actions)
        } else if (uiState.stage != PackPurchaseStage.FAILED) {
            PackPurchaseNoPacks()
        }
        PackPurchaseRestoreNote(actions = actions)
    }
}

@Composable
private fun PackPurchaseStageNote(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    when (uiState.stage) {
        PackPurchaseStage.PURCHASING -> PackPurchaseNoteCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_purchasing_heading),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            HhLoadingWheel(contentDesc = stringResource(R.string.feature_tailor_impl_pack_purchase_purchasing_heading))
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_purchasing_body),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        PackPurchaseStage.PENDING -> PackPurchaseNoteCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_heading),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_body),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            HhDivider()
            HhHeroNumeral(
                value = uiState.purchasedCredits.toString(),
                caption = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_caption),
                contentDescription = pluralStringResource(
                    R.plurals.feature_tailor_impl_pack_purchase_purchased_credits_value_description,
                    uiState.purchasedCredits,
                    uiState.purchasedCredits,
                ),
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_pending_note),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
            PackPurchaseBackToPreview(actions = actions)
        }
        PackPurchaseStage.SUCCESS -> PackPurchaseNoteCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_success_heading),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_success_body),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            HhDivider()
            HhHeroNumeral(
                value = uiState.purchasedCredits.toString(),
                caption = stringResource(R.string.feature_tailor_impl_pack_purchase_success_caption),
                contentDescription = pluralStringResource(
                    R.plurals.feature_tailor_impl_pack_purchase_purchased_credits_value_description,
                    uiState.purchasedCredits,
                    uiState.purchasedCredits,
                ),
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_success_uses_line),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
            PackPurchaseBackToPreview(actions = actions)
        }
        PackPurchaseStage.CANCELLED -> PackPurchaseNoteCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_cancelled_note),
                style = HhTheme.typography.bodyLarge,
                color = HhTheme.colors.onSurface,
            )
            HhOutlinedButton(
                onClick = actions.onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now_action),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
        }
        PackPurchaseStage.RESTORING -> PackPurchaseNoteCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_restoring),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            HhLoadingWheel(contentDesc = stringResource(R.string.feature_tailor_impl_pack_purchase_restoring))
        }
        PackPurchaseStage.RESTORED -> PackPurchaseNoteCard {
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_restored_heading),
                style = HhTheme.typography.titleLarge,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_restored_body),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        PackPurchaseStage.FAILED -> PackPurchaseFailureNote(
            reason = uiState.failureReason,
            actions = actions,
        )
        else -> Unit
    }
}

@Composable
private fun PackPurchaseNoteCard(content: @Composable ColumnScope.() -> Unit) {
    HhCard { content() }
}

@Composable
private fun PackPurchaseFailureNote(
    reason: PurchaseFailureReason?,
    actions: PackPurchaseActions,
) {
    PackPurchaseNoteCard {
        HhErrorCallout(
            title = stringResource(reason.titleRes()),
            supportingText = stringResource(reason.bodyRes()),
        )
        PackPurchaseTextLink(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_dismiss),
            onClick = actions.onDismiss,
        )
    }
}

@Composable
private fun PackPurchaseBackToPreview(actions: PackPurchaseActions) {
    HhButton(
        onClick = actions.onNotNow,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HhTheme.spacing.d48),
        text = {
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_back_to_preview),
                style = HhTheme.typography.labelLarge,
            )
        },
    )
}

@Composable
private fun PackPurchaseHeadline(uiState: PackPurchaseUiState) {
    val pack = uiState.selectedPack ?: return
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_eyebrow),
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_headline),
            style = HhTheme.typography.displaySmall,
            color = HhTheme.colors.onSurface,
        )
        HhHeroNumeral(
            value = pack.formattedPrice(),
            caption = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_applications_count,
                pack.credits,
                pack.credits,
            ),
            contentDescription = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_price_content_description,
                pack.credits,
                pack.credits,
                pack.formattedPrice(),
            ),
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_price_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PackPurchaseBalance(uiState: PackPurchaseUiState) {
    PackPurchaseSectionCard(
        eyebrow = stringResource(R.string.feature_tailor_impl_pack_purchase_balance_label),
        contentDescription = pluralStringResource(
            R.plurals.feature_tailor_impl_pack_purchase_balance_content_description,
            uiState.purchasedCredits,
            uiState.purchasedCredits,
            uiState.freeCredits,
        ),
    ) {
        val stacked = LocalDensity.current.fontScale > HH_STACK_FONT_SCALE
        if (stacked) {
            Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
                PackPurchaseBalanceNumeral(uiState = uiState)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xl),
            ) {
                PackPurchaseBalanceNumeral(uiState = uiState)
            }
        }
    }
}

@Composable
private fun PackPurchaseBalanceNumeral(uiState: PackPurchaseUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhHeroNumeral(
            value = uiState.freeCredits.toString(),
            caption = stringResource(R.string.feature_tailor_impl_pack_purchase_free_caption),
            contentDescription = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_free_credits_value_description,
                uiState.freeCredits,
                uiState.freeCredits,
            ),
        )
        HhDivider(style = HhDividerStyle.Dashed)
        HhHeroNumeral(
            value = uiState.purchasedCredits.toString(),
            caption = stringResource(R.string.feature_tailor_impl_pack_purchase_purchased_caption),
            contentDescription = pluralStringResource(
                R.plurals.feature_tailor_impl_pack_purchase_purchased_credits_value_description,
                uiState.purchasedCredits,
                uiState.purchasedCredits,
            ),
        )
    }
}

@Composable
private fun PackPurchaseSectionCard(
    eyebrow: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val spoken = contentDescription
    HhSectionCard(modifier = modifier) {
        val scope = this
        Text(
            text = eyebrow,
            style = HhTheme.typography.monoSmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (spoken != null) {
            Box(modifier = Modifier.semantics { this.contentDescription = spoken }) { scope.content() }
        } else {
            content()
        }
    }
}

@Composable
private fun PackPurchaseIncludes(pack: ApplicationPack) {
    PackPurchaseSectionCard(eyebrow = pack.name) {
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_includes),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
        )
        PackPurchaseBullet(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_not_subscription),
        )
        PackPurchaseBullet(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_recorded_on_device),
        )
        PackPurchaseBullet(text = expiryNoteFor(pack = pack))
    }
}

@Composable
private fun expiryNoteFor(pack: ApplicationPack): String = if (pack.creditsExpire) {
    stringResource(R.string.feature_tailor_impl_pack_purchase_pack_credits_expire)
} else {
    stringResource(R.string.feature_tailor_impl_pack_purchase_pack_credits_do_not_expire)
}

@Composable
private fun PackPurchaseBullet(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Box(
            modifier = Modifier
                .padding(top = HhTheme.spacing.sm)
                .size(HhTheme.spacing.d4)
                .background(color = HhTheme.colors.onSurface, shape = RoundedCornerShape(HhTheme.shapes.xs)),
        )
        Text(
            text = text,
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PackPurchaseAlternatives(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    if (uiState.otherPacks.isEmpty()) return
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        uiState.otherPacks.forEach { pack ->
            PackPurchaseAlternativeLink(
                pack = pack,
                isSelected = pack.id == uiState.selectedPackId,
                onSelect = { actions.onSelectPack(pack.id) },
            )
        }
    }
}

@Composable
private fun PackPurchaseAlternativeLink(
    pack: ApplicationPack,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    val label = pluralStringResource(
        R.plurals.feature_tailor_impl_pack_purchase_other_pack_action,
        pack.credits,
        pack.credits,
        pack.formattedPrice(),
    )
    val spoken = pluralStringResource(
        R.plurals.feature_tailor_impl_pack_purchase_other_pack_description,
        pack.credits,
        pack.credits,
        pack.formattedPrice(),
    )
    Text(
        text = label,
        style = HhTheme.typography.labelLarge,
        color = if (isSelected) HhTheme.colors.onSurfaceVariant else HhTheme.colors.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .defaultMinSize(minHeight = HhTheme.spacing.d48)
            .clickable(onClickLabel = spoken, onClick = onSelect)
            .padding(vertical = HhTheme.spacing.sm),
    )
}

@Composable
private fun PackPurchaseNoPacks() {
    HhCard {
        HhSpotIllustration(kind = HhSpotKind.Empty)
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_no_packs_heading),
            style = HhTheme.typography.titleLarge,
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_no_packs_body),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun PackPurchaseRestoreNote(actions: PackPurchaseActions) {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        HhDivider(style = HhDividerStyle.Dashed)
        Text(
            text = stringResource(R.string.feature_tailor_impl_pack_purchase_refunds_help_note),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        PackPurchaseTextLink(
            label = stringResource(R.string.feature_tailor_impl_pack_purchase_restore_action),
            onClick = actions.onRestore,
        )
    }
}

@Composable
private fun PackPurchaseBottomBar(
    uiState: PackPurchaseUiState,
    actions: PackPurchaseActions,
) {
    val pack = uiState.selectedPack
    HhBottomActionBar {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            if (pack != null) {
                HhButton(
                    onClick = { actions.onBuy(pack.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = HhTheme.spacing.d48),
                    text = {
                        Text(
                            text = pluralStringResource(
                                R.plurals.feature_tailor_impl_pack_purchase_buy_action,
                                pack.credits,
                                pack.credits,
                                pack.formattedPrice(),
                                expiryLabelFor(pack = pack),
                            ),
                            style = HhTheme.typography.labelLarge,
                        )
                    },
                )
            }
            HhOutlinedButton(
                onClick = actions.onNotNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now_action),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
            Text(
                text = stringResource(R.string.feature_tailor_impl_pack_purchase_not_now_note),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun expiryLabelFor(pack: ApplicationPack): String = if (pack.creditsExpire) {
    stringResource(R.string.feature_tailor_impl_pack_purchase_credits_expire)
} else {
    stringResource(R.string.feature_tailor_impl_pack_purchase_credits_never_expire)
}

@Composable
private fun PackPurchaseTextLink(
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

private fun PurchaseFailureReason?.titleRes(): Int = when (this) {
    PurchaseFailureReason.PaymentDeclined ->
        R.string.feature_tailor_impl_pack_purchase_failed_payment_declined_title
    PurchaseFailureReason.PurchaseUnavailable ->
        R.string.feature_tailor_impl_pack_purchase_failed_purchase_unavailable_title
    PurchaseFailureReason.PaymentUnavailable,
    null,
    -> R.string.feature_tailor_impl_pack_purchase_failed_payment_unavailable_title
}

private fun PurchaseFailureReason?.bodyRes(): Int = when (this) {
    PurchaseFailureReason.PaymentDeclined ->
        R.string.feature_tailor_impl_pack_purchase_failed_payment_declined_body
    PurchaseFailureReason.PurchaseUnavailable ->
        R.string.feature_tailor_impl_pack_purchase_failed_purchase_unavailable_body
    PurchaseFailureReason.PaymentUnavailable,
    null,
    -> R.string.feature_tailor_impl_pack_purchase_failed_payment_unavailable_body
}
