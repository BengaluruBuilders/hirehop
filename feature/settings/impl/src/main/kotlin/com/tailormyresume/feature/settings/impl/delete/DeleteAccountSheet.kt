package com.tailormyresume.feature.settings.impl.delete

import androidx.compose.runtime.Composable

@Composable
internal fun DeleteAccountSheetContent(
    state: DeleteAccountUiState.Content,
    onTextChanged: (String) -> Unit,
    onDelete: () -> Unit,
    onKeep: () -> Unit,
) = Unit

@Composable
internal fun DeleteAccountSheet(
    viewModel: DeleteAccountViewModel,
    onKeep: () -> Unit,
) = Unit
