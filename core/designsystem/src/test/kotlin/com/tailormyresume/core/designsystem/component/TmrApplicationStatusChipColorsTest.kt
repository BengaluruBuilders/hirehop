package com.tailormyresume.core.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.theme.TmrTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmrApplicationStatusChipColorsTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun interviewUsesThePrimaryColourOnTheNeutralContainer() {
        rule.setContent {
            TmrPreviewTheme(darkTheme = true) {
                val colors = TmrTheme.colors
                assertEquals(colors.primary, colors.applicationStatusContent(TmrApplicationStatusKind.Interview))
                assertEquals(colors.neutralContainer, colors.applicationStatusContainer(TmrApplicationStatusKind.Interview))
            }
        }
        rule.waitForIdle()
    }
}
