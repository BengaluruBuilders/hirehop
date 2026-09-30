package com.hirehop.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry
import org.junit.Test

class OfflineResumeTextParserTest {
    private val parser = OfflineResumeTextParser()

    private fun parseResource(name: String): CandidateProfile = parser.parse(resourceText(name))

    private fun CandidateProfile.entriesOf(category: EntryCategory): List<ProfileEntry> =
        entries.filter { it.category == category }

    private fun CandidateProfile.everything(): String = toString()

    @Test
    fun parsesContactDetailsAndNameFromEngineeringResume() {
        val profile = parseResource("resume_1.txt")
        assertThat(profile.fullName).isEqualTo("RAHUL VERMA")
        assertThat(profile.email).isEqualTo("rahul.verma2002@gmail.com")
        assertThat(profile.phone).isEqualTo("+91 98765 43210")
        assertThat(profile.headline).isEmpty()
    }

    @Test
    fun parsesSkillsAcrossLabelledLines() {
        val profile = parseResource("resume_1.txt")
        assertThat(profile.skills).containsExactly(
            "Kotlin", "Java", "Python", "SQL", "Git", "Android Studio", "Postman", "Communication", "Teamwork",
        ).inOrder()
    }

    @Test
    fun parsesEducationEntriesWithOrganizationAndDates() {
        val education = parseResource("resume_1.txt").entriesOf(EntryCategory.EDUCATION)
        assertThat(education.map { it.title })
            .containsExactly("B.Tech in Computer Science and Engineering", "Class XII (PCM)").inOrder()
        val degree = education.first()
        assertThat(degree.organization).isEqualTo("Visvesvaraya Technological University, Belagavi")
        assertThat(degree.startDate).isEqualTo("2021")
        assertThat(degree.endDate).isEqualTo("2025")
        assertThat(degree.bullets.map { it.text }).containsExactly("CGPA: 8.4/10")
        assertThat(education.last().endDate).isEqualTo("2021")
    }

    @Test
    fun parsesInternshipWithWrappedBulletLines() {
        val internship = parseResource("resume_1.txt").entriesOf(EntryCategory.EXPERIENCE).single()
        assertThat(internship.title).isEqualTo("Android Developer Intern")
        assertThat(internship.organization).isEqualTo("Infosys Springboard, Remote")
        assertThat(internship.startDate).isEqualTo("Jun 2024")
        assertThat(internship.endDate).isEqualTo("Aug 2024")
        assertThat(internship.bullets.map { it.text }).containsExactly(
            "Built 3 screens with Jetpack Compose and connected them to REST APIs using Retrofit.",
            "Wrote unit tests with JUnit that raised coverage to 70 percent.",
            "Fixed 15 bugs reported by the QA team.",
        ).inOrder()
    }

    @Test
    fun parsesProjectsIncludingDescriptionAndTechnologyLines() {
        val projects = parseResource("resume_1.txt").entriesOf(EntryCategory.PROJECT)
        assertThat(projects.map { it.title })
            .containsExactly("Expense Tracker App | Kotlin, Room, Compose", "Student Result Portal").inOrder()
        assertThat(projects.first().bullets).hasSize(2)
        assertThat(projects.last().bullets.map { it.text })
            .containsExactly("Technologies: Java, MySQL", "Built a web portal for viewing results.").inOrder()
    }

    @Test
    fun parsesCertificationsAndPositionsOfResponsibility() {
        val profile = parseResource("resume_1.txt")
        assertThat(profile.entriesOf(EntryCategory.CERTIFICATION).map { it.title })
            .containsExactly("Google Android Basics in Kotlin", "NPTEL Python for Everybody").inOrder()
        val position = profile.entriesOf(EntryCategory.ACHIEVEMENT).single()
        assertThat(position.title).isEqualTo("Coordinator, College Coding Club")
        assertThat(position.bullets.single().text).isEqualTo("Organised 4 coding contests for 200 students.")
    }

    @Test
    fun dropsObjectivePersonalDetailsHobbiesAndDeclarationFromEngineeringResume() {
        val text = parseResource("resume_1.txt").everything()
        listOf(
            "14/08/2002", "Suresh", "Hindu", "Single", "Nationality", "Religion", "challenging position",
            "Cricket", "hereby", "Signature", "Bengaluru",
        ).forEach { assertThat(text).doesNotContain(it) }
    }

