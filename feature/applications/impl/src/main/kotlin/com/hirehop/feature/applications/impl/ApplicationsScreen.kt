package com.hirehop.feature.applications.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hirehop.core.designsystem.component.HhButton
import com.hirehop.core.designsystem.component.HhLoadingWheel
import com.hirehop.core.designsystem.component.HhTopAppBar
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.applications.api.R as apiR

@Composable
internal fun ApplicationsRoute(
    onApplicationClick: (String) -> Unit,
    onNewApplicationClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ApplicationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ApplicationsScreen(
        uiState = uiState,
        onApplicationClick = onApplicationClick,
        onNewApplicationClick = onNewApplicationClick,
        modifier = modifier,
    )
}

@Composable
internal fun ApplicationsScreen(
    uiState: ApplicationsUiState,
    onApplicationClick: (String) -> Unit,
    onNewApplicationClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { HhTopAppBar(title = stringResource(apiR.string.feature_applications_api_title)) },
        floatingActionButton = {
            if (uiState !is ApplicationsUiState.Empty) {
                NewApplicationButton(onClick = onNewApplicationClick)
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            ApplicationsBody(
                uiState = uiState,
                onApplicationClick = onApplicationClick,
                onNewApplicationClick = onNewApplicationClick,
            )
        }
    }
}

@Composable
private fun BoxScope.ApplicationsBody(
    uiState: ApplicationsUiState,
    onApplicationClick: (String) -> Unit,
    onNewApplicationClick: () -> Unit,
) {
    when (uiState) {
        ApplicationsUiState.Loading -> HhLoadingWheel(
            contentDesc = stringResource(R.string.feature_applications_loading),
            modifier = Modifier.align(Alignment.Center),
        )
        ApplicationsUiState.Empty -> ApplicationsEmptyState(
            onAnalyzeClick = onNewApplicationClick,
            modifier = Modifier.align(Alignment.Center),
        )
        is ApplicationsUiState.Success -> ApplicationsList(
            uiState = uiState,
            onApplicationClick = onApplicationClick,
        )
    }
}

@Composable
private fun ApplicationsList(
    uiState: ApplicationsUiState.Success,
    onApplicationClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = uiState.items, key = { it.id }) { application ->
            ApplicationCard(
                application = application,
                onClick = { onApplicationClick(application.id) },
            )
        }
    }
}

@Composable
private fun NewApplicationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        icon = { Icon(imageVector = HhIcons.Add, contentDescription = null) },
        text = { Text(stringResource(R.string.feature_applications_new)) },
    )
}

@Composable
private fun ApplicationsEmptyState(
    onAnalyzeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.feature_applications_empty_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        HhButton(
            onClick = onAnalyzeClick,
            text = { Text(stringResource(R.string.feature_applications_empty_action)) },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenSuccessPreview() {
    HhTheme {
        ApplicationsScreen(
            uiState = ApplicationsUiState.Success(previewApplications()),
            onApplicationClick = {},
            onNewApplicationClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenEmptyPreview() {
    HhTheme {
        ApplicationsScreen(
            uiState = ApplicationsUiState.Empty,
            onApplicationClick = {},
            onNewApplicationClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationsScreenLoadingPreview() {
    HhTheme {
        ApplicationsScreen(
            uiState = ApplicationsUiState.Loading,
            onApplicationClick = {},
            onNewApplicationClick = {},
        )
    }
}
