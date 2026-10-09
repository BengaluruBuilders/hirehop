package com.tailormyresume.feature.onboarding.impl.consent

import android.content.Context
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.onboarding.impl.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ConsentUploadFailedLiveRegionTest {
    @get:Rule
    val rule = createComposeRule()

    private val liveRegionNodes = SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)

    private val actions = ConsentActions(
        onPurposeToggle = {},
        onAgree = {},
        onNotNow = {},
        onReadAgain = {},
        onBack = {},
    )

    private fun show(uploadFailed: Boolean) {
        rule.setContent {
            TmrTheme {
                ConsentScreen(
                    uiState = ConsentUiState(entries = consentPurposeStates(), uploadFailed = uploadFailed),
                    actions = actions,
                )
            }
        }
    }

    @Test
    fun uploadFailureIsOnePoliteLiveRegionHoldingTheMessage() {
        show(uploadFailed = true)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val node = rule.onNode(liveRegionNodes).fetchSemanticsNode()
        assertThat(node.config[SemanticsProperties.LiveRegion]).isEqualTo(LiveRegionMode.Polite)
        assertThat(node.config[SemanticsProperties.Text].joinToString { it.text })
            .isEqualTo(context.getString(R.string.feature_onboarding_impl_consent_upload_failed))
        rule.onAllNodes(liveRegionNodes).assertCountEquals(1)
    }

    @Test
    fun noFailureMeansNoLiveRegion() {
        show(uploadFailed = false)

        rule.onAllNodes(liveRegionNodes).assertCountEquals(0)
    }
}
