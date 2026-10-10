package com.tailormyresume.feature.analysis.impl.job

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun JobScreen(
    state: JobUiState,
    onTextChange: (String) -> Unit,
    onPaste: () -> Unit,
    onUseLink: () -> Unit,
    onClear: () -> Unit,
    onAnalyze: () -> Unit,
    modifier: Modifier = Modifier,
) = Unit
