package com.hirehop.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.ProfileEntry

@Composable
fun FactIdTag(
    factId: String,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(id = R.string.core_ui_fact_id_content_description, factId)
    val shape = RoundedCornerShape(HhTheme.shapes.xs)
    Text(
        text = factId,
        modifier = modifier
            .clearAndSetSemantics { contentDescription = description }
            .background(color = HhTheme.colors.spot, shape = shape)
            .border(width = HhTheme.spacing.d2, color = HhTheme.colors.hairlineStrong, shape = shape)
            .padding(horizontal = HhTheme.spacing.d12 / 2, vertical = HhTheme.spacing.d4 / 2),
        style = HhTheme.typography.mono,
        color = HhTheme.colors.spotInk,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun FactIdTag(
    entry: ProfileEntry,
    modifier: Modifier = Modifier,
) {
    FactIdTag(factId = entry.id, modifier = modifier)
}
