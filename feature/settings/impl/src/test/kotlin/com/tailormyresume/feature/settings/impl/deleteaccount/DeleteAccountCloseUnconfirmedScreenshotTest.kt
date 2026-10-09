package com.tailormyresume.feature.settings.impl.deleteaccount

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.domain.account.AccountDeletionCounts
import com.tailormyresume.core.screenshot.TmrTestDevices
import com.tailormyresume.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class DeleteAccountCloseUnconfirmedScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun closeUnconfirmed_readsInLightAndDark() = runBlocking<Unit> {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                DeleteAccountScreen(
                    uiState = DeleteAccountUiState.Ready(
                        counts = AccountDeletionCounts(profileFacts = 18, applications = 4, unusedCredits = 4),
                        accountEmail = "priya.d@example.com",
                        isOffline = false,
                        failure = DeleteAccountFailure.CLOSE_UNCONFIRMED,
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
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = "src/test/screenshots",
            screenName = "DeleteAccountCloseUnconfirmed",
            device = TmrTestDevices.board,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }
}
