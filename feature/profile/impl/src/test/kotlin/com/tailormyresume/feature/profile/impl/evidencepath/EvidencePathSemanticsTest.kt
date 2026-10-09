package com.tailormyresume.feature.profile.impl.evidencepath

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.screenshot.TmrTestDevices
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = TmrTestDevices.BOARD_QUALIFIERS)
class EvidencePathSemanticsTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun categoryChipsAreOneSelectableGroup() {
        composeRule.setContent {
            TmrTheme(darkTheme = false) {
                EvidencePathScreen(
                    uiState = EvidencePathUiState(category = EvidenceCategory.PROJECTS),
                    actions = EvidencePathActions.None,
                    onBack = {},
                )
            }
        }

        composeRule
            .onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.SelectableGroup))
            .assertCountEquals(1)
    }
}
