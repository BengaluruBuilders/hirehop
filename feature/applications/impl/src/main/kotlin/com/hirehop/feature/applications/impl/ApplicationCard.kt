package com.hirehop.feature.applications.impl

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.core.model.JobApplication
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
internal fun ApplicationCard(
    application: JobApplication,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = application.job.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = application.job.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ApplicationStatusChip(status = application.status)
            }
            application.gapAnalysis?.keywordCoverage?.let { coverage ->
                Text(
                    text = stringResource(
                        R.string.feature_applications_impl_coverage,
                        coverage.covered,
                        coverage.total,
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = stringResource(
                    R.string.feature_applications_impl_updated,
                    rememberRelativeTime(application.updatedAt),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun rememberRelativeTime(instant: Instant): String = remember(instant) {
    DateUtils.getRelativeTimeSpanString(
        instant.toEpochMilliseconds(),
        Clock.System.now().toEpochMilliseconds(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
}

@Preview(showBackground = true)
@Composable
private fun ApplicationCardPreview() {
    HhTheme {
        ApplicationCard(application = previewApplication(), onClick = {})
    }
}
