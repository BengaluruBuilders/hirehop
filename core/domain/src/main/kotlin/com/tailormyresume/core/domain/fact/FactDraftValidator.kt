package com.tailormyresume.core.domain.fact

object FactDraftValidator {
    const val DETAIL_LIMIT = 600

    fun validate(draft: FactDraft): List<FactDraftError> = buildList {
        if (draft.title.isBlank()) {
            add(FactDraftError(FactField.TITLE, FactDraftErrorReason.REQUIRED))
        }
        if (endsBeforeStart(draft.startDate, draft.endDate)) {
            add(FactDraftError(FactField.END_DATE, FactDraftErrorReason.END_BEFORE_START))
        }
        if (draft.detail.length > DETAIL_LIMIT) {
            add(FactDraftError(FactField.DETAIL, FactDraftErrorReason.TOO_LONG))
        }
    }

    private fun endsBeforeStart(startDate: String, endDate: String): Boolean {
        val start = FreeFormDate.parse(startDate) ?: return false
        val end = FreeFormDate.parse(endDate) ?: return false
        return end < start
    }
}
