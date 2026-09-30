package com.hirehop.feature.tailor.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hirehop.feature.tailor.impl.document.ResumeDocument
import com.hirehop.feature.tailor.impl.document.ResumeEntry
import com.hirehop.feature.tailor.impl.document.ResumeSection
import com.hirehop.feature.tailor.impl.document.SKILLS_HEADING

@Composable
internal fun ResumePreviewContent(
    document: ResumeDocument,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "note") {
            Text(
                text = stringResource(R.string.feature_tailor_impl_preview_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        item(key = "header") { PreviewHeader(document, Modifier.padding(horizontal = 16.dp)) }
        items(items = document.sections, key = { it.category.name }) { section ->
            PreviewSection(section, Modifier.padding(horizontal = 16.dp))
        }
        if (document.skills.isNotEmpty()) {
            item(key = "skills") { PreviewSkills(document.skills, Modifier.padding(horizontal = 16.dp)) }
        }
    }
}

@Composable
private fun PreviewSkills(skills: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PreviewHeading(SKILLS_HEADING)
        Text(text = skills.joinToString(", "), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun PreviewHeader(document: ResumeDocument, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = document.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        if (document.contactLine.isNotEmpty()) {
            Text(
                text = document.contactLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (document.headline.isNotEmpty()) {
            Text(text = document.headline, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PreviewSection(section: ResumeSection, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PreviewHeading(section.heading)
        section.entries.forEach { PreviewEntry(it) }
    }
}

@Composable
private fun PreviewHeading(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(top = 8.dp)) {
        Text(text = text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        HorizontalDivider()
    }
}

@Composable
private fun PreviewEntry(entry: ResumeEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = listOf(entry.title, entry.organization).filter { it.isNotEmpty() }.joinToString(", "),
            style = MaterialTheme.typography.titleSmall,
        )
        if (entry.dateRange.isNotEmpty()) {
            Text(
                text = entry.dateRange,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        entry.bullets.forEach {
            Text(text = "• $it", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
