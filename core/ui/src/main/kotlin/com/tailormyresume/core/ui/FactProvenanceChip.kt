package com.tailormyresume.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tailormyresume.core.designsystem.component.TmrProvenanceChip
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry

@Composable
fun FactProvenanceChip(
    source: FactSource,
    modifier: Modifier = Modifier,
    isConfirmed: Boolean = false,
) {
    val kind = FactSourceProvenance().confirmedKindOf(source, isConfirmed)
    TmrProvenanceChip(kind = kind, modifier = modifier, label = kind.label())
}

@Composable
fun FactProvenanceChip(
    entry: ProfileEntry,
    modifier: Modifier = Modifier,
) {
    val kind = FactSourceProvenance().kindOf(entry)
    TmrProvenanceChip(kind = kind, modifier = modifier, label = kind.label())
}
