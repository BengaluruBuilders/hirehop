package com.tailormyresume.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val Placeholder = Color.Unspecified
val TmrColors.surfaceRaised: Color get() = Placeholder
val TmrColors.surfaceHigh: Color get() = Placeholder
val TmrColors.sheetOption: Color get() = Placeholder
val TmrColors.uploadCard: Color get() = Placeholder
val TmrColors.fill: Color get() = Placeholder
val TmrColors.disabledFill: Color get() = Placeholder
val TmrColors.line: Color get() = Placeholder
val TmrColors.lineStrong: Color get() = Placeholder
val TmrColors.lineHigher: Color get() = Placeholder
val TmrColors.tabDivider: Color get() = Placeholder
val TmrColors.text: Color get() = Placeholder
val TmrColors.textSecondary: Color get() = Placeholder
val TmrColors.textMuted: Color get() = Placeholder
val TmrColors.textDisabled: Color get() = Placeholder
val TmrColors.ink: Color get() = Placeholder
val TmrColors.lime: Color get() = Placeholder
val TmrColors.limeSelected: Color get() = Placeholder
val TmrColors.limeSoft: Color get() = Placeholder
val TmrColors.amber: Color get() = Placeholder
val TmrColors.amberHighlight: Color get() = Placeholder
val TmrColors.blue: Color get() = Placeholder
val TmrColors.cheek: Color get() = Placeholder
val TmrColors.paper: Color get() = Placeholder
val TmrColors.paperFold: Color get() = Placeholder
val TmrColors.segOffer: Color get() = Placeholder
val TmrColors.segRejected: Color get() = Placeholder
val TmrColors.rejectedBorder: Color get() = Placeholder

private val PlaceholderStyle = TextStyle.Default
val TmrTypography.headline: TextStyle get() = PlaceholderStyle
val TmrTypography.headlineSmall: TextStyle get() = PlaceholderStyle
val TmrTypography.title: TextStyle get() = PlaceholderStyle
val TmrTypography.display: TextStyle get() = PlaceholderStyle
val TmrTypography.displayLarge: TextStyle get() = PlaceholderStyle
val TmrTypography.label: TextStyle get() = PlaceholderStyle
val TmrTypography.labelWide: TextStyle get() = PlaceholderStyle
val TmrTypography.mono15: TextStyle get() = PlaceholderStyle
val TmrTypography.mono14: TextStyle get() = PlaceholderStyle
val TmrTypography.body: TextStyle get() = PlaceholderStyle
val TmrTypography.bodyLarge: TextStyle get() = PlaceholderStyle
val TmrTypography.bodySmall: TextStyle get() = PlaceholderStyle
val TmrTypography.caption: TextStyle get() = PlaceholderStyle
val TmrTypography.strongLarge: TextStyle get() = PlaceholderStyle
val TmrTypography.strongSmall: TextStyle get() = PlaceholderStyle

private val PlaceholderShape = RoundedCornerShape(0.dp)
val TmrShapes.hero get() = PlaceholderShape
val TmrShapes.cardLarge get() = PlaceholderShape
val TmrShapes.cardSmall get() = PlaceholderShape
val TmrShapes.toast get() = PlaceholderShape
val TmrShapes.paperCorner get() = PlaceholderShape
val TmrShapes.bar get() = PlaceholderShape

private val PlaceholderDp: Dp = 0.dp
val TmrSpacing.topBarButton: Dp get() = PlaceholderDp
val TmrSpacing.primaryButtonHeight: Dp get() = PlaceholderDp
val TmrSpacing.tabItem: Dp get() = PlaceholderDp
val TmrSpacing.tabCentreDisc: Dp get() = PlaceholderDp
val TmrSpacing.toastTop: Dp get() = PlaceholderDp
val TmrSpacing.sheetPaddingTop: Dp get() = PlaceholderDp
val TmrSpacing.sheetPaddingHorizontal: Dp get() = PlaceholderDp
val TmrSpacing.sheetPaddingBottom: Dp get() = PlaceholderDp
val TmrSpacing.sheetHandleWidth: Dp get() = PlaceholderDp
val TmrSpacing.sheetHandleHeight: Dp get() = PlaceholderDp

class TmrIdleSpecs {
    val bobAmplitude: Dp = 0.dp
    val bobAngularPeriodMs = 0f
    val bobPhaseMax = 0
    val wobbleDegrees = 0f
    val wobbleAngularPeriodMs = 0f
    val wobbleOffset: Dp = 0.dp
    val wobbleOffsetAngularPeriodMs = 0f
    val storyDurationMs = 0
    val spinnerTurnMs = 0
    val storyAutoAdvance = false
    val spinnerAnimated = false
    fun bobOffsetDp(timeMs: Long, phase: Int): Float = 0f
}

val TmrMotion.idle: TmrIdleSpecs get() = TmrIdleSpecs()
