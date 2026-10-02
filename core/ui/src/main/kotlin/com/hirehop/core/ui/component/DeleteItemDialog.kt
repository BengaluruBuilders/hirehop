package com.hirehop.core.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import com.hirehop.core.designsystem.component.HhConfirmDialog
import com.hirehop.core.designsystem.component.HhDestructiveButton

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
    HhConfirmDialog(
        title = content.title,
        message = "${content.body}\n\n${content.note}",
        confirmLabel = content.deleteAction.label,
        cancelLabel = content.keepAction.label,
        onConfirm = onDelete,
        onCancel = onKeep,
        modifier = modifier,
        destructive = true,
    )
}

@Composable
fun DestructiveActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    HhDestructiveButton(
        label = label,
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
    )
}
