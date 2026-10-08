package com.tailormyresume.core.ui

import com.tailormyresume.core.designsystem.component.TmrApplicationStatusKind
import com.tailormyresume.core.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplicationStatusKindMapperTest {

    private val mapper = ApplicationStatusKindMapper()

    @Test
    fun mapsEveryApplicationStatus() {
        assertEquals(TmrApplicationStatusKind.Saved, mapper.kindOf(ApplicationStatus.SAVED))
        assertEquals(TmrApplicationStatusKind.Applied, mapper.kindOf(ApplicationStatus.APPLIED))
        assertEquals(TmrApplicationStatusKind.Interview, mapper.kindOf(ApplicationStatus.INTERVIEW))
        assertEquals(TmrApplicationStatusKind.Offer, mapper.kindOf(ApplicationStatus.OFFER))
        assertEquals(TmrApplicationStatusKind.Rejected, mapper.kindOf(ApplicationStatus.REJECTED))
        assertEquals(TmrApplicationStatusKind.NoResponse, mapper.kindOf(ApplicationStatus.NO_RESPONSE))
    }

    @Test
    fun coversEveryCaseOfTheSourceEnum() {
        assertEquals(ApplicationStatus.entries.size, ApplicationStatus.entries.map { mapper.kindOf(it) }.size)
    }
}
