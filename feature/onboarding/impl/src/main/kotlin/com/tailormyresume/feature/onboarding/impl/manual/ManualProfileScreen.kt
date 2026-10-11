package com.tailormyresume.feature.onboarding.impl.manual

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.navigation.Navigator

@Composable
internal fun ManualProfileScreen(
    state: ManualProfileUiState,
    onFieldChange: (ManualField, String) -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Unit

@Composable
internal fun ManualProfileRoute(
    viewModel: ManualProfileViewModel,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) = Unit
