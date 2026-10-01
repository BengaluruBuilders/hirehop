package com.hirehop.feature.applications.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.theme.HhTheme

private val CREDIT_TABULAR_FIGURES = "tnum"
private val CREDIT_BORDER = 1.dp

@Composable
internal fun ApplicationCreditChip(
    credits: ApplicationCreditLine,
    modifier: Modifier = Modifier,
) {
    val unit = when (credits.unit) {
        ApplicationCreditUnit.Free -> stringResource(R.string.feature_applications_impl_credit_free)
        ApplicationCreditUnit.Left -> stringResource(R.string.feature_applications_impl_credit_left)
    }
    val line = when (credits.unit) {
        ApplicationCreditUnit.Free -> pluralStringResource(
            id = R.plurals.feature_applications_impl_credit_line_free,
            credits.amount,
            credits.amount,
        )
        ApplicationCreditUnit.Left -> pluralStringResource(
            id = R.plurals.feature_applications_impl_credit_line_left,
            credits.amount,
            credits.amount,
        )
    }
    Row(
        modifier = modifier
            .heightIn(min = HhTheme.spacing.d48)
            .clip(RoundedCornerShape(HhTheme.shapes.full))
            .background(color = HhTheme.colors.surface)
            .border(
                width = CREDIT_BORDER,
                color = HhTheme.colors.hairlineStrong,
                shape = RoundedCornerShape(HhTheme.shapes.full),
            )
            .padding(horizontal = HhTheme.spacing.sm, vertical = HhTheme.spacing.xs)
            .semantics { contentDescription = line },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs),
    ) {
        Text(
            text = credits.amount.toString(),
            style = HhTheme.typography.labelLarge.copy(
                fontFeatureSettings = CREDIT_TABULAR_FIGURES,
            ),
            color = HhTheme.colors.onSurface,
        )
        Text(
            text = unit,
            style = HhTheme.typography.labelMedium,
            color = HhTheme.colors.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ApplicationCreditChipPreview() {
    HhTheme(darkTheme = false) {
        ApplicationCreditChip(credits = ApplicationCreditLine(1, ApplicationCreditUnit.Free))
    }
}
