package com.tailormyresume.core.designsystem.theme

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
import com.tailormyresume.core.designsystem.component.tmrReducedMotion

internal val LocalTmrDark = staticCompositionLocalOf { false }

object TmrTheme {
    val colors: TmrColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTmrColors.current

    val typography: TmrTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTmrTypography.current

    val spacing: TmrSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalTmrSpacing.current

    val shapes: TmrShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalTmrShapes.current

    val elevation: TmrElevation
        @Composable
        @ReadOnlyComposable
        get() = LocalTmrElevation.current

    val motion: TmrMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalTmrMotion.current

    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalTmrDark.current
}

internal fun TmrColors.toLightScheme(): ColorScheme = lightColorScheme(
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

internal fun TmrColors.toDarkScheme(): ColorScheme = darkColorScheme(
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

val LightColorScheme: ColorScheme = TmrLightColors.toLightScheme()

val DarkColorScheme: ColorScheme = TmrDarkColors.toDarkScheme()

@Composable
fun TmrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val tmrColors = if (darkTheme) TmrDarkColors else TmrLightColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val motion = if (tmrReducedMotion()) TmrMotionDefaults.Reduced else TmrMotionDefaults.Default
    CompositionLocalProvider(
        LocalTmrDark provides darkTheme,
        LocalTmrColors provides tmrColors,
        LocalTmrTypography provides TmrTypographyTokens.Default,
        LocalTmrSpacing provides TmrSpacingDefaults.Default,
        LocalTmrShapes provides TmrShapesDefaults.Default,
        LocalTmrElevation provides if (darkTheme) TmrElevationDefaults.Dark else TmrElevationDefaults.Light,
        LocalTmrMotion provides motion,
        LocalBackgroundTheme provides BackgroundTheme(color = tmrColors.background, tonalElevation = 0.dp),
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TmrTypographyTokens.Default.toMaterial(),
            shapes = TmrMaterialShapes,
            content = content,
        )
    }
}
