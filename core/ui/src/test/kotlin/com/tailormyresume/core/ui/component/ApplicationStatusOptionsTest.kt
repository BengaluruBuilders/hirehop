package com.tailormyresume.core.ui.component

import com.tailormyresume.core.model.ApplicationStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicationStatusOptionsTest {

    @Test
    fun noResponseIsOfferedOnlyWhenCurrent() {
        ApplicationStatus.entries.forEach { current ->
            val offered = applicationStatusOptionsFor(current) { it.name }.map { it.status }
            assertEquals(
                "current = $current",
                current == ApplicationStatus.NO_RESPONSE,
                ApplicationStatus.NO_RESPONSE in offered,
            )
        }
    }

    @Test
    fun everyOtherStatusIsAlwaysOffered() {
        val offered = applicationStatusOptionsFor(ApplicationStatus.APPLIED) { it.name }.map { it.status }
        assertTrue(offered.containsAll(ApplicationStatus.entries - ApplicationStatus.NO_RESPONSE))
        assertFalse(ApplicationStatus.NO_RESPONSE in offered)
    }
}
