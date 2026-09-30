package com.hirehop.feature.tailor.impl.exported

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.tailor.api.navigation.ExportedNavKey
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.export.createDocxShareIntent
import com.hirehop.feature.tailor.impl.export.createPdfShareIntent
import com.hirehop.feature.tailor.impl.exportpreview.ExportFormat

@Composable
internal fun ExportedRoute(
    key: ExportedNavKey,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExportedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.feature_tailor_impl_exported_chooser_title)
    val shareSubject = stringResource(
        R.string.feature_tailor_impl_exported_share_subject,
        uiState.jobTitle,
        uiState.jobCompany,
    )
    val actions = remember(viewModel, onNavigateBack) {
        viewModel.toActions(onNavigateBack = onNavigateBack)
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.shareRequest) {
        val request = uiState.shareRequest ?: return@LaunchedEffect
        context.startActivity(
            when (request.format) {
                ExportFormat.PDF -> createPdfShareIntent(
                    context = context,
                    file = request.file,
                    subject = shareSubject,
                    chooserTitle = chooserTitle,
                )

                ExportFormat.DOCX -> createDocxShareIntent(
                    context = context,
                    file = request.file,
                    subject = shareSubject,
                    chooserTitle = chooserTitle,
                )
            },
        )
        viewModel.onAction(ExportedAction.ShareHandedToSystem)
    }
    ExportedScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun ExportedViewModel.toActions(
    onNavigateBack: () -> Unit,
): ExportedActions = ExportedActions(
    onOpenStatusSheet = { onAction(ExportedAction.OpenStatusSheet) },
    onDismissStatusSheet = { onAction(ExportedAction.DismissStatusSheet) },
    onConfirmStatus = { status -> onAction(ExportedAction.ConfirmStatus(status)) },
    onShare = { onAction(ExportedAction.RequestShare) },
    onNavigateBack = onNavigateBack,
)
