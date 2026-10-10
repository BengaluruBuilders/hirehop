package com.tailormyresume.core.designsystem.component.hero

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.theme.TmrMotion

enum class TmrPaigePose { Signin1, Signin2, Signin3, Upload, Job, Question, Tailoring, Fail, Home, Paywall, Delete }

internal class TmrPaigeSpec(
    val eyeWidth: Dp,
    val eyeHeight: Dp,
    val eyeShiftX: Dp,
    val eyeShiftY: Dp,
    val mouthWidth: Dp,
    val mouthHeight: Dp,
    val ticks: Boolean,
    val arms: Boolean,
    val sad: Boolean,
    val rotationDegrees: Float,
    val shiftX: Dp,
    val lift: Dp,
    val bobPhase: Int?,
    val wobble: Boolean,
)

internal fun TmrPaigePose.spec(): TmrPaigeSpec = TmrPaigeSpec(0.dp, 0.dp, 0.dp, 0.dp, 0.dp, 0.dp, false, false, false, 0f, 0.dp, 0.dp, null, false)

internal fun paigeTranslationYDp(motion: TmrMotion, spec: TmrPaigeSpec, timeMs: Long): Float = 0f

internal fun paigeRotationDegrees(motion: TmrMotion, spec: TmrPaigeSpec, timeMs: Long): Float = 0f

@Composable
internal fun rememberPaigeTimeMs(motion: TmrMotion): () -> Long = { -1L }

@Composable
fun TmrPaige(pose: TmrPaigePose, modifier: Modifier = Modifier) = Unit
