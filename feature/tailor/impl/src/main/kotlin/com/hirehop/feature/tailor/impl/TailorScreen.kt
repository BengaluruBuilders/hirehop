package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons

@Composable
internal fun TailorScreen(
    uiState: TailorUiState,
    exportState: ExportUiState,
    onBackClick: () -> Unit,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onAcceptAllSafeChanges: () -> Unit,
    onExport: () -> Unit,
    onExportFailureShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPreview by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val failureMessage = stringResource(R.string.feature_tailor_impl_export_failed)
    LaunchedEffect(exportState) {
        if (exportState == ExportUiState.Failed) {
            snackbarHostState.showSnackbar(failureMessage)
            onExportFailureShown()
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            TailorTopBar(
                showPreview = showPreview,
                canPreview = uiState is TailorUiState.Success,
                onBackClick = onBackClick,
                onTogglePreview = { showPreview = !showPreview },
            )
        },
        bottomBar = {
            if (uiState is TailorUiState.Success) {
                ExportBar(uiState.canExport, exportState == ExportUiState.Exporting, onExport)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        TailorBody(
            uiState = uiState,
            showPreview = showPreview,
            onAccept = onAccept,
            onReject = onReject,
            onAcceptAllSafeChanges = onAcceptAllSafeChanges,
            contentPadding = padding,
        )
    }
}

@Composable
private fun TailorBody(
    uiState: TailorUiState,
    showPreview: Boolean,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onAcceptAllSafeChanges: () -> Unit,
    contentPadding: PaddingValues,
) {
    when (uiState) {
        TailorUiState.Loading -> CenteredContent(contentPadding) {
            HhLoadingWheel(contentDesc = stringResource(R.string.feature_tailor_impl_loading))
        }
        TailorUiState.NotFound -> CenteredContent(contentPadding) {
            Text(text = stringResource(R.string.feature_tailor_impl_not_found))
        }
        is TailorUiState.Success -> if (showPreview) {
            ResumePreviewContent(
                document = uiState.document,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding.withVerticalSpacing(),
            )
        } else {
            ReviewContent(
                state = uiState,
                onAccept = onAccept,
                onReject = onReject,
                onAcceptAllSafeChanges = onAcceptAllSafeChanges,
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding.withVerticalSpacing(),
            )
        }
    }
}

private fun PaddingValues.withVerticalSpacing(): PaddingValues = PaddingValues(
    top = calculateTopPadding() + 8.dp,
    bottom = calculateBottomPadding() + 16.dp,
)

@Composable
private fun CenteredContent(contentPadding: PaddingValues, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun TailorTopBar(
    showPreview: Boolean,
    canPreview: Boolean,
    onBackClick: () -> Unit,
    onTogglePreview: () -> Unit,
) {
    HhTopAppBar(
        title = stringResource(
            if (showPreview) R.string.feature_tailor_impl_title_preview else R.string.feature_tailor_impl_title_review,
        ),
        navigationIcon = HhIcons.ArrowBack,
        navigationIconContentDescription = stringResource(R.string.feature_tailor_impl_back),
        onNavigationClick = onBackClick,
        actions = {
            TextButton(onClick = onTogglePreview, enabled = canPreview) {
                Text(
                    stringResource(
                        if (showPreview) {
                            R.string.feature_tailor_impl_review_action
                        } else {
                            R.string.feature_tailor_impl_preview_action
                        },
                    ),
                )
            }
        },
    )
}

@Composable
private fun ExportBar(canExport: Boolean, exporting: Boolean, onExport: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        HhButton(
            onClick = onExport,
            enabled = canExport && !exporting,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = {
                Text(
                    stringResource(
                        if (exporting) R.string.feature_tailor_impl_exporting else R.string.feature_tailor_impl_export,
                    ),
                )
            },
        )
    }
}
