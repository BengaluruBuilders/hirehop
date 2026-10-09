package com.tailormyresume.core.designsystem.component

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmrLiveRegionTest {
    @get:Rule
    val rule = createComposeRule()

    private val liveRegionNodes = SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)

    private fun liveTexts(): List<String> = rule.onAllNodes(liveRegionNodes).fetchSemanticsNodes().map { node ->
        node.config.getOrNull(SemanticsProperties.Text).orEmpty().joinToString(" ") { it.text }
    }

    @Test
    fun errorCalloutIsOneLiveRegionHoldingTitleAndSupportingText() {
        rule.setContent {
            TmrPreviewTheme(darkTheme = false) {
                TmrErrorCallout(title = "Save failed", supportingText = "Check your connection")
            }
        }
        rule.onAllNodes(liveRegionNodes).assertCountEquals(1)
        val text = liveTexts().single()
        assertEquals(true, text.contains("Save failed") && text.contains("Check your connection"))
    }

    @Test
    fun offlineBannerIsPoliteLiveRegionHoldingMessage() {
        rule.setContent {
            TmrPreviewTheme(darkTheme = false) {
                TmrOfflineBanner(message = "You are offline", supportingText = "Saved work still opens")
            }
        }
        rule.onAllNodes(liveRegionNodes).assertCountEquals(1)
        val node = rule.onNode(liveRegionNodes).fetchSemanticsNode()
        assertEquals(LiveRegionMode.Polite, node.config[SemanticsProperties.LiveRegion])
        assertEquals(true, liveTexts().single().contains("You are offline"))
    }

    @Test
    fun fieldErrorFooterIsLiveRegionAndFieldExposesErrorState() {
        rule.setContent {
            TmrPreviewTheme(darkTheme = false) {
                TmrTextField(value = "", onValueChange = {}, label = "Email", errorText = "Enter a valid email")
            }
        }
        rule.onAllNodes(liveRegionNodes).assertCountEquals(1)
        assertEquals(true, liveTexts().single().contains("Enter a valid email"))
        val errored = SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)
        val fieldError = rule.onNode(errored).fetchSemanticsNode().config[SemanticsProperties.Error]
        assertEquals("Enter a valid email", fieldError)
    }

    @Test
    fun fieldWithoutErrorHasNoLiveRegionAndNoErrorState() {
        rule.setContent {
            TmrPreviewTheme(darkTheme = false) {
                TmrTextField(value = "", onValueChange = {}, label = "Email")
            }
        }
        rule.onAllNodes(liveRegionNodes).assertCountEquals(0)
        rule.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Error)).assertCountEquals(0)
    }
}
