package com.hirehop.feature.tailor.impl.exported

import android.content.ActivityNotFoundException
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.model.ExportFormat
import com.hirehop.feature.tailor.api.navigation.ExportedNavKey
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.export.createDocxViewIntent
import com.hirehop.feature.tailor.impl.export.createExportShareIntent
import com.hirehop.feature.tailor.impl.export.createPdfViewIntent
import com.hirehop.feature.tailor.impl.jobLine

@Composable
internal fun ExportedRoute(
    key: ExportedNavKey,
    onNavigateBack: () -> Unit,
    onDone: () -> Unit,
    onGetPrepQuestions: () -> Unit,
    onWriteCoverLetter: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExportedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.feature_tailor_impl_exported_chooser_title)
    val shareSubject = jobLine(uiState.jobTitle, uiState.jobCompany)
        ?: stringResource(R.string.feature_tailor_impl_exported_share_subject_bare)
    val actions = remember(viewModel, onNavigateBack, onDone, onGetPrepQuestions, onWriteCoverLetter) {
        ExportedActions(
            onOpenStatusSheet = { viewModel.onAction(ExportedAction.OpenStatusSheet) },
            onDismissStatusSheet = { viewModel.onAction(ExportedAction.DismissStatusSheet) },
            onConfirmStatus = { status -> viewModel.onAction(ExportedAction.ConfirmStatus(status)) },
            onUndoStatus = { viewModel.onAction(ExportedAction.UndoStatus) },
            onDismissUndo = { viewModel.onAction(ExportedAction.DismissUndo) },
            onShare = { viewModel.onAction(ExportedAction.RequestShare) },
            onOpen = { viewModel.onAction(ExportedAction.RequestOpen) },
            onGetPrepQuestions = onGetPrepQuestions,
            onWriteCoverLetter = onWriteCoverLetter,
            onDone = onDone,
            onNavigateBack = onNavigateBack,
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    LaunchedEffect(uiState.fileRequest) {
        val request = uiState.fileRequest ?: return@LaunchedEffect
        val intent = when (request.action) {
            ExportedFileAction.SHARE ->
                createExportShareIntent(context, request.file, request.format, shareSubject, chooserTitle)

            ExportedFileAction.OPEN -> when (request.format) {
                ExportFormat.PDF -> createPdfViewIntent(context, request.file)
                ExportFormat.DOCX -> createDocxViewIntent(context, request.file)
            }
        }
        try {
            context.startActivity(intent)
        } catch (missing: ActivityNotFoundException) {
            viewModel.onAction(ExportedAction.FileRequestHandled)
            return@LaunchedEffect
        }
        viewModel.onAction(ExportedAction.FileRequestHandled)
    }
    ExportedScreen(uiState = uiState, actions = actions, modifier = modifier)
}
