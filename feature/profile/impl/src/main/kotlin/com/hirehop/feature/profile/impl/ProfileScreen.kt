package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.feature.profile.api.R as apiR

@Composable
internal fun ProfileRoute(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()
    val actions = remember(viewModel) { viewModel.toActions() }
    ProfileScreen(
        uiState = uiState,
        importState = importState,
        actions = actions,
        modifier = modifier,
    )
}

@Composable
internal fun ProfileScreen(
    uiState: ProfileUiState,
    importState: ResumeImportState,
    actions: ProfileActions,
    modifier: Modifier = Modifier,
) {
    var sheet by rememberSaveable(stateSaver = ProfileSheetSaver) { mutableStateOf<ProfileSheet?>(null) }
    Scaffold(
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
) {
    when (uiState) {
        ProfileUiState.Loading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            HhLoadingWheel(contentDesc = stringResource(R.string.feature_profile_impl_loading))
        }

        ProfileUiState.Empty -> ProfileEmptyState(
            onPasteResume = { onOpenSheet(ProfileSheet.PasteResume) },
            onAddManually = actions.onStartManual,
            onLoadDemo = actions.onLoadDemo,
        )

        is ProfileUiState.Success -> ProfileOverview(
            state = uiState,
            actions = actions,
            onOpenSheet = onOpenSheet,
        )
    }
}
