package com.hirehop.feature.tailor.impl.credits

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.feature.tailor.api.navigation.CreditsNavKey
import com.hirehop.feature.tailor.impl.R

@Composable
internal fun CreditsRoute(
    key: CreditsNavKey,
    onNavigateBack: () -> Unit,
    onGetPack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreditsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val refundSubject = uiState.refundOrderId?.let { orderId ->
        stringResource(R.string.feature_tailor_impl_credits_refund_mail_subject, orderId)
    } ?: stringResource(R.string.feature_tailor_impl_credits_refund_mail_subject_no_order)
    val helpSubject = stringResource(R.string.feature_tailor_impl_credits_help_mail_subject)
    val actions = remember(viewModel, onNavigateBack, onGetPack, refundSubject, helpSubject) {
        CreditsActions(
            onGetPack = onGetPack,
            onAskRefund = { context.composeMail(refundSubject) },
            onContactHelp = { context.composeMail(helpSubject) },
            onRetry = { viewModel.onAction(CreditsAction.Retry) },
            onNavigateBack = onNavigateBack,
        )
    }
    LaunchedEffect(key) { viewModel.onEnter(key) }
    CreditsScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun Context.composeMail(subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO, "mailto:".toUri()).apply {
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    try {
        startActivity(intent)
    } catch (missing: ActivityNotFoundException) {
        return
    }
}
