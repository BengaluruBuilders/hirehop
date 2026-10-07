package com.hirehop.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hirehop.core.designsystem.R

@OptIn(ExperimentalTextApi::class)
private fun openSans(weight: FontWeight) = Font(
    R.font.core_designsystem_open_sans,
    weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

@OptIn(ExperimentalTextApi::class)
object HhFontFamilies {
    val sans = FontFamily(
        openSans(FontWeight.Normal),
        openSans(FontWeight.Medium),
        openSans(FontWeight.Bold),
        openSans(FontWeight.ExtraBold),
    )

    val mono = FontFamily(
        Font(R.font.core_designsystem_ibm_plex_mono_medium, FontWeight.Medium),
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
        displayL = sans(36, 42, FontWeight.Medium, (-0.02).em),
        displayM = sans(32, 39, FontWeight.Medium, (-0.02).em),
        headlineL = sans(28, 36, FontWeight.Medium, (-0.01).em),
        headlineM = sans(22, 30, FontWeight.Medium, (-0.01).em),
        titleL = sans(20, 28, FontWeight.Medium, 0.em),
        titleM = sans(16, 24, FontWeight.Bold, 0.em),
        titleS = sans(14, 21, FontWeight.Bold, 0.em),
        bodyL = sans(16, 24, FontWeight.Normal, 0.em),
        bodyM = sans(14, 22, FontWeight.Normal, 0.em),
        labelL = sans(13, 19, FontWeight.Bold, 0.em),
        labelM = sans(12, 18, FontWeight.Bold, 0.em),
        button = sans(15, 22, FontWeight.Bold, 0.04.em),
        bodyS = sans(12, 16, FontWeight.Normal, 0.01.em),
        numeralHero = sans(44, 52, FontWeight.ExtraBold, (-0.02).em),
        numeralM = sans(18, 26, FontWeight.ExtraBold, 0.em),
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
