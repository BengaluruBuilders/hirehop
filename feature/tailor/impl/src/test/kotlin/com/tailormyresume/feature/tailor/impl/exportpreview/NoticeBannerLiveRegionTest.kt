package com.tailormyresume.feature.tailor.impl.exportpreview

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NoticeBannerLiveRegionTest {
    @get:Rule
    val rule = createComposeRule()

    private val liveRegionNodes = SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)

    private fun show(tone: NoticeTone) {
        rule.setContent { TmrTheme { NoticeBanner(message = "Payment failed", tone = tone) } }
    }

    @Test
    fun errorBannerIsPoliteLiveRegion() {
        show(NoticeTone.Error)
        val node = rule.onNode(liveRegionNodes).fetchSemanticsNode()
        assertEquals(LiveRegionMode.Polite, node.config[SemanticsProperties.LiveRegion])
        assertTrue(node.config[SemanticsProperties.Text].joinToString { it.text }.contains("Payment failed"))
    }

    @Test
    fun offlineBannerIsLiveRegion() {
        show(NoticeTone.Offline)
        rule.onAllNodes(liveRegionNodes).assertCountEquals(1)
    }

    @Test
    fun goodBannerIsNotLiveRegion() {
        show(NoticeTone.Good)
        rule.onAllNodes(liveRegionNodes).assertCountEquals(0)
    }
}
