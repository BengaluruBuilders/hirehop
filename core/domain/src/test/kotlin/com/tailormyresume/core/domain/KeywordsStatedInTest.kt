package com.tailormyresume.core.domain

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.JobRequirement
import com.tailormyresume.core.model.RequirementPriority
import com.tailormyresume.core.model.RequirementType
import org.junit.Test

class KeywordsStatedInTest {
    private fun requirement(vararg keywords: String) = JobRequirement(
        id = "req-1",
        text = "Experience with ${keywords.joinToString()}",
        type = RequirementType.TOOL,
        priority = RequirementPriority.MUST_HAVE,
        keywords = keywords.toList(),
    )

    @Test
    fun displayCasedSkillIsStated() {
        assertThat(keywordsStatedIn(requirement("Power BI"), "Built Power BI dashboards")).containsExactly("Power BI")
        assertThat(keywordsStatedIn(requirement("Google Sheets"), "Tracked budgets in Google Sheets"))
            .containsExactly("Google Sheets")
    }

    @Test
    fun aliasAndCaseVariantsAreStated() {
        listOf("power bi", "PowerBI", "Microsoft Power BI").forEach { keyword ->
            assertThat(keywordsStatedIn(requirement(keyword), "Built Power BI dashboards")).containsExactly("Power BI")
        }
        assertThat(keywordsStatedIn(requirement("ms excel"), "Modelled in Excel")).containsExactly("Excel")
    }

    @Test
    fun shortSkillIsNotStatedInsideSymbolJoinedToken() {
        assertThat(keywordsStatedIn(requirement("C"), "Wrote C++ code")).isEmpty()
        assertThat(keywordsStatedIn(requirement("R"), "Managed the R&D budget")).isEmpty()
        assertThat(keywordsStatedIn(requirement("Go"), "Owned the go-to-market plan")).isEmpty()
    }

    @Test
    fun nonLexiconKeywordStillMatchesByStem() {
        assertThat(keywordsStatedIn(requirement("stakeholders"), "Presented to a stakeholder")).containsExactly("stakeholders")
    }
}
