package com.hirehop.feature.profile.impl

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.screenshot.HH_THEME_DARK
import com.hirehop.core.screenshot.HH_THEME_LIGHT
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureForDevice
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS, sdk = [ROBORAZZI_SDK])
class ProfileOverviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun fullState_readsInLightAndDark() = captureBothThemes(
        screenName = "ProfileOverviewFull",
        uiState = ProfileUiState.Success(profile = confirmedProfile, unconfirmedCount = 0),
    )

    @Test
    fun partlyConfirmedState_readsInLightAndDark() = captureBothThemes(
        screenName = "ProfileOverviewPartlyConfirmed",
        uiState = partlyConfirmedState(),
    )

    @Test
    fun emptyState_readsInLightAndDark() = captureBothThemes(
        screenName = "ProfileOverviewEmpty",
        uiState = ProfileUiState.Empty,
    )

    @Test
    fun offlineState_readsInLightAndDark() = captureBothThemes(
        screenName = "ProfileOverviewOffline",
        uiState = ProfileUiState.Success(
            profile = confirmedProfile,
            unconfirmedCount = 0,
            isOffline = true,
        ),
    )

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun partlyConfirmedState_atLargeTextStacksAndWraps() = captureBothThemes(
        screenName = "ProfileOverviewPartlyConfirmedFont200",
        uiState = partlyConfirmedState(),
        device = HhTestDevices.boardLargeFont,
    )

    private fun captureBothThemes(
        screenName: String,
        uiState: ProfileUiState,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        darkTheme.value = false
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                ProfileScreen(
                    uiState = uiState,
                    importState = ResumeImportState(),
                    actions = ProfileActions.None,
                )
            }
        }
        capture(theme = HH_THEME_LIGHT, screenName = screenName, device = device)
        darkTheme.value = true
        composeRule.waitForIdle()
        capture(theme = HH_THEME_DARK, screenName = screenName, device = device)
    }

    private suspend fun capture(
        theme: String,
        screenName: String,
        device: HhTestDevice,
    ) {
        composeRule.captureForDevice(
            outputDirectory = outputDirectory,
            screenName = screenName,
            device = device,
            theme = theme,
        )
    }
}

private const val ROBORAZZI_SDK = 34
private const val ROBORAZZI_OUTPUT_DIR_PROPERTY = "roborazzi.output.dir"
private const val TRACKED_OUTPUT_DIR = "src/test/screenshots"

private val outputDirectory: String =
    System.getProperty(ROBORAZZI_OUTPUT_DIR_PROPERTY) ?: TRACKED_OUTPUT_DIR

private fun partlyConfirmedState(): ProfileUiState = ProfileUiState.Success(
    profile = partlyConfirmedProfile,
    unconfirmedCount = partlyConfirmedProfile.entries.count { !it.isConfirmed },
)

private val partlyConfirmedProfile = CandidateProfile(
    fullName = "Priya Deshmukh",
    email = "priya.d@example.com",
    phone = "+91 90000 00000",
    headline = "B.Tech CS 2026",
    skills = listOf("SQL", "Excel", "Power BI", "Kotlin", "Android"),
    entries = listOf(
        fact(
            id = "E-01",
            category = EntryCategory.EDUCATION,
            title = "B.Tech Computer Science",
            source = FactSource.IMPORTED,
            isConfirmed = true,
        ),
        fact(
            id = "C-01",
            category = EntryCategory.EDUCATION,
            title = "DBMS coursework",
            source = FactSource.USER_STATED,
            isConfirmed = true,
        ),
        fact(
            id = "I-01",
            category = EntryCategory.EXPERIENCE,
            title = "Data intern, Kiran Agro Exports",
            source = FactSource.IMPORTED,
            isConfirmed = true,
        ),
        fact(
            id = "P-02",
            category = EntryCategory.PROJECT,
            title = "Placement Stats Dashboard",
            source = FactSource.IMPORTED,
            isConfirmed = false,
        ),
        fact(
            id = "X-01",
            category = EntryCategory.ACHIEVEMENT,
            title = "Smart India Hackathon 2024",
            source = FactSource.USER_STATED,
            isConfirmed = false,
        ),
    ),
)

private val confirmedProfile = partlyConfirmedProfile.copy(
    entries = partlyConfirmedProfile.entries.map { it.copy(isConfirmed = true) },
)

private fun fact(
    id: String,
    category: EntryCategory,
    title: String,
    source: FactSource,
    isConfirmed: Boolean,
) = ProfileEntry(
    id = id,
    category = category,
    title = title,
    organization = "College training and placement cell",
    startDate = "2024",
    endDate = "2025",
    bullets = listOf(
        EvidenceBullet(id = "$id-b1", text = "Built a dashboard the placement cell used."),
    ),
    source = source,
    isConfirmed = isConfirmed,
)
