package com.hirehop.feature.profile.impl

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.screenshot.HhTestDevice
import com.hirehop.core.screenshot.HhTestDevices
import com.hirehop.core.screenshot.captureMultiTheme
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = HhTestDevices.BOARD_QUALIFIERS)
class ProfileOverviewScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val darkTheme = mutableStateOf(false)

    @Test
    fun emptyState() = capture("ProfileOverviewEmpty", ProfileUiState.Empty(headerLine = HEADER_LINE))

    @Test
    fun partlyConfirmedState() = capture("ProfileOverviewPartlyConfirmed", success(partlyConfirmedProfile))

    @Test
    fun fullState() = capture("ProfileOverviewFull", success(confirmedProfile))

    @Test
    fun offlineState() = capture("ProfileOverviewOffline", success(confirmedProfile, isOffline = true))

    @Test
    fun expandedProjectsSection() = capture(
        screenName = "ProfileSectionProjects",
        uiState = success(partlyConfirmedProfile),
        expanded = ProfileSectionKind.Projects,
    )

    @Test
    fun expandedSkillsSection() = capture(
        screenName = "ProfileSectionSkills",
        uiState = success(partlyConfirmedProfile),
        expanded = ProfileSectionKind.Skills,
    )

    @Test
    @Config(fontScale = HhTestDevices.LARGE_FONT_SCALE)
    fun partlyConfirmedState_atLargeText() = capture(
        screenName = "ProfileOverviewPartlyConfirmedFont200",
        uiState = success(partlyConfirmedProfile),
        device = HhTestDevices.boardLargeFont,
    )

    private fun capture(
        screenName: String,
        uiState: ProfileUiState,
        expanded: ProfileSectionKind? = null,
        device: HhTestDevice = HhTestDevices.board,
    ) = runBlocking {
        composeRule.setContent {
            HhTheme(darkTheme = darkTheme.value) {
                ProfileScreen(
                    uiState = uiState,
                    actions = ProfileActions.None,
                    navigation = ProfileNavigation.None,
                    initiallyExpanded = expanded,
                )
            }
        }
        composeRule.captureMultiTheme(
            outputDirectory = SCREENSHOT_DIRECTORY,
            screenName = screenName,
            device = device,
            setTheme = { dark -> darkTheme.value = dark },
        )
        Unit
    }
}

private const val SCREENSHOT_DIRECTORY = "src/test/screenshots"
private const val HEADER_LINE = "Priya Deshmukh · Data Operations Associate"

private fun success(profile: CandidateProfile, isOffline: Boolean = false) =
    ProfileUiState.Success(profile = profile, isOffline = isOffline)

private val partlyConfirmedProfile = CandidateProfile(
    fullName = "Priya Deshmukh",
    email = "priya.d@example.com",
    phone = "+91 98220 41873",
    headline = "Data Operations Associate",
    skills = listOf("SQL", "Excel", "Power BI", "Python", "Data cleaning", "Reporting", "Communication"),
    entries = listOf(
        fact("I-01", EntryCategory.EXPERIENCE, "Data Operations Associate, Saffron Retail", FactSource.IMPORTED, true),
        fact("I-02", EntryCategory.EXPERIENCE, "Data Intern, Kiran Agro Exports", FactSource.IMPORTED, false),
        fact("U-01", EntryCategory.EDUCATION, "B.Tech Computer Science", FactSource.IMPORTED, true),
        fact("U-02", EntryCategory.EDUCATION, "Coursework: DBMS, Probability and Statistics", FactSource.USER_STATED, true),
        fact("C-01", EntryCategory.PROJECT, "Library database project (DBMS course)", FactSource.IMPORTED, true),
        fact("C-02", EntryCategory.PROJECT, "Placement Stats Dashboard", FactSource.IMPORTED, true),
        fact("C-03", EntryCategory.PROJECT, "Event budget tracker in Excel for the coding club", FactSource.USER_STATED, true),
        fact("X-01", EntryCategory.CERTIFICATION, "Google Data Analytics Certificate", FactSource.IMPORTED, false),
        fact("P-01", EntryCategory.ACHIEVEMENT, "Smart India Hackathon 2024", FactSource.IMPORTED, true),
        fact("P-02", EntryCategory.ACHIEVEMENT, "Treasurer, coding club", FactSource.USER_STATED, true),
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
    organization = "",
    startDate = "",
    endDate = "",
    bullets = emptyList(),
    source = source,
    isConfirmed = isConfirmed,
)
