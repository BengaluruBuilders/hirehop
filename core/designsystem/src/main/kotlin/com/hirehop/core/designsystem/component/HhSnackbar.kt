package com.hirehop.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

enum class HhSnackbarDuration { Standard, Extended }

@Composable
fun HhSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    val fade = HhTheme.motion.proofSpecs.fade
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(fade),
        exit = fadeOut(fade),
    ) {
        val colors = HhTheme.colors
        Snackbar(
            modifier = Modifier.padding(HhTheme.spacing.gutter),
            snackbarData = snackbarData,
            shape = HhTheme.shapes.banner,
            containerColor = colors.inverseSurface,
            contentColor = colors.inverseOnSurface,
            actionContentColor = colors.inversePrimary,
            dismissActionContentColor = colors.inverseOnSurface,
        )
    }
}

suspend fun SnackbarHostState.showHhSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: HhSnackbarDuration = HhSnackbarDuration.Standard,
): SnackbarResult = showSnackbar(
    message,
    actionLabel,
    withDismissAction,
    if (duration == HhSnackbarDuration.Extended) SnackbarDuration.Long else SnackbarDuration.Short,
)

private class HhPreviewSnackbarData(
    snackbarVisuals: SnackbarVisuals,
) : SnackbarData {
    override val visuals: SnackbarVisuals = snackbarVisuals
    override fun performAction(): Unit = Unit
    override fun dismiss(): Unit = Unit
}

private const val HH_SNACKBAR_SAMPLE_MESSAGE = "Saved on this phone"
private const val HH_SNACKBAR_SAMPLE_ACTION = "Undo"

@Preview(showBackground = true)
@Composable
private fun HhSnackbarPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhSnackbar(snackbarData = hhSnackbarSampleData())
    }
}

@Preview(showBackground = true)
@Composable
private fun HhSnackbarDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhSnackbar(snackbarData = hhSnackbarSampleData())
    }
}

private fun hhSnackbarSampleData(): SnackbarData = HhPreviewSnackbarData(
    snackbarVisuals = object : SnackbarVisuals {
        override val message: String = HH_SNACKBAR_SAMPLE_MESSAGE
        override val actionLabel: String? = HH_SNACKBAR_SAMPLE_ACTION
        override val withDismissAction: Boolean = false
        override val duration: SnackbarDuration = SnackbarDuration.Short
    },
)
