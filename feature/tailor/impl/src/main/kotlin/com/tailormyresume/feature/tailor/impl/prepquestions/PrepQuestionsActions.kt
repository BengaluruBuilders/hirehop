package com.tailormyresume.feature.tailor.impl.prepquestions

sealed interface PrepQuestionsAction {
    data class ReportInaccurate(val questionId: String) : PrepQuestionsAction

    data object DismissMessage : PrepQuestionsAction

    data object Retry : PrepQuestionsAction
}

data class PrepQuestionsActions(
    val onReportInaccurate: (String) -> Unit,
    val onDismissMessage: () -> Unit,
    val onRetry: () -> Unit,
    val onOpenPrepPlan: () -> Unit,
    val onEditFact: (entryId: String, entryType: String) -> Unit,
    val onNavigateBack: () -> Unit,
)
