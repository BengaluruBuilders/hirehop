package com.hirehop.feature.profile.impl

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry

internal object DemoProfileProvider {

    fun profile(): CandidateProfile = CandidateProfile(
        fullName = "Aarav Mehta",
        email = "aarav.mehta@example.com",
        phone = "+91 98765 43210",
        headline = "Final-year B.Tech CSE student building Android and backend projects",
        skills = listOf(
            "Kotlin",
            "Java",
            "Android",
            "Jetpack Compose",
            "SQL",
            "Git",
            "REST APIs",
            "Python",
        ),
        entries = listOf(education(), internship(), campusEventsProject(), expenseSplitterProject()),
    )

    private fun education() = confirmedEntry(
        id = "demo-education",
        category = EntryCategory.EDUCATION,
        title = "B.Tech in Computer Science and Engineering",
        organization = "Sahyadri Institute of Technology, Pune",
        startDate = "2022",
        endDate = "2026",
        bullets = listOf(
            "CGPA 8.4 out of 10 after six semesters.",
            "Coursework: data structures, database systems, operating systems, computer networks.",
        ),
    )

    private fun internship() = confirmedEntry(
        id = "demo-internship",
        category = EntryCategory.EXPERIENCE,
        title = "Android Developer Intern",
        organization = "BrightLeaf Software, Pune",
        startDate = "Jun 2025",
        endDate = "Aug 2025",
        bullets = listOf(
            "Built the order tracking screen of a delivery app with Jetpack Compose.",
            "Fixed 12 crash reports in the checkout flow.",
            "Wrote unit tests for three ViewModels with JUnit.",
        ),
    )

    private fun campusEventsProject() = confirmedEntry(
        id = "demo-project-campus",
        category = EntryCategory.PROJECT,
        title = "Campus Events App",
        organization = "Personal project",
        startDate = "Jan 2025",
        endDate = "Apr 2025",
        bullets = listOf(
            "Built an Android app in Kotlin that lists campus events and works offline with Room.",
            "Added event reminders with WorkManager.",
            "Shared a beta with 40 classmates and fixed the issues they reported.",
        ),
    )

    private fun expenseSplitterProject() = confirmedEntry(
        id = "demo-project-expense",
        category = EntryCategory.PROJECT,
        title = "Expense Splitter API",
        organization = "Personal project",
        startDate = "Aug 2024",
        endDate = "Nov 2024",
        bullets = listOf(
            "Built a REST API in Python with Flask and PostgreSQL to split shared expenses.",
            "Wrote 25 unit tests with pytest.",
        ),
    )

    private fun confirmedEntry(
        id: String,
        category: EntryCategory,
        title: String,
        organization: String,
        startDate: String,
        endDate: String,
        bullets: List<String>,
    ): ProfileEntry = ProfileEntry(
        id = id,
        category = category,
        title = title,
        organization = organization,
        startDate = startDate,
        endDate = endDate,
        bullets = bullets.mapIndexed { index, text ->
            EvidenceBullet(id = "$id-bullet-${index + 1}", text = text)
        },
        source = FactSource.USER_STATED,
        isConfirmed = true,
    )
}
