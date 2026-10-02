package com.hirehop.feature.onboarding.impl.importresume

import android.content.Context
import android.content.ContextWrapper
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.onboarding.api.navigation.ImportResumeNavKey

private const val RESUME_PICKER_KEY = "hirehop.onboarding.importResume.picker"

@Composable
fun ImportResumeRoute(
    key: ImportResumeNavKey,
    onBack: () -> Unit,
    onGoToGuidedForm: (resumedFromScan: Boolean) -> Unit,
    onReviewFacts: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ImportResumeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val launcher = rememberResumePickerLauncher { uri ->
        if (uri == null) {
            viewModel.onPickerDismissed()
        } else {
            viewModel.onFileChosen(resumeFileOf(context, uri))
        }
    }
    val actions = remember(viewModel, launcher, onBack, onGoToGuidedForm, onReviewFacts) {
        ImportResumeActions(
            onBack = onBack,
            onPickFile = {
                viewModel.onPickRequested()
                launcher.launch(resumeMimeTypes())
            },
            onChooseAnotherFile = {
                viewModel.onChooseAnotherFile()
                launcher.launch(resumeMimeTypes())
            },
            onRetry = {
                viewModel.onRetry()
                launcher.launch(resumeMimeTypes())
            },
            onStartGuidedForm = {
                val resumedFromScan = viewModel.uiState.value.stage == ImportStage.ScannedNoText
                viewModel.onStartGuidedForm()
                onGoToGuidedForm(resumedFromScan)
            },
            onReviewFacts = onReviewFacts,
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    ImportResumeScreen(uiState = uiState, actions = actions, modifier = modifier)
}

@Composable
private fun rememberResumePickerLauncher(
    onPicked: (Uri?) -> Unit,
): ActivityResultLauncher<Array<String>> {
    val context = LocalContext.current
    val registry = remember(context) { context.requireComponentActivity().activityResultRegistry }
    val callback = rememberUpdatedState(onPicked)
    val launcher = remember(registry) {
        registry.register(RESUME_PICKER_KEY, ActivityResultContracts.OpenDocument()) { uri ->
            callback.value(uri)
        }
    }
    DisposableEffect(launcher) { onDispose { launcher.unregister() } }
    return launcher
}

private fun resumeFileOf(context: Context, uri: Uri): ResumeFile {
    val measured = measureOf(context, uri)
    return ResumeFile(
        displayName = measured.name.ifBlank { uri.lastPathSegment.orEmpty().substringAfterLast('/') },
        mimeType = context.contentResolver.getType(uri).orEmpty(),
        byteSize = measured.size,
        uri = uri.toString(),
    )
}

private fun measureOf(context: Context, uri: Uri): MeasuredUri {
    var name = ""
    var size = 0L
    runCatching {
        context.contentResolver
            .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    name = cursor.textAt(0)
                    size = cursor.sizeAt(1)
                }
            }
    }
    return MeasuredUri(name = name, size = size)
}

private fun Context.requireComponentActivity(): ComponentActivity {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is ComponentActivity) return current
        current = current.baseContext
    }
    throw IllegalStateException("Import resume needs an activity to host the system file picker")
}

private data class MeasuredUri(val name: String, val size: Long)

private fun Cursor.textAt(index: Int): String = if (isNull(index)) "" else getString(index).orEmpty()

private fun Cursor.sizeAt(index: Int): Long = if (isNull(index)) 0L else getLong(index)
