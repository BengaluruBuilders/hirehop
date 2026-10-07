package com.hirehop.core.domain.coverletter

data class CoverLetterDraft(
    val greeting: String,
    val openingParagraph: String,
    val evidenceParagraph: String,
    val closingParagraph: String,
    val generationId: String? = null,
)
