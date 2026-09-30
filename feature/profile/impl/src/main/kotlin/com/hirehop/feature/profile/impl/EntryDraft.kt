package com.hirehop.feature.profile.impl

import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.ProfileEntry

data class BulletDraft(
    val id: String?,
    val text: String,
)

data class EntryDraft(
    val category: EntryCategory,
    val title: String,
    val organization: String,
    val startDate: String,
    val endDate: String,
    val bullets: List<BulletDraft>,
) {
    val canSave: Boolean get() = title.isNotBlank()

    companion object {
        fun blank(category: EntryCategory): EntryDraft = EntryDraft(
            category = category,
            title = "",
            organization = "",
            startDate = "",
            endDate = "",
            bullets = listOf(BulletDraft(id = null, text = "")),
        )
    }
}

internal fun EntryDraft.withBulletText(index: Int, text: String): EntryDraft = copy(
    bullets = bullets.mapIndexed { position, bullet ->
        if (position == index) bullet.copy(text = text) else bullet
    },
)

internal fun EntryDraft.withNewBullet(): EntryDraft =
    copy(bullets = bullets + BulletDraft(id = null, text = ""))

internal fun EntryDraft.withoutBullet(index: Int): EntryDraft =
    copy(bullets = bullets.filterIndexed { position, _ -> position != index })

data class ContactDraft(
    val fullName: String,
    val email: String,
    val phone: String,
    val headline: String,
)

fun ProfileEntry.toDraft(): EntryDraft = EntryDraft(
    category = category,
    title = title,
    organization = organization,
    startDate = startDate,
    endDate = endDate,
    bullets = bullets.map { BulletDraft(id = it.id, text = it.text) },
)
