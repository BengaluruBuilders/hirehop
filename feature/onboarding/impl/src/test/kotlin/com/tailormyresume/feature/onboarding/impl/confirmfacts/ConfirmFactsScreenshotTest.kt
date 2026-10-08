package com.tailormyresume.feature.onboarding.impl.confirmfacts

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.screenshot.TmrTestDevice
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
class ConfirmFactsScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun allPendingShowsTheRemovedNotice() {
        captureBothThemes(
            screenName = "ConfirmFactsAllPending",
            uiState = stateFor(DebugScenario.DEFAULT),
        )
    }

    @Test
    fun partlyConfirmedTicksTheCounterUp() {
        captureBothThemes(
            screenName = "ConfirmFactsPartlyConfirmed",
            uiState = stateFor(DebugScenario.PARTLY_CONFIRMED),
        )
    }

    @Test
    fun fullyConfirmedDropsTheOpenLine() {
        captureBothThemes(
            screenName = "ConfirmFactsFullyConfirmed",
            uiState = stateFor(DebugScenario.FULLY_CONFIRMED),
        )
    }

    @Test
    fun emptySectionOffersTwoEqualChoices() {
        captureBothThemes(
            screenName = "ConfirmFactsEmptySection",
            uiState = stateFor(DebugScenario.FULLY_CONFIRMED),
        )
    }

    @Test
    fun emptyStateSaysNothingToConfirm() {
        captureBothThemes(
            screenName = "ConfirmFactsEmpty",
            uiState = stateFor(DebugScenario.EMPTY),
        )
    }

    @Test
    fun offlineKeepsWorkingOnThePhone() {
        captureBothThemes(
            screenName = "ConfirmFactsOffline",
            uiState = stateFor(DebugScenario.OFFLINE),
        )
    }

    @Test
    fun errorOnlyOnARealFailedSave() {
        captureBothThemes(
            screenName = "ConfirmFactsError",
            uiState = stateFor(DebugScenario.ERROR),
        )
    }

    @Test
    @Config(fontScale = TmrTestDevices.LARGE_FONT_SCALE)
    fun partlyConfirmedAtLargeTextStacksFullWidth() {
        captureBothThemes(
            screenName = "ConfirmFactsPartlyConfirmedFont200",
            uiState = stateFor(DebugScenario.PARTLY_CONFIRMED),
            device = TmrTestDevices.boardLargeFont,
        )
    }

    private fun captureBothThemes(
        screenName: String,
        uiState: ConfirmFactsUiState,
        device: TmrTestDevice = TmrTestDevices.board,
    ) = runBlocking {
        showScreen(uiState)
        composeRule.captureMultiTheme(
            outputDirectory = OUTPUT,
            screenName = screenName,
            device = device,
            setTheme = { dark -> composeRule.runOnUiThread { darkTheme.value = dark } },
        )
    }

    private fun showScreen(uiState: ConfirmFactsUiState) {
        composeRule.setContent {
            TmrTheme(darkTheme = darkTheme.value) {
                ConfirmFactsScreen(uiState = uiState, actions = noOpActions)
            }
        }
        composeRule.waitForIdle()
    }

    private fun stateFor(scenario: DebugScenario): ConfirmFactsUiState =
        ConfirmFactsScenarioMapper.withProfile(
            state = ConfirmFactsScenarioMapper.seed(scenario),
            profile = sampleImportedProfile,
            scenario = scenario,
        )

    private val noOpActions = ConfirmFactsActions(
        onBack = {},
        onConfirm = {},
        onEdit = { _, _ -> },
        onAddOne = {},
        onSkip = {},
        onContinue = {},
        onImportResume = {},
    )

    private companion object {
        const val OUTPUT = "src/test/screenshots"

        val sampleImportedProfile = CandidateProfile(
            fullName = "Asha Rao",
            email = "asha.rao@example.com",
            phone = "+91 90000 00000",
            headline = "",
            skills = listOf("SQL", "Power BI", "Excel", "Pivot tables"),
            entries = listOf(
                entry(
                    id = "W-01",
                    category = EntryCategory.EXPERIENCE,
                    title = "Data Operations Associate, Saffron Retail",
                    organization = "Pune",
                    startDate = "Jul 2025",
                    endDate = "now",
                    detail = "Built weekly sales reports in Excel for 40 stores; cleaned order data with SQL.",
                ),
                entry(
                    id = "I-01",
                    category = EntryCategory.EXPERIENCE,
                    title = "Data intern, Kiran Agro Exports",
                    organization = "Nashik",
                    startDate = "May 2025",
                    endDate = "Jul 2025",
                    detail = "Cleaned 12,000 rows of sales data in Excel; built weekly pivot reports.",
                ),
                entry(
                    id = "P-02",
                    category = EntryCategory.PROJECT,
                    title = "Placement Stats Dashboard",
                    organization = "Power BI",
                    startDate = "2024",
                    endDate = "2024",
                    detail = "3 batches of placement data, built for the college T&P cell.",
                ),
                entry(
                    id = "X-01",
                    category = EntryCategory.ACHIEVEMENT,
                    title = "Smart India Hackathon 2024",
                    organization = "",
                    startDate = "2024",
                    endDate = "",
                    detail = "Internal-round finalist.",
                ),
            ),
        )

        fun entry(
            id: String,
            category: EntryCategory,
            title: String,
            organization: String,
            startDate: String,
            endDate: String,
            detail: String,
        ) = ProfileEntry(
            id = id,
            category = category,
            title = title,
            organization = organization,
            startDate = startDate,
            endDate = endDate,
            bullets = listOf(EvidenceBullet(id = "$id-b1", text = detail)),
            source = FactSource.IMPORTED,
            isConfirmed = false,
        )
    }
}
