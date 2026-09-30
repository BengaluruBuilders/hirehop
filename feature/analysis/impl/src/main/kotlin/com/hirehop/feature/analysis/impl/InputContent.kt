package com.hirehop.feature.analysis.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import kotlinx.coroutines.launch

@Composable
internal fun InputContent(
    state: AnalysisUiState.Input,
    onJobTextChange: (String) -> Unit,
    onAnalyze: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.feature_analysis_input_hint),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = state.jobText,
            onValueChange = onJobTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.feature_analysis_input_label)) },
            placeholder = { Text(stringResource(R.string.feature_analysis_input_placeholder)) },
            minLines = 10,
            maxLines = 18,
        )
        CountAndPasteRow(state.jobText.length, onPaste = onJobTextChange)
        HhButton(
            onClick = onAnalyze,
            enabled = state.canAnalyze,
            modifier = Modifier.fillMaxWidth(),
            text = { Text(stringResource(R.string.feature_analysis_analyze)) },
        )
    }
}

@Composable
private fun CountAndPasteRow(characterCount: Int, onPaste: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                R.string.feature_analysis_character_count,
                characterCount,
                MAX_JOB_TEXT_LENGTH,
            ),
            style = MaterialTheme.typography.labelMedium,
        )
        PasteButton(onPaste)
    }
}

@Composable
private fun PasteButton(onPaste: (String) -> Unit) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    TextButton(
        onClick = {
            scope.launch { clipboard.readText()?.let(onPaste) }
        },
    ) {
        Text(stringResource(R.string.feature_analysis_paste))
    }
}

private suspend fun Clipboard.readText(): String? = getClipEntry()
    ?.clipData
    ?.takeIf { it.itemCount > 0 }
    ?.getItemAt(0)
    ?.text
    ?.toString()
