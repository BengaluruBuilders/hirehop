package com.hirehop.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.hirehop.core.designsystem.component.HhFactId
import com.hirehop.core.model.ProfileEntry

@Composable
fun FactIdTag(
    factId: String,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(id = R.string.core_ui_fact_id_content_description, factId)
    HhFactId(
        id = factId,
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
    )
}

@Composable
fun FactIdTag(
    entry: ProfileEntry,
    modifier: Modifier = Modifier,
) {
    FactIdTag(factId = entry.id, modifier = modifier)
}
