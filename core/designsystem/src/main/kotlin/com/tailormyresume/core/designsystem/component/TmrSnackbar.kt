package com.tailormyresume.core.designsystem.component

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
import com.tailormyresume.core.designsystem.theme.TmrTheme

enum class TmrSnackbarDuration { Standard, Extended }

@Composable
fun TmrSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    visible: Boolean = true,
) {
    TmrVisibility(visible = visible, modifier = modifier) {
        val colors = TmrTheme.colors
        Snackbar(
            modifier = Modifier.padding(TmrTheme.spacing.gutter),
            snackbarData = snackbarData,
            shape = TmrTheme.shapes.banner,
            containerColor = colors.inverseSurface,
            contentColor = colors.inverseOnSurface,
            actionContentColor = colors.inversePrimary,
            dismissActionContentColor = colors.inverseOnSurface,
        )
    }
}

suspend fun SnackbarHostState.showTmrSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: TmrSnackbarDuration = TmrSnackbarDuration.Standard,
): SnackbarResult = showSnackbar(
    message,
    actionLabel,
    withDismissAction,
    if (duration == TmrSnackbarDuration.Extended) SnackbarDuration.Long else SnackbarDuration.Short,
)

private class TmrPreviewSnackbarData(
    snackbarVisuals: SnackbarVisuals,
) : SnackbarData {
    override val visuals: SnackbarVisuals = snackbarVisuals
    override fun performAction(): Unit = Unit
    override fun dismiss(): Unit = Unit
}

private const val TMR_SNACKBAR_SAMPLE_MESSAGE = "Saved on this phone"
private const val TMR_SNACKBAR_SAMPLE_ACTION = "Undo"

@Preview(showBackground = true)
@Composable
private fun TmrSnackbarPreview() {
    TmrPreviewTheme(darkTheme = false) {
        TmrSnackbar(snackbarData = tmrSnackbarSampleData())
    }
}

@Preview(showBackground = true)
@Composable
private fun TmrSnackbarDarkPreview() {
    TmrPreviewTheme(darkTheme = true) {
        TmrSnackbar(snackbarData = tmrSnackbarSampleData())
    }
}

private fun tmrSnackbarSampleData(): SnackbarData = TmrPreviewSnackbarData(
    snackbarVisuals = object : SnackbarVisuals {
        override val message: String = TMR_SNACKBAR_SAMPLE_MESSAGE
        override val actionLabel: String? = TMR_SNACKBAR_SAMPLE_ACTION
        override val withDismissAction: Boolean = false
        override val duration: SnackbarDuration = SnackbarDuration.Short
    },
)
