package com.tailormyresume.app.debug

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.app.ui.AppRootState
import org.junit.Test

class DebugPreviewRootTest {

    @Test
    fun previewShowsWelcome_onlyAfterMainWasSeenAndRootIsFirstRun() {
        assertThat(previewShowsWelcome(seenMain = true, rootState = AppRootState.FirstRun)).isTrue()
        assertThat(previewShowsWelcome(seenMain = false, rootState = AppRootState.FirstRun)).isFalse()
        assertThat(previewShowsWelcome(seenMain = true, rootState = AppRootState.Main)).isFalse()
        assertThat(previewShowsWelcome(seenMain = true, rootState = AppRootState.Loading)).isFalse()
        assertThat(previewShowsWelcome(seenMain = false, rootState = AppRootState.Main)).isFalse()
    }
}
