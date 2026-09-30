package com.hirehop.feature.tailor.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.tailor.impl.export.createPdfShareIntent

@Composable
internal fun TailorRoute(
    onBackClick: () -> Unit,
    viewModel: TailorViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()
    ShareOnReady(exportState, uiState, viewModel::onExportHandled)
    TailorScreen(
        uiState = uiState,
        exportState = exportState,
        onBackClick = onBackClick,
        onAccept = viewModel::onAccept,
        onReject = viewModel::onReject,
        onAcceptAllSafeChanges = viewModel::onAcceptAllSafeChanges,
        onExport = viewModel::onExport,
        onExportFailureShown = viewModel::onExportHandled,
        modifier = modifier,
    )
}

@Composable
private fun ShareOnReady(
    exportState: ExportUiState,
    uiState: TailorUiState,
    onHandled: () -> Unit,
) {
    val context = LocalContext.current
    val subjectName = (uiState as? TailorUiState.Success)?.document?.name.orEmpty()
    LaunchedEffect(exportState) {
        if (exportState is ExportUiState.Ready) {
            val intent = createPdfShareIntent(
                context = context,
                file = exportState.file,
                subject = context.getString(R.string.feature_tailor_impl_share_subject, subjectName),
                chooserTitle = context.getString(R.string.feature_tailor_impl_share_chooser),
            )
            context.startActivity(intent)
            onHandled()
        }
    }
}
