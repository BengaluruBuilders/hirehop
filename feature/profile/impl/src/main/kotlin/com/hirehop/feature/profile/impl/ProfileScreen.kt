package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhScaffold
import com.hirehop.core.designsystem.component.HhSpotIllustration
import com.hirehop.core.designsystem.component.HhSpotKind
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.DebugScenario
import com.hirehop.feature.profile.api.R as apiR

@Composable
internal fun ProfileRoute(
    modifier: Modifier = Modifier,
    scenario: DebugScenario = DebugScenario.defaultValue,
    onOpenFact: (entryId: String?) -> Unit = {},
    onAddEvidence: () -> Unit = {},
    onBuildStepByStep: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    LaunchedEffect(viewModel, scenario) {
        viewModel.selectScenario(scenario)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    ProfileScreen(
        uiState = uiState,
        importState = importState,
        actions = actions,
        onOpenFact = onOpenFact,
        onAddEvidence = onAddEvidence,
        onBuildStepByStep = onBuildStepByStep,
        modifier = modifier,
    )
}

@Composable
internal fun ProfileScreen(
    uiState: ProfileUiState,
    importState: ResumeImportState,
    actions: ProfileActions,
    modifier: Modifier = Modifier,
    onOpenFact: (entryId: String?) -> Unit = {},
    onAddEvidence: () -> Unit = {},
    onBuildStepByStep: () -> Unit = {},
) {
    var sheet by rememberSaveable(stateSaver = ProfileSheetSaver) { mutableStateOf<ProfileSheet?>(null) }
    HhScaffold(
        modifier = modifier,
        topBar = {
            HhTopAppBar(
                title = stringResource(apiR.string.feature_profile_api_title),
                actions = {
                    if (uiState is ProfileUiState.Success) {
                        ProfileOverflowMenu(onClearProfile = { sheet = ProfileSheet.ClearProfile })
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            ProfileContent(
                uiState = uiState,
                actions = actions,
                onOpenSheet = { sheet = it },
                onOpenFact = onOpenFact,
                onAddEvidence = onAddEvidence,
                onBuildStepByStep = onBuildStepByStep,
            )
        }
    }
    ProfileSheetHost(
        sheet = sheet,
        uiState = uiState,
        importState = importState,
        actions = actions,
        onDismiss = { sheet = null },
    )
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    actions: ProfileActions,
    onOpenSheet: (ProfileSheet) -> Unit,
    onOpenFact: (String?) -> Unit,
    onAddEvidence: () -> Unit,
    onBuildStepByStep: () -> Unit,
) {
    when (uiState) {
        ProfileUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            HhLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_loading))
        }

        ProfileUiState.Empty -> ProfileEmptyState(
            onImportResume = { onOpenSheet(ProfileSheet.PasteResume) },
            onBuildStepByStep = onBuildStepByStep,
        )

        ProfileUiState.Failure -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            HhSpotIllustration(
                kind = HhSpotKind.Error,
                tint = HhTheme.colors.error,
                contentDescription = stringResource(R.string.feature_profile_impl_error_headline),
            )
        }

        is ProfileUiState.Success -> ProfileOverview(
            state = uiState,
            actions = actions,
            onOpenSheet = onOpenSheet,
            onOpenFact = onOpenFact,
            onAddEvidence = onAddEvidence,
        )
    }
}
