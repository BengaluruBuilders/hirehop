package com.tailormyresume.feature.tailor.impl

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
import com.tailormyresume.feature.tailor.impl.coverletter.CoverLetterActions
import com.tailormyresume.feature.tailor.impl.coverletter.CoverLetterScreen
import com.tailormyresume.feature.tailor.impl.coverletter.CoverLetterStage
import com.tailormyresume.feature.tailor.impl.coverletter.CoverLetterUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class OfflineStripLiveRegionTest {
    @get:Rule
    val rule = createComposeRule()

    private val liveRegionNodes = SemanticsMatcher.keyIsDefined(SemanticsProperties.LiveRegion)

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun assertOnePoliteRegionHolding(messageRes: Int) {
        rule.onAllNodes(liveRegionNodes).assertCountEquals(1)
        val node = rule.onNode(liveRegionNodes).fetchSemanticsNode()
        assertThat(node.config[SemanticsProperties.LiveRegion]).isEqualTo(LiveRegionMode.Polite)
        assertThat(node.config[SemanticsProperties.Text].joinToString { it.text })
            .isEqualTo(context.getString(messageRes))
    }

    @Test
    fun reviewOfflineStripIsPoliteLiveRegionHoldingItsMessage() {
        rule.setContent {
            TmrTheme {
                TailorScreen(
                    uiState = ReviewFixtures.partlyReviewed().copy(isOffline = true),
                    actions = noTailorActions(),
                    interaction = ReviewInteraction(),
                )
            }
        }

        assertOnePoliteRegionHolding(R.string.feature_tailor_impl_offline_message)
    }

    @Test
    fun reviewWithoutOfflineHasNoLiveRegion() {
        rule.setContent {
            TmrTheme {
                TailorScreen(
                    uiState = ReviewFixtures.partlyReviewed(),
                    actions = noTailorActions(),
                    interaction = ReviewInteraction(),
                )
            }
        }

        rule.onAllNodes(liveRegionNodes).assertCountEquals(0)
    }

    @Test
    fun coverLetterOfflineStripIsPoliteLiveRegionHoldingItsMessage() {
        rule.setContent {
            TmrTheme {
                CoverLetterScreen(
                    uiState = CoverLetterUiState(stage = CoverLetterStage.NO_MATCHING_EVIDENCE, isOffline = true),
                    actions = CoverLetterActions(
                        onWriteOne = {},
                        onBeginEdit = {},
                        onEditTextChanged = {},
                        onSaveEdit = {},
                        onCancelEdit = {},
                        onReportInaccurate = {},
                        onDismissMessage = {},
                        onRetry = {},
                        onNavigateBack = {},
                        onSkipLetter = {},
                        onPreviewExport = {},
                        onPrepQuestions = {},
                    ),
                )
            }
        }

        assertOnePoliteRegionHolding(R.string.feature_tailor_impl_cover_letter_offline)
    }
}
