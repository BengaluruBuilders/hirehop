package com.hirehop.core.ui.component

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.hirehop.core.designsystem.component.HhApplicationStatusChip
import com.hirehop.core.designsystem.component.HhApplicationStatusKind
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhOutlineButton
import com.hirehop.core.designsystem.component.HhPrimaryButton
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
        title = title,
        subtitle = note,
    ) {
        if (eyebrow != null) {
            Text(
                text = eyebrow,
                style = HhTheme.typography.labelL,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
        ) {
            HhPrimaryButton(
                label = saveLabel,
                onClick = { onConfirm(selection.confirm()) },
                modifier = Modifier.weight(1f),
            )
            HhOutlineButton(
                label = cancelLabel,
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
            )
        }
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
            .heightIn(min = HhTheme.spacing.touch)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        ApplicationStatusSelectionRing(
            selected = selected,
            modifier = Modifier.padding(horizontal = HhTheme.spacing.xs),
        )
        HhApplicationStatusChip(kind = kind, label = option.label)
    }
}

@Composable
private fun ApplicationStatusSelectionRing(
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    val ringColor = if (selected) HhTheme.colors.primary else HhTheme.colors.outline
    Box(
        modifier = modifier
            .size(HhTheme.spacing.d20)
            .border(
                width = HhTheme.spacing.d2,
                color = ringColor,
                shape = HhTheme.shapes.pill,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(HhTheme.spacing.d12)
                .background(
                    color = if (selected) ringColor else Color.Transparent,
                    shape = HhTheme.shapes.pill,
                ),
        )
    }
}
