package com.hirehop.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.EvidenceBullet
import com.hirehop.core.model.GuardrailViolation
import org.junit.Test

class OfflineFabricationGuardTest {
    private val guard = OfflineFabricationGuard()
    private val profile = profileOf(listOf("Kotlin", "Python", "SQL"))

    private fun check(proposed: String, vararg sources: String) =
        guard.check(proposed, sources.mapIndexed { i, text -> EvidenceBullet("s$i", text) }, profile)

    @Test
    fun emptySourcesGiveMissingSourceOnly() {
        assertThat(guard.check("Built an app", emptyList(), profile))
            .containsExactly(GuardrailViolation.MissingSource)
    }

    @Test
    fun unchangedTextHasNoViolations() {
        assertThat(check("Built REST APIs using Kotlin", "Built REST APIs using Kotlin")).isEmpty()
    }

    @Test
    fun newPercentIsUnsupportedNumber() {
        val violations = check("Built REST APIs using Kotlin and cut latency by 40%", "Built REST APIs using Kotlin")
        assertThat(violations).contains(GuardrailViolation.UnsupportedNumber("40%"))
    }

    @Test
    fun newPlainNumberIsUnsupportedNumber() {
        val violations = check("Fixed 25 bugs", "Fixed bugs reported by QA")
        assertThat(violations).containsExactly(GuardrailViolation.UnsupportedNumber("25"))
    }

    @Test
    fun numbersPresentInSourceAreSupported() {
        assertThat(check("Fixed 15 bugs in 2024", "In 2024 I fixed 15 bugs")).isEmpty()
    }

    @Test
    fun commaFormattedAndDecimalNumbersMatchTheirSourceForm() {
        assertThat(check("Cleaned 50000 rows", "Cleaned 50,000 rows")).isEmpty()
        assertThat(check("Reached CGPA 8.4", "CGPA 8.4 out of 10")).isEmpty()
        assertThat(check("Reached CGPA 9.4", "CGPA 8.4 out of 10"))
            .containsExactly(GuardrailViolation.UnsupportedNumber("9.4"))
    }

    @Test
    fun percentWordAndSymbolAreTheSameNumber() {
        assertThat(check("Raised coverage to 70%", "Raised coverage to 70 percent")).isEmpty()
        assertThat(check("Raised coverage to 70", "Raised coverage to 70 percent"))
            .containsExactly(GuardrailViolation.UnsupportedNumber("70"))
    }

    @Test
    fun tokensWithDigitsInsideWordsAreNotNumbers() {
        assertThat(check("Stored files in S3 and EC2", "Stored files in S3 and EC2")).isEmpty()
        assertThat(check("Built the 2nd module", "Built the module")).isEmpty()
    }

    @Test
    fun newToolIsUnsupportedTerm() {
        val violations = check("Built REST APIs using Kotlin and Kubernetes", "Built REST APIs using Kotlin")
        assertThat(violations).containsExactly(GuardrailViolation.UnsupportedTerm("Kubernetes"))
    }

    @Test
    fun termSupportedOnlyByProfileSkillsIsAllowed() {
        assertThat(check("Built APIs in Python", "Built APIs")).isEmpty()
    }

    @Test
    fun aliasInSourceSupportsCanonicalTermInProposal() {
        assertThat(check("Fixed bugs in the JavaScript frontend", "Fixed bugs in the js frontend")).isEmpty()
        assertThat(check("Analysed data in Excel", "Analysed data in ms excel")).isEmpty()
    }

    @Test
    fun javaDoesNotSupportJavaScript() {
        val violations = check("Built a JavaScript app", "Built a Java app")
        assertThat(violations).contains(GuardrailViolation.UnsupportedTerm("JavaScript"))
    }

    @Test
    fun cPlusPlusDoesNotSupportC() {
        val violations = check("Wrote drivers in C", "Wrote drivers in C++")
        assertThat(violations).contains(GuardrailViolation.UnsupportedTerm("C"))
    }

    @Test
    fun assistedToLedIsVerbEscalation() {
        val violations = check("Led the migration to a new database", "Assisted with migration to a new database")
        assertThat(violations).containsExactly(GuardrailViolation.VerbEscalation(from = "assisted", to = "led"))
    }

