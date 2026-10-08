package com.tailormyresume.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.tailormyresume.core.designsystem.theme.TmrColors
import com.tailormyresume.core.designsystem.theme.TmrDarkColors
import com.tailormyresume.core.designsystem.theme.TmrLightColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TmrPaletteContrastTest {
    @Test
    fun appSurfacePaletteMatchesTheClaudeDesignCanvas() {
        with(TmrLightColors) {
            assertEquals(Color(0xFFFFFFFF), background)
            assertEquals(Color(0xFFF4F6F1), card)
            assertEquals(Color(0xFFFFFFFF), header)
            assertEquals(Color(0xFFAEFF00), brand)
            assertEquals(Color(0xFF000000), onBrand)
        }
        with(TmrDarkColors) {
            assertEquals(Color(0xFF000000), background)
            assertEquals(Color(0xFF161817), card)
            assertEquals(Color(0xFF232524), document)
            assertEquals(Color(0xFFAEFF00), brand)
            assertEquals(Color(0xFF000000), onBrand)
        }
    }

    @Test
    fun textAndActionPairsMeetNormalTextContrast() {
        listOf("light" to TmrLightColors, "dark" to TmrDarkColors).forEach { (mode, colors) ->
            pairs(colors).forEach { (name, foreground, background) ->
                val ratio = contrast(foreground, background)
                assertTrue("$mode $name contrast was $ratio", ratio >= 4.5f)
            }
        }
    }

    @Test
    fun statusBarIconPolarityFollowsTheTopSurface() {
        assertTrue(tmrDarkStatusBarIcons(lightTop = true, colors = TmrLightColors))
        assertTrue(tmrDarkStatusBarIcons(lightTop = false, colors = TmrLightColors))
        assertFalse(tmrDarkStatusBarIcons(lightTop = true, colors = TmrDarkColors))
        assertFalse(tmrDarkStatusBarIcons(lightTop = false, colors = TmrDarkColors))
    }

    private fun pairs(colors: TmrColors): List<Triple<String, Color, Color>> = with(colors) {
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
