package com.tailormyresume.feature.profile.impl.overview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import com.tailormyresume.core.navigation.Navigator

internal enum class ProfileTarget { SETTINGS, CONTACT, SUMMARY, EXPERIENCE, EDUCATION, SKILLS, ACHIEVEMENTS, LINKEDIN, REPLACE }

internal fun ProfileTarget.navKey(): NavKey = TODO()

internal fun Navigator.open(target: ProfileTarget) = navigate(target.navKey())

@Composable
internal fun ProfileScreen(
    state: ProfileUiState,
    onOpen: (ProfileTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
}

@Composable
internal fun ProfileRoute(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
}
