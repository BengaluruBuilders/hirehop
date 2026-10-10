package com.tailormyresume.feature.analysis.impl.job

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

@Composable
internal fun JobEventHandler(
    events: Flow<JobEvent>,
    onAnalyzed: (applicationId: String) -> Unit,
    onRetry: () -> Unit,
) = Unit
