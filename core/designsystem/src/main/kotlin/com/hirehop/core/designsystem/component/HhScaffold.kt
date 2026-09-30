package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = { HhScaffoldDefaultSnackbarHost() },
    floatingActionButton: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = bottomBar,
        snackbarHost = snackbarHost,
        floatingActionButton = floatingActionButton,
        contentWindowInsets = contentWindowInsets,
        containerColor = HhTheme.colors.background,
        content = content,
    )
}

@Composable
private fun HhScaffoldDefaultSnackbarHost() {
    val hostState = remember { SnackbarHostState() }
    SnackbarHost(hostState = hostState) { snackbarData ->
        HhSnackbar(snackbarData = snackbarData)
    }
}

@Preview(showBackground = true)
@Composable
private fun HhScaffoldPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhScaffold {}
    }
}

@Preview(showBackground = true)
@Composable
private fun HhScaffoldDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhScaffold {}
    }
}
