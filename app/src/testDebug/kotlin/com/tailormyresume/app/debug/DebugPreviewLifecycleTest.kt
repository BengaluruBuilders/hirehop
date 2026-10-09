package com.tailormyresume.app.debug

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.app.ai.DebugPreviewMode
import org.junit.Test

class DebugPreviewLifecycleTest {
    private val previewMode = DebugPreviewMode()
    private var forced = 0
    private var released = 0
    private val lifecycle = DebugPreviewLifecycle(previewMode, { forced++ }, { released++ })

    @Test
    fun aNewInstanceStartsWithoutAPreview() {
        previewMode.active = true

        lifecycle.onCreate()

        assertThat(previewMode.active).isFalse()
        assertThat(released).isEqualTo(1)
    }

    @Test
    fun startingWithAnOpenPreviewRaisesTheFlagAndForcesPayment() {
        lifecycle.onStart(opened = true)

        assertThat(previewMode.active).isTrue()
        assertThat(forced).isEqualTo(1)
    }

    @Test
    fun startingWithoutAPreviewKeepsTheFlagDown() {
        previewMode.active = true

        lifecycle.onStart(opened = false)

        assertThat(previewMode.active).isFalse()
        assertThat(forced).isEqualTo(0)
    }

    @Test
    fun stoppingWithoutFinishingReleasesTheFlagAndPayment() {
        lifecycle.onStart(opened = true)

        val close = lifecycle.onStop(opened = true, finishing = false, changingConfigurations = false)

        assertThat(close).isFalse()
        assertThat(previewMode.active).isFalse()
        assertThat(released).isEqualTo(1)
    }

    @Test
    fun theFlagStaysDownWhenARecreatedInstanceReachesTheMenu() {
        lifecycle.onStart(opened = true)
        lifecycle.onStop(opened = true, finishing = false, changingConfigurations = true)

        lifecycle.onCreate()
        lifecycle.onStart(opened = false)

        assertThat(previewMode.active).isFalse()
    }

    @Test
    fun finishingAnOpenPreviewAsksToClose() {
        assertThat(lifecycle.onStop(opened = true, finishing = true, changingConfigurations = false)).isTrue()
        assertThat(lifecycle.onStop(opened = false, finishing = true, changingConfigurations = false)).isFalse()
    }
}
