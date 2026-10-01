package com.hirehop.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.EntryCategory
import org.junit.Test

class FactDraftValidatorTest {
    private fun draft(
        title: String = "Placement Stats Dashboard",
        startDate: String = "Jan 2024",
        endDate: String = "Apr 2024",
        detail: String = "Built a dashboard for the college cell.",
    ) = FactDraft(
        category = EntryCategory.PROJECT,
        title = title,
        organization = "College placement cell",
        startDate = startDate,
        endDate = endDate,
        detail = detail,
    )

    @Test
    fun validDraftHasNoErrors() {
        assertThat(FactDraftValidator.validate(draft())).isEmpty()
    }

    @Test
    fun blankTitleIsRequired() {
        assertThat(FactDraftValidator.validate(draft(title = "  ")))
            .containsExactly(FactDraftError(FactField.TITLE, FactDraftErrorReason.REQUIRED))
    }

    @Test
    fun emptyTitleIsRequired() {
        assertThat(FactDraftValidator.validate(draft(title = "")))
            .containsExactly(FactDraftError(FactField.TITLE, FactDraftErrorReason.REQUIRED))
    }

    @Test
    fun endBeforeStartOnNamedDatesIsAnError() {
        assertThat(FactDraftValidator.validate(draft(startDate = "Jan 2024", endDate = "Dec 2023")))
            .containsExactly(FactDraftError(FactField.END_DATE, FactDraftErrorReason.END_BEFORE_START))
    }

    @Test
    fun endBeforeStartOnNumericDatesIsAnError() {
        assertThat(FactDraftValidator.validate(draft(startDate = "2025-06", endDate = "2025-01")))
            .containsExactly(FactDraftError(FactField.END_DATE, FactDraftErrorReason.END_BEFORE_START))
    }

    @Test
    fun endBeforeStartAcrossYearLengthIsAnError() {
        assertThat(FactDraftValidator.validate(draft(startDate = "2025", endDate = "2024")))
            .containsExactly(FactDraftError(FactField.END_DATE, FactDraftErrorReason.END_BEFORE_START))
    }

    @Test
    fun equalDatesAreNotAnError() {
        assertThat(FactDraftValidator.validate(draft(startDate = "2024", endDate = "2024"))).isEmpty()
    }

    @Test
    fun startYearAloneIsNotEarlierThanItsMonth() {
        assertThat(FactDraftValidator.validate(draft(startDate = "2025", endDate = "2025-06"))).isEmpty()
    }

    @Test
    fun aBareYearNeverFailsAgainstAnotherBareYearOfTheSameLength() {
        assertThat(FactDraftValidator.validate(draft(startDate = "2022", endDate = "2026"))).isEmpty()
    }

    @Test
    fun unparseableEndDateIsAccepted() {
        assertThat(FactDraftValidator.validate(draft(startDate = "2025-06", endDate = "Present"))).isEmpty()
    }

    @Test
    fun unparseableStartDateIsAccepted() {
        assertThat(FactDraftValidator.validate(draft(startDate = "Summer internship", endDate = "2025-01"))).isEmpty()
    }

    @Test
    fun partialDateIsAccepted() {
        assertThat(FactDraftValidator.validate(draft(startDate = "2025", endDate = "2025-11"))).isEmpty()
    }

    @Test
    fun impossibleMonthIsNotADate() {
        assertThat(FactDraftValidator.validate(draft(startDate = "2025-13", endDate = "2025-01"))).isEmpty()
    }

    @Test
    fun dayInNamedDateIsCompared() {
        assertThat(FactDraftValidator.validate(draft(startDate = "12 Jan 2024", endDate = "2 Jan 2024")))
            .containsExactly(FactDraftError(FactField.END_DATE, FactDraftErrorReason.END_BEFORE_START))
    }

    @Test
    fun detailPastTheLimitIsTooLong() {
        val long = "x".repeat(FactDraftValidator.DETAIL_LIMIT + 1)

        assertThat(FactDraftValidator.validate(draft(detail = long)))
            .containsExactly(FactDraftError(FactField.DETAIL, FactDraftErrorReason.TOO_LONG))
    }

    @Test
    fun detailAtTheLimitIsAccepted() {
        assertThat(FactDraftValidator.validate(draft(detail = "x".repeat(FactDraftValidator.DETAIL_LIMIT)))).isEmpty()
    }

    @Test
    fun everyErrorIsReportedTogether() {
        val long = "x".repeat(FactDraftValidator.DETAIL_LIMIT + 1)

        assertThat(FactDraftValidator.validate(draft(title = "", startDate = "2025", endDate = "2024", detail = long)))
            .containsExactly(
                FactDraftError(FactField.TITLE, FactDraftErrorReason.REQUIRED),
                FactDraftError(FactField.END_DATE, FactDraftErrorReason.END_BEFORE_START),
                FactDraftError(FactField.DETAIL, FactDraftErrorReason.TOO_LONG),
            ).inOrder()
    }

    @Test
    fun blankOrganizationIsNotAnError() {
        val withoutOrganization = draft().copy(organization = "  ")

        assertThat(FactDraftValidator.validate(withoutOrganization)).isEmpty()
    }
}
