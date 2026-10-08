package com.tailormyresume.feature.profile.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.profile.impl.R

private val ChipHeight = 48.dp
private val CloseSize = 24.dp

@Composable
internal fun RemovableChip(
    label: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val removeDescription = stringResource(R.string.feature_profile_impl_remove_chip, label)
    Row(
        modifier = modifier
            .heightIn(min = ChipHeight)
            .clip(TmrTheme.shapes.pill)
            .background(TmrTheme.colors.primaryContainer)
            .clickable(onClickLabel = removeDescription, role = Role.Button, onClick = onRemove)
            .padding(start = 16.dp, end = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = TmrTheme.typography.labelL, color = TmrTheme.colors.onSurface)
        Icon(
            imageVector = TmrIcons.Close,
            contentDescription = null,
            tint = TmrTheme.colors.onSurface,
            modifier = Modifier.size(CloseSize),
        )
    }
}
