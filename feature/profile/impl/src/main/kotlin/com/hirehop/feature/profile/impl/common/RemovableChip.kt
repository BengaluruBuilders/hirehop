package com.hirehop.feature.profile.impl.common

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
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.profile.impl.R

private val ChipHeight = 32.dp
private val CloseSize = 18.dp

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
            .clip(HhTheme.shapes.pill)
            .background(HhTheme.colors.primaryContainer)
            .clickable(onClickLabel = removeDescription, role = Role.Button, onClick = onRemove)
            .padding(start = HhTheme.spacing.md, end = HhTheme.spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = HhTheme.typography.labelL, color = HhTheme.colors.onPrimaryContainer)
        Icon(
            imageVector = HhIcons.Close,
            contentDescription = null,
            tint = HhTheme.colors.onPrimaryContainer,
            modifier = Modifier.size(CloseSize),
        )
    }
}
