package com.hirehop.feature.onboarding.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.data.connectivity.ConnectivityMonitor
import com.hirehop.core.designsystem.component.HhDecoration
import com.hirehop.core.designsystem.component.HhDecorationKind
import com.hirehop.core.designsystem.component.HhHeroCard
import com.hirehop.core.designsystem.component.HhIconButton
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.illustration.HhCharacterIllustration
import com.hirehop.core.designsystem.illustration.HhIllustration
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.onboarding.impl.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal enum class NoticeTone { Neutral, Success, Error }

private val NOTICE_ICON_SIZE = 20.dp
private val DISCLOSURE_ICON_SIZE = 18.dp
private val STATE_ILLUSTRATION_SIZE = 160.dp
private val STATE_CARD_PADDING = 20.dp
private val NOTICE_BORDER = 2.dp

internal fun ConnectivityMonitor.observeOffline(forced: Boolean): Flow<Boolean> =
    isOnline.map { online -> forced || !online }

@Composable
internal fun OnboardingNotice(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.Neutral,
) {
    val colors = HhTheme.colors
    val shape = HhTheme.shapes.banner
    val container = when (tone) {
        NoticeTone.Neutral -> colors.neutralContainer
        NoticeTone.Success -> colors.metContainer
        NoticeTone.Error -> colors.card
    }
    val content = if (tone == NoticeTone.Success) colors.onMetContainer else colors.onSurface
    val tint = when (tone) {
        NoticeTone.Neutral -> colors.onNeutralContainer
        NoticeTone.Success -> colors.onMetContainer
        NoticeTone.Error -> colors.error
    }
    val framed = if (tone == NoticeTone.Error) Modifier.border(NOTICE_BORDER, colors.error, shape) else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .then(framed)
            .background(container, shape)
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(NOTICE_ICON_SIZE))
        Text(text = text, style = HhTheme.typography.bodyM, color = content, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun DisclosureCard(
    text: AnnotatedString,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val shape = HhTheme.shapes.card
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.document, shape)
            .border(HhTheme.spacing.d2 / 2, colors.outlineVariant, shape)
            .padding(horizontal = HhTheme.spacing.cardPadding, vertical = HhTheme.spacing.sm + HhTheme.spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm + HhTheme.spacing.xxs),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.padding(top = HhTheme.spacing.xxs).size(DISCLOSURE_ICON_SIZE),
        )
        Text(text = text, style = HhTheme.typography.bodyM, color = colors.onSurface, modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun ReasonText(
    text: String,
    modifier: Modifier = Modifier,
    emphasis: IntRange? = null,
) {
    val styled = buildAnnotatedString {
        append(text)
        if (emphasis != null) {
            addStyle(SpanStyle(fontWeight = FontWeight.Bold), emphasis.first, emphasis.last + 1)
        }
    }
    Text(
        text = styled,
        modifier = modifier.fillMaxWidth().padding(horizontal = HhTheme.spacing.sm),
        style = HhTheme.typography.labelM,
        color = HhTheme.colors.onSurface,
    )
}

@Composable
internal fun StateCard(
    illustration: HhIllustration,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    illustrationDescription: String? = null,
    extra: (@Composable () -> Unit)? = null,
) {
    HhHeroCard(
        modifier = modifier,
        contentPadding = PaddingValues(STATE_CARD_PADDING),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d12 + HhTheme.spacing.xxs),
        ) {
            Box(
                modifier = Modifier
                    .size(STATE_ILLUSTRATION_SIZE)
                    .clip(HhTheme.shapes.pill)
                    .background(HhTheme.colors.primaryContainer),
                contentAlignment = Alignment.BottomCenter,
            ) {
                HhCharacterIllustration(
                    illustration = illustration,
                    contentDescription = illustrationDescription,
                    modifier = Modifier.size(STATE_ILLUSTRATION_SIZE),
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                Text(
                    text = title,
                    style = HhTheme.typography.titleL,
                    color = HhTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = body,
                    style = HhTheme.typography.bodyM,
                    color = HhTheme.colors.body,
                    textAlign = TextAlign.Center,
                )
                if (extra != null) {
                    extra()
                }
            }
        }
    }
}

@Composable
internal fun MessageCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    HhHeroCard(modifier = modifier, contentPadding = PaddingValues(HhTheme.spacing.xl)) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            Text(text = title, style = HhTheme.typography.titleL, color = HhTheme.colors.onSurface)
            Text(text = body, style = HhTheme.typography.bodyL, color = HhTheme.colors.body)
        }
    }
}

private val STEP_SQUIGGLE_WIDTH = 58.dp
private val STEP_SQUIGGLE_HEIGHT = 16.dp
private val STEP_CHIP_HEIGHT = 32.dp

@Composable
internal fun OnboardingStepBar(
    step: Int?,
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    backContentDescription: String = "",
    onBrand: Boolean = false,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(HhTheme.spacing.touch),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            HhIconButton(
                icon = HhIcons.ArrowBack,
                contentDescription = backContentDescription,
                onClick = onBack,
                containerColor = if (onBrand) HhTheme.colors.onBrand else HhTheme.colors.surface,
                borderColor = if (onBrand) Color.Transparent else HhTheme.colors.outlineVariant,
            )
        } else {
            Box(modifier = Modifier.width(HhTheme.spacing.touch))
        }
        if (step != null) {
            HhDecoration(
                kind = HhDecorationKind.Squiggle,
                color = HhTheme.colors.coral,
                modifier = Modifier.size(width = STEP_SQUIGGLE_WIDTH, height = STEP_SQUIGGLE_HEIGHT),
            )
            Box(
                modifier = Modifier
                    .height(STEP_CHIP_HEIGHT)
                    .background(
                        if (onBrand) HhTheme.colors.brandPressed else HhTheme.colors.primaryContainer,
                        HhTheme.shapes.pill,
                    )
                    .padding(horizontal = HhTheme.spacing.d12 + HhTheme.spacing.xxs),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.feature_onboarding_impl_step_chip, step, ONBOARDING_STEP_COUNT),
                    style = HhTheme.typography.labelL,
                    color = if (onBrand) HhTheme.colors.onBrand else HhTheme.colors.onPrimaryContainer,
                )
            }
        }
    }
}

internal const val ONBOARDING_STEP_COUNT = 5
