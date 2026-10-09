package com.tailormyresume.core.designsystem.component

import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
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

    @Test
    @Config(fontScale = 2f)
    fun atLargeFontTheTextNeverRendersBelowItsBaseSize() {
        showTooWideWord()

        assertTrue(renderedSp() * 2f >= BASE_SP)
    }

    @Test
    @Config(fontScale = 1f)
    fun atDefaultFontTheTextIsNotShrunkAtAll() {
        showTooWideWord()

        assertEquals(BASE_SP, renderedSp(), 0f)
    }

    private fun renderedSp(): Float = requireNotNull(layout).layoutInput.style.fontSize.value

    private fun showTooWideWord() {
        composeRule.setContent {
            TmrFitText(
                text = "Extraordinarilylongwordthatcannotfit",
                modifier = Modifier.width(60.dp),
                style = TextStyle(fontSize = BASE_SP.sp),
                onTextLayout = { layout = it },
            )
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val BASE_SP = 18f
    }
}
