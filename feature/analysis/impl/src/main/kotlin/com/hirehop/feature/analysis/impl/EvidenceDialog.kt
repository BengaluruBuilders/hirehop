package com.hirehop.feature.analysis.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
internal fun EvidenceDialog(
    requirementText: String,
    keywords: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var statement by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_analysis_evidence_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = requirementText, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(R.string.feature_analysis_evidence_dialog_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedTextField(
                    value = statement,
                    onValueChange = { statement = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.feature_analysis_evidence_dialog_label)) },
                    minLines = 2,
                )
                EvidenceDisclosure(keywords = keywords, statement = statement.trim())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(statement) },
                enabled = statement.isNotBlank(),
            ) {
                Text(stringResource(R.string.feature_analysis_evidence_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_analysis_cancel))
            }
        },
    )
}

@Composable
private fun EvidenceDisclosure(keywords: List<String>, statement: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (keywords.isNotEmpty()) {
            Text(
                text = stringResource(R.string.feature_analysis_dialog_adds_skills, keywords.joinToString()),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (statement.isNotEmpty()) {
            Text(
                text = stringResource(R.string.feature_analysis_dialog_adds_line, statement),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
