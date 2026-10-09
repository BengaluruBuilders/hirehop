package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry
import com.tailormyresume.core.model.fitBulletsToLimit
import com.tailormyresume.core.model.splitBulletsForEntries

internal class EntryDraft(
    var title: String,
    var organization: String = "",
    var startDate: String = "",
    var endDate: String = "",
) {
    val bullets = mutableListOf<String>()
}

internal class EntrySectionParser(private val category: EntryCategory) {
    private val drafts = mutableListOf<EntryDraft>()
    private var lastWasBullet = false

    fun accept(rawLine: String) {
        val bullet = BulletLine.parse(rawLine)
        val current = drafts.lastOrNull()
        when {
            bullet.marked -> addBullet(bullet.text)
            continuesLastBullet(rawLine) -> extendLastBullet(rawLine)
            current != null && detailLine.containsMatchIn(rawLine) -> addBullet(rawLine)
            category in singleLineCategories -> startEntry(rawLine)
            current != null && isMetaLine(current, rawLine) -> mergeMeta(current, rawLine)
            current != null && isProse(rawLine) -> addBullet(rawLine)
            else -> startEntry(rawLine)
        }
    }

    val entryCount: Int get() = keptDrafts().size

    fun build(nextEntryId: () -> String, entriesBefore: Int = 0, draftsAfter: Int = 0): List<ProfileEntry> {
        val kept = keptDrafts()
        val built = mutableListOf<ProfileEntry>()
        kept.forEachIndexed { index, draft ->
            val after = draftsAfter + kept.size - index - 1
            splitBulletsForEntries(draft.bullets, entriesBefore + built.size, after).forEach { chunk ->
                built += draft.toEntry(nextEntryId(), chunk)
            }
        }
        return built
    }

    private fun keptDrafts(): List<EntryDraft> = drafts.filter { it.title.isNotBlank() || it.bullets.isNotEmpty() }

    private fun EntryDraft.toEntry(id: String, chunk: List<String>) = ProfileEntry(
        id = id,
        category = category,
        title = title.ifBlank { defaultTitle },
        organization = organization,
        startDate = startDate,
        endDate = endDate,
        bullets = fitBulletsToLimit(chunk).mapIndexed { index, text -> EvidenceBullet("$id-b${index + 1}", text) },
        source = FactSource.IMPORTED,
        isConfirmed = false,
    )

    private val defaultTitle: String = category.name.lowercase().replaceFirstChar { it.uppercase() }

    private fun startEntry(text: String) {
        val parts = HeaderLineParser.parseEntryLine(text, category)
        drafts += EntryDraft(parts.title, parts.organization, parts.start, parts.end)
        lastWasBullet = false
    }

    private fun addBullet(text: String) {
        if (drafts.isEmpty()) drafts += EntryDraft(title = "")
        drafts.last().bullets += text.replace(whitespace, " ").trim()
        lastWasBullet = true
    }

    private fun continuesLastBullet(line: String): Boolean {
        if (!lastWasBullet || !line.first().isLowerCase()) return false
        val previous = drafts.lastOrNull()?.bullets?.lastOrNull().orEmpty()
        val looksLikeEntryLine = DateRangeExtractor.find(line) != null || entrySeparator.containsMatchIn(line)
        return !previous.endsWith('.') && !looksLikeEntryLine
    }

    private fun extendLastBullet(text: String) {
        val bullets = drafts.last().bullets
        bullets[bullets.lastIndex] = "${bullets.last()} ${text.trim()}"
    }

    private fun isMetaLine(current: EntryDraft, line: String): Boolean {
        if (current.bullets.isNotEmpty() || line.length > MAX_META_LENGTH || line.endsWith('.')) return false
        if (category == EntryCategory.PROJECT) return HeaderLineParser.isDateOnly(line)
        val hasDates = DateRangeExtractor.find(line) != null
        val missingDetail = current.organization.isEmpty() || (current.startDate.isEmpty() && current.endDate.isEmpty())
        return missingDetail && (hasDates || current.organization.isEmpty())
    }

    private fun mergeMeta(current: EntryDraft, line: String) {
        val meta = HeaderLineParser.parseMetaLine(line)
        if (current.organization.isEmpty()) current.organization = meta.organization
        if (current.startDate.isEmpty() && current.endDate.isEmpty()) {
            current.startDate = meta.start
            current.endDate = meta.end
        }
    }

    private fun isProse(line: String): Boolean = line.length > MAX_TITLE_LENGTH || line.endsWith('.')

    private companion object {
        const val MAX_META_LENGTH = 90
        const val MAX_TITLE_LENGTH = 90
        val whitespace = Regex("\\s+")
        val entrySeparator = Regex("\\s\\|\\s|\\s[-–—]\\s")
        val singleLineCategories = setOf(EntryCategory.CERTIFICATION, EntryCategory.ACHIEVEMENT)
        val detailLine = Regex(
            "^(?:cgpa|gpa|percentage|score|marks|grade|aggregate|technologies|technology|tech stack|tools|stack|environment|role|duration|link|github|domain|skills used)\\b\\s*[:\\-–]",
            RegexOption.IGNORE_CASE,
        )
    }
}
