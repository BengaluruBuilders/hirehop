package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.testing.data.sampleProfile
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w411dp-h2400dp-xxhdpi")
class ConfirmFactsContinuationAndLimitNotesTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun entry(id: String, title: String, bullets: List<String>) = ProfileEntry(
        id = id,
        category = EntryCategory.EXPERIENCE,
        title = title,
        organization = "Nashik",
        startDate = "May 2025",
        endDate = "Jul 2025",
        bullets = bullets.mapIndexed { index, text -> EvidenceBullet("$id-b${index + 1}", text) },
        source = FactSource.IMPORTED,
        isConfirmed = false,
    )

    private fun lines(count: Int, longAt: Int? = null) =
        (1..count).map { if (it == longAt) "x".repeat(450) else "Did task $it." }

    private fun show(vararg entries: ProfileEntry) {
        val state = ConfirmFactsScenarioMapper.withProfile(
            state = ConfirmFactsScenarioMapper.seed(DebugScenario.DEFAULT),
            profile = sampleProfile.copy(entries = entries.toList()),
            scenario = DebugScenario.DEFAULT,
        )
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ConfirmFactsScreen(
                    uiState = state,
                    actions = ConfirmFactsActions(
                        onBack = {},
                        onConfirm = {},
                        onEdit = { _, _ -> },
                        onAddOne = {},
                        onSkip = {},
                        onContinue = {},
                        onImportResume = {},
                    ),
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun secondCardOfOneSplitRoleIsLabelledContinued() {
        show(entry("W-01", "Data intern", lines(15)), entry("W-02", "Data intern", lines(5)))

        composeRule.onNodeWithText("Continued").assertExists()
        composeRule.onNode(hasContentDescription("Continued. ", substring = true)).assertExists()
    }

    @Test
    fun differentRolesAreNotLabelledContinued() {
        show(entry("W-01", "Data intern", lines(3)), entry("W-02", "Data analyst", lines(3)))

        composeRule.onNodeWithText("Continued").assertDoesNotExist()
    }

    @Test
    fun entryThatIsTooLongAndOverFifteenShowsTheCombinedNote() {
        show(entry("W-01", "Data intern", lines(20, longAt = 3)))

        composeRule.onNodeWithText("One line is too long and lines past 15", substring = true).assertExists()
    }
}
