package com.tailormyresume.core.designsystem.component.hero

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.isFocusable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.component.TmrPreviewTheme
import com.tailormyresume.core.designsystem.theme.LocalTmrMotion
import com.tailormyresume.core.designsystem.theme.TmrMotionDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmrHeroTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private var captured: () -> Long = { 0L }

    @Test
    fun everyPoseHasSpec() {
        TmrPaigePose.entries.forEach { pose ->
            assertNotNull(pose.name, pose.spec())
        }

        val signin1 = TmrPaigePose.Signin1.spec()
        assertEquals(14.dp, signin1.eyeWidth)
        assertEquals(19.dp, signin1.eyeHeight)
        assertEquals(0.dp, signin1.eyeShiftX)
        assertEquals(0.dp, signin1.eyeShiftY)
        assertEquals(22.dp, signin1.mouthWidth)
        assertEquals(12.dp, signin1.mouthHeight)
        assertTrue(signin1.ticks)
        assertTrue(signin1.arms)
        assertTrue(!signin1.sad)
        assertEquals(4f, signin1.rotationDegrees, 0f)
        assertEquals(0.dp, signin1.shiftX)
        assertEquals((-46).dp, signin1.lift)
        assertEquals(0, signin1.bobPhase)
        assertTrue(!signin1.wobble)

        val tailoring = TmrPaigePose.Tailoring.spec()
        assertEquals(16.dp, tailoring.eyeWidth)
        assertEquals(6.dp, tailoring.eyeHeight)
        assertEquals(20.dp, tailoring.mouthWidth)
        assertEquals(10.dp, tailoring.mouthHeight)
        assertTrue(tailoring.ticks)
        assertTrue(!tailoring.arms)
        assertTrue(!tailoring.sad)
        assertEquals(0f, tailoring.rotationDegrees, 0f)
        assertEquals(0.dp, tailoring.lift)
        assertNull(tailoring.bobPhase)
        assertTrue(tailoring.wobble)

        val delete = TmrPaigePose.Delete.spec()
        assertEquals(11.dp, delete.eyeWidth)
        assertEquals(13.dp, delete.eyeHeight)
        assertEquals(4.dp, delete.eyeShiftY)
        assertTrue(delete.sad)
        assertTrue(!delete.arms)
        assertTrue(!delete.ticks)
        assertEquals(-4f, delete.rotationDegrees, 0f)
        assertNull(delete.bobPhase)
        assertTrue(!delete.wobble)
    }

    @Test
    fun translationYIs5dpAtQuarterPeriod() {
        val spec = TmrPaigePose.Signin1.spec()
        val timeMs = (420 * Math.PI / 2).toLong()
        val value = paigeTranslationYDp(TmrMotionDefaults.Default, spec, timeMs)
        assertEquals(5f, value, 0.01f)
    }

    @Test
    fun reducedHasZeroTimeTransformAtAllTimes() {
        val times = listOf(0L, 105L, 420L, 1000L, 2639L)
        TmrPaigePose.entries.forEach { pose ->
            val spec = pose.spec()
            times.forEach { timeMs ->
                assertEquals(0f, paigeTranslationYDp(TmrMotionDefaults.Reduced, spec, timeMs), 0f)
                assertEquals(0f, paigeRotationDegrees(TmrMotionDefaults.Reduced, spec, timeMs), 0f)
            }
        }
    }

    @Test
    fun tailoringWobbleWhenNotReduced() {
        val spec = TmrPaigePose.Tailoring.spec()
        val timeMs = (300 * Math.PI / 2).toLong()
        val value = paigeRotationDegrees(TmrMotionDefaults.Default, spec, timeMs)
        assertEquals(4f, value, 0.01f)
    }

    @Test
    fun reducedClockStaysZero() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalTmrMotion provides TmrMotionDefaults.Reduced) {
                val clock = rememberPaigeTimeMs(TmrMotionDefaults.Reduced)
                SideEffect { captured = clock }
            }
        }
        var previous = 0L
        listOf(0L, 105L, 420L, 1000L, 2639L).forEach { ms ->
            rule.mainClock.advanceTimeBy(ms - previous)
            previous = ms
            assertEquals(0L, captured())
        }
    }

    @Test
    fun nonReducedClockRuns() {
        rule.mainClock.autoAdvance = false
        rule.setContent {
            CompositionLocalProvider(LocalTmrMotion provides TmrMotionDefaults.Default) {
                val clock = rememberPaigeTimeMs(TmrMotionDefaults.Default)
                SideEffect { captured = clock }
            }
        }
        rule.mainClock.advanceTimeBy(1000)
        assertTrue(captured() > 0L)
    }

    @Test
    fun paigeIsNotATalkBackStop() {
        rule.setContent {
            TmrPreviewTheme {
                CompositionLocalProvider(LocalTmrMotion provides TmrMotionDefaults.Reduced) {
                    TmrPaige(TmrPaigePose.Home)
                }
            }
        }
        rule.onAllNodes(
            SemanticsMatcher.keyIsDefined(SemanticsProperties.ContentDescription),
            useUnmergedTree = true,
        ).assertCountEquals(0)
        rule.onAllNodes(isFocusable(), useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun headlineTwoLines() {
        val headline =
            "Add the confirmed facts your recruiter already knows about you so the tailored " +
                "application reads like yours from the first line."
        rule.setContent {
            TmrPreviewTheme {
                Box(modifier = Modifier.width(300.dp)) {
                    TmrHeroCard(
                        color = TmrHeroColor.Blue,
                        label = "Step one",
                        headline = headline,
                    )
                }
            }
        }
        assertTrue(lineCountOf(headline) <= 2)
        assertEquals(28.sp, styleOf(headline).fontSize)
        assertTrue(rule.onAllNodesWithText("STEP ONE").fetchSemanticsNodes().isNotEmpty())
        assertEquals(12.sp, styleOf("STEP ONE").fontSize)
    }

    private fun layoutResultOf(text: String): TextLayoutResult {
        val action = rule.onNodeWithText(text).fetchSemanticsNode().config.getOrNull(SemanticsActions.GetTextLayoutResult)
        val results = mutableListOf<TextLayoutResult>()
        action?.action?.invoke(results)
        return results.first()
    }

    private fun styleOf(text: String): TextStyle = layoutResultOf(text).layoutInput.style

    private fun lineCountOf(text: String): Int = layoutResultOf(text).lineCount
}
