package com.hirehop.feature.profile.impl.guidedform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhCard
import com.hirehop.core.designsystem.component.HhHeadline
import com.hirehop.core.designsystem.icon.HhIcons
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.profile.impl.R

private val SegmentHeight = 6.dp
private val StepTile = 44.dp

@Composable
internal fun StepProgress(stepIndex: Int) {
    val total = GUIDED_STEPS.size
    val stepName = stringResource(stepTitleRes(guidedStepAt(stepIndex)))
    val description = stringResource(
        R.string.feature_profile_impl_guided_form_step_counter_description,
        stepIndex + 1,
        total,
        stepName,
    )
    Column(
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm), verticalAlignment = Alignment.Bottom) {
            Text(
                text = stringResource(R.string.feature_profile_impl_guided_form_step_of, stepIndex + 1, total),
                style = HhTheme.typography.titleM,
                color = HhTheme.colors.onSurface,
            )
            Text(text = stepName, style = HhTheme.typography.labelM, color = HhTheme.colors.onSurfaceVariant)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.xs)) {
            repeat(total) { position ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(SegmentHeight)
                        .background(
                            if (position <= stepIndex) HhTheme.colors.brand else HhTheme.colors.primaryContainer,
                            RoundedCornerShape(SegmentHeight / 2),
                        ),
                )
            }
        }
    }
}

@Composable
internal fun IntroContent() {
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md)) {
        HhHeadline(
            text = stringResource(R.string.feature_profile_impl_guided_form_intro_title),
            style = HhTheme.typography.headlineL,
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_guided_form_intro),
            style = HhTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            color = HhTheme.colors.onSurfaceVariant,
        )
        GUIDED_STEPS.forEachIndexed { index, step ->
            HhCard(contentPadding = PaddingValues(HhTheme.spacing.md)) {
                Row(horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(StepTile).background(HhTheme.colors.primaryContainer, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(step.icon(), contentDescription = null, tint = HhTheme.colors.onSurface)
                    }
                    Text(
                        text = stringResource(R.string.feature_profile_impl_guided_form_intro_step_item, index + 1, stringResource(stepTitleRes(step))),
                        style = HhTheme.typography.titleM,
                        color = HhTheme.colors.onSurface,
                    )
                }
            }
        }
    }
}

private fun GuidedStep.icon() = when (this) {
    GuidedStep.CONTACT -> HhIcons.Profile
    GuidedStep.EDUCATION -> HhIcons.Description
    GuidedStep.SKILLS -> HhIcons.Check
    GuidedStep.EXPERIENCE -> HhIcons.Applications
}
