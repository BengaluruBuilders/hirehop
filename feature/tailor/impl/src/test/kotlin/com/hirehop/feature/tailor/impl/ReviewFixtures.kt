package com.hirehop.feature.tailor.impl

import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.BulletDecision
import com.hirehop.core.model.CandidateProfile
import com.hirehop.core.model.EditType
import com.hirehop.core.model.EntryCategory
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.FactSource
import com.hirehop.core.model.GapAnalysis
import com.hirehop.core.model.GuardrailViolation
import com.hirehop.core.model.JobApplication
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.KeywordCoverage
import com.hirehop.core.model.MatchStatus
import com.hirehop.core.model.ProfileEntry
import com.hirehop.core.model.RequirementMatch
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import com.hirehop.core.model.TailoredBullet
import com.hirehop.core.model.TailoredResume
import kotlin.time.Instant

internal object ReviewFixtures {

    const val WEEKLY = "t-weekly"
    const val ORDERS = "t-orders"
    const val INTERN = "t-intern"
    const val DASHBOARD = "t-dashboard"
    const val EDUCATION = "t-education"

    val profile = CandidateProfile(
        fullName = "Asha Rao",
        email = "asha@example.com",
        phone = "+91 98765 43210",
        headline = "Data analyst",
        skills = listOf("SQL", "Excel (pivots)", "Power BI"),
        entries = listOf(
            entry(
                id = "exp-saffron",
                category = EntryCategory.EXPERIENCE,
                title = "Data Operations Associate",
                organization = "Saffron Retail",
                start = "Pune · Jul 2025",
                end = "now",
                bullets = listOf(
                    EvidenceBullet("exp-saffron-b1", "Made sales reports every week."),
                    EvidenceBullet("exp-saffron-b2", "Did order data cleaning with SQL."),
                ),
            ),
            entry(
                id = "exp-kiran",
                category = EntryCategory.EXPERIENCE,
                title = "Data intern",
                organization = "Kiran Agro Exports",
                start = "Nashik · May",
                end = "Jul 2025",
                bullets = listOf(EvidenceBullet("exp-kiran-b1", "Cleaned sales data in Excel. Made weekly pivot reports.")),
            ),
            entry(
                id = "project-stats",
                category = EntryCategory.PROJECT,
                title = "Placement Stats Dashboard",
                organization = "Power BI",
                start = "",
                end = "",
                bullets = listOf(
                    EvidenceBullet("project-stats-b1", "Built a Power BI dashboard of placement data across 3 batches."),
                ),
                source = FactSource.USER_STATED,
            ),
            entry(
                id = "edu-btech",
                category = EntryCategory.EDUCATION,
                title = "B.Tech Computer Science",
                organization = "",
                start = "",
                end = "2024",
                bullets = listOf(
                    EvidenceBullet("edu-btech-b1", "B.Tech Computer Science, 2024"),
                    EvidenceBullet("edu-btech-b2", "Coursework: DBMS (SQL)"),
                ),
            ),
        ),
    )

    private fun entry(
        id: String,
        category: EntryCategory,
        title: String,
        organization: String,
        start: String,
        end: String,
        bullets: List<EvidenceBullet>,
        source: FactSource = FactSource.IMPORTED,
    ) = ProfileEntry(
        id = id,
        category = category,
        title = title,
        organization = organization,
        startDate = start,
        endDate = end,
        bullets = bullets,
        source = source,
        isConfirmed = true,
    )

    private fun bullet(
        id: String,
        entryId: String,
        original: String,
        proposed: String,
        sources: List<String>,
        editTypes: List<EditType>,
        keywords: List<String> = emptyList(),
        decision: BulletDecision = BulletDecision.PENDING,
        violations: List<GuardrailViolation> = emptyList(),
    ) = TailoredBullet(
        id = id,
        entryId = entryId,
        originalText = original,
        proposedText = proposed,
        sourceIds = sources,
        editTypes = editTypes,
        keywordsUsed = keywords,
        violations = violations,
        decision = decision,
    )

