package com.hirehop.feature.applications.impl

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.component.HhConfirmDialog
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
internal fun ApplicationDeleteDialog(
    jobTitle: String,
    company: String,
    scope: WorkspaceDeleteScope,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HhConfirmDialog(
        title = stringResource(
            id = R.string.feature_applications_impl_delete_title,
            jobTitle,
            company,
        ),
        message = deleteDialogMessage(scope),
        confirmLabel = stringResource(R.string.feature_applications_impl_delete_confirm),
        cancelLabel = stringResource(R.string.feature_applications_impl_delete_keep),
        onConfirm = onConfirm,
        onCancel = onCancel,
        modifier = modifier,
        destructive = true,
    )
}

@Composable
private fun deleteDialogMessage(scope: WorkspaceDeleteScope): String {
    val items = buildList {
        if (scope.hasJobDescription) add(stringResource(R.string.feature_applications_impl_delete_scope_jd))
        if (scope.hasGapAnalysis) add(stringResource(R.string.feature_applications_impl_delete_scope_gap))
        if (scope.hasTailoredResume) add(stringResource(R.string.feature_applications_impl_delete_scope_resume))
        if (scope.prepTaskCount > 0) {
            add(
                pluralStringResource(
                    id = R.plurals.feature_applications_impl_delete_scope_tasks,
                    scope.prepTaskCount,
                    scope.prepTaskCount,
                ),
            )
        }
        if (scope.hasNotes) add(stringResource(R.string.feature_applications_impl_delete_scope_notes))
    }
    val separator = stringResource(R.string.feature_applications_impl_delete_scope_separator_and)
    val scopeSentence = when {
        items.isEmpty() -> stringResource(R.string.feature_applications_impl_delete_scope_none)
        items.size == 1 -> items.first()
        else -> items.dropLast(1).joinToString(separator = ", ") + separator + items.last()
    }
    val note = stringResource(
        id = R.string.feature_applications_impl_delete_note,
        pluralStringResource(
            id = R.plurals.feature_applications_impl_delete_note_facts,
            scope.profileFactCount,
            scope.profileFactCount,
        ),
        pluralStringResource(
            id = R.plurals.feature_applications_impl_delete_note_credits,
            scope.creditCount,
            scope.creditCount,
        ),
    )
    return stringResource(R.string.feature_applications_impl_delete_message, scopeSentence, note)
}

@Preview(showBackground = true)
@Composable
private fun ApplicationDeleteDialogPreview() {
    HhTheme(darkTheme = false) {
        ApplicationDeleteDialog(
            jobTitle = NORTHWIND_ROLE,
            company = NORTHWIND_COMPANY,
            scope = WorkspaceDeleteScope(
                hasJobDescription = true,
                hasGapAnalysis = true,
                hasTailoredResume = true,
                hasNotes = true,
                prepTaskCount = 3,
                profileFactCount = 18,
                creditCount = 4,
            ),
            onConfirm = {},
            onCancel = {},
        )
    }
}
