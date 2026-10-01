package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.core.designsystem.component.HhDivider
import com.hirehop.core.designsystem.theme.HhTheme
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeEntry
import com.hirehop.feature.tailor.impl.document.ResumeSection
import com.hirehop.feature.tailor.impl.document.SKILLS_HEADING

private const val BULLET_GLYPH = "•"

@Composable
internal fun ResumePreviewContent(
    document: ResumeDocument,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier.background(HhTheme.colors.surface),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.md),
    ) {
        item(key = "note") {
            Text(
                text = stringResource(R.string.feature_tailor_impl_preview_note),
                style = HhTheme.typography.bodySmall,
                color = HhTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = HhTheme.spacing.lg),
            )
        }
        item(key = "header") { PreviewHeader(document, Modifier.padding(horizontal = HhTheme.spacing.lg)) }
        items(items = document.sections, key = { it.category.name }) { section ->
            PreviewSection(section, Modifier.padding(horizontal = HhTheme.spacing.lg))
        }
        if (document.skills.isNotEmpty()) {
            item(key = "skills") { PreviewSkills(document.skills, Modifier.padding(horizontal = HhTheme.spacing.lg)) }
        }
    }
}

@Composable
private fun PreviewSkills(skills: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        PreviewHeading(SKILLS_HEADING)
        Text(
            text = skills.joinToString(", "),
            style = HhTheme.typography.bodyMedium,
            color = HhTheme.colors.onSurface,
        )
    }
}

@Composable
private fun PreviewHeader(document: ResumeDocument, modifier: Modifier = Modifier) {
    val colors = HhTheme.colors
    Column(modifier = modifier) {
        Text(
            text = document.name,
            style = HhTheme.typography.headlineSmall,
            color = colors.onSurface,
            fontWeight = FontWeight.Bold,
        )
        if (document.contactLine.isNotEmpty()) {
            Text(
                text = document.contactLine,
                style = HhTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        if (document.headline.isNotEmpty()) {
            Text(
                text = document.headline,
                style = HhTheme.typography.bodyMedium,
                color = colors.onSurface,
            )
        }
    }
}

@Composable
private fun PreviewSection(section: ResumeSection, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm)) {
        PreviewHeading(section.heading)
        section.entries.forEach { PreviewEntry(it) }
    }
}

@Composable
private fun PreviewHeading(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(top = HhTheme.spacing.sm)) {
        Text(
            text = text,
            style = HhTheme.typography.titleMedium,
            color = HhTheme.colors.onSurface,
            fontWeight = FontWeight.Bold,
        )
        HhDivider()
    }
}

@Composable
private fun PreviewEntry(entry: ResumeEntry) {
    val colors = HhTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(HhTheme.spacing.xxs)) {
        Text(
            text = listOf(entry.title, entry.organization).filter { it.isNotEmpty() }.joinToString(", "),
            style = HhTheme.typography.titleSmall,
            color = colors.onSurface,
        )
        if (entry.dateRange.isNotEmpty()) {
            Text(
                text = entry.dateRange,
                style = HhTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
            )
        }
        entry.bullets.forEach { bullet ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(HhTheme.spacing.sm),
            ) {
                Text(
                    text = BULLET_GLYPH,
                    style = HhTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                )
                Text(
                    text = bullet,
                    style = HhTheme.typography.bodyMedium,
                    color = colors.onSurface,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
        }
    }
}
