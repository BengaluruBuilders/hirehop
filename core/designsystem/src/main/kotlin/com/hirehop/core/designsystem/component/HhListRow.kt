package com.hirehop.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.hirehop.core.designsystem.theme.HhTheme

@Composable
fun HhListRow(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    showDivider: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val rowModifier = if (onClick == null) {
        modifier
    } else {
        modifier.clickable(onClick = onClick)
    }
    Column(modifier = rowModifier) {
        if (showDivider) {
            HhDivider()
        }
        Row(
            modifier = Modifier
                .defaultMinSize(minHeight = HhHeightListRow)
                .padding(
                    horizontal = HhTheme.spacing.d20,
                    vertical = HhSpacingFourteen,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
        ) {
            if (leading != null) {
                Box { leading() }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs),
            ) {
                content()
            }
            if (trailing != null) {
                Box { trailing() }
            }
        }
    }
}

private const val HH_LIST_ROW_SAMPLE_TITLE = "Wrote weekly SQL reports in PostgreSQL"
private const val HH_LIST_ROW_SAMPLE_BODY = "F-07, confirmed 12 Sep"

@Preview(showBackground = true)
@Composable
private fun HhListRowPreview() {
    HhPreviewTheme(darkTheme = false) {
        HhListRowPreviewColumn()
    }
}

@Preview(showBackground = true)
@Composable
private fun HhListRowDarkPreview() {
    HhPreviewTheme(darkTheme = true) {
        HhListRowPreviewColumn()
    }
}

@Composable
private fun HhListRowPreviewColumn() {
    Column {
        HhListRow(showDivider = false) {
            Text(
                text = HH_LIST_ROW_SAMPLE_TITLE,
                style = HhTheme.typography.bodyLarge,
                color = HhTheme.colors.onSurface,
            )
            Text(
                text = HH_LIST_ROW_SAMPLE_BODY,
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
            )
        }
        HhListRow {
            Text(
                text = HH_LIST_ROW_SAMPLE_TITLE,
                style = HhTheme.typography.bodyLarge,
                color = HhTheme.colors.onSurface,
            )
        }
    }
}
