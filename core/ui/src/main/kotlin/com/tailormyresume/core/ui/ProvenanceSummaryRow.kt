package com.tailormyresume.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.designsystem.component.TmrProvenanceChip
import com.tailormyresume.core.designsystem.component.TmrProvenanceKind
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry

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
    kinds: List<TmrProvenanceKind>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
    ) {
        kinds.distinct().forEach { kind ->
            TmrProvenanceChip(kind = kind, label = kind.label())
        }
    }
}
