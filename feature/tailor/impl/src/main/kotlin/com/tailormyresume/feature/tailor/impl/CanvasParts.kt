package com.tailormyresume.feature.tailor.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.component.TmrSpotKind
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme

internal enum class BannerTone { Neutral, Ok, Warn }

@Composable
internal fun NoticeStrip(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.Neutral,
) {
    val colors = TmrTheme.colors
    val container = when (tone) {
        BannerTone.Neutral -> colors.primaryContainer
        BannerTone.Ok -> colors.metContainer
        BannerTone.Warn -> colors.partialContainer
    }
    val tint = when (tone) {
        BannerTone.Neutral -> colors.onSurface
        BannerTone.Ok -> colors.met
        BannerTone.Warn -> colors.partial
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(container, TmrTheme.shapes.banner)
            .padding(horizontal = TmrTheme.spacing.lg, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Text(text = text, style = TmrTheme.typography.titleS, color = colors.onSurface)
    }
}

@Composable
internal fun NoteLine(text: String, icon: ImageVector, modifier: Modifier = Modifier) {
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
            modifier = Modifier.size(18.dp),
        )
        Text(text = text, style = TmrTheme.typography.bodyS, color = colors.onSurfaceVariant)
    }
}

@Composable
internal fun StatusPill(label: String, icon: ImageVector?, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .defaultMinSize(minHeight = 26.dp)
            .background(TmrTheme.colors.primaryContainer, TmrTheme.shapes.pill)
            .padding(start = if (icon != null) 7.dp else 10.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        }
        Text(
            text = label,
            style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold),
            color = color,
        )
    }
}

@Composable
internal fun ProgressBar(fraction: Float, modifier: Modifier = Modifier) {
    val colors = TmrTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction.coerceIn(0f, 1f), 0f..1f) }
            .background(colors.primaryContainer, TmrTheme.shapes.pill),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(8.dp)
                .background(colors.brand, TmrTheme.shapes.pill),
        )
    }
}

internal enum class StepMark { Done, Now, Waiting }

internal data class GenerationStep(val title: String, val detail: String?, val status: String, val mark: StepMark)

@Composable
internal fun GenerationSteps(steps: List<GenerationStep>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
        steps.forEach { step -> GenerationStepRow(step) }
    }
}

@Composable
private fun GenerationStepRow(step: GenerationStep) {
    val colors = TmrTheme.colors
    val description = listOfNotNull(step.title, step.detail, step.status).joinToString(". ")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.card, TmrTheme.shapes.statusRow)
            .defaultMinSize(minHeight = 64.dp)
            .padding(horizontal = TmrTheme.spacing.lg, vertical = TmrTheme.spacing.md)
            .clearAndSetSemantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepMarkIcon(step.mark)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                style = TmrTheme.typography.titleS,
                color = if (step.mark == StepMark.Waiting) colors.onSurfaceVariant else colors.onSurface,
            )
            if (step.detail != null) {
                Text(text = step.detail, style = TmrTheme.typography.bodyS, color = colors.onSurfaceVariant)
            }
        }
        Text(text = step.status, style = TmrTheme.typography.labelM, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun StepMarkIcon(mark: StepMark) {
    val colors = TmrTheme.colors
    when (mark) {
        StepMark.Done -> Box(
            modifier = Modifier.size(24.dp).background(colors.brand, TmrTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(TmrIcons.Check, contentDescription = null, tint = colors.onBrand, modifier = Modifier.size(16.dp))
        }
        StepMark.Now -> Box(Modifier.size(24.dp).border(3.dp, colors.primary, TmrTheme.shapes.pill))
        StepMark.Waiting -> Box(Modifier.size(24.dp).border(2.dp, colors.outlineVariant, TmrTheme.shapes.pill))
    }
}

@Composable
internal fun StatusCard(kind: TmrSpotKind, title: String, body: String, modifier: Modifier = Modifier) {
    val colors = TmrTheme.colors
    val isError = kind == TmrSpotKind.Error
    Column(
        modifier = modifier.fillMaxWidth().padding(top = TmrTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(if (isError) colors.errorContainer else colors.primaryContainer, TmrTheme.shapes.pill),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = kind.messageIcon(),
                contentDescription = null,
                tint = if (isError) colors.error else colors.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        }
        TmrHeadline(
            text = title,
            style = TmrTheme.typography.headlineL,
            modifier = Modifier.fillMaxWidth(),
            color = colors.onSurface,
        )
        Text(
            text = body,
            style = TmrTheme.typography.bodyL,
            color = colors.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private fun TmrSpotKind.messageIcon(): ImageVector = when (this) {
    TmrSpotKind.Error -> TmrIcons.Error
    TmrSpotKind.Offline -> TmrIcons.Offline
    else -> TmrIcons.Info
}