    @Test
    fun helpedToOwnedIsVerbEscalation() {
        val violations = check("Owned the reporting process", "Helped with the reporting process")
        assertThat(violations).containsExactly(GuardrailViolation.VerbEscalation(from = "helped", to = "owned"))
    }

    @Test
    fun contributedToDevelopedIsVerbEscalation() {
        val violations = check("Built the payment screen", "Contributed to the payment screen")
        assertThat(violations).containsExactly(GuardrailViolation.VerbEscalation(from = "contributed", to = "built"))
    }

    @Test
    fun synonymsShareTheirRung() {
        assertThat(check("Created the report", "Built the report")).isEmpty()
        assertThat(check("Supported the team", "Helped the team")).isEmpty()
        assertThat(check("Managed the club", "Headed the club")).isEmpty()
    }

    @Test
    fun weakerVerbThanSourceIsAllowed() {
        assertThat(check("Assisted with the report", "Led the report")).isEmpty()
    }

    @Test
    fun verbAddedWhereSourceHadNoOwnershipVerbIsEscalation() {
        val violations = check("Led the coding club", "Coding club coordinator")
        assertThat(violations).containsExactly(GuardrailViolation.VerbEscalation(from = "none", to = "led"))
    }

    @Test
    fun leadingVerbEscalationIsCaughtEvenWhenSourceContainsAStrongerLaterVerb() {
        val violations = check("Built the dashboard", "Assisted a senior analyst in building the dashboard")
        assertThat(violations).containsExactly(GuardrailViolation.VerbEscalation(from = "assisted", to = "built"))
    }

    @Test
    fun teamOfWithoutSourceIsScaleClaimAndNumber() {
        val violations = check("Led a team of 10 to build the app", "Built the app")
        assertThat(violations).contains(GuardrailViolation.UnsupportedScaleClaim("team of 10"))
        assertThat(violations).contains(GuardrailViolation.UnsupportedNumber("10"))
        assertThat(violations).contains(GuardrailViolation.VerbEscalation(from = "built", to = "led"))
    }

    @Test
    fun scaleWordsNeedASource() {
        assertThat(check("Built an app for customers", "Built an app"))
            .containsExactly(GuardrailViolation.UnsupportedScaleClaim("customers"))
        assertThat(check("Improved revenue tracking", "Improved tracking"))
            .containsExactly(GuardrailViolation.UnsupportedScaleClaim("revenue"))
        assertThat(check("Saved Rs 5 lakh", "Saved Rs 5"))
            .contains(GuardrailViolation.UnsupportedScaleClaim("lakh"))
        assertThat(check("Served users", "Built an app"))
            .containsExactly(GuardrailViolation.UnsupportedScaleClaim("users"))
    }

    @Test
    fun scaleWordInSourceIsSupportedInEitherNumber() {
        assertThat(check("Support for the user", "Helped users log in")).isEmpty()
        assertThat(check("Presented to a team of 4", "Worked in a team of 4")).isEmpty()
    }

    @Test
    fun multipleViolationsAreAllReported() {
        val violations = check("Led a team of 12 using Docker and cut costs by 30%", "Assisted the team")
        assertThat(violations.filterIsInstance<GuardrailViolation.UnsupportedNumber>().map { it.value })
            .containsExactly("12", "30%")
        assertThat(violations).contains(GuardrailViolation.UnsupportedTerm("Docker"))
        assertThat(violations.filterIsInstance<GuardrailViolation.VerbEscalation>()).hasSize(1)
    }

    @Test
    fun cleanRewritesProduceNoViolations() {
        val source = "Responsible for building REST APIs using Kotlin and Spring Boot for the inventory module"
        listOf(
            "Building REST APIs using Kotlin and Spring Boot for the inventory module",
            "Using Kotlin and Spring Boot, responsible for building REST APIs for the inventory module",
            "Building REST APIs using Kotlin and Spring Boot",
        ).forEach { assertThat(check(it, source)).isEmpty() }
    }

    @Test
    fun rewordedAliasIsClean() {
        assertThat(check("Fixing bugs in the JavaScript frontend", "Worked on the task of fixing bugs in the js frontend"))
            .isEmpty()
    }
}
