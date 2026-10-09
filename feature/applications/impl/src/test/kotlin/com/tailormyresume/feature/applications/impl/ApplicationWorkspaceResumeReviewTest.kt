package com.tailormyresume.feature.applications.impl

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ApplicationStatus
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
    fun exportedResume_showsReviewResumeAndNoBareOpen() {
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
    fun notExportedResume_showsReviewResume() {
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

    @Test
    fun reviewLabel_click_firesOnReviewOnce() {
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

        composeRule.onNodeWithText(REVIEW_LABEL).performClick()

        assertThat(reviewed).isEqualTo(1)
    }

    @Test
    fun reviewLabel_fitsOnOneLine_exported() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ApplicationDetailScreen(
                    uiState = previewWorkspaceReadyState(),
                    onAction = {},
                    now = PREVIEW_INSTANT,
                )
            }
        }

        val results = mutableListOf<TextLayoutResult>()
        composeRule.onNodeWithText(REVIEW_LABEL)
            .performScrollTo()
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)

        assertThat(results.single().lineCount).isEqualTo(1)
    }

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun reviewLabel_fitsAtFont200_exported() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ApplicationDetailScreen(
                    uiState = previewWorkspaceReadyState(),
                    onAction = {},
                    now = PREVIEW_INSTANT,
                )
            }
        }

        composeRule.onNodeWithText(REVIEW_LABEL).performScrollTo().assertIsDisplayed()

        val results = mutableListOf<TextLayoutResult>()
        composeRule.onNodeWithText(REVIEW_LABEL)
            .performScrollTo()
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)

        assertThat(results).hasSize(1)
        assertThat(results.single().hasVisualOverflow).isFalse()
        assertThat(results.single().lineCount).isAtMost(3)
    }

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun reviewLabel_fitsAtFont200_notExported() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ApplicationDetailScreen(
                    uiState = previewWorkspaceReadyState(
                        status = ApplicationStatus.SAVED,
                        resume = WorkspaceResume.NotExported,
                    ),
                    onAction = {},
                    now = PREVIEW_INSTANT,
                )
            }
        }

        composeRule.onNodeWithText(REVIEW_LABEL).performScrollTo().assertIsDisplayed()

        val results = mutableListOf<TextLayoutResult>()
        composeRule.onNodeWithText(REVIEW_LABEL)
            .performScrollTo()
            .fetchSemanticsNode()
            .config[SemanticsActions.GetTextLayoutResult]
            .action
            ?.invoke(results)

        assertThat(results).hasSize(1)
        assertThat(results.single().hasVisualOverflow).isFalse()
        assertThat(results.single().lineCount).isAtMost(3)
    }

    private companion object {
        const val REVIEW_LABEL = "Review resume"
        const val SHARE_LABEL = "Share"
    }
}
