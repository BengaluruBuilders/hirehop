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
import androidx.compose.ui.text.style.LineHeightStyle.Alignment
import androidx.compose.ui.text.style.LineHeightStyle.Trim
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hirehop.core.designsystem.R

internal val HhBricolageGrotesque = FontFamily(
    Font(R.font.core_designsystem_bricolage_grotesque_semibold, FontWeight.SemiBold),
)

internal val HhAnekLatin = FontFamily(
    Font(R.font.core_designsystem_anek_latin_regular, FontWeight.Normal),
    Font(R.font.core_designsystem_anek_latin_semibold, FontWeight.SemiBold),
)

internal val HhJetBrainsMono = FontFamily(
    Font(R.font.core_designsystem_jetbrains_mono_regular, FontWeight.Normal),
)

@Immutable
data class HhTypography(
    val heroNumeral: TextStyle,
    val displayLarge: TextStyle,
    val displaySmall: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle,
    val mono: TextStyle,
    val monoSmall: TextStyle,
    val monoLarge: TextStyle,
)

internal object HhTypographyTokens {
    private val lineHeightStyle = LineHeightStyle(alignment = Alignment.Center, trim = Trim.None)

    val Default = HhTypography(
        heroNumeral = TextStyle(
            fontFamily = HhBricolageGrotesque,
            fontWeight = FontWeight.SemiBold,
            fontSize = 40.sp,
            lineHeight = 40.sp,
            letterSpacing = (-0.02).em,
            lineHeightStyle = lineHeightStyle,
        ),
        displayLarge = TextStyle(
            fontFamily = HhBricolageGrotesque,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.01).em,
            lineHeightStyle = lineHeightStyle,
        ),
        displaySmall = TextStyle(
            fontFamily = HhBricolageGrotesque,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        headlineSmall = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 26.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        titleLarge = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        titleMedium = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        titleSmall = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        bodyLarge = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        bodyMedium = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        bodySmall = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        labelLarge = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        labelMedium = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        labelSmall = TextStyle(
            fontFamily = HhAnekLatin,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.04.em,
            lineHeightStyle = lineHeightStyle,
        ),
        mono = TextStyle(
            fontFamily = HhJetBrainsMono,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        monoSmall = TextStyle(
            fontFamily = HhJetBrainsMono,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            lineHeightStyle = lineHeightStyle,
        ),
        monoLarge = TextStyle(
            fontFamily = HhJetBrainsMono,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            lineHeightStyle = lineHeightStyle,
        ),
    )
}

val LocalHhTypography: ProvidableCompositionLocal<HhTypography> =
    staticCompositionLocalOf { HhTypographyTokens.Default }

internal val HhMaterialTypography = Typography(
    displayLarge = HhTypographyTokens.Default.displayLarge,
    displaySmall = HhTypographyTokens.Default.displaySmall,
    headlineSmall = HhTypographyTokens.Default.headlineSmall,
    titleLarge = HhTypographyTokens.Default.titleLarge,
    titleMedium = HhTypographyTokens.Default.titleMedium,
    titleSmall = HhTypographyTokens.Default.titleSmall,
    bodyLarge = HhTypographyTokens.Default.bodyLarge,
    bodyMedium = HhTypographyTokens.Default.bodyMedium,
    bodySmall = HhTypographyTokens.Default.bodySmall,
    labelLarge = HhTypographyTokens.Default.labelLarge,
    labelMedium = HhTypographyTokens.Default.labelMedium,
    labelSmall = HhTypographyTokens.Default.labelSmall,
)
