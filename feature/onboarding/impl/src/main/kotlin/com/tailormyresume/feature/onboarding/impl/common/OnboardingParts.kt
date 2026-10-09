package com.tailormyresume.feature.onboarding.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.data.connectivity.ConnectivityMonitor
import com.tailormyresume.core.designsystem.component.TmrBackButton
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal enum class NoticeTone { Neutral, Success, Warning, Error }

private const val STACKED_FONT_SCALE = 1.5f
private val STACKED_MAX_SCREEN_HEIGHT = 560.dp
private val NOTICE_ICON_SIZE = 20.dp
private val DISCLOSURE_ICON_SIZE = 18.dp
private val STATE_CIRCLE_SIZE = 88.dp
private val STATE_ICON_SIZE = 40.dp

@Composable
internal fun rememberIsStacked(): Boolean {
    val density = LocalDensity.current
    val windowHeight = with(density) { LocalWindowInfo.current.containerSize.height.toDp() }
    return density.fontScale >= STACKED_FONT_SCALE || windowHeight < STACKED_MAX_SCREEN_HEIGHT
}

@Composable
internal fun stackedHyphens(): Hyphens = if (rememberIsStacked()) Hyphens.Auto else Hyphens.Unspecified

internal fun ConnectivityMonitor.observeOffline(forced: Boolean): Flow<Boolean> =
    isOnline.map { online -> forced || !online }

@Composable
internal fun OnboardingNotice(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.Neutral,
) {
    val colors = TmrTheme.colors
    val shape = TmrTheme.shapes.banner
    val container = when (tone) {
        NoticeTone.Neutral -> colors.primaryContainer
        NoticeTone.Success -> colors.metContainer
        NoticeTone.Warning -> colors.partialContainer
        NoticeTone.Error -> colors.errorContainer
    }
    val tint = when (tone) {
        NoticeTone.Neutral -> colors.onSurface
        NoticeTone.Success -> colors.met
        NoticeTone.Warning -> colors.partial
        NoticeTone.Error -> colors.error
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite }
            .background(container, shape)
            .padding(horizontal = TmrTheme.spacing.cardPadding, vertical = TmrTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(NOTICE_ICON_SIZE))
        Text(
            text = text,
            style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.Bold, hyphens = stackedHyphens()),
            color = colors.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun DisclosureCard(
    text: AnnotatedString,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    val colors = TmrTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onSurfaceVariant,
            modifier = Modifier.size(DISCLOSURE_ICON_SIZE),
        )
        Text(
            text = text,
            style = TmrTheme.typography.bodyM.copy(fontWeight = FontWeight.SemiBold, hyphens = stackedHyphens()),
            color = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
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
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = TmrIcons.Info,
            contentDescription = null,
            tint = TmrTheme.colors.onSurfaceVariant,
            modifier = Modifier.size(DISCLOSURE_ICON_SIZE),
        )
        Text(
            text = styled,
            modifier = Modifier.weight(1f),
            style = TmrTheme.typography.labelM.copy(hyphens = stackedHyphens()),
            color = TmrTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
internal fun StateCard(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.Neutral,
    extra: (@Composable () -> Unit)? = null,
) {
    val colors = TmrTheme.colors
    val container = when (tone) {
        NoticeTone.Neutral -> colors.card
        NoticeTone.Success -> colors.metContainer
        NoticeTone.Warning -> colors.partialContainer
        NoticeTone.Error -> colors.errorContainer
    }
    val tint = when (tone) {
        NoticeTone.Neutral -> colors.onSurface
        NoticeTone.Success -> colors.met
        NoticeTone.Warning -> colors.partial
        NoticeTone.Error -> colors.error
    }
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = TmrTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier.size(STATE_CIRCLE_SIZE).background(container, TmrTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(STATE_ICON_SIZE))
        }
        TmrHeadline(text = title, style = TmrTheme.typography.headlineL, color = colors.onSurface)
        Text(
            text = body,
            style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (extra != null) {
            extra()
        }
    }
}

@Composable
internal fun OnboardingStepBar(
    onBack: (() -> Unit)?,
    modifier: Modifier = Modifier,
    backContentDescription: String = "",
    title: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = TmrTheme.spacing.touch + TmrTheme.spacing.md),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            TmrBackButton(contentDescription = backContentDescription, onClick = onBack)
        }
        if (title != null) {
            Text(
                text = title,
                modifier = Modifier.weight(1f).padding(vertical = TmrTheme.spacing.sm).semantics { heading() },
                style = TmrTheme.typography.titleL,
                color = TmrTheme.colors.onSurface,
            )
        }
    }
}
