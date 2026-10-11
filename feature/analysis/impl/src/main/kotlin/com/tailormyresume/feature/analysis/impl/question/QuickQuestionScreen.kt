package com.tailormyresume.feature.analysis.impl.question

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun QuickQuestionScreen(
    state: QuickQuestionUiState.Ready,
    onPick: (QuickChoice) -> Unit,
    onDetailChange: (String) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) = Unit
