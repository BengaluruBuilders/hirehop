package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmrFitTextFloorTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var layout: TextLayoutResult? = null
    private var density: Density? = null
    private var layoutPasses = 0

    @Test
    @Config(fontScale = 1.5f)
    fun at150TheTextNeverRendersBelowItsDefaultScaleDp() {
        show(width = 60.dp, text = TOO_WIDE_WORD, sp = 30f)

        assertTrue(renderedDp() >= 30f)
    }

    @Test
    @Config(fontScale = 2f)
    fun at200TheTextNeverRendersBelowItsDefaultScaleDp() {
        show(width = 60.dp, text = TOO_WIDE_WORD, sp = 30f)

        assertTrue(renderedDp() >= 30f)
    }

    @Test
    @Config(fontScale = 2f)
    fun at200ASmallLabelShrinksOnlyAsFarAsItsDefaultScaleDp() {
        show(width = 70.dp, text = TOO_WIDE_WORD, sp = 12f)

        assertTrue(renderedDp() >= 12f)
    }

    @Test
    @Config(fontScale = 2f)
    fun at200AWordThatFitsAfterShrinkingEndsWholeAndAboveTheFloor() {
        show(width = 90.dp, text = "Confirmed", sp = 12f)

        val result = requireNotNull(layout)
        assertFalse(result.splitsAWord())
        assertTrue(renderedDp() >= 12f)
        assertTrue(renderedDp() < with(requireNotNull(density)) { 12.sp.toDp() }.value)
    }

    @Test
    @Config(fontScale = 1f)
    fun atDefaultFontTheTextIsNotShrunkAtAll() {
        show(width = 60.dp, text = TOO_WIDE_WORD, sp = 30f)

        assertEquals(30f, requireNotNull(layout).layoutInput.style.fontSize.value, 0f)
    }

    @Test
    @Config(fontScale = 2f)
    fun theSizeSettlesWithinTwoLayoutPasses() {
        show(width = 90.dp, text = "Confirmed", sp = 12f)

        assertTrue(layoutPasses <= 2)
        assertFalse(requireNotNull(layout).splitsAWord())
    }

    private fun renderedDp(): Float =
        with(requireNotNull(density)) { requireNotNull(layout).layoutInput.style.fontSize.toDp() }.value

    private fun show(width: Dp, text: String, sp: Float) {
        composeRule.setContent {
            density = LocalDensity.current
            TmrFitText(
                text = text,
                modifier = Modifier.width(width),
                style = TextStyle(fontSize = sp.sp),
                onTextLayout = {
                    layout = it
                    layoutPasses++
                },
            )
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TOO_WIDE_WORD = "Extraordinarilylongwordthatcannotfit"
    }
}
