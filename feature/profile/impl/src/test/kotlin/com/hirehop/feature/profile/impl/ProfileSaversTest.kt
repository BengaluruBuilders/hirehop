package com.hirehop.feature.profile.impl

import androidx.compose.runtime.saveable.SaverScope
import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.EntryCategory
import org.junit.Test

class ProfileSaversTest {

    private val scope = SaverScope { true }

    @Test
    fun sheetSaver_restoresEverySheet() {
        val sheets = listOf(
            null,
            ProfileSheet.PasteResume,
            ProfileSheet.EditContact,
            ProfileSheet.AddSkill,
            ProfileSheet.ConfirmAll,
            ProfileSheet.ClearProfile,
            ProfileSheet.DeleteEntry("entry-1"),
            ProfileSheet.EditEntry(entryId = "entry-2", category = EntryCategory.PROJECT),
            ProfileSheet.EditEntry(entryId = null, category = EntryCategory.ACHIEVEMENT),
        )

        sheets.forEach { sheet ->
            val saved = checkNotNull(with(ProfileSheetSaver) { scope.save(sheet) })
            assertThat(ProfileSheetSaver.restore(saved)).isEqualTo(sheet)
        }
    }

    @Test
    fun entryDraftSaver_restoresDraftWithNewAndExistingBullets() {
        val draft = EntryDraft(
            category = EntryCategory.EXPERIENCE,
            title = "Intern",
            organization = "BrightLeaf",
            startDate = "Jun 2025",
            endDate = "Aug 2025",
            bullets = listOf(BulletDraft("b-1", "Built a screen."), BulletDraft(null, "")),
        )

        val saved = checkNotNull(with(EntryDraftSaver) { scope.save(draft) })

        assertThat(EntryDraftSaver.restore(saved)).isEqualTo(draft)
    }

    @Test
    fun contactDraftSaver_restoresContact() {
        val contact = ContactDraft("Asha Rao", "asha@example.com", "+91 90000 00000", "Android developer")

        val saved = checkNotNull(with(ContactDraftSaver) { scope.save(contact) })

        assertThat(ContactDraftSaver.restore(saved)).isEqualTo(contact)
    }
}
