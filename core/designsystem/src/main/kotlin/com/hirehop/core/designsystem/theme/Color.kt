package com.hirehop.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

internal val HhLightBackground = Color(0xFFF7F4EF)
internal val HhLightSurface = Color(0xFFFFFFFF)
internal val HhLightSurface2 = Color(0xFFF2EDE3)
internal val HhLightSurface3 = Color(0xFFEDE6D9)
internal val HhLightSurface4 = Color(0xFFE7DFD0)
internal val HhLightInk = Color(0xFF16130F)
internal val HhLightInk2 = Color(0xFF55504A)
internal val HhLightInk3 = Color(0xFF8C8880)
internal val HhLightHairline = Color(0xFFEAE5DD)
internal val HhLightHairlineStrong = Color(0xFFD5CFC4)
internal val HhLightPrimary = Color(0xFF5A3FD6)
internal val HhLightPrimaryHover = Color(0xFF4C34B8)
internal val HhLightPrimaryPressed = Color(0xFF3F2B98)
internal val HhLightPrimaryContainer = Color(0xFFEBE8FA)
internal val HhLightAccent = Color(0xFFE2703A)
internal val HhLightAccentContainer = Color(0xFFFCE9DF)
internal val HhLightTeal = Color(0xFF177E89)
internal val HhLightTrust = Color(0xFF177E89)
internal val HhLightSuccess = Color(0xFF1E7A4F)
internal val HhLightSuccessContainer = Color(0xFFDCEFE3)
internal val HhLightWarning = Color(0xFF8A5300)
internal val HhLightWarningContainer = Color(0xFFFFF6E5)
internal val HhLightOnWarningContainer = Color(0xFF8A5300)
internal val HhLightWarningBorder = Color(0xFFF2D9A8)
internal val HhLightError = Color(0xFFB3261E)
internal val HhLightErrorContainer = Color(0xFFFDE7E5)
internal val HhLightOnErrorContainer = Color(0xFFB3261E)
internal val HhLightSpot = Color(0xFFF2EDE3)
internal val HhLightSpotInk = Color(0xFF7A746A)
internal val HhLightGap = Color(0xFFA8542F)
internal val HhLightGapContainer = Color(0xFFF7DED2)
internal val HhLightOnGapContainer = Color(0xFFA8542F)
internal val HhLightEvidence = Color(0xFFF8E36F)
internal val HhLightEvidenceContainer = Color(0xFFFBF3C4)
internal val HhLightOnEvidenceContainer = Color(0xFF16130F)
internal val HhLightHaptics = Color(0xFFF2EDE3)
internal val HhLightOnPrimary = Color(0xFFFFFFFF)
internal val HhLightOnSecondary = Color(0xFFFFFFFF)
internal val HhLightOnTertiary = Color(0xFFFFFFFF)
internal val HhLightOnAccent = Color(0xFFFFFFFF)
internal val HhLightOnSuccess = Color(0xFFFFFFFF)
internal val HhLightOnWarning = Color(0xFFFFFFFF)
internal val HhLightOnError = Color(0xFFFFFFFF)

internal val HhDarkBackground = Color(0xFF12100D)
internal val HhDarkSurface = Color(0xFF1B1814)
internal val HhDarkSurface2 = Color(0xFF221F1A)
internal val HhDarkSurface3 = Color(0xFF2A2620)
internal val HhDarkSurface4 = Color(0xFF33302A)
internal val HhDarkBorder = Color(0xFF4A453D)
internal val HhDarkDivider = Color(0xFF3D3932)
internal val HhDarkInk = Color(0xFFF3EFE8)
internal val HhDarkInk2 = Color(0xFFC4BEB2)
internal val HhDarkInk3 = Color(0xFF938C7E)
internal val HhDarkPrimary = Color(0xFFB6A6F5)
internal val HhDarkPrimaryHover = Color(0xFFC6B8F8)
internal val HhDarkPrimaryPressed = Color(0xFFA692EC)
internal val HhDarkPrimaryContainer = Color(0xFF37323D)
internal val HhDarkAccent = Color(0xFFB0A9FF)
internal val HhDarkAccentContainer = Color(0xFF3A3550)
internal val HhDarkTeal = Color(0xFF7AB1B4)
internal val HhDarkTrust = Color(0xFF7AB1B4)
internal val HhDarkSuccess = Color(0xFF7FD1A0)
internal val HhDarkSuccessContainer = Color(0xFF2D392D)
internal val HhDarkWarning = Color(0xFFF2D9A8)
internal val HhDarkWarningContainer = Color(0xFF4C4436)
internal val HhDarkWarningBorder = Color(0xFF6B604C)
internal val HhDarkError = Color(0xFFFFB4AB)
internal val HhDarkErrorContainer = Color(0xFF44342F)
internal val HhDarkSpot = Color(0xFF2A2620)
internal val HhDarkSpotInk = Color(0xFF938C7E)
internal val HhDarkGap = Color(0xFFE39A72)
internal val HhDarkGapContainer = Color(0xFF4A3125)
internal val HhDarkOnGapContainer = Color(0xFFF7DED2)
internal val HhDarkEvidence = Color(0xFFC9B43F)
internal val HhDarkEvidenceContainer = Color(0xFF4A4220)
internal val HhDarkOnEvidenceContainer = Color(0xFFF3EFE8)
internal val HhDarkHaptics = Color(0xFF2A2620)
internal val HhDarkOnPrimary = Color(0xFF12100D)
internal val HhDarkOnSecondary = Color(0xFF12100D)
internal val HhDarkOnTertiary = Color(0xFF12100D)
internal val HhDarkOnAccent = Color(0xFF12100D)
internal val HhDarkOnSuccess = Color(0xFF12100D)
internal val HhDarkOnWarning = Color(0xFF12100D)
internal val HhDarkOnError = Color(0xFF12100D)

