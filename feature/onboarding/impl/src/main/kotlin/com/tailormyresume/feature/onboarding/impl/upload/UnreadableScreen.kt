package com.tailormyresume.feature.onboarding.impl.upload

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.navigation.Navigator
import com.tailormyresume.feature.onboarding.impl.importresume.UploadFailure

@Composable
internal fun UnreadableScreen(
    failure: UploadFailure,
    onChooseAnotherClick: () -> Unit,
    onPasteClick: () -> Unit,
    modifier: Modifier = Modifier,
) = Unit

@Composable
internal fun UnreadableRoute(
    viewModel: UnreadableViewModel,
    navigator: Navigator,
    modifier: Modifier = Modifier,
) = Unit
