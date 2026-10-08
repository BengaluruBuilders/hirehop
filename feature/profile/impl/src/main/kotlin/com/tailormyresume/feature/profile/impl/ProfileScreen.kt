package com.tailormyresume.feature.profile.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.core.designsystem.component.TmrLoadingWheel
import com.tailormyresume.core.designsystem.component.TmrScreen
import com.tailormyresume.core.designsystem.component.TmrSpotIllustration
import com.tailormyresume.core.designsystem.component.TmrSpotKind
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.DebugScenario

@Composable
internal fun ProfileRoute(
    navigation: ProfileNavigation,
    modifier: Modifier = Modifier,
    scenario: DebugScenario = DebugScenario.defaultValue,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel, scenario) {
        viewModel.selectScenario(scenario)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    ProfileScreen(uiState = uiState, actions = actions, navigation = navigation, modifier = modifier)
}

@Composable
internal fun ProfileScreen(
    uiState: ProfileUiState,
    actions: ProfileActions,
    navigation: ProfileNavigation,
    modifier: Modifier = Modifier,
    initiallyExpanded: ProfileSectionKind? = null,
) {
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    val success = uiState as? ProfileUiState.Success
    val openSection = success?.overview?.sections?.firstOrNull { it.kind == expanded }
    if (success != null && openSection != null) {
        BackHandler { expanded = null }
        ExpandedSectionScreen(
            state = success,
            section = openSection,
            actions = actions,
            navigation = navigation,
            onClose = { expanded = null },
            modifier = modifier,
        )
    } else {
        ProfileHomeScreen(
            uiState = uiState,
            actions = actions,
            navigation = navigation,
            onOpenSection = { expanded = it },
            modifier = modifier,
        )
    }
}

@Composable
private fun ProfileHomeScreen(
    uiState: ProfileUiState,
    actions: ProfileActions,
    navigation: ProfileNavigation,
    onOpenSection: (ProfileSectionKind) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        ProfileUiState.Loading -> CenteredScreen(modifier) {
            TmrLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_loading))
        }

        ProfileUiState.Failure -> CenteredScreen(modifier) { ProfileFailure() }

        is ProfileUiState.Empty -> ProfileEmptyScreen(
            navigation = navigation,
            modifier = modifier,
        )

        is ProfileUiState.Success -> ProfileOverviewScreen(
            state = uiState,
            actions = actions,
            navigation = navigation,
            onOpenSection = onOpenSection,
            modifier = modifier,
        )
    }
}

@Composable
private fun CenteredScreen(
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    TmrScreen(modifier = modifier) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            content()
        }
    }
}

@Composable
private fun ProfileFailure() {
    Column(
        modifier = Modifier.padding(TmrTheme.spacing.gutter),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
    ) {
        TmrSpotIllustration(
            kind = TmrSpotKind.Error,
            tint = TmrTheme.colors.error,
            contentDescription = stringResource(R.string.feature_profile_impl_error_headline),
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_error_headline),
            style = TmrTheme.typography.titleL,
            color = TmrTheme.colors.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_error_body),
            style = TmrTheme.typography.bodyM,
            color = TmrTheme.colors.body,
            textAlign = TextAlign.Center,
        )
    }
}
