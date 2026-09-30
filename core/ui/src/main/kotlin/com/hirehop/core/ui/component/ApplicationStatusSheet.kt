package com.hirehop.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhApplicationStatusKind
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.ui.ApplicationStatusKindMapper

@Stable
data class ApplicationStatusOption(
    val status: ApplicationStatus,
    val label: String,
)

fun applicationStatusOptions(labelOf: (ApplicationStatus) -> String): List<ApplicationStatusOption> =
    ApplicationStatus.entries.map { status ->
        ApplicationStatusOption(status = status, label = labelOf(status))
    }

@Stable
class ApplicationStatusSelection(initial: ApplicationStatus) {

    var selected: ApplicationStatus by mutableStateOf(initial)
        private set

    fun select(status: ApplicationStatus) {
        selected = status
    }

    fun confirm(): ApplicationStatus = selected
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationStatusSheet(
    current: ApplicationStatus,
    options: List<ApplicationStatusOption>,
    onConfirm: (ApplicationStatus) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    saveLabel: String,
    cancelLabel: String,
    eyebrow: String? = null,
    title: String? = null,
    note: String? = null,
    selection: ApplicationStatusSelection = remember(current) { ApplicationStatusSelection(current) },
) {
    val kindMapper = ApplicationStatusKindMapper()
    HhBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        if (eyebrow != null) {
            Text(
                text = eyebrow,
                style = HhTheme.typography.monoSmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        if (title != null) {
            Text(
                text = title,
                style = HhTheme.typography.displaySmall,
                color = HhTheme.colors.onSurface,
            )
        }
        options.forEach { option ->
            ApplicationStatusOptionRow(
                option = option,
                kind = kindMapper.kindOf(option.status),
                selected = selection.selected == option.status,
                onSelect = { selection.select(option.status) },
            )
        }
        if (note != null) {
            Text(
                text = note,
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        ApplicationStatusSheetButtons(
            saveLabel = saveLabel,
            cancelLabel = cancelLabel,
            onConfirm = { onConfirm(selection.confirm()) },
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun ApplicationStatusSheetButtons(
    saveLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        HhButton(
            onClick = onConfirm,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = saveLabel,
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
        HhOutlinedButton(
            onClick = onDismiss,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = cancelLabel,
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
    }
}

@Composable
private fun ApplicationStatusOptionRow(
    option: ApplicationStatusOption,
    kind: HhApplicationStatusKind,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = HhTheme.spacing.d48)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .background(
                color = if (selected) HhTheme.colors.primaryContainer else Color.Transparent,
                shape = RoundedCornerShape(HhTheme.shapes.sm),
            )
            .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        ApplicationStatusMark(kind = kind)
        Text(
            text = option.label,
            style = HhTheme.typography.bodyLarge,
            color = HhTheme.colors.onSurface,
            modifier = Modifier.weight(1f),
        )
        ApplicationStatusSelectionRing(selected = selected)
    }
}

@Composable
private fun ApplicationStatusMark(
    kind: HhApplicationStatusKind,
    modifier: Modifier = Modifier,
) {
    val mark = when (kind) {
        HhApplicationStatusKind.Applied,
        HhApplicationStatusKind.Interview,
        HhApplicationStatusKind.Offer,
        -> HhTheme.colors.primary

        HhApplicationStatusKind.Saved,
        HhApplicationStatusKind.Rejected,
        HhApplicationStatusKind.NoResponse,
        -> HhTheme.colors.onSurfaceVariant
    }
    Canvas(modifier = modifier.size(HhTheme.spacing.d8)) {
        drawApplicationStatusMark(kind = kind, mark = mark)
    }
}

private fun DrawScope.drawApplicationStatusMark(
    kind: HhApplicationStatusKind,
    mark: Color,
) {
    val stroke = MARK_STROKE.toPx()
    val half = size.minDimension / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    val ringRadius = half - stroke / 2f
    when (kind) {
        HhApplicationStatusKind.Saved -> drawRect(
            color = mark,
            topLeft = Offset(center.x - half, center.y - half),
            size = Size(half * 2f, half * 2f),
            style = Stroke(width = stroke),
        )

        HhApplicationStatusKind.Applied -> drawCircle(
            color = mark,
            radius = ringRadius,
            center = center,
            style = Stroke(width = stroke),
        )

        HhApplicationStatusKind.Interview -> {
            drawCircle(
                color = mark,
                radius = ringRadius,
                center = center,
                style = Stroke(width = stroke),
            )
            val inner = half - stroke
            drawArc(
                color = mark,
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(center.x - inner, center.y - inner),
                size = Size(inner * 2f, inner * 2f),
                style = Fill,
            )
        }

        HhApplicationStatusKind.Offer -> drawCircle(color = mark, radius = half, center = center)
        HhApplicationStatusKind.Rejected -> drawCircle(color = mark, radius = half, center = center)

        HhApplicationStatusKind.NoResponse -> drawCircle(
            color = mark,
            radius = ringRadius,
            center = center,
            style = Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(stroke * 1.6f, stroke * 1.2f)),
            ),
        )
    }
}

@Composable
private fun ApplicationStatusSelectionRing(selected: Boolean) {
    Box(
        modifier = Modifier
            .size(HhTheme.spacing.d20)
            .border(
                width = MARK_RING_STROKE,
                color = if (selected) HhTheme.colors.primary else HhTheme.colors.onSurfaceVariant,
                shape = RoundedCornerShape(HhTheme.shapes.full),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(HhTheme.spacing.d12)
                    .background(
                        color = HhTheme.colors.primary,
                        shape = RoundedCornerShape(HhTheme.shapes.full),
                    ),
            )
        }
    }
}

private val MARK_STROKE = 1.5.dp

private val MARK_RING_STROKE = 2.dp
