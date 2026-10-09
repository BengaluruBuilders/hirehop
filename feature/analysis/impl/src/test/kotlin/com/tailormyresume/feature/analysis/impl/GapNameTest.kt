package com.tailormyresume.feature.analysis.impl

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import org.junit.Test

class GapNameTest {

    private fun requirement(text: String, vararg keywords: String) = JobRequirement(
        id = "req",
        text = text,
        type = RequirementType.SKILL,
        priority = RequirementPriority.MUST_HAVE,
        keywords = keywords.toList(),
    )

    @Test
    fun longSentence_isNamedBySkillsFromItsKeywords() {
        val name = requirement("You will build screens with Kotlin and Hilt in a modular codebase.", "kotlin", "hilt").gapName()

        assertThat(name).isEqualTo("Kotlin, Hilt")
    }

    @Test
    fun longSentence_namesAtMostThreeSkills() {
        val name = requirement(
            "You will ship features with Kotlin, Python, SQL and Android every single sprint.",
            "kotlin",
            "python",
            "sql",
            "android",
        ).gapName()

        assertThat(name).isEqualTo("Kotlin, Python, SQL")
    }

    @Test
    fun shortTextKeepsItsHeadline() {
        assertThat(requirement("DAX measures in Power BI", "dax", "power bi").gapName()).isEqualTo("DAX measures in Power BI")
    }

    @Test
    fun sentenceWithoutKeywordsKeepsItsHeadline() {
        val text = "Senior Android Developer at Northwind Mobile, Bengaluru"

        assertThat(requirement(text).gapName()).isEqualTo(text)
    }

    @Test
    fun detailInBrackets_doesNotCountTowardsTheLength() {
        assertThat(requirement("Cloud data warehouse (Snowflake or BigQuery)", "snowflake").gapName())
            .isEqualTo("Cloud data warehouse")
    }
}
