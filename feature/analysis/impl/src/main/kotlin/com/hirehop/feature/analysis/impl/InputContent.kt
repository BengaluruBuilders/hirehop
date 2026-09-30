package com.hirehop.feature.analysis.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.designsystem.component.HhTextField
import com.hirehop.core.designsystem.component.HhTrustChip
import com.hirehop.core.designsystem.component.HhTrustKind
import com.hirehop.core.designsystem.theme.HhTheme
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
            .padding(screenPadding()),
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        Text(
            text = stringResource(R.string.feature_analysis_impl_input_hint),
            modifier = Modifier.fillMaxWidth(),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        HhTextField(
            value = state.jobText,
            onValueChange = onJobTextChange,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.feature_analysis_impl_input_label),
            placeholder = stringResource(R.string.feature_analysis_impl_input_placeholder),
            singleLine = false,
            minLines = 10,
        )
        CountAndPasteRow(characterCount = state.jobText.length, onPaste = onJobTextChange)
        HhButton(
            onClick = onAnalyze,
            enabled = state.canAnalyze,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = HhTheme.spacing.d48),
            text = {
                Text(
                    text = stringResource(R.string.feature_analysis_impl_analyze),
                    style = HhTheme.typography.labelLarge,
                )
            },
        )
        HhTrustChip(
            kind = HhTrustKind.NeverInvents,
            label = stringResource(R.string.feature_analysis_impl_trust_no_invent),
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
                R.string.feature_analysis_impl_character_count,
                characterCount,
                MAX_JOB_TEXT_LENGTH,
            ),
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
        PasteButton(onPaste)
    }
}

@Composable
private fun PasteButton(onPaste: (String) -> Unit) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    HhOutlinedButton(
        onClick = { scope.launch { clipboard.readText()?.let(onPaste) } },
        modifier = Modifier.heightIn(min = HhTheme.spacing.d48),
        text = {
            Text(
                text = stringResource(R.string.feature_analysis_impl_paste),
                style = HhTheme.typography.labelLarge,
            )
        },
    )
}

private suspend fun Clipboard.readText(): String? = getClipEntry()
    ?.clipData
    ?.takeIf { it.itemCount > 0 }
    ?.getItemAt(0)
    ?.text
    ?.toString()
