package com.hirehop.feature.applications.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.feature.applications.api.R as apiR

@Composable
internal fun ApplicationsScreen(
    onAddApplicationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(apiR.string.feature_applications_api_title)
    Scaffold(
        modifier = modifier,
        topBar = { HhTopAppBar(title = title) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddApplicationClick) {
                Icon(
                    imageVector = HhIcons.Add,
                    contentDescription = stringResource(R.string.feature_applications_impl_add_application),
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = title)
        }
    }
}