internal val HhExtendedDarkSurface1 = Color(0xFF221F1A)
internal val HhExtendedDarkSurface2 = Color(0xFF2A2620)
internal val HhExtendedDarkSurface3 = Color(0xFF33302A)
internal val HhExtendedDarkSurface4 = Color(0xFF4A453D)
internal val HhExtendedDarkDivider = Color(0xFF3D3932)
internal val HhExtendedDarkAccent = Color(0xFFB0A9FF)

internal val HhScrim = Color(0x99000000)

@Immutable
data class HhColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val surfaceVariant: Color,
    val surface2: Color,
    val surface3: Color,
    val surface4: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val surfaceContainerBorder: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val hairline: Color,
    val hairlineStrong: Color,
    val outline: Color,
    val outlineVariant: Color,
    val primary: Color,
    val primaryHover: Color,
    val primaryPressed: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
    val accent: Color,
    val onAccent: Color,
    val teal: Color,
    val trust: Color,
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val warningBorder: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val spot: Color,
    val spotContainer: Color,
    val spotInk: Color,
    val onSpot: Color,
    val onSpotVariant: Color,
    val onSpotContainer: Color,
    val gap: Color,
    val gapContainer: Color,
    val onGapContainer: Color,
    val evidence: Color,
    val evidenceContainer: Color,
    val onEvidenceContainer: Color,
    val haptics: Color,
    val scrim: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val inversePrimary: Color,
    val extendedDarkSurface1: Color,
    val extendedDarkSurface2: Color,
    val extendedDarkSurface3: Color,
    val extendedDarkSurface4: Color,
    val extendedDarkDivider: Color,
    val extendedDarkAccent: Color,
)

internal val HhLightColors = HhColors(
    background = HhLightBackground,
    onBackground = HhLightInk,
    surface = HhLightSurface,
    onSurface = HhLightInk,
    onSurfaceVariant = HhLightInk2,
    surfaceVariant = HhLightSurface2,
    surface2 = HhLightSurface2,
    surface3 = HhLightSurface3,
    surface4 = HhLightSurface4,
    surfaceContainerLowest = HhLightSurface,
    surfaceContainerLow = HhLightBackground,
    surfaceContainer = HhLightSurface2,
    surfaceContainerHigh = HhLightSurface3,
    surfaceContainerHighest = HhLightSurface4,
    surfaceContainerBorder = HhLightHairlineStrong,
    ink = HhLightInk,
    ink2 = HhLightInk2,
    ink3 = HhLightInk3,
    hairline = HhLightHairline,
    hairlineStrong = HhLightHairlineStrong,
    outline = HhLightHairlineStrong,
    outlineVariant = HhLightHairline,
    primary = HhLightPrimary,
    primaryHover = HhLightPrimaryHover,
    primaryPressed = HhLightPrimaryPressed,
    onPrimary = HhLightOnPrimary,
    primaryContainer = HhLightPrimaryContainer,
    onPrimaryContainer = HhLightPrimaryPressed,
    secondary = HhLightTeal,
    onSecondary = HhLightOnSecondary,
    secondaryContainer = HhLightSpot,
    onSecondaryContainer = HhLightSpotInk,
    tertiary = HhLightAccent,
    onTertiary = HhLightOnTertiary,
    tertiaryContainer = HhLightAccentContainer,
    onTertiaryContainer = HhLightAccent,
    accent = HhLightAccent,
    onAccent = HhLightOnAccent,
    teal = HhLightTeal,
    trust = HhLightTrust,
    success = HhLightSuccess,
    onSuccess = HhLightOnSuccess,
    successContainer = HhLightSuccessContainer,
    onSuccessContainer = HhLightSuccess,
    warning = HhLightWarning,
    onWarning = HhLightOnWarning,
    warningContainer = HhLightWarningContainer,
    onWarningContainer = HhLightOnWarningContainer,
    warningBorder = HhLightWarningBorder,
    error = HhLightError,
    onError = HhLightOnError,
    errorContainer = HhLightErrorContainer,
    onErrorContainer = HhLightOnErrorContainer,
    spot = HhLightSpot,
    spotContainer = HhLightSpot,
    spotInk = HhLightSpotInk,
    onSpot = HhLightSpotInk,
    onSpotVariant = HhLightInk2,
    onSpotContainer = HhLightInk,
    gap = HhLightGap,
    gapContainer = HhLightGapContainer,
    onGapContainer = HhLightOnGapContainer,
    evidence = HhLightEvidence,
    evidenceContainer = HhLightEvidenceContainer,
    onEvidenceContainer = HhLightOnEvidenceContainer,
    haptics = HhLightHaptics,
    scrim = HhScrim,
    inverseSurface = HhExtendedDarkSurface1,
    inverseOnSurface = HhDarkInk,
    inversePrimary = HhDarkPrimary,
    extendedDarkSurface1 = HhExtendedDarkSurface1,
    extendedDarkSurface2 = HhExtendedDarkSurface2,
    extendedDarkSurface3 = HhExtendedDarkSurface3,
    extendedDarkSurface4 = HhExtendedDarkSurface4,
    extendedDarkDivider = HhExtendedDarkDivider,
    extendedDarkAccent = HhExtendedDarkAccent,
)

