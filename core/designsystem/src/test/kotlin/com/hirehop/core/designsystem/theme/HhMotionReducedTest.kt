package com.hirehop.core.designsystem.theme

import androidx.compose.animation.core.snap
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HhMotionReducedTest {
    private val reduced = HhMotionDefaults.Reduced

    @Test
    fun everySpatialSpecSnaps() {
        assertEquals(snap(), reduced.proofSpecs.spatial)
        assertEquals(snap(), reduced.proofSpecs.spatialFast)
        assertEquals(snap(), reduced.proofSpecs.travel)
        assertEquals(snap(), reduced.proofSpecs.offset)
        assertEquals(snap(), reduced.proofSpecs.size)
        assertEquals(snap(), reduced.hopSpecs.spatial)
        assertEquals(snap(), reduced.hopSpecs.scale)
    }

    @Test
    fun staggerIsOffAndTheFlagIsSet() {
        assertEquals(0, reduced.proofSpecs.staggerMs)
        assertEquals(0, reduced.proofSpecs.staggerMax)
        assertTrue(reduced.reduced)
    }
}
