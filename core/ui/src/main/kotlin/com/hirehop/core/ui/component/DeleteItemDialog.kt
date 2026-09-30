package com.hirehop.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.theme.HhTheme

enum class DeleteItemKind {
    PROFILE_FACT,
    APPLICATION,
}

enum class DeleteItemActionLayout {
    FULL_WIDTH_EQUAL,
}

@Immutable
data class DeleteItemAction(
    val label: String,
    val isDestructive: Boolean,
    val layout: DeleteItemActionLayout,
)

@Immutable
data class DeleteItemDialogCopy(
    val titlePrefix: String,
    val bodyIntro: String,
    val notePrefix: String,
    val sharedFilesNote: String,
    val keepActionLabel: String,
    val deleteActionLabel: String,
)

@Immutable
data class DeleteItemDialogSpec(
    val kind: DeleteItemKind,
    val subject: String,
    val deletedParts: List<String>,
    val keptCount: Int,
    val keptNounSingular: String,
    val keptNounPlural: String,
    val keptVerbSingular: String,
    val keptVerbPlural: String,
    val copy: DeleteItemDialogCopy,
) {
    val keptNoun: String
        get() = if (keptCount == 1) keptNounSingular else keptNounPlural

    val keptVerb: String
        get() = if (keptCount == 1) keptVerbSingular else keptVerbPlural
}

@Immutable
data class DeleteItemDialogContent(
    val kind: DeleteItemKind,
    val title: String,
    val body: String,
    val note: String,
    val actions: List<DeleteItemAction>,
) {
    val keepAction: DeleteItemAction
        get() = actions.first()

    val deleteAction: DeleteItemAction
        get() = actions.last()
}

fun DeleteItemDialogSpec.toContent(): DeleteItemDialogContent = DeleteItemDialogContent(
    kind = kind,
    title = "${copy.titlePrefix} $subject?",
    body = "${copy.bodyIntro} ${deletedParts.asSentence()}.",
    note = "${copy.notePrefix} $keptCount $keptNoun $keptVerb. ${copy.sharedFilesNote}",
    actions = listOf(
        DeleteItemAction(
            label = copy.keepActionLabel,
            isDestructive = false,
            layout = DeleteItemActionLayout.FULL_WIDTH_EQUAL,
        ),
        DeleteItemAction(
            label = copy.deleteActionLabel,
            isDestructive = true,
            layout = DeleteItemActionLayout.FULL_WIDTH_EQUAL,
        ),
    ),
)

private fun List<String>.asSentence(): String = when (size) {
    0 -> ""
    1 -> first()
    2 -> "${first()} and ${last()}"
    else -> dropLast(1).joinToString(separator = ", ") + ", and ${last()}"
}

@Composable
fun DeleteItemDialog(
    content: DeleteItemDialogContent,
    onKeep: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = HhTheme.colors
    val shape = RoundedCornerShape(HhTheme.shapes.lg)
    Dialog(
        onDismissRequest = onKeep,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(HhTheme.spacing.d24)
                .background(color = colors.surface, shape = shape)
                .border(width = 1.dp, color = colors.hairline, shape = shape)
                .padding(HhTheme.spacing.d24),
            verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            Text(
                text = content.title,
                style = HhTheme.typography.displaySmall,
                color = colors.onSurface,
            )
            Text(
                text = content.body,
                style = HhTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
            )
            Text(
                text = content.note,
                style = HhTheme.typography.titleSmall,
                color = colors.onSurface,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.d8),
            ) {
                content.actions.forEach { action ->
                    if (action.isDestructive) {
                        DestructiveActionButton(
                            label = action.label,
                            onClick = onDelete,
                        )
                    } else {
                        HhOutlinedButton(
                            onClick = onKeep,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = HhTheme.spacing.d48),
                        ) {
                            Text(
                                text = action.label,
                                style = HhTheme.typography.titleMedium,
                                color = colors.onSurface,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DestructiveActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = HhTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = HhTheme.spacing.d48)
            .background(
                color = if (enabled) colors.error else colors.surface3,
                shape = RoundedCornerShape(HhTheme.shapes.full),
            )
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = HhTheme.spacing.d12, vertical = HhTheme.spacing.md),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = HhTheme.typography.titleMedium,
            color = if (enabled) colors.onError else colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}
