package com.tailormyresume.feature.applications.impl

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.ApplicationStatus
import com.tailormyresume.core.model.KeywordCoverage
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class ApplicationRowClickTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun inProgressCard_titleClickOpensTheWorkspace() {
        var opened = 0
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                ApplicationRow(
                    row = ApplicationListRow(
                        id = "row-1",
                        role = "Associate Analyst",
                        company = "Northwind GCC",
                        status = ApplicationStatus.APPLIED,
                        coverage = KeywordCoverage(covered = 9, total = 14),
                        updatedAt = PREVIEW_INSTANT,
                        isSyncPending = false,
                        isExported = false,
                    ),
                    now = PREVIEW_INSTANT,
                    onClick = { opened++ },
                    onStatusClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Associate Analyst", useUnmergedTree = true).performClick()

        assertThat(opened).isEqualTo(1)
    }
}
