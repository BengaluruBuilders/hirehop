package com.tailormyresume.feature.profile.impl.guidedform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tailormyresume.core.designsystem.component.TmrCard
import com.tailormyresume.core.designsystem.component.TmrHeadline
import com.tailormyresume.core.designsystem.icon.TmrIcons
import com.tailormyresume.core.designsystem.theme.TmrTheme
import com.tailormyresume.feature.profile.impl.R

private val SegmentHeight = 6.dp
private val StepTile = 44.dp
private const val LABEL_WRAP_FONT_SCALE = 1.3f

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StepProgress(
    number: Int,
    stepName: String?,
    filledBars: Int,
    doneSteps: Set<GuidedStep>,
    currentStep: GuidedStep?,
) {
    val total = GUIDED_STEPS.size
    val description =
        if (stepName != null) {
            stringResource(
                R.string.feature_profile_impl_guided_form_step_counter_description,
                number,
                total,
                stepName,
            )
        } else {
            stringResource(R.string.feature_profile_impl_guided_form_step_of, number, total)
        }
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm)) {
        Column(
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = description },
            verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.sm), verticalAlignment = Alignment.Bottom) {
                Text(
                    text = stringResource(R.string.feature_profile_impl_guided_form_step_of, number, total),
                    style = TmrTheme.typography.titleM,
                    color = TmrTheme.colors.onSurface,
                )
                if (stepName != null) {
                    Text(text = stepName, style = TmrTheme.typography.labelM, color = TmrTheme.colors.onSurfaceVariant)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs)) {
                repeat(total) { position ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(SegmentHeight)
                            .background(
                                if (position < filledBars) TmrTheme.colors.brand else TmrTheme.colors.primaryContainer,
                                RoundedCornerShape(SegmentHeight / 2),
                            ),
                    )
                }
            }
        }
        val wrapLabels = LocalDensity.current.fontScale > LABEL_WRAP_FONT_SCALE
        val labelModifier: RowScope.() -> Modifier = { if (wrapLabels) Modifier else Modifier.weight(1f) }
        val labelsContent: @Composable RowScope.() -> Unit = {
            GUIDED_STEPS.forEach { step ->
                val name = stringResource(stepTitleRes(step))
                val done = step in doneSteps
                val doneDescription =
                    if (done) stringResource(R.string.feature_profile_impl_guided_form_step_done_description, name) else null
                Row(
                    modifier = labelModifier()
                        .semantics(mergeDescendants = true) {
                            if (doneDescription != null) contentDescription = doneDescription
                        },
                    horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (done) {
                        Icon(TmrIcons.Check, contentDescription = null, tint = TmrTheme.colors.met, modifier = Modifier.size(TmrTheme.spacing.md))
                    }
                    Text(
                        text = name,
                        style = TmrTheme.typography.bodyS.copy(fontWeight = FontWeight.Bold),
                        color = if (step == currentStep) TmrTheme.colors.onSurface else TmrTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }
        if (wrapLabels) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
            ) { labelsContent() }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.xs),
                content = labelsContent,
            )
        }
    }
}

@Composable
internal fun IntroContent() {
    Column(verticalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md)) {
        TmrHeadline(
            text = stringResource(R.string.feature_profile_impl_guided_form_intro_title),
            style = TmrTheme.typography.headlineL,
        )
        Text(
            text = stringResource(R.string.feature_profile_impl_guided_form_intro),
            style = TmrTheme.typography.bodyL.copy(fontWeight = FontWeight.SemiBold),
            color = TmrTheme.colors.onSurfaceVariant,
        )
        GUIDED_STEPS.forEachIndexed { index, step ->
            TmrCard(contentPadding = PaddingValues(TmrTheme.spacing.md)) {
                Row(horizontalArrangement = Arrangement.spacedBy(TmrTheme.spacing.md), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(StepTile).background(TmrTheme.colors.primaryContainer, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(step.icon(), contentDescription = null, tint = TmrTheme.colors.onSurface)
                    }
                    Text(
                        text = stringResource(R.string.feature_profile_impl_guided_form_intro_step_item, index + 1, stringResource(stepTitleRes(step))),
                        style = TmrTheme.typography.titleM,
                        color = TmrTheme.colors.onSurface,
                    )
                }
            }
        }
    }
}

private fun GuidedStep.icon() = when (this) {
    GuidedStep.CONTACT -> TmrIcons.Profile
    GuidedStep.EDUCATION -> TmrIcons.Description
    GuidedStep.SKILLS -> TmrIcons.Check
    GuidedStep.EXPERIENCE -> TmrIcons.Applications
}
