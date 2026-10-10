package com.tailormyresume.feature.profile.impl.experience

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.tailormyresume.core.navigation.Navigator

@Composable
internal fun ExperienceScreen(
    state: ExperienceUiState,
    onRole: (String) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
}

@Composable
internal fun ExperienceRoute(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: ExperienceViewModel = hiltViewModel(),
) {
}
