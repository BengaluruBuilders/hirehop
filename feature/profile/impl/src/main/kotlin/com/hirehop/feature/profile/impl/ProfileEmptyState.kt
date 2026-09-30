package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhOutlinedButton
import com.hirehop.core.ui.EmptyState

@Composable
internal fun ProfileEmptyState(
    onPasteResume: () -> Unit,
    onAddManually: () -> Unit,
    onLoadDemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    EmptyState(
        title = stringResource(R.string.feature_profile_impl_empty_title),
        message = stringResource(R.string.feature_profile_impl_empty_message),
        modifier = modifier,
        action = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                HhButton(
                    onClick = onPasteResume,
                    modifier = Modifier.fillMaxWidth(),
                    text = { Text(stringResource(R.string.feature_profile_impl_action_paste_resume)) },
                )
                HhOutlinedButton(
                    onClick = onAddManually,
                    modifier = Modifier.fillMaxWidth(),
                    text = { Text(stringResource(R.string.feature_profile_impl_action_add_manually)) },
                )
                TextButton(onClick = onLoadDemo) {
                    Text(stringResource(R.string.feature_profile_impl_action_load_demo))
                }
                Text(
                    text = stringResource(R.string.feature_profile_impl_trust_copy),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}
