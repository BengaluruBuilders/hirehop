package com.hirehop.core.ui

import com.hirehop.core.designsystem.component.HhApplicationStatusKind
import com.hirehop.core.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplicationStatusKindMapperTest {

    private val mapper = ApplicationStatusKindMapper()

    @Test
    fun mapsEveryApplicationStatus() {
        assertEquals(HhApplicationStatusKind.Saved, mapper.kindOf(ApplicationStatus.SAVED))
        assertEquals(HhApplicationStatusKind.Applied, mapper.kindOf(ApplicationStatus.APPLIED))
        assertEquals(HhApplicationStatusKind.Interview, mapper.kindOf(ApplicationStatus.INTERVIEW))
        assertEquals(HhApplicationStatusKind.Offer, mapper.kindOf(ApplicationStatus.OFFER))
        assertEquals(HhApplicationStatusKind.Rejected, mapper.kindOf(ApplicationStatus.REJECTED))
        assertEquals(HhApplicationStatusKind.NoResponse, mapper.kindOf(ApplicationStatus.NO_RESPONSE))
    }

    @Test
    fun coversEveryCaseOfTheSourceEnum() {
        assertEquals(ApplicationStatus.entries.size, ApplicationStatus.entries.map { mapper.kindOf(it) }.size)
    }
}
