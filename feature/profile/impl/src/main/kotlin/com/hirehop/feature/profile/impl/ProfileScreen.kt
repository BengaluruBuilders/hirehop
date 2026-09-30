package com.hirehop.feature.profile.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.feature.profile.api.R as apiR

@Composable
internal fun ProfileScreen(modifier: Modifier = Modifier) {
    val title = stringResource(apiR.string.feature_profile_api_title)
    Scaffold(
        modifier = modifier,
        topBar = { HhTopAppBar(title = title) },
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
