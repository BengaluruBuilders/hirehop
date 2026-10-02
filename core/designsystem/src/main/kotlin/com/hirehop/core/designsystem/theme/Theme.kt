package com.hirehop.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.hhReducedMotion

internal val LocalHhDark = staticCompositionLocalOf { false }

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

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalHhDark.current
}

internal fun HhColors.toLightScheme(): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = primary,
    onSecondary = onPrimary,
    secondaryContainer = primaryContainer,
    onSecondaryContainer = onPrimaryContainer,
    tertiary = special,
    onTertiary = onSpecial,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
    background = background,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = card,
    onSurfaceVariant = onSurfaceVariant,
    surfaceContainerLowest = document,
    surfaceContainerLow = card,
    surfaceContainer = ground,
    surfaceContainerHigh = card,
    surfaceContainerHighest = card,
    inverseSurface = inverseSurface,
    inverseOnSurface = inverseOnSurface,
    inversePrimary = inversePrimary,
    outline = outline,
    outlineVariant = outlineVariant,
    scrim = scrim,
)

internal fun HhColors.toDarkScheme(): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimaryContainer,
    secondary = primary,
    onSecondary = onPrimary,
    secondaryContainer = primaryContainer,
    onSecondaryContainer = onPrimaryContainer,
    tertiary = special,
    onTertiary = onSpecial,
    error = error,
    onError = onError,
    errorContainer = errorContainer,
    onErrorContainer = onErrorContainer,
    background = background,
    onBackground = onSurface,
    surface = surface,
    onSurface = onSurface,
    surfaceVariant = card,
    onSurfaceVariant = onSurfaceVariant,
    surfaceBright = document,
    surfaceContainerLowest = ground,
    surfaceContainerLow = card,
    surfaceContainer = card,
    surfaceContainerHigh = tool,
    surfaceContainerHighest = tool,
    inverseSurface = inverseSurface,
    inverseOnSurface = inverseOnSurface,
    inversePrimary = inversePrimary,
    outline = outline,
    outlineVariant = outlineVariant,
    scrim = scrim,
)

val LightColorScheme: ColorScheme = HhLightColors.toLightScheme()

val DarkColorScheme: ColorScheme = HhDarkColors.toDarkScheme()

@Composable
fun HhTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val hhColors = if (darkTheme) HhDarkColors else HhLightColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val motion = if (hhReducedMotion()) HhMotionDefaults.Reduced else HhMotionDefaults.Default
    CompositionLocalProvider(
        LocalHhDark provides darkTheme,
        LocalHhColors provides hhColors,
        LocalHhTypography provides HhTypographyTokens.Default,
        LocalHhSpacing provides HhSpacingDefaults.Default,
        LocalHhShapes provides HhShapesDefaults.Default,
        LocalHhElevation provides if (darkTheme) HhElevationDefaults.Dark else HhElevationDefaults.Light,
        LocalHhMotion provides motion,
        LocalBackgroundTheme provides BackgroundTheme(color = hhColors.background, tonalElevation = 0.dp),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HhTypographyTokens.Default.toMaterial(),
            shapes = HhMaterialShapes,
            content = content,
        )
    }
}
