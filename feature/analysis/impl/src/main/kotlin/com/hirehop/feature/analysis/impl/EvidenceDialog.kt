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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.domain.keywordsStatedIn
import com.hirehop.core.model.JobRequirement

@Composable
internal fun EvidenceDialog(
    requirement: JobRequirement,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var statement by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.feature_analysis_evidence_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = requirement.text, style = MaterialTheme.typography.titleSmall)
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
                EvidenceDisclosure(requirement = requirement, statement = statement.trim())
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
private fun EvidenceDisclosure(requirement: JobRequirement, statement: String) {
    val statedKeywords = remember(requirement, statement) { keywordsStatedIn(requirement, statement) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = if (statedKeywords.isEmpty()) {
                stringResource(R.string.feature_analysis_dialog_adds_no_skills)
            } else {
                stringResource(R.string.feature_analysis_dialog_adds_skills, statedKeywords.joinToString())
            },
            style = MaterialTheme.typography.bodySmall,
        )
        if (statement.isNotEmpty()) {
            Text(
                text = stringResource(R.string.feature_analysis_dialog_adds_line, statement),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
