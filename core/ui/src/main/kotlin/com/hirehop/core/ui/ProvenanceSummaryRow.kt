package com.hirehop.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.designsystem.component.HhProvenanceKind
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry

@Composable
fun ProvenanceSummaryRow(
    entries: List<ProfileEntry>,
    modifier: Modifier = Modifier,
) {
    ProvenanceKindRow(
        kinds = FactSourceProvenance().kindsOfEntries(entries),
        modifier = modifier,
    )
}

@Composable
fun ProvenanceSummaryRowForSources(
    sources: List<FactSource>,
    modifier: Modifier = Modifier,
) {
    ProvenanceKindRow(
        kinds = FactSourceProvenance().kindsOf(sources),
        modifier = modifier,
    )
}

@Composable
private fun ProvenanceKindRow(
    kinds: List<HhProvenanceKind>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        kinds.distinct().forEach { kind ->
            HhProvenanceChip(kind = kind)
        }
    }
}
