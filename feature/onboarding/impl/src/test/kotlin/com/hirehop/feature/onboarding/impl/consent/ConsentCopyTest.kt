package com.hirehop.feature.onboarding.impl.consent

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File
import java.util.Locale

class ConsentCopyTest {

    private val consentStrings: String = readStrings("strings_consent.xml")

    private val signInStrings: String = readStrings("strings_sign_in.xml")

    @Test
    fun theLedgerClaimsNoOutsiderHoldsTheText() {
        val analyseLine = stringValue(consentStrings, "feature_onboarding_impl_consent_purpose_analyse_supporting")

        assertThat(analyseLine).contains("goes to no outside service")
        assertThat(analyseLine).contains("no provider holds a copy")
    }

    @Test
    fun theLedgerNeverInventsAZeroRetentionGuarantee() {
        val analyseLine = stringValue(
            consentStrings,
            "feature_onboarding_impl_consent_purpose_analyse_supporting",
        ).lowercase(Locale.ROOT)

        assertThat(analyseLine).doesNotContain("never share")
        assertThat(analyseLine).doesNotContain("we never")
        assertThat(analyseLine).doesNotContain("openai")
    }

    @Test
    fun theLedgerDoesNotNameAPrivacyPolicyUrl() {
        val policyLine = stringValue(consentStrings, "feature_onboarding_impl_consent_policy_value")

        assertThat(policyLine).contains("Not published yet")
        assertThat(policyLine).doesNotContain("http")
    }

    @Test
    fun theLedgerKeepsTheNoSensitiveFieldsCommitment() {
        val commitment = stringValue(consentStrings, "feature_onboarding_impl_consent_commitment_never_asks")

        assertThat(commitment).contains("date of birth")
        assertThat(commitment).contains("photo")
        assertThat(commitment).contains("religion")
        assertThat(commitment).contains("caste")
        assertThat(commitment).contains("marital status")
    }

    @Test
    fun theLedgerKeepsBothWaysOut() {
        assertThat(consentStrings).contains("feature_onboarding_impl_consent_action_agree")
        assertThat(consentStrings).contains("feature_onboarding_impl_consent_action_not_now")
        assertThat(consentStrings).contains("feature_onboarding_impl_consent_declined_action_back")
    }

    @Test
    fun noRawDesignPlaceholderIsCommitted() {
        assertThat(consentStrings).doesNotContain("{{")
        assertThat(signInStrings).doesNotContain("{{")
    }

    @Test
    fun theSignInDisclosureSaysNothingLeavesThePhone() {
        val body = stringValue(signInStrings, "feature_onboarding_impl_sign_in_what_leaves_body")

        assertThat(body).contains("Nothing")
        assertThat(body).contains("stay on this phone")
    }

    @Test
    fun theSignInButtonDoesNotPromiseAGoogleRoundTrip() {
        assertThat(signInStrings).contains("feature_onboarding_impl_sign_in_action_continue")
        assertThat(signInStrings).doesNotContain("Continue with Google")
    }

    @Test
    fun theSignInScreenKeepsTheSkipPathAndTheUnderEighteenPath() {
        assertThat(signInStrings).contains("feature_onboarding_impl_sign_in_action_not_now")
        assertThat(signInStrings).contains("feature_onboarding_impl_sign_in_action_under_18")
    }

    @Test
    fun theSignInScreenNeverAsksForASensitiveField() {
        val forbidden = listOf("date of birth", "photo", "religion", "caste", "marital status")

        forbidden.forEach { field ->
            assertThat(signInStrings.lowercase(Locale.ROOT)).doesNotContain(field)
        }
    }

    @Test
    fun theSignInScreenNamesTheLocalAccountButNoEmail() {
        val successStatus = stringValue(signInStrings, "feature_onboarding_impl_sign_in_success_status")

        assertThat(successStatus).contains("on this phone")
        assertThat(signInStrings).doesNotContain("priya.d@example.com")
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
