package com.hirehop.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hirehop.core.designsystem.R

object HhFontFamilies {
    val sans = FontFamily(
        Font(R.font.core_designsystem_anek_latin_regular, FontWeight.Normal),
        Font(R.font.core_designsystem_anek_latin_semibold, FontWeight.SemiBold),
        Font(R.font.core_designsystem_anek_latin_semibold, FontWeight.Bold),
        Font(R.font.core_designsystem_anek_latin_semibold, FontWeight.ExtraBold),
    )

    val mono = FontFamily(
        Font(R.font.core_designsystem_jetbrains_mono_regular, FontWeight.Medium),
    )
}

@Immutable
class HhTypography(
    val displayL: TextStyle,
    val displayM: TextStyle,
    val headlineL: TextStyle,
    val headlineM: TextStyle,
    val titleL: TextStyle,
    val titleM: TextStyle,
    val titleS: TextStyle,
    val bodyL: TextStyle,
    val bodyM: TextStyle,
    val labelL: TextStyle,
    val labelM: TextStyle,
    val button: TextStyle,
    val bodyS: TextStyle,
    val numeralHero: TextStyle,
    val numeralM: TextStyle,
    val factId: TextStyle,
)

internal object HhTypographyTokens {
    private val lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    )

    private fun sans(
        size: Int,
        line: Int,
        weight: FontWeight,
        tracking: TextUnit,
    ) = TextStyle(
        fontFamily = HhFontFamilies.sans,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = tracking,
        fontFeatureSettings = TABULAR_FIGURES,
        lineHeightStyle = lineHeightStyle,
    )

    val Default = HhTypography(
        displayL = sans(36, 40, FontWeight.ExtraBold, (-0.6 / 36).em),
        displayM = sans(32, 38, FontWeight.ExtraBold, (-0.5 / 32).em),
        headlineL = sans(28, 34, FontWeight.ExtraBold, (-0.4 / 28).em),
        headlineM = sans(22, 28, FontWeight.ExtraBold, (-0.01).em),
        titleL = sans(20, 26, FontWeight.ExtraBold, 0.em),
        titleM = sans(16, 22, FontWeight.Bold, 0.em),
        titleS = sans(14, 20, FontWeight.Bold, 0.em),
        bodyL = sans(15, 22, FontWeight.Normal, 0.em),
        bodyM = sans(14, 21, FontWeight.Normal, 0.em),
        labelL = sans(13, 18, FontWeight.SemiBold, 0.em),
        labelM = sans(12, 16, FontWeight.SemiBold, 0.em),
        button = sans(16, 20, FontWeight.Bold, 0.em),
        bodyS = sans(12, 16, FontWeight.Normal, 0.01.em),
        numeralHero = sans(44, 48, FontWeight.ExtraBold, (-0.02).em),
        numeralM = sans(18, 24, FontWeight.Bold, 0.em),
        factId = TextStyle(
            fontFamily = HhFontFamilies.mono,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.02.em,
            lineHeightStyle = lineHeightStyle,
        ),
    )
}

private const val TABULAR_FIGURES = "tnum"

val LocalHhTypography: ProvidableCompositionLocal<HhTypography> =
    staticCompositionLocalOf { HhTypographyTokens.Default }

internal fun HhTypography.toMaterial(): Typography = Typography(
    displayLarge = displayL,
    displayMedium = displayM,
    displaySmall = headlineL,
    headlineLarge = headlineL,
    headlineMedium = headlineM,
    headlineSmall = titleL,
    titleLarge = titleL,
    titleMedium = titleM,
    titleSmall = titleS,
    bodyLarge = bodyL,
    bodyMedium = bodyM,
    bodySmall = bodyS,
    labelLarge = labelL,
    labelMedium = labelM,
    labelSmall = labelM,
)
