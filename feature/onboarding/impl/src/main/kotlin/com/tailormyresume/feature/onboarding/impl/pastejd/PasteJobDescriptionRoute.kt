package com.tailormyresume.feature.onboarding.impl.pastejd

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tailormyresume.core.domain.onboarding.OnboardingStep
import com.tailormyresume.feature.onboarding.api.navigation.PasteJobDescriptionNavKey

@Composable
internal fun PasteJobDescriptionRoute(
    key: PasteJobDescriptionNavKey,
    onNavigateBack: () -> Unit,
    onNavigateToStep: (OnboardingStep) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PasteJobDescriptionViewModel = hiltViewModel(),
    sharedText: String = "",
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val actions = remember(viewModel, context, onNavigateBack) {
        viewModel.toActions(context = context, onBack = onNavigateBack)
    }
    LaunchedEffect(key) { viewModel.onEnter(key = key, sharedText = sharedText) }
    LaunchedEffect(uiState.nextStep) {
        val step = uiState.nextStep
        if (step != null) {
            viewModel.onAction(PasteJobDescriptionAction.NextStepConsumed)
            onNavigateToStep(step)
        }
    }
    PasteJobDescriptionScreen(uiState = uiState, actions = actions, modifier = modifier)
}

private fun PasteJobDescriptionViewModel.toActions(
    context: Context,
    onBack: () -> Unit,
): PasteJobDescriptionActions =
    PasteJobDescriptionActions(
        onTextChange = { value -> onAction(PasteJobDescriptionAction.TextChanged(value)) },
        onPaste = { onAction(PasteJobDescriptionAction.Pasted(clipboardText(context))) },
        onCompanyChange = { value -> onAction(PasteJobDescriptionAction.CompanyChanged(value)) },
        onRoleChange = { value -> onAction(PasteJobDescriptionAction.RoleChanged(value)) },
        onClear = { onAction(PasteJobDescriptionAction.ClearTapped) },
        onAnalyse = { onAction(PasteJobDescriptionAction.AnalyseTapped) },
        onRetry = { onAction(PasteJobDescriptionAction.RetryTapped) },
        onBack = onBack,
    )

private fun clipboardText(context: Context): String {
    val manager = context.getSystemService(ClipboardManager::class.java)
    val clip = manager?.primaryClip
    return if (clip != null && clip.itemCount > 0) clip.getItemAt(0).coerceToText(context).toString() else ""
}
