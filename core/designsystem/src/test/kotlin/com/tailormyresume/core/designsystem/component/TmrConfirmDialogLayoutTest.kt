package com.tailormyresume.core.designsystem.component

import androidx.activity.ComponentActivity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.tailormyresume.core.designsystem.icon.TmrIcons
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TmrConfirmDialogLayoutTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun cancelComesBeforeConfirm() {
        rule.setContent { TmrConfirmPanelContent() }
        val cancel = rule.onNodeWithText("Keep my account").fetchSemanticsNode().boundsInRoot.top
        val confirm = rule.onNodeWithText("Delete account").fetchSemanticsNode().boundsInRoot.top
        assertTrue(cancel < confirm)
    }

    @Test
    fun iconTileShownOnlyWhenIconGiven() {
        rule.setContent { TmrConfirmPanelContent(icon = TmrIcons.Error) }
        rule.onNodeWithTag("confirmIconTile").assertExists()
    }

    @Test
    fun iconTileIsAbsentWithoutIcon() {
        rule.setContent { TmrConfirmPanelContent() }
        rule.onNodeWithTag("confirmIconTile").assertDoesNotExist()
    }

    @Test
    fun iconTileIsDecorative() {
        rule.setContent { TmrConfirmPanelContent(icon = TmrIcons.Error) }
        val config =
            rule.onNodeWithTag("confirmIconTile", useUnmergedTree = true).fetchSemanticsNode().config
        assertNull(config.getOrNull(SemanticsProperties.ContentDescription))
    }

    @androidx.compose.runtime.Composable
    private fun TmrConfirmPanelContent(icon: androidx.compose.ui.graphics.vector.ImageVector? = null) {
        TmrPreviewTheme(darkTheme = false) {
            TmrConfirmPanel(
                title = "Delete your account?",
                message = null,
                confirmLabel = "Delete account",
                cancelLabel = "Keep my account",
                onConfirm = {},
                onCancel = {},
                destructive = true,
                icon = icon,
            )
        }
    }
}
