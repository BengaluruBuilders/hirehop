package com.hirehop.core.domain.prep

data class PrepQuestion(
    val id: String,
    val kind: PrepQuestionKind,
    val prompt: String,
    val requirementText: String,
    val backingFactId: String?,
    val why: String = "",
    val gapAdvice: String? = null,
    val generationId: String? = null,
) {
    val isTiedToFact: Boolean get() = kind != PrepQuestionKind.GAP
}
