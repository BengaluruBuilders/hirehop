package com.tailormyresume.core.domain.offline

internal enum class JdSection { REQUIRED, PREFERRED, RESPONSIBILITIES, IGNORED, NONE }

internal object JdSectionHeaders {
    private val exactLabels: Map<String, JdSection> = buildMap {
        register(
            JdSection.REQUIRED,
            "requirements", "requirement", "required skills", "skills required", "required qualifications",
            "qualifications", "qualification", "must have", "must haves", "minimum qualifications",
            "basic qualifications", "eligibility", "eligibility criteria", "what you need",
            "what we are looking for", "what we're looking for", "who you are", "skills and qualifications",
            "key skills", "skills", "technical skills", "desired skills", "desired candidate profile",
            "candidate profile", "ideal candidate", "you have", "what you bring", "education", "experience",
            "skills and experience",
        )
        register(
            JdSection.PREFERRED,
            "preferred", "preferred qualifications", "preferred skills", "nice to have", "nice to haves",
            "good to have", "bonus", "bonus points", "plus", "pluses", "added advantage", "desirable",
            "optional",
        )
        register(
            JdSection.RESPONSIBILITIES,
            "responsibilities", "key responsibilities", "roles and responsibilities",
            "role and responsibilities", "what you will do", "what you'll do", "duties", "day to day",
            "the role", "about the role", "about the job", "job description", "job responsibilities",
            "your role", "role overview", "job summary", "what you will be doing", "your responsibilities",
        )
        register(
            JdSection.IGNORED,
            "about us", "about the company", "about company", "who we are", "company overview", "benefits",
            "perks", "perks and benefits", "what we offer", "why join us", "how to apply", "compensation",
            "salary", "location", "job type", "apply now", "equal opportunity", "our culture",
            "working hours", "contact", "note", "disclaimer",
        )
    }

    private val looseTriggers: List<Pair<String, JdSection>> = listOf(
        "preferred" to JdSection.PREFERRED,
        "nice to have" to JdSection.PREFERRED,
        "good to have" to JdSection.PREFERRED,
        "bonus" to JdSection.PREFERRED,
        "added advantage" to JdSection.PREFERRED,
        "desirable" to JdSection.PREFERRED,
        "about us" to JdSection.IGNORED,
        "about the company" to JdSection.IGNORED,
        "benefits" to JdSection.IGNORED,
        "perks" to JdSection.IGNORED,
        "what we offer" to JdSection.IGNORED,
        "how to apply" to JdSection.IGNORED,
        "responsibilities" to JdSection.RESPONSIBILITIES,
        "what you will do" to JdSection.RESPONSIBILITIES,
        "what you'll do" to JdSection.RESPONSIBILITIES,
        "what you will be doing" to JdSection.RESPONSIBILITIES,
        "requirements" to JdSection.REQUIRED,
        "requirement" to JdSection.REQUIRED,
        "qualifications" to JdSection.REQUIRED,
        "must have" to JdSection.REQUIRED,
        "required" to JdSection.REQUIRED,
        "eligibility" to JdSection.REQUIRED,
    )

    private fun MutableMap<String, JdSection>.register(section: JdSection, vararg labels: String) {
        labels.forEach { put(it, section) }
    }

    fun normalise(label: String): String = label.lowercase()
        .replace('’', '\'')
        .replace("&", " and ")
        .replace("-", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    fun classifyExact(label: String): JdSection? = exactLabels[normalise(label)]

    fun classifyLoose(label: String): JdSection? {
        val normalised = normalise(label)
        return exactLabels[normalised] ?: looseTriggers.firstOrNull { normalised.contains(it.first) }?.second
    }
}
