package com.hirehop.feature.tailor.impl.exported

import android.content.ActivityNotFoundException
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.tailor.api.navigation.ShareLastExportNavKey
import com.hirehop.feature.tailor.impl.R
import com.hirehop.feature.tailor.impl.export.createExportShareIntent
import com.hirehop.feature.tailor.impl.jobLine

@Composable
internal fun ShareLastExportRoute(
    key: ShareLastExportNavKey,
    onDone: () -> Unit,
    onFileMissing: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShareLastExportViewModel = hiltViewModel(),
) {
    val outcome by viewModel.outcome.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val chooserTitle = stringResource(R.string.feature_tailor_impl_exported_chooser_title)
    val bareSubject = stringResource(R.string.feature_tailor_impl_exported_share_subject_bare)
    val share = outcome as? ShareLastExportOutcome.Share
    val subject = share?.let { jobLine(it.request.jobTitle, it.request.jobCompany) } ?: bareSubject
    LaunchedEffect(key) { viewModel.onEnter(key.applicationId) }
    LaunchedEffect(outcome) {
        when (val current = outcome) {
            null -> return@LaunchedEffect
            ShareLastExportOutcome.Missing -> onFileMissing()
            is ShareLastExportOutcome.Share -> {
                val request = current.request
                try {
                    context.startActivity(createExportShareIntent(context, request.file, request.format, subject, chooserTitle))
                } catch (missing: ActivityNotFoundException) {
                    onDone()
                    return@LaunchedEffect
                }
                onDone()
            }
        }
    }
    Box(modifier = modifier.fillMaxSize().background(HhTheme.colors.background))
}
