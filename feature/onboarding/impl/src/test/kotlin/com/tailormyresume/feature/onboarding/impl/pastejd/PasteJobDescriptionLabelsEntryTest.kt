package com.tailormyresume.feature.onboarding.impl.pastejd

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class PasteJobDescriptionLabelsEntryTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val state = mutableStateOf(PasteJobDescriptionUiState())
    private val companyValues = mutableListOf<String>()
    private val roleValues = mutableListOf<String>()

    private fun show(company: String = "", role: String = "") {
        state.value = PasteJobDescriptionUiState(
            freeAnalysesLeft = FREE_LEFT,
            text = SAMPLE_JD,
            company = company,
            role = role,
        )
        composeRule.setContent {
            TmrTheme {
                PasteJobDescriptionScreen(
                    uiState = state.value,
                    actions = PasteJobDescriptionActions(
                        onTextChange = {},
                        onPaste = {},
                        onCompanyChange = { value ->
                            companyValues += value
                            state.value = state.value.copy(company = value)
                        },
                        onRoleChange = { value ->
                            roleValues += value
                            state.value = state.value.copy(role = value)
                        },
                        onClear = {},
                        onAnalyse = {},
                        onRetry = {},
                        onBack = {},
                    ),
                )
            }
        }
    }

    private fun fields() = composeRule.onAllNodes(hasSetTextAction())

    private fun companyField() = fields()[0]

    private fun roleField() = fields()[1]

    private fun assertFieldsShown() {
        composeRule.onNodeWithText("Company").assertExists()
        composeRule.onNodeWithText("Role").assertExists()
        fields().assertCountEquals(2)
    }

    private fun assertFieldsHidden() {
        composeRule.onAllNodesWithText("Company").assertCountEquals(0)
        composeRule.onAllNodesWithText("Role").assertCountEquals(0)
        fields().assertCountEquals(0)
    }

    private fun tapAddControl() {
        composeRule.onNodeWithText(ADD_CONTROL).performClick()
    }

    @Test
    fun blankPrefill_showsAddRoleAndCompanyControl() {
        show()

        composeRule.onNodeWithText(ADD_CONTROL).assertExists()
        assertFieldsHidden()
    }

    @Test
    fun blankPrefill_tapOnControl_revealsCompanyAndRoleFields() {
        show()

        tapAddControl()

        assertFieldsShown()
    }

    @Test
    fun prefilled_showsChipsAndNoAddControl() {
        show(company = "Northwind GCC", role = "Associate Analyst")

        composeRule.onNodeWithText(ADD_CONTROL).assertDoesNotExist()
        composeRule.onNode(hasText("Northwind GCC") and !hasSetTextAction()).assertExists()
        composeRule.onNode(hasText("Associate Analyst") and !hasSetTextAction()).assertExists()
        composeRule.onNodeWithContentDescription(EDIT_DESCRIPTION).assertExists()
    }

    @Test
    fun typingInRevealedFields_sendsTheValuesToTheActions() {
        show()
        tapAddControl()

        companyField().performTextInput("Northwind")
        roleField().performTextInput("Analyst")

        assertEquals("Northwind", companyValues.last())
        assertEquals("Analyst", roleValues.last())
    }

    @Test
    fun clearingARevealedField_keepsTheFieldsOnScreen() {
        show()
        tapAddControl()
        companyField().performTextInput("A")

        companyField().performTextClearance()

        assertFieldsShown()
    }

    @Test
    fun clearingAPrefilledField_keepsTheFieldsOnScreen() {
        show(company = "Northwind")
        composeRule.onNodeWithContentDescription(EDIT_DESCRIPTION).performClick()

        companyField().performTextClearance()

        assertFieldsShown()
    }

    private companion object {
        const val FREE_LEFT = 2
        const val ADD_CONTROL = "Add role and company"
        const val EDIT_DESCRIPTION = "Edit role and company"

        val SAMPLE_JD: String = "Associate Analyst, Business Intelligence at Northwind Global " +
            "Capability Centre, Bengaluru. You will build weekly reports in SQL and Advanced " +
            "Excel, and model dashboards in Power BI or Tableau. The team works in Agile with " +
            "JIRA and reports to stakeholders every Friday morning."
    }
}
