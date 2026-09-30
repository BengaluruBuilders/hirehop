package com.hirehop.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.hirehop.core.designsystem.component.HhProvenanceChip
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry

@Composable
fun FactProvenanceChip(
    source: FactSource,
    modifier: Modifier = Modifier,
    isConfirmed: Boolean = false,
) {
    HhProvenanceChip(
        kind = FactSourceProvenance().confirmedKindOf(source, isConfirmed),
        modifier = modifier,
    )
}

@Composable
fun FactProvenanceChip(
    entry: ProfileEntry,
    modifier: Modifier = Modifier,
) {
    HhProvenanceChip(
        kind = FactSourceProvenance().kindOf(entry),
        modifier = modifier,
    )
}
