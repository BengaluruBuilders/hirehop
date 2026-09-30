package com.hirehop.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.model.ProfileEntry

@Composable
fun FactIdTag(
    factId: String,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(id = R.string.core_ui_fact_id_content_description, factId)
    val shape = RoundedCornerShape(4.dp)
    Text(
        text = factId,
        modifier = modifier
            .clearAndSetSemantics { contentDescription = description }
            .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = shape)
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = shape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