    @Test
    fun importedEntriesAreUnconfirmedWithStableIds() {
        val profile = parseResource("resume_1.txt")
        assertThat(profile.entries).isNotEmpty()
        profile.entries.forEach {
            assertThat(it.isConfirmed).isFalse()
            assertThat(it.source).isEqualTo(FactSource.IMPORTED)
            it.bullets.forEachIndexed { index, bullet -> assertThat(bullet.id).isEqualTo("${it.id}-b${index + 1}") }
        }
        assertThat(profile.entries.map { it.id }).containsNoDuplicates()
        assertThat(profile.entries.flatMap { e -> e.bullets.map { it.id } }).containsNoDuplicates()
        assertThat(parseResource("resume_1.txt")).isEqualTo(profile)
    }

    @Test
    fun parsesCommerceResumeWithTitleCaseHeadingsAndDashBullets() {
        val profile = parseResource("resume_2.txt")
        assertThat(profile.fullName).isEqualTo("Priya Nair")
        assertThat(profile.email).isEqualTo("priya.nair@outlook.com")
        assertThat(profile.phone).isEqualTo("9876501234")
        assertThat(profile.headline).isEqualTo("Commerce Graduate seeking accounts and audit roles")
    }

    @Test
    fun parsesCommerceEducationAndExperience() {
        val profile = parseResource("resume_2.txt")
        val degree = profile.entriesOf(EntryCategory.EDUCATION).first()
        assertThat(degree.title).isEqualTo("B.Com (Finance)")
        assertThat(degree.organization).isEqualTo("Mahatma Gandhi University, Kochi")
        assertThat(degree.startDate).isEqualTo("2022")
        assertThat(degree.endDate).isEqualTo("2025")
        assertThat(degree.bullets.single().text).isEqualTo("Percentage: 78%")
        val internship = profile.entriesOf(EntryCategory.EXPERIENCE).single()
        assertThat(internship.title).isEqualTo("Accounts Intern")
        assertThat(internship.organization).isEqualTo("Menon & Co, Chartered Accountants")
        assertThat(internship.startDate).isEqualTo("Jan 2025")
        assertThat(internship.endDate).isEqualTo("Apr 2025")
        assertThat(internship.bullets).hasSize(3)
        assertThat(internship.bullets.first().text).isEqualTo("Prepared GST returns for 12 small clients using Tally Prime")
    }

    @Test
    fun parsesCommerceSkillsSeparatedByCommasAndSemicolons() {
        assertThat(parseResource("resume_2.txt").skills).containsExactly(
            "Tally Prime",
            "GST",
            "MS Excel",
            "Bank Reconciliation",
            "Communication skills",
            "Time management",
        ).inOrder()
    }

    @Test
    fun parsesCertificationsWithAmpersandHeadingAndBulletOnlyAchievements() {
        val profile = parseResource("resume_2.txt")
        assertThat(profile.entriesOf(EntryCategory.CERTIFICATION).map { it.title })
            .containsExactly("Tally Prime Certification, Tally Education, 2024", "Advanced Excel course – Udemy")
        val achievement = profile.entriesOf(EntryCategory.ACHIEVEMENT).single()
        assertThat(achievement.bullets.single().text).isEqualTo("Won first prize in college commerce fest quiz, 2024")
    }

    @Test
    fun dropsInlineObjectiveDobMaritalStatusLanguagesAndDeclarationFromCommerceResume() {
        val text = parseResource("resume_2.txt").everything()
        listOf(
            "03-05-2003",
            "D.O.B",
            "Unmarried",
            "Marital",
            "Malayalam",
            "hereby",
            "entry level accounts position",
        ).forEach { assertThat(text).doesNotContain(it) }
    }

    @Test
    fun parsesContactFromPipeSeparatedNameLine() {
        val profile = parseResource("resume_3.txt")
        assertThat(profile.fullName).isEqualTo("ANANYA IYER")
        assertThat(profile.email).isEqualTo("ananya.iyer@gmail.com")
        assertThat(profile.phone).isEqualTo("+91-9123456780")
        assertThat(profile.headline).isEmpty()
    }

