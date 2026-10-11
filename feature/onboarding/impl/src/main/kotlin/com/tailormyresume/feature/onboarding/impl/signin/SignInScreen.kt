package com.tailormyresume.feature.onboarding.impl.signin

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

internal object SignInTags {
    const val STORY = "signin_story"
    const val STORY_TOUCH = "signin_story_touch"
}

@Composable
internal fun SignInRoute(viewModel: SignInViewModel, modifier: Modifier = Modifier) {
    Box(modifier)
}

@Composable
internal fun SignInScreen(
    uiState: SignInUiState,
    onContinueWithGoogle: () -> Unit,
    modifier: Modifier = Modifier,
    story: SignInStoryState = rememberSignInStoryState(uiState == SignInUiState.SigningIn),
) {
    Box(modifier)
}