    fun bullets(
        decisions: Map<String, BulletDecision> = emptyMap(),
        weeklyViolations: List<GuardrailViolation> = emptyList(),
    ): List<TailoredBullet> = listOf(
        bullet(
            id = WEEKLY,
            entryId = "exp-saffron",
            original = "Made sales reports every week.",
            proposed = if (weeklyViolations.any { it is GuardrailViolation.VerbEscalation }) {
                "Made weekly sales reports in Excel for 40 stores."
            } else {
                "Built weekly sales reports in Excel for 40 stores."
            },
            sources = listOf("exp-saffron-b1"),
            editTypes = listOf(EditType.REWORD, EditType.EMPHASISE),
            keywords = listOf("Excel", "reports"),
            decision = decisions[WEEKLY] ?: BulletDecision.PENDING,
            violations = weeklyViolations,
        ),
        bullet(
            id = ORDERS,
            entryId = "exp-saffron",
            original = "Did order data cleaning with SQL.",
            proposed = "Cleaned order data with SQL for weekly reporting.",
            sources = listOf("exp-saffron-b2"),
            editTypes = listOf(EditType.REWORD),
            keywords = listOf("SQL"),
            decision = decisions[ORDERS] ?: BulletDecision.PENDING,
        ),
        bullet(
            id = INTERN,
            entryId = "exp-kiran",
            original = "Cleaned sales data in Excel. Made weekly pivot reports.",
            proposed = "Cleaned 12,000 rows of sales data in Excel and built weekly pivot reports.",
            sources = listOf("exp-kiran-b1", "edu-btech-b2"),
            editTypes = listOf(EditType.MERGE, EditType.REWORD),
            keywords = listOf("Excel"),
            decision = decisions[INTERN] ?: BulletDecision.PENDING,
        ),
        bullet(
            id = DASHBOARD,
            entryId = "project-stats",
            original = "Built a Power BI dashboard of placement data across 3 batches.",
            proposed = "Built a Power BI dashboard of placement data across 3 batches for the T&P cell.",
            sources = listOf("project-stats-b1"),
            editTypes = listOf(EditType.EMPHASISE),
            decision = decisions[DASHBOARD] ?: BulletDecision.PENDING,
        ),
        bullet(
            id = EDUCATION,
            entryId = "edu-btech",
            original = "B.Tech Computer Science, 2024",
            proposed = "B.Tech Computer Science, 2024 · Coursework: DBMS (SQL)",
            sources = listOf("edu-btech-b1", "edu-btech-b2"),
            editTypes = listOf(EditType.MERGE),
            decision = decisions[EDUCATION] ?: BulletDecision.PENDING,
        ),
    )

    val gap = GapAnalysis(
        matches = listOf("Cloud data warehouse", "Agile / JIRA").mapIndexed { index, text ->
            RequirementMatch(
                requirement = JobRequirement(
                    id = "req-$index",
                    text = text,
                    type = RequirementType.TOOL,
                    priority = RequirementPriority.NICE_TO_HAVE,
                    keywords = emptyList(),
                ),
                status = MatchStatus.GAP,
                evidenceIds = emptyList(),
            )
        },
        keywordCoverage = KeywordCoverage(covered = 9, total = 14),
    )

    fun application(
        decisions: Map<String, BulletDecision> = emptyMap(),
        weeklyViolations: List<GuardrailViolation> = emptyList(),
        extra: List<TailoredBullet> = emptyList(),
    ): JobApplication = JobApplication(
        id = "app-1",
        job = JobDescription("Associate Analyst", "Northwind GCC", "", emptyList()),
        status = ApplicationStatus.SAVED,
        notes = "",
        gapAnalysis = gap,
        tailoredResume = TailoredResume(bullets(decisions, weeklyViolations) + extra),
        createdAt = Instant.fromEpochSeconds(0),
        updatedAt = Instant.fromEpochSeconds(0),
    )

    fun state(
        decisions: Map<String, BulletDecision> = emptyMap(),
        weeklyViolations: List<GuardrailViolation> = emptyList(),
        edited: Set<String> = emptySet(),
        regenerationsUsed: Int = 0,
        isOffline: Boolean = false,
        extra: List<TailoredBullet> = emptyList(),
        reportedIds: Set<String> = emptySet(),
    ): TailorUiState.Success = buildTailorUiState(
        TailorInputs(
            application = application(decisions, weeklyViolations, extra),
            profile = profile,
            isOffline = isOffline,
            regenerationsUsed = regenerationsUsed,
            editedBulletIds = edited,
            reportedIds = reportedIds,
        ),
    ) as TailorUiState.Success

    fun partlyReviewed(): TailorUiState.Success = state(
        decisions = mapOf(
            ORDERS to BulletDecision.ACCEPTED,
            INTERN to BulletDecision.ACCEPTED,
            DASHBOARD to BulletDecision.REJECTED,
        ),
        weeklyViolations = listOf(GuardrailViolation.VerbEscalation("made", "built")),
        edited = setOf(INTERN),
    )

    fun allReviewed(): TailorUiState.Success = state(
        decisions = mapOf(
            WEEKLY to BulletDecision.ACCEPTED,
            ORDERS to BulletDecision.ACCEPTED,
            INTERN to BulletDecision.ACCEPTED,
            DASHBOARD to BulletDecision.REJECTED,
            EDUCATION to BulletDecision.ACCEPTED,
        ),
        edited = setOf(INTERN),
    )

    fun change(state: TailorUiState.Success, bulletId: String): TailorBulletUi =
        state.changes.first { it.bullet.id == bulletId }
}
