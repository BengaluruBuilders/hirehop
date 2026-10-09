package com.tailormyresume.feature.profile.impl.guidedform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.feature.profile.api.navigation.GuidedProfileFormNavKey
import com.tailormyresume.feature.profile.impl.ProfileExit

internal data class GuidedFormNavigation(
    val onBack: () -> Unit,
    val onOpenEvidence: (category: String) -> Unit,
    val onAddJob: () -> Unit,
    val onEditFact: (entryId: String, entryType: String) -> Unit,
    val onExit: (ProfileExit) -> Unit,
)

@Composable
internal fun GuidedFormRoute(
    key: GuidedProfileFormNavKey,
    navigation: GuidedFormNavigation,
    modifier: Modifier = Modifier,
    viewModel: GuidedFormViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, navigation) { viewModel.toActions(navigation) }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.navigation) {
        when (val target = uiState.navigation) {
            null -> Unit
            is GuidedNavigation.Evidence -> {
                viewModel.onAction(GuidedFormAction.NavigationConsumed)
                navigation.onOpenEvidence(target.category)
            }
            is GuidedNavigation.Exit -> {
                viewModel.onAction(GuidedFormAction.NavigationConsumed)
                navigation.onExit(target.exit)
            }
        }
    }
    GuidedFormScreen(uiState = uiState, actions = actions, onBack = navigation.onBack, modifier = modifier)
}

private fun GuidedFormViewModel.toActions(navigation: GuidedFormNavigation): GuidedFormActions = GuidedFormActions(
    onValueChange = { field, value -> onAction(GuidedFormAction.ValueChanged(field, value)) },
    onAddSkill = { onAction(GuidedFormAction.AddSkill) },
    onRemoveSkill = { onAction(GuidedFormAction.RemoveSkill(it)) },
    onStartForm = { onAction(GuidedFormAction.StartForm) },
    onNext = { onAction(GuidedFormAction.Next) },
    onBack = { onAction(GuidedFormAction.Back) },
    onSaveAndFinishLater = { onAction(GuidedFormAction.SaveAndFinishLater) },
    onFinishSaved = { onAction(GuidedFormAction.FinishSaved) },
    onGoToProjects = { onAction(GuidedFormAction.GoToProjects) },
    onAddJob = navigation.onAddJob,
    onChooseExperience = { onAction(GuidedFormAction.ChooseExperience(it)) },
    onEditFact = navigation.onEditFact,
)
