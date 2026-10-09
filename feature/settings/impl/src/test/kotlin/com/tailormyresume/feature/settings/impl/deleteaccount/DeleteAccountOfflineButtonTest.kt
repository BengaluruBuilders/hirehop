package com.tailormyresume.feature.settings.impl.deleteaccount

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.account.AccountDeletionCounts
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class DeleteAccountOfflineButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var errorColour = Color.Unspecified
    private var disabledColour = Color.Unspecified

    private fun show(isOffline: Boolean) {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                errorColour = TmrTheme.colors.error
                disabledColour = TmrTheme.colors.onSurfaceVariant
                DeleteAccountScreen(
                    uiState = DeleteAccountUiState.Ready(
                        counts = AccountDeletionCounts(profileFacts = 18, applications = 4, unusedCredits = 4),
                        accountEmail = "priya.d@example.com",
                        isOffline = isOffline,
                        failure = null,
                        isConfirmVisible = false,
                    ),
                    actions = DeleteAccountActions(
                        onBack = {},
                        onKeepAccount = {},
                        onDeleteAccount = {},
                        onDeleteConfirmed = {},
                        onDeleteDismissed = {},
                        onDownloadData = {},
                    ),
                )
            }
        }
    }

    @Test
    fun offline_deleteAccountIsDisabledWithTheDisabledLabelColour() {
        show(isOffline = true)

        composeRule.onNodeWithText("Delete account").assertIsNotEnabled()
        assertThat(labelColor("Delete account")).isEqualTo(disabledColour)
        assertThat(labelColor("Delete account")).isNotEqualTo(errorColour)
    }

    @Test
    fun online_deleteAccountIsEnabledWithTheErrorLabelColour() {
        show(isOffline = false)

        composeRule.onNodeWithText("Delete account").assertIsEnabled()
        assertThat(labelColor("Delete account")).isEqualTo(errorColour)
    }

    private fun labelColor(text: String): Color {
        val action = composeRule.onNodeWithText(text).fetchSemanticsNode().config
            .getOrNull(SemanticsActions.GetTextLayoutResult)
        val results = mutableListOf<TextLayoutResult>()
        action?.action?.invoke(results)
        return results.first().layoutInput.style.color
    }
}
