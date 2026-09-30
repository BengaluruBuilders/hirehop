package com.hirehop.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhBottomActionBar(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues? = null,
    creditDisclosure: (@Composable () -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit,
) {
    val padding = contentPadding
        ?: PaddingValues(
            start = HhTheme.spacing.d20,
            end = HhTheme.spacing.d20,
            top = HhTheme.spacing.md,
            bottom = HhTheme.spacing.md,
        )
    Column(modifier = modifier.fillMaxWidth()) {
        HhDivider()
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = HhTheme.colors.surfaceContainer,
            shadowElevation = HhTheme.elevation.level3.elevation,
        ) {
            Column(
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                if (creditDisclosure != null) {
                    creditDisclosure()
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
                    content = actions,
                )
            }
        }
    }
}

private const val HH_BOTTOM_ACTION_BAR_SAMPLE_DISCLOSURE = "1 of 3 free today"
private const val HH_BOTTOM_ACTION_BAR_SAMPLE_CANCEL = "Not now"
private const val HH_BOTTOM_ACTION_BAR_SAMPLE_CONFIRM = "Analyse this JD"

@Preview(showBackground = true)
@Composable
private fun HhBottomActionBarPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhBottomActionBarPreviewBar()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhBottomActionBarDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhBottomActionBarPreviewBar()
    }
}

@Composable
private fun HhBottomActionBarPreviewBar() {
    HhBottomActionBar(
        creditDisclosure = {
            Text(
                text = HH_BOTTOM_ACTION_BAR_SAMPLE_DISCLOSURE,
                style = HhTheme.typography.labelMedium,
                color = HhTheme.colors.onSurfaceVariant,
            )
        },
    ) {
        HhOutlinedButton(onClick = {}, modifier = Modifier.weight(1f)) {
            Text(text = HH_BOTTOM_ACTION_BAR_SAMPLE_CANCEL, color = HhTheme.colors.onSurfaceVariant)
        }
        HhButton(onClick = {}, modifier = Modifier.weight(1f)) {
            Text(text = HH_BOTTOM_ACTION_BAR_SAMPLE_CONFIRM)
        }
    }
}
