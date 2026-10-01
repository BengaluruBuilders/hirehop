package com.hirehop.feature.tailor.impl.prepquestions

import com.hirehop.core.domain.prep.PrepQuestionKind
import com.hirehop.feature.tailor.impl.R

sealed interface PrepQuestionsAction {
    data class FilterChosen(val filter: PrepQuestionFilter) : PrepQuestionsAction

    data class PractiseToggled(val questionId: String) : PrepQuestionsAction

    data class ReportInaccurate(val questionId: String) : PrepQuestionsAction

    data object DismissMessage : PrepQuestionsAction

    data object Retry : PrepQuestionsAction
}

data class PrepQuestionsActions(
    val onFilterChosen: (PrepQuestionFilter) -> Unit,
    val onPractiseToggled: (String) -> Unit,
    val onReportInaccurate: (String) -> Unit,
    val onDismissMessage: () -> Unit,
    val onRetry: () -> Unit,
    val onNavigateBack: () -> Unit,
)

internal fun PrepQuestionKind.headingRes(): Int = when (this) {
    PrepQuestionKind.STRENGTH -> R.string.feature_tailor_impl_prep_questions_group_strength
    PrepQuestionKind.CLARIFY -> R.string.feature_tailor_impl_prep_questions_group_clarify
    PrepQuestionKind.GAP -> R.string.feature_tailor_impl_prep_questions_group_gap
}

internal fun PrepQuestionKind.kindLabelRes(): Int = when (this) {
    PrepQuestionKind.STRENGTH -> R.string.feature_tailor_impl_prep_questions_kind_strength
    PrepQuestionKind.CLARIFY -> R.string.feature_tailor_impl_prep_questions_kind_clarify
    PrepQuestionKind.GAP -> R.string.feature_tailor_impl_prep_questions_kind_gap
}

internal fun PrepQuestionKind.groupNoteRes(): Int? = when (this) {
    PrepQuestionKind.STRENGTH -> null
    PrepQuestionKind.CLARIFY -> R.string.feature_tailor_impl_prep_questions_group_clarify_note
    PrepQuestionKind.GAP -> R.string.feature_tailor_impl_prep_questions_group_gap_note
}
