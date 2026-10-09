package com.tailormyresume.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.tailormyresume.core.designsystem.theme.TmrColors
import com.tailormyresume.core.designsystem.theme.TmrDarkColors
import com.tailormyresume.core.designsystem.theme.TmrLightColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TmrBoundaryContrastTest {
    @Test
    fun boundaryTokenIsThreeToOneOnSurfaceAndCard() {
        listOf("light" to TmrLightColors, "dark" to TmrDarkColors).forEach { (mode, colors) ->
            surfaces(colors).forEach { (name, color) ->
                val ratio = contrast(colors.boundary, color)
                assertTrue("$mode boundary on $name contrast was $ratio", ratio >= 3.0f)
            }
        }
    }

    @Test
    fun outlineButtonAndTextFieldUseBoundary() {
        assertNotEquals(TmrLightColors.boundary, TmrLightColors.outlineVariant)
        assertNotEquals(TmrDarkColors.boundary, TmrDarkColors.outlineVariant)
        assertEquals(Color(0xFFD5D8D2), TmrLightColors.outlineVariant)
        assertEquals(Color(0xFF3A3D3B), TmrDarkColors.outlineVariant)
    }

    @Test
    fun disabledOutlineLabelKeepsNormalTextContrast() {
        listOf("light" to TmrLightColors, "dark" to TmrDarkColors).forEach { (mode, colors) ->
            listOf("background" to colors.background, "card" to colors.card).forEach { (name, color) ->
                val ratio = contrast(colors.onSurfaceVariant, color)
                assertTrue("$mode disabled outline label on $name contrast was $ratio", ratio >= 4.5f)
            }
        }
    }

    private fun surfaces(colors: TmrColors): List<Pair<String, Color>> = with(colors) {
        listOf(
            "background" to background,
            "surface" to surface,
            "card" to card,
            "sheet" to sheet,
        )
    }

    private fun contrast(first: Color, second: Color): Float {
        val brighter = maxOf(first.luminance(), second.luminance())
        val darker = minOf(first.luminance(), second.luminance())
        return (brighter + 0.05f) / (darker + 0.05f)
    }
}
