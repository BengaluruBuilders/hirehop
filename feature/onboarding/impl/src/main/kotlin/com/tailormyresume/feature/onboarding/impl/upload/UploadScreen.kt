package com.tailormyresume.feature.onboarding.impl.upload

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.navigation.Navigator

@Composable
internal fun UploadScreen(
    onUploadClick: () -> Unit,
    onPasteClick: () -> Unit,
    onManualClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Unit

@Composable
internal fun UploadRoute(
    viewModel: UploadViewModel,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) = Unit
