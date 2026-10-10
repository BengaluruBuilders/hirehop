package com.tailormyresume.feature.tailor.impl.result

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal enum class TailoredTab { Resume, Changes }

@Composable
internal fun TailoredScreen(
    state: TailoredUiState.Ready,
    tab: TailoredTab,
    onTabChange: (TailoredTab) -> Unit,
    onUndo: (String) -> Unit,
    onAcceptChanges: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier,
) = Unit
