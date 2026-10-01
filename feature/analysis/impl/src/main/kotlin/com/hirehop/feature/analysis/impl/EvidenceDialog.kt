package com.hirehop.feature.analysis.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhBottomSheet
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.domain.keywordsStatedIn
import com.hirehop.core.model.JobRequirement

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
internal fun EvidenceDialog(
    requirement: JobRequirement,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var statement by rememberSaveable { mutableStateOf("") }
    HhBottomSheet(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.feature_analysis_impl_evidence_dialog_title),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
            Text(
                text = requirement.text,
                modifier = Modifier.fillMaxWidth(),
                style = HhTheme.typography.titleMedium,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = stringResource(R.string.feature_analysis_impl_evidence_dialog_message),
                modifier = Modifier.fillMaxWidth(),
                style = HhTheme.typography.bodyMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
            HhTextField(
                value = statement,
                onValueChange = { statement = it },
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(R.string.feature_analysis_impl_evidence_dialog_label),
                singleLine = false,
                minLines = 3,
            )
            EvidenceDisclosure(requirement = requirement, statement = statement.trim())
            HhButton(
                onClick = { onConfirm(statement) },
                enabled = statement.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_analysis_impl_evidence_dialog_confirm),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
            HhOutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = HhTheme.spacing.d48),
                text = {
                    Text(
                        text = stringResource(R.string.feature_analysis_impl_cancel),
                        style = HhTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}

@Composable
private fun EvidenceDisclosure(requirement: JobRequirement, statement: String) {
    val statedKeywords = remember(requirement, statement) { keywordsStatedIn(requirement, statement) }
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
        Text(
            text = if (statedKeywords.isEmpty()) {
                stringResource(R.string.feature_analysis_impl_dialog_adds_no_skills)
            } else {
                stringResource(R.string.feature_analysis_impl_dialog_adds_skills, statedKeywords.joinToString())
            },
            modifier = Modifier.fillMaxWidth(),
            style = HhTheme.typography.bodySmall,
            color = HhTheme.colors.onSurfaceVariant,
        )
        if (statement.isNotEmpty()) {
            Text(
                text = stringResource(R.string.feature_analysis_impl_dialog_adds_line, statement),
                modifier = Modifier.fillMaxWidth(),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
    }
}
