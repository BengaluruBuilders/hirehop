package com.tailormyresume.feature.settings.impl.deleteaccount

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun AccountDeletedRoute(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AccountDeletedScreen(onDone = onDone, modifier = modifier)
}
