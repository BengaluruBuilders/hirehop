package com.tailormyresume.core.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmrHomeHeaderFontScaleTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun greetingKeepsItsWidthWhenCreditsPillGrowsAt200Percent() {
        rule.setContent {
            TmrPreviewTheme(darkTheme = false) {
                CompositionLocalProvider(LocalDensity provides Density(1f, 2f)) {
                    Box(Modifier.width(320.dp)) {
                        TmrCollapsingHomeHeader(
                            collapse = TmrHeaderCollapseState(),
                            title = "Applications",
                            greeting = "Hello, candidate",
                            headline = "Your applications",
                            trailing = { Text("123456") },
                        )
                    }
                }
            }
        }
        val greeting = rule.onNodeWithText("Hello, candidate").fetchSemanticsNode().boundsInRoot
        val credits = rule.onNodeWithText("123456").fetchSemanticsNode().boundsInRoot
        assertTrue(greeting.width > 100f)
        assertTrue(credits.top >= greeting.bottom)
    }
}
