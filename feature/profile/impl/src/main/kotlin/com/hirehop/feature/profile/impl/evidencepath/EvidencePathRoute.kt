package com.hirehop.feature.profile.impl.evidencepath

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.profile.api.navigation.FactEvidenceNavKey
import com.hirehop.feature.profile.impl.ProfileExit

internal data class EvidencePathNavigation(
    val onBack: () -> Unit,
    val onEditFact: (entryId: String, entryType: String) -> Unit,
    val onExit: (ProfileExit) -> Unit,
)

@Composable
internal fun EvidencePathRoute(
    key: FactEvidenceNavKey,
    navigation: EvidencePathNavigation,
    modifier: Modifier = Modifier,
    viewModel: EvidencePathViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val actions = remember(viewModel, navigation) { viewModel.toActions(navigation) }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.navigation) {
        val target = uiState.navigation
        if (target is EvidenceNavigation.Exit) {
            viewModel.onAction(EvidencePathAction.NavigationConsumed)
            navigation.onExit(target.exit)
        }
    }
    EvidencePathScreen(uiState = uiState, actions = actions, onBack = navigation.onBack, modifier = modifier)
}

private fun EvidencePathViewModel.toActions(navigation: EvidencePathNavigation): EvidencePathActions =
    EvidencePathActions(
        onCategoryChosen = { onAction(EvidencePathAction.CategoryChosen(it)) },
        onAnswerChanged = { onAction(EvidencePathAction.AnswerChanged(it)) },
        onSave = { onAction(EvidencePathAction.Save) },
        onSkip = { onAction(EvidencePathAction.Skip) },
        onAddMore = { onAction(EvidencePathAction.AddMore) },
        onFinish = { onAction(EvidencePathAction.Finish) },
        onEditFact = navigation.onEditFact,
    )
