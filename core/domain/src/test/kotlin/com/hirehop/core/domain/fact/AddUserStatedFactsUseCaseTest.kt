package com.hirehop.core.domain.fact

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.domain.AddUserStatedFactsUseCase
import com.hirehop.core.domain.FakeProfileRepository
import com.hirehop.core.domain.SequentialIdGenerator
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import kotlinx.coroutines.test.runTest
import org.junit.Test

class AddUserStatedFactsUseCaseTest {
    private val baseProfile = profile()
    private val repository = FakeProfileRepository(baseProfile)
    private val useCase = AddUserStatedFactsUseCase(repository, SequentialIdGenerator("bullet"))

    @Test
    fun aValidFactIsAddedAsAnUnconfirmedUserStatedEntry() = runTest {
        val outcome = useCase(listOf(draft(title = "Placement Stats Dashboard", category = EntryCategory.PROJECT)))

        val added = addedEntries(outcome)
        assertThat(added).hasSize(1)
        assertThat(added.single().title).isEqualTo("Placement Stats Dashboard")
        assertThat(added.single().category).isEqualTo(EntryCategory.PROJECT)
        assertThat(added.single().source).isEqualTo(FactSource.USER_STATED)
        assertThat(added.single().isConfirmed).isFalse()
    }

    @Test
    fun theDetailBecomesOneEvidenceBulletFromTheIdGenerator() = runTest {
        useCase(listOf(draft(detail = "Built a dashboard for the college cell.")))

        val bullets = checkNotNull(repository.current()).entries.last().bullets
        assertThat(bullets).hasSize(1)
        assertThat(bullets.single().text).isEqualTo("Built a dashboard for the college cell.")
        assertThat(bullets.single().id).isEqualTo("bullet-1")
    }

    @Test
    fun aBlankDetailBecomesNoBullet() = runTest {
        useCase(listOf(draft(detail = "   ")))

        assertThat(checkNotNull(repository.current()).entries.last().bullets).isEmpty()
    }

    @Test
    fun theEntryIsFiledWithAFreshIdPerFact() = runTest {
        val outcome = useCase(
            listOf(
                draft(title = "Campus Events App", category = EntryCategory.PROJECT),
                draft(title = "Coding club lead", category = EntryCategory.EXPERIENCE),
            ),
        )

        assertThat(addedEntries(outcome).map { it.id }).containsExactly("C-01", "I-01").inOrder()
    }

    @Test
    fun twoFactsOfOneCategoryGetDifferentIds() = runTest {
        val outcome = useCase(
            listOf(
                draft(title = "Campus Events App", category = EntryCategory.PROJECT),
                draft(title = "Placement Stats Dashboard", category = EntryCategory.PROJECT),
            ),
        )

        assertThat(addedEntries(outcome).map { it.id }).containsExactly("C-01", "C-02").inOrder()
    }

    @Test
    fun aNewFactDoesNotTakeAnIdThatIsAlreadyInTheProfile() = runTest {
        val taken = listOf(entry("I-01", EntryCategory.EXPERIENCE, "Android developer intern"))
        val withExisting = FakeProfileRepository(baseProfile.copy(entries = taken))

        val outcome = AddUserStatedFactsUseCase(withExisting, SequentialIdGenerator("bullet"))(
            listOf(draft(title = "Backend developer intern", category = EntryCategory.EXPERIENCE)),
        )

        assertThat(addedEntries(outcome).map { it.id }).containsExactly("I-02")
    }

    @Test
    fun existingEntriesAreKeptAndTheProfileIsSavedOnce() = runTest {
        val taken = listOf(entry("U-01", EntryCategory.EDUCATION, "B.Tech in Information Technology"))
        val withExisting = FakeProfileRepository(baseProfile.copy(entries = taken))

        val outcome = AddUserStatedFactsUseCase(withExisting, SequentialIdGenerator("bullet"))(
            listOf(
                draft(title = "Android bootcamp", category = EntryCategory.CERTIFICATION),
                draft(title = "Batch result", category = EntryCategory.ACHIEVEMENT),
            ),
        )

        assertThat(addedEntries(outcome).map { it.id }).containsExactly("X-01", "P-01").inOrder()
        assertThat(checkNotNull(withExisting.current()).entries.map { it.id })
            .containsExactly("U-01", "X-01", "P-01").inOrder()
        assertThat(withExisting.saveCount).isEqualTo(1)
    }

    @Test
    fun theRestOfTheProfileIsUntouched() = runTest {
        useCase(listOf(draft(title = "Campus Events App")))

        val profile = checkNotNull(repository.current())
        assertThat(profile.fullName).isEqualTo(baseProfile.fullName)
        assertThat(profile.email).isEqualTo(baseProfile.email)
        assertThat(profile.skills).isEqualTo(baseProfile.skills)
    }

    @Test
    fun aBlankDraftIsIgnoredWhenRealFactsArePresent() = runTest {
        val outcome = useCase(listOf(draft(title = "Campus Events App"), draft(title = "  ")))

        assertThat(addedEntries(outcome)).hasSize(1)
    }

