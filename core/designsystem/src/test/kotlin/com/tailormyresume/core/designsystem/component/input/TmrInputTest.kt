package com.tailormyresume.core.designsystem.component.input

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.R
import com.tailormyresume.core.designsystem.component.TmrPreviewTheme
import com.tailormyresume.core.designsystem.theme.TmrDarkColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmrInputTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun disabledTapRunsOnDisabledClickOnly() {
        var clicks = 0
        var disabledClicks = 0
        rule.setContent {
            TmrPreviewTheme {
                TmrPrimaryButton(
                    label = "Continue",
                    onClick = { clicks++ },
                    enabled = false,
                    onDisabledClick = { disabledClicks++ },
                )
            }
        }
        rule.onNodeWithText("Continue").performClick()
        assertEquals(1, disabledClicks)
        assertEquals(0, clicks)
    }

    @Test
    fun enabledTapRunsOnClick() {
        var clicks = 0
        var disabledClicks = 0
        rule.setContent {
            TmrPreviewTheme {
                TmrPrimaryButton(
                    label = "Continue",
                    onClick = { clicks++ },
                    enabled = true,
                    onDisabledClick = { disabledClicks++ },
                )
            }
        }
        rule.onNodeWithText("Continue").performClick()
        assertEquals(1, clicks)
        assertEquals(0, disabledClicks)
    }

    @Test
    fun disabledReportsButtonWithStateDescription() {
        rule.setContent {
            TmrPreviewTheme {
                TmrPrimaryButton(label = "Continue", onClick = {}, enabled = false)
            }
        }
        val config = rule.onNodeWithText("Continue").fetchSemanticsNode().config
        assertEquals(Role.Button, config.getOrNull(SemanticsProperties.Role))
        assertEquals(
            rule.activity.getString(R.string.core_designsystem_input_not_available),
            config.getOrNull(SemanticsProperties.StateDescription),
        )
    }

    @Test
    fun enabledColoursAndMinHeight() {
        rule.setContent {
            TmrPreviewTheme {
                Column {
                    TmrPrimaryButton(label = "Continue", onClick = {}, enabled = true)
                    TmrPrimaryButton(label = "Later", onClick = {}, enabled = false)
                }
            }
        }
        assertEquals(TmrDarkColors.ink, labelColor("Continue"))
        assertEquals(TmrDarkColors.textDisabled, labelColor("Later"))
        rule.onNodeWithText("Continue").assertHeightIsAtLeast(56.dp)
    }

    @Test
    fun choiceRowRolesAndSelectedState() {
        var clicks = 0
        rule.setContent {
            TmrPreviewTheme {
                Column {
                    TmrChoiceRow(label = "Yes", selected = true, onClick = { clicks++ })
                    TmrChoiceRow(label = "No", selected = false, onClick = { clicks++ })
                }
            }
        }
        val selected = rule.onNodeWithText("Yes").fetchSemanticsNode().config
        val unselected = rule.onNodeWithText("No").fetchSemanticsNode().config
        assertEquals(Role.RadioButton, selected.getOrNull(SemanticsProperties.Role))
        assertEquals(Role.RadioButton, unselected.getOrNull(SemanticsProperties.Role))
        assertEquals(true, selected.getOrNull(SemanticsProperties.Selected))
        assertEquals(false, unselected.getOrNull(SemanticsProperties.Selected))
        rule.onNodeWithText("No").performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun toggleSwitchRoleToggleableAndCallback() {
        var captured: Boolean? = null
        rule.setContent {
            TmrPreviewTheme {
                Column {
                    TmrToggle(checked = false, onCheckedChange = { captured = it }, modifier = Modifier.testTag("t"))
                    TmrToggle(checked = true, onCheckedChange = { captured = it }, modifier = Modifier.testTag("u"))
                }
            }
        }
        val off = rule.onNodeWithTag("t").fetchSemanticsNode().config
        assertEquals(Role.Switch, off.getOrNull(SemanticsProperties.Role))
        assertEquals(ToggleableState.Off, off.getOrNull(SemanticsProperties.ToggleableState))
        val on = rule.onNodeWithTag("u").fetchSemanticsNode().config
        assertEquals(Role.Switch, on.getOrNull(SemanticsProperties.Role))
        assertEquals(ToggleableState.On, on.getOrNull(SemanticsProperties.ToggleableState))
        rule.onNodeWithTag("t").performClick()
        assertEquals(true, captured)
    }

    @Test
    fun textFieldLabelIsAccessibleNameAndEditable() {
        var typed: String? = null
        rule.setContent {
            TmrPreviewTheme {
                TmrTextField(value = "", onValueChange = { typed = it }, label = "Email")
            }
        }
        rule.onNodeWithContentDescription("Email").performTextInput("a")
        assertEquals("a", typed)
        rule.onNodeWithText("Email").assertIsDisplayed()
    }

    @Test
    fun readOnlyFieldRejectsInput() {
        var typed: String? = null
        rule.setContent {
            TmrPreviewTheme {
                TmrTextField(value = "x", onValueChange = { typed = it }, label = "Email", readOnly = true)
            }
        }
        val config = rule.onNodeWithContentDescription("Email").fetchSemanticsNode().config
        assertNull(config.getOrNull(SemanticsActions.SetText))
        assertTrue(typed == null)
    }

    private fun labelColor(text: String): Color {
        val action = rule.onNodeWithText(text).fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)
        val results = mutableListOf<TextLayoutResult>()
        action?.action?.invoke(results)
        return results.first().layoutInput.style.color
    }
}
