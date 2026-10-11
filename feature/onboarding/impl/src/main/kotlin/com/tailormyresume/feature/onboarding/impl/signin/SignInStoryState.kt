package com.tailormyresume.feature.onboarding.impl.signin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

internal const val SIGN_IN_STORY_COUNT = 3

internal const val STORY_HOLD_THRESHOLD_MS = 250L

@Stable
internal class SignInStoryState(private val durationMs: Int = 5000) {
    var index by mutableIntStateOf(0)
        private set

    var fraction by mutableFloatStateOf(0f)
        private set

    var paused by mutableStateOf(false)
        private set

    fun tick(dtMs: Long) = Unit

    fun step(delta: Int) = Unit

    fun onDown() = Unit

    fun onUp(heldMs: Long, x: Float, width: Float) = Unit

    fun onCancel() = Unit
}

@Composable
internal fun rememberSignInStoryState(signingIn: Boolean): SignInStoryState = remember { SignInStoryState() }