    @Test
    fun anEmptyListAddsNothing() = runTest {
        assertThat(useCase(emptyList())).isEqualTo(AddFactsOutcome.NothingToAdd)
        assertThat(repository.saveCount).isEqualTo(0)
    }

    @Test
    fun anAllBlankListAddsNothing() = runTest {
        val outcome = useCase(
            listOf(
                draft(title = " ", organization = " ", startDate = " ", endDate = " ", detail = " "),
                draft(title = "", organization = "", startDate = "", endDate = "", detail = ""),
            ),
        )

        assertThat(outcome).isEqualTo(AddFactsOutcome.NothingToAdd)
        assertThat(repository.saveCount).isEqualTo(0)
        assertThat(checkNotNull(repository.current())).isEqualTo(baseProfile)
    }

    @Test
    fun oneInvalidDraftRejectsEveryFactAndWritesNothing() = runTest {
        val outcome = useCase(
            listOf(
                draft(title = "Campus Events App", category = EntryCategory.PROJECT),
                draft(title = "", category = EntryCategory.EXPERIENCE, detail = "Ran the club."),
            ),
        )

        assertThat(outcome).isEqualTo(
            AddFactsOutcome.Rejected(listOf(FactDraftError(FactField.TITLE, FactDraftErrorReason.REQUIRED))),
        )
        assertThat(repository.saveCount).isEqualTo(0)
        assertThat(checkNotNull(repository.current())).isEqualTo(baseProfile)
    }

    @Test
    fun everyErrorFromEveryDraftComesBackInTheRejection() = runTest {
        val outcome = useCase(
            listOf(
                draft(title = "", category = EntryCategory.PROJECT, detail = "Built it."),
                draft(title = "Campus Events App", category = EntryCategory.PROJECT, startDate = "2025", endDate = "2024"),
            ),
        )

        val rejected = outcome as AddFactsOutcome.Rejected
        assertThat(rejected.errors).containsExactly(
            FactDraftError(FactField.TITLE, FactDraftErrorReason.REQUIRED),
            FactDraftError(FactField.END_DATE, FactDraftErrorReason.END_BEFORE_START),
        ).inOrder()
        assertThat(repository.saveCount).isEqualTo(0)
    }

    @Test
    fun aRejectedWriteNeverConsumesABulletId() = runTest {
        useCase(listOf(draft(title = "", category = EntryCategory.PROJECT, detail = "Built it.")))

        useCase(listOf(draft(title = "Campus Events App", category = EntryCategory.PROJECT, detail = "Built it.")))

        assertThat(checkNotNull(repository.current()).entries.last().bullets.single().id).isEqualTo("bullet-1")
    }

    @Test
    fun noProfileMeansNothingIsAdded() = runTest {
        val empty = FakeProfileRepository(null)
        val outcome = AddUserStatedFactsUseCase(empty, SequentialIdGenerator("bullet"))(
            listOf(draft(title = "Campus Events App")),
        )

        assertThat(outcome).isEqualTo(AddFactsOutcome.NothingToAdd)
        assertThat(empty.saveCount).isEqualTo(0)
        assertThat(empty.current()).isNull()
    }

    @Test
    fun textIsTrimmedBeforeItIsStored() = runTest {
        useCase(
            listOf(
                draft(
                    title = "  Campus Events App  ",
                    organization = " Personal project ",
                    startDate = " 2024 ",
                    endDate = " 2025 ",
                    detail = "  Built it offline with Room.  ",
                ),
            ),
        )

        val entry = checkNotNull(repository.current()).entries.single()
        assertThat(entry.title).isEqualTo("Campus Events App")
        assertThat(entry.organization).isEqualTo("Personal project")
        assertThat(entry.startDate).isEqualTo("2024")
        assertThat(entry.endDate).isEqualTo("2025")
        assertThat(entry.bullets.single().text).isEqualTo("Built it offline with Room.")
    }

    private fun addedEntries(outcome: AddFactsOutcome): List<ProfileEntry> {
        assertThat(outcome).isInstanceOf(AddFactsOutcome.Added::class.java)
        return (outcome as AddFactsOutcome.Added).entries
    }

    private fun draft(
        title: String = "Fact",
        category: EntryCategory = EntryCategory.PROJECT,
        organization: String = "",
        startDate: String = "",
        endDate: String = "",
        detail: String = "",
    ) = FactDraft(
        category = category,
        title = title,
        organization = organization,
        startDate = startDate,
        endDate = endDate,
        detail = detail,
    )

    private fun entry(id: String, category: EntryCategory, title: String) = ProfileEntry(
        id = id,
        category = category,
        title = title,
        organization = "",
        startDate = "",
        endDate = "",
        bullets = listOf(EvidenceBullet("$id-b1", "Text for $id.")),
        source = FactSource.IMPORTED,
        isConfirmed = true,
    )

    private fun profile() = CandidateProfile(
        fullName = "Test Candidate",
        email = "test@example.com",
        phone = "+91 90000 00000",
        headline = "Android engineering student",
        skills = listOf("Kotlin", "Room"),
        entries = emptyList(),
    )
}
