package com.tailormyresume.feature.settings.impl.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.SignInAccount
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Instant

@RunWith(AndroidJUnit4::class)
class SettingsSignOutDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val noActions = SettingsActions(
        onSignOut = {},
        onSignOutConfirm = {},
        onSignOutDismiss = {},
        onCreditsAndHelp = {},
        onYourData = {},
        onConsentNotice = {},
        onDeleteAccount = {},
    )

    private fun show(account: SignInAccount?) {
        composeRule.setContent {
            TmrTheme {
                SettingsScreen(
                    uiState = SettingsUiState.Content(
                        account = account,
                        creditsLeft = 1,
                        consentAcceptedAt = Instant.fromEpochSeconds(1_802_606_400),
                        isOffline = false,
                        isSignOutConfirmVisible = true,
                    ),
                    actions = noActions,
                    versionName = "1.0",
                )
            }
        }
    }

    @Test
    fun dialogSaysTheDataStaysOnThisPhoneAndADifferentAccountRemovesIt() {
        show(SignInAccount.localAccount)

        composeRule.onNodeWithText("stay on this phone", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("different Google account", substring = true).assertIsDisplayed()
    }

    @Test
    fun dialogWithoutEmailSaysTheSameThing() {
        show(null)

        composeRule.onNodeWithText("stay on this phone", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("different Google account", substring = true).assertIsDisplayed()
    }

    @Test
    fun dialogNoLongerPromisesTheDataStaysInTheAccount() {
        show(SignInAccount.localAccount)

        composeRule.onNodeWithText("in your account", substring = true).assertDoesNotExist()
    }
}
