package com.tailormyresume.feature.onboarding.impl.paste

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.navigation.Navigator

@Composable
internal fun PasteResumeScreen(
    state: PasteResumeUiState,
    onTextChange: (String) -> Unit,
    onReadClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Unit

@Composable
internal fun PasteResumeRoute(
    viewModel: PasteResumeViewModel,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) = Unit
