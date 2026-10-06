package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val LANDSCAPE_QUALIFIERS = "w800dp-h360dp-normal-long-notround-any-440dpi-keyshidden-nonav"

private const val SWIPE_COUNT = 5
private const val SWIPE_START = 0.6f
private const val SWIPE_END = 0.1f
private const val SWIPE_X = 4f
private const val FORM_TAG = "form"

private val device = HhTestDevice("landscape-phone", LANDSCAPE_QUALIFIERS, 1.0f)

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = LANDSCAPE_QUALIFIERS)
class HhScreenLandscapeScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Composable
    private fun FormContent() {
        HhTheme(darkTheme = darkTheme.value) {
            HhScreen(
                sheet = false,
                header = {
                    HhInnerHeader(
                        title = "Paste a job description",
                        onBack = {},
                        backContentDescription = "Back",
                    )
                },
                bottomBar = {
                    HhBottomActionBar {
                        HhPrimaryButton(label = "Analyse", onClick = {})
                    }
                },
            ) { padding ->
                Column(
                    Modifier.fillMaxSize().padding(padding)
                        .testTag(FORM_TAG)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = HhTheme.spacing.gutter),
                    verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
                ) {
                    HhTextField(value = "", onValueChange = {}, label = "Job description", minLines = 6, singleLine = false)
                    HhTextField(value = "", onValueChange = {}, label = "Company")
                    HhTextField(value = "", onValueChange = {}, label = "Role")
                }
            }
        }
    }

    private fun swipeFormUp() {
        repeat(SWIPE_COUNT) {
            composeRule.onNodeWithTag(FORM_TAG).performTouchInput {
                swipe(Offset(SWIPE_X, height * SWIPE_START), Offset(SWIPE_X, height * SWIPE_END))
            }
        }
    }

    @Test
    fun formReachableAfterScroll_headerScrollsAway() {
        composeRule.setContent { FormContent() }
        swipeFormUp()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Role").assertIsDisplayed()
        val role = composeRule.onNodeWithText("Role").getUnclippedBoundsInRoot()
        val action = composeRule.onNodeWithText("Analyse").getUnclippedBoundsInRoot()
        assertTrue(role.bottom <= action.top)
        composeRule.onNodeWithText("Paste a job description").assertIsNotDisplayed()
        composeRule.onAllNodes(isFocused()).assertCountEquals(0)
    }

    @Test
    fun landscapeForm_readsInLightAndDark() = runBlocking<Unit> {
        composeRule.setContent { FormContent() }
        swipeFormUp()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Paste a job description").assertIsNotDisplayed()
        composeRule.onNodeWithText("Role").assertIsDisplayed()
        composeRule.onAllNodes(isFocused()).assertCountEquals(0)
        composeRule.captureMultiTheme(
            outputDirectory = "src/test/screenshots",
            screenName = "HhScreenLandscapeForm",
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    @Test
    fun landscapeFormInitial_readsInLightAndDark() = runBlocking<Unit> {
        composeRule.setContent { FormContent() }
        composeRule.waitForIdle()
        composeRule.captureMultiTheme(
            outputDirectory = "src/test/screenshots",
            screenName = "HhScreenLandscapeFormInitial",
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }
}
