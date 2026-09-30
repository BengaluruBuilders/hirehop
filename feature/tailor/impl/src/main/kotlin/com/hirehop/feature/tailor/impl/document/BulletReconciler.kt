package com.hirehop.feature.tailor.impl.document

import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.EditType
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume

internal fun TailoredBullet.matches(bullet: EvidenceBullet): Boolean =
    bullet.id in sourceIds && originalText.trim() == bullet.text.trim()

internal fun TailoredBullet.isFreshFor(entry: ProfileEntry): Boolean = entry.bullets.any(::matches)

internal object BulletReconciler {

    fun reconcile(entry: ProfileEntry, resume: TailoredResume): List<String> {
        val fresh = resume.bullets.filter { it.entryId == entry.id && it.isFreshFor(entry) }
        val resolved = entry.bullets.map { resolve(it, fresh) }
        val result = resolved.filter { it.movedTo == null }.toMutableList()
        resolved.filter { it.movedTo != null }
            .sortedBy { it.movedTo }
            .forEach { result.add((it.movedTo ?: 0).coerceAtMost(result.size), it) }
        return result.map { it.text }
    }

    private fun resolve(bullet: EvidenceBullet, fresh: List<TailoredBullet>): ResolvedBullet {
        val match = fresh.firstOrNull { it.matches(bullet) }
        val applies = match != null &&
            match.violations.isEmpty() &&
            match.decision == BulletDecision.ACCEPTED
        if (match == null || !applies) return ResolvedBullet(bullet.text, movedTo = null)
        val movedTo = if (EditType.REORDER in match.editTypes) fresh.indexOf(match) else null
        return ResolvedBullet(match.proposedText, movedTo)
    }

    private data class ResolvedBullet(val text: String, val movedTo: Int?)
}
