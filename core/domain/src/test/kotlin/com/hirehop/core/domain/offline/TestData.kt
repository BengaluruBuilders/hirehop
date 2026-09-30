package com.hirehop.core.domain.offline

import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.ProfileEntry

internal val jobDescriptionResources = listOf(
    "jd_android.txt",
    "jd_data_analyst.txt",
    "jd_finance.txt",
    "jd_marketing.txt",
    "jd_trap_cloud.txt",
)

internal fun resourceText(name: String): String {
    val stream = checkNotNull(TestResources::class.java.classLoader?.getResourceAsStream(name)) { "Missing $name" }
    return stream.bufferedReader().use { it.readText() }
}

private object TestResources

internal fun entry(
    id: String,
    category: EntryCategory,
    title: String,
    vararg bullets: String,
    confirmed: Boolean = true,
): ProfileEntry = ProfileEntry(
    id = id,
    category = category,
    title = title,
    organization = "",
    startDate = "",
    endDate = "",
    bullets = bullets.mapIndexed { index, text -> EvidenceBullet("$id-b${index + 1}", text) },
    source = FactSource.IMPORTED,
    isConfirmed = confirmed,
)

internal fun profileOf(
    skills: List<String> = emptyList(),
    vararg entries: ProfileEntry,
): CandidateProfile = CandidateProfile(
    fullName = "Test Candidate",
    email = "test@example.com",
    phone = "",
    headline = "",
    skills = skills,
    entries = entries.toList(),
)

internal val sampleProfile: CandidateProfile = profileOf(
    listOf("Kotlin", "Java", "Python", "SQL", "Git", "Communication skills", "Teamwork"),
    entry(
        "edu-1",
        EntryCategory.EDUCATION,
        "B.Tech in Computer Science",
        "CGPA 8.4 out of 10",
        "Coursework in data structures, DBMS and operating systems",
    ),
    entry(
        "exp-1",
        EntryCategory.EXPERIENCE,
        "Software Engineering Intern",
        "Responsible for building REST APIs using Kotlin and Spring Boot for the inventory module",
        "Wrote unit tests with JUnit to improve reliability",
        "Assisted senior developers with code reviews on Git",
        "Worked on the task of fixing bugs in the js frontend",
    ),
    entry(
        "proj-1",
        EntryCategory.PROJECT,
        "Expense Tracker Android App",
        "Built an Android app using Jetpack Compose and Room to track daily expenses",
        "Published the app to 50 beta testers",
        "Created charts in ms excel to analyse spending data",
    ),
    entry(
        "proj-2",
        EntryCategory.PROJECT,
        "Sales Dashboard",
        "Analysed 12 months of sales data using SQL and Power BI",
        "Presented findings to a class of 40 students",
    ),
    entry(
        "unconfirmed-1",
        EntryCategory.EXPERIENCE,
        "Kubernetes Lab",
        "Deployed containers with Kubernetes on AWS",
        confirmed = false,
    ),
)
