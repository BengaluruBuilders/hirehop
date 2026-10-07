package com.hirehop.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.hirehop.core.designsystem.theme.HhColors
import com.hirehop.core.designsystem.theme.HhDarkColors
import com.hirehop.core.designsystem.theme.HhLightColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HhPaletteContrastTest {
    @Test
    fun appSurfacePaletteMatchesPlayScreenshotSamples() {
        with(HhLightColors) {
            assertEquals(Color(0xFFFFFFFF), background)
            assertEquals(Color(0xFFF4F6F1), card)
            assertEquals(Color(0xFF000000), header)
            assertEquals(Color(0xFFAAFF00), brand)
            assertEquals(Color(0xFF000000), onBrand)
        }
        with(HhDarkColors) {
            assertEquals(Color(0xFF000000), background)
            assertEquals(Color(0xFF141614), card)
            assertEquals(Color(0xFF252624), document)
            assertEquals(Color(0xFFAAFF00), brand)
            assertEquals(Color(0xFF000000), onBrand)
        }
    }

    @Test
    fun textAndActionPairsMeetNormalTextContrast() {
        listOf("light" to HhLightColors, "dark" to HhDarkColors).forEach { (mode, colors) ->
            pairs(colors).forEach { (name, foreground, background) ->
                val ratio = contrast(foreground, background)
                assertTrue("$mode $name contrast was $ratio", ratio >= 4.5f)
            }
        }
    }

    @Test
    fun statusBarIconPolarityFollowsTheTopSurface() {
        assertTrue(hhDarkStatusBarIcons(lightTop = true, colors = HhLightColors))
        assertFalse(hhDarkStatusBarIcons(lightTop = false, colors = HhLightColors))
        assertFalse(hhDarkStatusBarIcons(lightTop = true, colors = HhDarkColors))
        assertFalse(hhDarkStatusBarIcons(lightTop = false, colors = HhDarkColors))
    }

    private fun pairs(colors: HhColors): List<Triple<String, Color, Color>> = with(colors) {
        listOf(
            Triple("header", onHeader, header),
            Triple("header secondary", onHeaderVariant, header),
            Triple("header control", onHeaderControl, headerControl),
            Triple("brand action", onBrand, brand),
            Triple("special action", onSpecial, special),
            Triple("body", onSurface, background),
            Triple("secondary body", onSurfaceVariant, background),
            Triple("card secondary body", onSurfaceVariant, card),
            Triple("link", primary, background),
            Triple("primary", onPrimary, primary),
            Triple("primary container", onPrimaryContainer, primaryContainer),
            Triple("met", onMetContainer, metContainer),
            Triple("partial", onPartialContainer, partialContainer),
            Triple("gap", onGapContainer, gapContainer),
            Triple("dock", onToolVariant, tool),
            Triple("selected dock", onToolSelected, tool),
            Triple("error action", onError, error),
        )
    }

    private fun contrast(first: Color, second: Color): Float {
        val brighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (brighter + 0.05f) / (darker + 0.05f)
    }
}
