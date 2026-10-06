package com.hirehop.feature.onboarding.impl.consent

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.util.Locale

class ConsentCopyTest {

    private val consentStrings: String = readStrings("strings_consent.xml")

    private val signInStrings: String = readStrings("strings_sign_in.xml")

    @Test
    fun theNoticeKeepsTheNoSensitiveFieldsCommitment() {
        val commitment = stringValue(consentStrings, "feature_onboarding_impl_consent_commitment_never_asks")

        assertThat(commitment).contains("date of birth")
        assertThat(commitment).contains("photo")
        assertThat(commitment).contains("religion")
        assertThat(commitment).contains("caste")
        assertThat(commitment).contains("marital status")
    }

    @Test
    fun theNoticeDoesNotNameAPrivacyPolicyUrl() {
        val policyLine = stringValue(consentStrings, "feature_onboarding_impl_consent_policy_value")

        assertThat(policyLine).contains("Not published yet")
        assertThat(policyLine).doesNotContain("http")
    }

    @Test
    fun theNoticeKeepsBothWaysOut() {
        assertThat(consentStrings).contains("feature_onboarding_impl_consent_action_agree")
        assertThat(consentStrings).contains("feature_onboarding_impl_consent_action_not_now")
        assertThat(consentStrings).contains("feature_onboarding_impl_consent_declined_action_read_again")
    }

    @Test
    fun noRawDesignPlaceholderIsCommitted() {
        assertThat(consentStrings).doesNotContain("{{")
        assertThat(signInStrings).doesNotContain("{{")
    }

    @Test
    fun theSignInScreenKeepsTheUnderEighteenPath() {
        assertThat(signInStrings).contains("feature_onboarding_impl_sign_in_action_under_18")
    }

    @Test
    fun theMatchNoticeNamesOpenAiAndStatesNoRetentionPeriod() {
        val body = stringValue(consentStrings, "feature_onboarding_impl_consent_match_body")

        assertThat(body).contains("OpenAI")
        assertThat(body).doesNotContainMatch("\\d+\\s*(day|days|hour|hours|month|months|year|years)")
        assertThat(body.lowercase(Locale.ROOT)).doesNotContain("retain")
        assertThat(body.lowercase(Locale.ROOT)).doesNotContain("retention")
    }

    @Test
    fun theSignInScreenNeverAsksForASensitiveField() {
        val forbidden = listOf("date of birth", "photo", "religion", "caste", "marital status")

        forbidden.forEach { field ->
            assertThat(signInStrings.lowercase(Locale.ROOT)).doesNotContain(field)
        }
    }

    private fun stringValue(xml: String, name: String): String {
        val marker = "name=\"$name\">"
        val start = xml.indexOf(marker)
        assertThat(start).isAtLeast(0)
        val end = xml.indexOf("</string>", start)
        assertThat(end).isGreaterThan(start)
        return xml.substring(start + marker.length, end)
    }

    private fun readStrings(fileName: String): String {
        val candidates = listOf(
            File("src/main/res/values/$fileName"),
            File("feature/onboarding/impl/src/main/res/values/$fileName"),
        )
        val file = candidates.firstOrNull { it.exists() }
        assertThat(file).isNotNull()
        return requireNotNull(file).readText()
    }
}
