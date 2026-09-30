package com.hirehop.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

object HhTheme {
    val colors: HhColors
        @Composable
        @ReadOnlyComposable
        get() = LocalHhColors.current

    val typography: HhTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalHhTypography.current

    val spacing: HhSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalHhSpacing.current

    val shapes: HhShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalHhShapes.current

    val elevation: HhElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalHhElevation.current

    val motion: HhMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalHhMotion.current
}

val LightColorScheme = lightColorScheme(
    primary = HhLightPrimary,
    onPrimary = HhLightColors.onPrimary,
    primaryContainer = HhLightPrimaryContainer,
    onPrimaryContainer = HhLightColors.onPrimaryContainer,
    secondary = HhLightTeal,
    onSecondary = HhLightColors.onSecondary,
    secondaryContainer = HhLightSpot,
    onSecondaryContainer = HhLightSpotInk,
    tertiary = HhLightAccent,
    onTertiary = HhLightColors.onTertiary,
    tertiaryContainer = HhLightAccentContainer,
    onTertiaryContainer = HhLightAccent,
    error = HhLightError,
    onError = HhLightColors.onError,
    errorContainer = HhLightErrorContainer,
    onErrorContainer = HhLightOnErrorContainer,
    background = HhLightBackground,
    onBackground = HhLightInk,
    surface = HhLightSurface,
    onSurface = HhLightInk,
    surfaceVariant = HhLightSurface2,
    onSurfaceVariant = HhLightInk2,
    surfaceContainerLowest = HhLightSurface,
    surfaceContainerLow = HhLightBackground,
    surfaceContainer = HhLightSurface2,
    surfaceContainerHigh = HhLightSurface3,
    surfaceContainerHighest = HhLightSurface4,
    inverseSurface = HhExtendedDarkSurface1,
    inverseOnSurface = HhDarkInk,
    inversePrimary = HhDarkPrimary,
    outline = HhLightHairlineStrong,
    outlineVariant = HhLightHairline,
    scrim = HhScrim,
)

val DarkColorScheme = darkColorScheme(
    primary = HhDarkPrimary,
    onPrimary = HhDarkBackground,
    primaryContainer = HhDarkPrimaryContainer,
    onPrimaryContainer = HhDarkInk,
    secondary = HhDarkTeal,
    onSecondary = HhDarkBackground,
    secondaryContainer = HhDarkSpot,
    onSecondaryContainer = HhDarkSpotInk,
    tertiary = HhDarkAccent,
    onTertiary = HhDarkBackground,
    tertiaryContainer = HhDarkAccentContainer,
    onTertiaryContainer = HhDarkAccent,
    error = HhDarkError,
    onError = HhDarkBackground,
    errorContainer = HhDarkErrorContainer,
    onErrorContainer = HhDarkError,
    background = HhDarkBackground,
    onBackground = HhDarkInk,
    surface = HhDarkSurface,
    onSurface = HhDarkInk,
    surfaceVariant = HhDarkSurface2,
    onSurfaceVariant = HhDarkInk2,
    surfaceContainerLowest = HhDarkBackground,
    surfaceContainerLow = HhDarkSurface,
    surfaceContainer = HhDarkSurface2,
    surfaceContainerHigh = HhDarkSurface3,
    surfaceContainerHighest = HhDarkSurface4,
    inverseSurface = HhLightInk,
    inverseOnSurface = HhLightBackground,
    inversePrimary = HhLightPrimary,
    outline = HhDarkBorder,
    outlineVariant = HhDarkDivider,
    scrim = HhScrim,
)

@Composable
fun HhTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val hhColors = if (darkTheme) HhDarkColors else HhLightColors
    val backgroundTheme = BackgroundTheme(
        color = hhColors.background,
        tonalElevation = 0.dp,
    )
    CompositionLocalProvider(
        LocalHhColors provides hhColors,
        LocalBackgroundTheme provides backgroundTheme,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HhMaterialTypography,
            shapes = HhMaterialShapes,
            content = content,
        )
    }
}
