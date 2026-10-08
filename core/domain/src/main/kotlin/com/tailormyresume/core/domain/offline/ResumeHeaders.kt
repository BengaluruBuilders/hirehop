package com.tailormyresume.core.domain.offline

internal enum class ResumeSection { PREAMBLE, EDUCATION, EXPERIENCE, PROJECT, SKILLS, CERTIFICATION, ACHIEVEMENT, IGNORED }

internal data class ResumeHeader(val section: ResumeSection, val remainder: String, val switchesSection: Boolean)

internal object ResumeHeaders {
    private const val MAX_LABEL_LENGTH = 40
    private val bulletLead = Regex("^\\s*(?:[•·●▪◦►➢✓✔*>]|[\\-–—]\\s|\\d{1,2}[.)]\\s)")
    private val inlineLabel = Regex("^([A-Za-z][A-Za-z &/\\-]{2,$MAX_LABEL_LENGTH}?)\\s*:\\s*(\\S.*)$")
    private val decoration = Regex("^[^A-Za-z]+|[^A-Za-z]+$")
    private val whitespace = Regex("\\s+")
    private val lineOnlyLabels = setOf(
        "languages",
        "languages known",
        "language proficiency",
        "hobbies",
        "interests",
        "hobbies and interests",
        "strengths",
        "strength",
    )

    private val table: Map<String, ResumeSection> = buildMap {
        register(
            ResumeSection.EDUCATION,
            "education", "educational qualification", "educational qualifications", "academic details",
            "academic qualification", "academic qualifications", "academics", "academic background",
            "education details", "educational details", "educational background", "qualifications",
        )
        register(
            ResumeSection.EXPERIENCE,
            "experience", "work experience", "professional experience", "internships", "internship",
            "internship experience", "internships and training", "employment history", "work history",
            "industrial training", "experience details",
        )
        register(
            ResumeSection.PROJECT,
            "projects", "academic projects", "personal projects", "project work", "key projects",
            "project details", "projects undertaken", "major projects", "mini projects",
        )
        register(
            ResumeSection.SKILLS,
            "skills", "technical skills", "key skills", "core competencies", "it skills",
            "tools and technologies", "technical proficiency", "soft skills", "skills and tools",
            "technical skills and tools", "skill set", "technical expertise", "areas of expertise",
        )
        register(
            ResumeSection.CERTIFICATION,
            "certifications", "certification", "certificates", "courses", "training",
            "certifications and courses", "licenses and certifications", "courses and certifications",
            "professional certifications", "training and certifications",
        )
        register(
            ResumeSection.ACHIEVEMENT,
            "achievements", "awards", "accomplishments", "positions of responsibility",
            "position of responsibility", "extra curricular activities", "extracurricular activities",
            "co curricular activities", "leadership", "honors and awards", "awards and achievements",
            "achievements and awards", "academic achievements", "leadership and activities", "volunteering",
        )
        register(
            ResumeSection.IGNORED,
            "objective", "career objective", "professional objective", "career summary", "summary",
            "professional summary", "profile", "profile summary", "about me", "declaration", "personal details",
            "personal information", "personal profile", "personal", "hobbies", "interests", "hobbies and interests",
            "languages", "languages known", "language proficiency", "references", "reference", "strengths",
            "strength", "address", "contact", "contact details",
        )
    }

    fun detect(line: String, current: ResumeSection): ResumeHeader? {
        if (bulletLead.containsMatchIn(line)) return null
        val standalone = lookup(line.replace(decoration, ""), current)
        if (standalone != null && line.length <= MAX_LABEL_LENGTH + 2) {
            return ResumeHeader(standalone, "", switchesSection = true)
        }
        val inline = inlineLabel.matchEntire(line) ?: return null
        val label = inline.groupValues[1]
        val section = lookup(label, current) ?: return null
        val lineOnly = keyOf(label) in lineOnlyLabels
        return ResumeHeader(section, inline.groupValues[2], switchesSection = !lineOnly)
    }

    private fun lookup(label: String, current: ResumeSection): ResumeSection? {
        val key = keyOf(label)
        if (key == "languages" && current == ResumeSection.SKILLS) return null
        return table[key]
    }

    private fun keyOf(label: String): String = label.lowercase()
        .replace('’', '\'')
        .replace("&", " and ")
        .replace("-", " ")
        .replace(whitespace, " ")
        .trim()

    private fun MutableMap<String, ResumeSection>.register(section: ResumeSection, vararg labels: String) {
        labels.forEach { put(it, section) }
    }
}
