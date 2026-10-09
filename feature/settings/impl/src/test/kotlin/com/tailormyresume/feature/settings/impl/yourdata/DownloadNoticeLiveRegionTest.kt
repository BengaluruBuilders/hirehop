package com.tailormyresume.feature.settings.impl.yourdata

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DownloadNoticeLiveRegionTest {
    @get:Rule
    val rule = createComposeRule()

    private val liveRegionNodes = SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)

    @Test
    fun offlineNoteIsOnePoliteLiveRegionHoldingItsText() {
        rule.setContent { TmrTheme { DownloadNotice(isOffline = true) } }

        rule.onAllNodes(liveRegionNodes).assertCountEquals(1)
        val node = rule.onNode(liveRegionNodes).fetchSemanticsNode()
        assertThat(node.config[SemanticsProperties.LiveRegion]).isEqualTo(LiveRegionMode.Polite)
        assertThat(node.config[SemanticsProperties.Text]).isNotEmpty()
    }
}