internal val HhDarkColors = HhColors(
    background = HhDarkBackground,
    onBackground = HhDarkInk,
    surface = HhDarkSurface,
    onSurface = HhDarkInk,
    onSurfaceVariant = HhDarkInk2,
    surfaceVariant = HhDarkSurface2,
    surface2 = HhDarkSurface2,
    surface3 = HhDarkSurface3,
    surface4 = HhDarkSurface4,
    surfaceContainerLowest = HhDarkBackground,
    surfaceContainerLow = HhDarkSurface,
    surfaceContainer = HhDarkSurface2,
    surfaceContainerHigh = HhDarkSurface2,
    surfaceContainerHighest = HhDarkSurface3,
    surfaceContainerBorder = HhDarkBorder,
    ink = HhDarkInk,
    ink2 = HhDarkInk2,
    ink3 = HhDarkInk3,
    hairline = HhDarkDivider,
    hairlineStrong = HhDarkBorder,
    outline = HhDarkBorder,
    outlineVariant = HhDarkDivider,
    primary = HhDarkPrimary,
    primaryHover = HhDarkPrimaryHover,
    primaryPressed = HhDarkPrimaryPressed,
    onPrimary = HhDarkOnPrimary,
    primaryContainer = HhDarkPrimaryContainer,
    onPrimaryContainer = HhDarkInk,
    secondary = HhDarkTeal,
    onSecondary = HhDarkOnSecondary,
    secondaryContainer = HhDarkSpot,
    onSecondaryContainer = HhDarkSpotInk,
    tertiary = HhDarkAccent,
    onTertiary = HhDarkOnTertiary,
    tertiaryContainer = HhDarkAccentContainer,
    onTertiaryContainer = HhDarkAccent,
    accent = HhDarkAccent,
    onAccent = HhDarkOnAccent,
    teal = HhDarkTeal,
    trust = HhDarkTrust,
    success = HhDarkSuccess,
    onSuccess = HhDarkOnSuccess,
    successContainer = HhDarkSuccessContainer,
    onSuccessContainer = HhDarkSuccess,
    warning = HhDarkWarning,
    onWarning = HhDarkOnWarning,
    warningContainer = HhDarkWarningContainer,
    onWarningContainer = HhDarkWarning,
    warningBorder = HhDarkWarningBorder,
    error = HhDarkError,
    onError = HhDarkOnError,
    errorContainer = HhDarkErrorContainer,
    onErrorContainer = HhDarkError,
    spot = HhDarkSpot,
    spotContainer = HhDarkSpot,
    spotInk = HhDarkSpotInk,
    onSpot = HhDarkSpotInk,
    onSpotVariant = HhDarkInk2,
    onSpotContainer = HhDarkInk,
    gap = HhDarkGap,
    gapContainer = HhDarkGapContainer,
    onGapContainer = HhDarkOnGapContainer,
    evidence = HhDarkEvidence,
    evidenceContainer = HhDarkEvidenceContainer,
    onEvidenceContainer = HhDarkOnEvidenceContainer,
    haptics = HhDarkHaptics,
    scrim = HhScrim,
    inverseSurface = HhLightInk,
    inverseOnSurface = HhLightBackground,
    inversePrimary = HhLightPrimary,
    extendedDarkSurface1 = HhExtendedDarkSurface1,
    extendedDarkSurface2 = HhExtendedDarkSurface2,
    extendedDarkSurface3 = HhExtendedDarkSurface3,
    extendedDarkSurface4 = HhExtendedDarkSurface4,
    extendedDarkDivider = HhExtendedDarkDivider,
    extendedDarkAccent = HhExtendedDarkAccent,
)

val LocalHhColors: ProvidableCompositionLocal<HhColors> =
    staticCompositionLocalOf { HhLightColors }
