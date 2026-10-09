package com.tailormyresume.feature.onboarding.impl.common

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.designsystem.component.splitsAWord
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.DebugScenario
import com.tailormyresume.core.testing.data.sampleProfile
import com.tailormyresume.feature.onboarding.impl.confirmfacts.ConfirmFactsActions
import com.tailormyresume.feature.onboarding.impl.confirmfacts.ConfirmFactsScenarioMapper
import com.tailormyresume.feature.onboarding.impl.confirmfacts.ConfirmFactsScreen
import com.tailormyresume.feature.onboarding.impl.welcome.WelcomeActions
import com.tailormyresume.feature.onboarding.impl.welcome.WelcomeScreen
import com.tailormyresume.feature.onboarding.impl.welcome.WelcomeUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w230dp-h780dp-normal-long-notround-any-xhdpi-keyshidden-nonav", fontScale = 2f)
class OnboardingBrandFitTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun welcomeSubline_shrinksSoTheBrandStaysWhole() {
        composeRule.setContent {
            TmrTheme { WelcomeScreen(uiState = WelcomeUiState(), actions = WelcomeActions({}, {}, {}, {}, {})) }
        }

        assertBrandTextShrunkAndWhole("never invents", baseSp = SUBLINE_SP, expectedCount = 1)
    }

    @Test
    fun partlyConfirmed_noticesShrinkWholeAndAreNotClipped() {
        composeRule.setContent {
            TmrTheme {
                ConfirmFactsScreen(
                    uiState = ConfirmFactsScenarioMapper.withProfile(
                        state = ConfirmFactsScenarioMapper.seed(DebugScenario.PARTLY_CONFIRMED),
                        profile = sampleProfile,
                        scenario = DebugScenario.PARTLY_CONFIRMED,
                    ),
                    actions = ConfirmFactsActions({}, {}, { _, _ -> }, {}, {}, {}, {}),
                )
            }
        }

        assertBrandTextShrunkAndWhole("TailorMyResume", baseSp = NOTICE_SP, expectedCount = 2)
    }

    private fun assertBrandTextShrunkAndWhole(key: String, baseSp: Float, expectedCount: Int) {
        val layouts = textLayouts().filter { it.layoutInput.text.text.contains(key) }
        assertThat(layouts).hasSize(expectedCount)
        layouts.forEach { assertShrunkAndWhole(it, baseSp) }
    }

    private fun assertShrunkAndWhole(layout: TextLayoutResult, baseSp: Float) {
        val text = layout.layoutInput.text.text
        val lineEnds = (0 until layout.lineCount - 1).map(layout::getLineEnd)
        val brand = Regex("TailorMyResume").find(text)
        assertThat(brand).isNotNull()
        assertThat(lineEnds.any { it > brand!!.range.first && it <= brand.range.last }).isFalse()
        assertThat(layout.splitsAWord()).isFalse()
        assertThat(layout.hasVisualOverflow).isFalse()
        assertThat(layout.layoutInput.style.fontSize.value).isLessThan(baseSp)
        val density = Density(composeRule.activity.resources.displayMetrics.density, composeRule.activity.resources.configuration.fontScale)
        val renderedDp = with(density) { layout.layoutInput.style.fontSize.toDp() }.value
        assertThat(renderedDp).isAtLeast(baseSp)
    }

    private fun textLayouts(): List<TextLayoutResult> =
        composeRule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult),
            useUnmergedTree = true,
        ).fetchSemanticsNodes().flatMap { node ->
            mutableListOf<TextLayoutResult>().also { node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(it) }
        }

    private companion object {
        const val SUBLINE_SP = 16f
        const val NOTICE_SP = 15f
    }
}