    @Test
    fun keepsBulletOnlyEducationAsEvidenceUnderDefaultTitle() {
        val education = parseResource("resume_3.txt").entriesOf(EntryCategory.EDUCATION).single()
        assertThat(education.title).isEqualTo("Education")
        assertThat(education.bullets.map { it.text }).containsExactly(
            "B.E. Computer Science, Anna University, Chennai (2021-2025), CGPA 8.9",
            "Class XII, Chennai Public School (2021), 94.2%",
        ).inOrder()
    }

    @Test
    fun parsesProjectsWithDatesInTitleLine() {
        val projects = parseResource("resume_3.txt").entriesOf(EntryCategory.PROJECT)
        assertThat(projects.map { it.title }).containsExactly("Sales Insights Dashboard", "Customer Churn Prediction").inOrder()
        assertThat(projects.first().startDate).isEqualTo("Oct 2024")
        assertThat(projects.first().endDate).isEqualTo("Dec 2024")
        assertThat(projects.first().bullets).hasSize(2)
        assertThat(projects.last().bullets.single().text)
            .isEqualTo("Trained a logistic regression model in scikit-learn with 82% accuracy.")
    }

    @Test
    fun parsesPipeSeparatedSkillsAndHyphenatedActivityHeading() {
        val profile = parseResource("resume_3.txt")
        assertThat(profile.skills).containsExactly("Python", "SQL", "Power BI", "Excel", "Tableau").inOrder()
        val activity = profile.entriesOf(EntryCategory.ACHIEVEMENT).single()
        assertThat(activity.bullets.single().text).isEqualTo("Volunteer, NSS unit - organised blood donation camp")
    }

    @Test
    fun dropsPipeSeparatedDobAndNationalitySegments() {
        val text = parseResource("resume_3.txt").everything()
        listOf("DOB", "21 Jan", "Nationality", "Indian", "hands-on experience").forEach {
            assertThat(text).doesNotContain(it)
        }
    }

    @Test
    fun emptyTextGivesEmptyProfile() {
        val profile = parser.parse("  \n\n ")
        assertThat(profile.fullName).isEmpty()
        assertThat(profile.entries).isEmpty()
        assertThat(profile.skills).isEmpty()
    }

    @Test
    fun documentTitleLineIsNotTheName() {
        val profile = parser.parse("RESUME\nMeera Shah\nSkills\nPython, SQL")
        assertThat(profile.fullName).isEqualTo("Meera Shah")
        assertThat(profile.skills).containsExactly("Python", "SQL").inOrder()
    }

    @Test
    fun duplicateSkillsAreRemovedCaseInsensitively() {
        val profile = parser.parse("Meera Shah\nSkills\nPython, python, SQL\nTechnical Skills: sql, Git")
        assertThat(profile.skills).containsExactly("Python", "SQL", "Git").inOrder()
    }

    @Test
    fun phoneIsNotMistakenForDateRange() {
        val profile = parser.parse("Meera Shah\nB.Tech 2021 - 2025\nEducation\nB.Tech in IT | 2021 - 2025")
        assertThat(profile.phone).isEmpty()
        assertThat(profile.entries.single().startDate).isEqualTo("2021")
    }

    @Test
    fun declarationWithoutHeadingStillStopsImport() {
        val profile = parser.parse("Meera Shah\nSkills\nPython\nI hereby declare that this is true.\nFather's Name: Ravi Shah")
        assertThat(profile.everything()).doesNotContain("hereby")
        assertThat(profile.everything()).doesNotContain("Ravi")
    }

    @Test
    fun sensitiveFieldsAreDroppedEverywhere() {
        val profile = parser.parse(
            "Meera Shah\nGender: Female\nReligion: Jain\nCaste: General\nBlood Group: O+\nSkills\nNationality: Indian\nPython",
        )
        assertThat(profile.skills).containsExactly("Python")
        assertThat(profile.everything()).doesNotContain("Jain")
        assertThat(profile.everything()).doesNotContain("Female")
        assertThat(profile.everything()).doesNotContain("General")
    }
}
