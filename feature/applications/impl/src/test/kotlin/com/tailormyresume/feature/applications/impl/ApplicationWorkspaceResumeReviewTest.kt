package com.tailormyresume.feature.applications.impl

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ApplicationWorkspaceResumeReviewTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun exportedResume_showsReviewTailoredResumeAndNoBareOpen() {
        var reviewed = 0
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                WorkspaceResumeSection(
                    resume = previewExportedResume(),
                    canReview = true,
                    onPreview = {},
                    onShare = {},
                    onReview = { reviewed++ },
                )
            }
        }

        composeRule.onNodeWithText(REVIEW_LABEL).assertExists()
        composeRule.onNodeWithText("Open").assertDoesNotExist()
        composeRule.onNodeWithText(REVIEW_LABEL).performClick()

        assertThat(reviewed).isEqualTo(1)
    }

    @Test
    fun notExportedResume_showsReviewTailoredResume() {
        var reviewed = 0
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                WorkspaceResumeSection(
                    resume = WorkspaceResume.NotExported,
                    canReview = true,
                    onPreview = {},
                    onShare = {},
                    onReview = { reviewed++ },
                )
            }
        }

        composeRule.onNodeWithText(REVIEW_LABEL).assertExists()
        composeRule.onNodeWithText("Open").assertDoesNotExist()
        composeRule.onNodeWithText(REVIEW_LABEL).performClick()

        assertThat(reviewed).isEqualTo(1)
    }

    @Test
    fun exportedResume_shareAndReviewAreDistinct() {
        var shared = 0
        var reviewed = 0
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                WorkspaceResumeSection(
                    resume = previewExportedResume(),
                    canReview = true,
                    onPreview = {},
                    onShare = { shared++ },
                    onReview = { reviewed++ },
                )
            }
        }

        composeRule.onNodeWithText(SHARE_LABEL).assertExists()
        composeRule.onNodeWithText(REVIEW_LABEL).assertExists()
        composeRule.onNodeWithContentDescription(REVIEW_DESCRIPTION).assertExists()
        composeRule.onNodeWithText(SHARE_LABEL).performClick()

        assertThat(shared).isEqualTo(1)
        assertThat(reviewed).isEqualTo(0)
    }

    @Test
    fun workspaceReviewClick_emitsResumeReviewChosen() {
        val recorded = mutableListOf<ApplicationWorkspaceAction>()
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ApplicationDetailScreen(
                    uiState = previewWorkspaceReadyState(),
                    onAction = { action -> recorded.add(action) },
                    now = PREVIEW_INSTANT,
                )
            }
        }

        composeRule.onNodeWithText(REVIEW_LABEL).performScrollTo().performClick()

        assertThat(recorded.count { action -> action == ApplicationWorkspaceAction.ResumeReviewChosen }).isEqualTo(1)
    }

    private companion object {
        const val REVIEW_LABEL = "Review tailored resume"
        const val SHARE_LABEL = "Share"
        const val REVIEW_DESCRIPTION = "Review the tailored resume for this application"
    }
}
