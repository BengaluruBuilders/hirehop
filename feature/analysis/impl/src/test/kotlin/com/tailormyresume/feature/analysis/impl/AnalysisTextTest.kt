package com.tailormyresume.feature.analysis.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AnalysisTextTest {

    @Test
    fun splitDetail_separatesTheParentheses() {
        assertThat("Cloud data warehouse (Snowflake or BigQuery)".splitDetail())
            .isEqualTo("Cloud data warehouse" to "Snowflake or BigQuery")
    }

    @Test
    fun splitDetail_keepsPlainText() {
        assertThat("SQL".splitDetail()).isEqualTo("SQL" to null)
    }

    @Test
    fun headline_dropsTheParentheses() {
        assertThat("Agile / JIRA (nice to have)".headline()).isEqualTo("Agile / JIRA")
    }

    @Test
    fun headline_dropsThePriorityMarkerAndClosingPunctuation() {
        assertThat("Nice to have Agile and JIRA.".headline()).isEqualTo("Agile and JIRA")
    }

    @Test
    fun withoutCompany_removesTheCompanyIgnoringCase() {
        assertThat("Product Analyst at KESTREL PAY".withoutCompany("Kestrel Pay")).isEqualTo("Product Analyst")
    }

    @Test
    fun withoutCompany_keepsTheRoleWhenTheCompanyIsBlank() {
        assertThat("Product Analyst".withoutCompany(" ")).isEqualTo("Product Analyst")
    }

    @Test
    fun withoutCompany_keepsTheTitleWhenTheCompanyIsInsideAWord() {
        assertThat("Metadata Engineer".withoutCompany("Meta")).isEqualTo("Metadata Engineer")
        assertThat("Data Analyst".withoutCompany("Data")).isEqualTo("Data Analyst")
    }

    @Test
    fun withoutCompany_removesTheCompanyAsASeparateSegment() {
        assertThat("Backend Engineer at Acme".withoutCompany("Acme")).isEqualTo("Backend Engineer")
        assertThat("Acme | SDE".withoutCompany("Acme")).isEqualTo("SDE")
        assertThat("Analyst (Acme)".withoutCompany("Acme")).isEqualTo("Analyst")
    }
}
