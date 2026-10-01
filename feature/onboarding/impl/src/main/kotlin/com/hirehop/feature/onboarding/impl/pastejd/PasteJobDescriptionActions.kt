package com.hirehop.feature.onboarding.impl.pastejd

sealed interface PasteJobDescriptionAction {
    data class TextChanged(val value: String) : PasteJobDescriptionAction

    data class CompanyChanged(val value: String) : PasteJobDescriptionAction

    data class RoleChanged(val value: String) : PasteJobDescriptionAction

    data object ClearTapped : PasteJobDescriptionAction

    data object AnalyseTapped : PasteJobDescriptionAction

    data object RetryTapped : PasteJobDescriptionAction

    data object DismissMessageTapped : PasteJobDescriptionAction

    data object AnalysisRequestConsumed : PasteJobDescriptionAction
}

data class PasteJobDescriptionActions(
    val onTextChange: (String) -> Unit,
    val onCompanyChange: (String) -> Unit,
    val onRoleChange: (String) -> Unit,
    val onClear: () -> Unit,
    val onAnalyse: () -> Unit,
    val onRetry: () -> Unit,
    val onDismissMessage: () -> Unit,
)
