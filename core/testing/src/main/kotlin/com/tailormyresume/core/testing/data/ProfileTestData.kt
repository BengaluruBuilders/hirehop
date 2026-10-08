package com.tailormyresume.core.testing.data

import com.tailormyresume.core.model.CandidateProfile
import com.tailormyresume.core.model.EntryCategory
import com.tailormyresume.core.model.EvidenceBullet
import com.tailormyresume.core.model.FactSource
import com.tailormyresume.core.model.ProfileEntry

val sampleEducationEntry = ProfileEntry(
    id = "entry-education",
    category = EntryCategory.EDUCATION,
    title = "B.Tech in Computer Science",
    organization = "Example Institute of Technology",
    startDate = "2022",
    endDate = "2026",
    bullets = listOf(
        EvidenceBullet(
            id = "bullet-education-1",
            text = "Completed coursework in data structures, databases, and operating systems.",
        ),
        EvidenceBullet(
            id = "bullet-education-2",
            text = "Ranked in the top 10 percent of the batch by CGPA.",
        ),
    ),
    source = FactSource.IMPORTED,
    isConfirmed = true,
)

val sampleProjectEntry = ProfileEntry(
    id = "entry-project",
    category = EntryCategory.PROJECT,
    title = "Campus Events App",
    organization = "Personal project",
    startDate = "2025",
    endDate = "2025",
    bullets = listOf(
        EvidenceBullet(
            id = "bullet-project-1",
            text = "Built an Android app in Kotlin that lists campus events offline with Room.",
        ),
        EvidenceBullet(
            id = "bullet-project-2",
            text = "Added event reminders with WorkManager and shared a beta with 40 classmates.",
        ),
    ),
    source = FactSource.USER_STATED,
    isConfirmed = false,
)

val sampleProfile = CandidateProfile(
    fullName = "Asha Rao",
    email = "asha.rao@example.com",
    phone = "+91 90000 00000",
    headline = "Final-year computer science student focused on Android development",
    skills = listOf("Kotlin", "Android", "SQL", "Git"),
    entries = listOf(sampleEducationEntry, sampleProjectEntry),
)
