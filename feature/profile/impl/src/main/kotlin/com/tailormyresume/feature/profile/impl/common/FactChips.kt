package com.tailormyresume.feature.profile.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrFactId
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.profile.impl.R

private val ChipHeight = 26.dp
private val ChipIconSize = 16.dp

@Composable
internal fun FactIdChip(
    id: String,
    status: FactStatus,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(
        R.string.feature_profile_impl_fact_chip_description,
        id,
        stringResource(status.labelRes()),
    )
    TmrFactId(id = id, modifier = modifier.clearAndSetSemantics { contentDescription = description })
}

@Composable
internal fun ToConfirmChip(
    label: String,
    modifier: Modifier = Modifier,
) {
    NeutralChip(label = label, icon = TmrIcons.Error, modifier = modifier, tint = TmrTheme.colors.onSurfaceVariant)
}

@Composable
internal fun NeutralChip(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = TmrTheme.colors.onSurface,
) {
    Row(
        modifier = modifier
            .heightIn(min = ChipHeight)
            .background(TmrTheme.colors.primaryContainer, TmrTheme.shapes.pill)
            .padding(start = 7.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(ChipIconSize))
        Text(text = label, style = TmrTheme.typography.labelM.copy(fontWeight = FontWeight.ExtraBold), color = tint)
    }
}
