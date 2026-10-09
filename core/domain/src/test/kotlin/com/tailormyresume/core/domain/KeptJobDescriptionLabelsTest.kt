package com.tailormyresume.core.domain

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.core.model.KeptJobDescription
import org.junit.Test

class KeptJobDescriptionLabelsTest {

    private val typed = KeptJobDescription(text = "jd", company = "Typed Co", role = "Typed Role")
    private val prefilled = typed.copy(companyIsPrefill = true, roleIsPrefill = true)

    @Test
    fun typedValue_winsOverTheBackendValue() {
        assertThat(typed.resolvedTitle("Backend Role")).isEqualTo("Typed Role")
        assertThat(typed.resolvedCompany("Backend Co")).isEqualTo("Typed Co")
    }

    @Test
    fun untouchedPrefill_yieldsToTheBackendValue() {
        assertThat(prefilled.resolvedTitle("Backend Role")).isEqualTo("Backend Role")
        assertThat(prefilled.resolvedCompany("Backend Co")).isEqualTo("Backend Co")
    }

    @Test
    fun untouchedPrefill_staysWhenTheBackendValueIsBlank() {
        assertThat(prefilled.resolvedTitle("")).isEqualTo("Typed Role")
        assertThat(prefilled.resolvedCompany("")).isEqualTo("Typed Co")
    }

    @Test
    fun blankTypedValue_fallsBackToTheBackendValue() {
        assertThat(typed.copy(role = "").resolvedTitle("Backend Role")).isEqualTo("Backend Role")
    }
}
