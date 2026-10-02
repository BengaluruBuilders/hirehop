package com.hirehop.feature.analysis.impl

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
}
