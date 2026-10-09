package com.tailormyresume.feature.applications.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.component.TmrHeaderCollapseState
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ApplicationsCompactHeaderTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<ApplicationsAction>()

    @Test
    fun collapsedHeaderOffersPasteAJobInsteadOfCredits() {
        show(fraction = 1f)

        composeRule.onNodeWithText("4 credits left").assertDoesNotExist()
        composeRule.onNodeWithText("Paste a job").assertIsDisplayed().performClick()
        assertThat(actions).contains(ApplicationsAction.PasteJobChosen)
    }

    @Test
    fun expandedHeaderKeepsCreditsPill() {
        show(fraction = 0f)

        composeRule.onNodeWithText("4 credits left").assertIsDisplayed()
    }

    private fun show(fraction: Float) {
        composeRule.setContent {
            TmrTheme(darkTheme = true) {
                ApplicationsScreen(
                    uiState = previewListState(),
                    onAction = { actions.add(it) },
                    collapse = TmrHeaderCollapseState(initialFraction = fraction),
                    now = PREVIEW_INSTANT,
                )
            }
        }
    }
}
