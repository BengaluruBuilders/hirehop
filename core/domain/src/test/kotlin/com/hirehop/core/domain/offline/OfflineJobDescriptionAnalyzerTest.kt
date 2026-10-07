package com.hirehop.core.domain.offline

import com.google.common.truth.Truth.assertThat
import com.hirehop.core.model.JobDescription
import com.hirehop.core.model.JobRequirement
import com.hirehop.core.model.RequirementPriority
import com.hirehop.core.model.RequirementType
import kotlinx.coroutines.test.runTest
import org.junit.Test

class OfflineJobDescriptionAnalyzerTest {
    private val analyzer = OfflineJobDescriptionAnalyzer()

    private suspend fun analyze(resource: String): JobDescription = analyzer.analyze(resourceText(resource))

    private fun JobDescription.requirementWith(keyword: String): JobRequirement =
        requirements.first { keyword in it.keywords }

    private fun JobDescription.requirementText(text: String): JobRequirement =
        requirements.first { it.text == text }

    @Test
    fun extractsTitleAndCompanyFromAtPattern() = runTest {
        val job = analyze("jd_android.txt")
        assertThat(job.title).isEqualTo("Junior Android Developer")
        assertThat(job.company).isEqualTo("Zenith Apps")
    }

    @Test
    fun extractsTitleAndCompanyFromSeparatorPattern() = runTest {
        val job = analyze("jd_data_analyst.txt")
        assertThat(job.title).isEqualTo("Data Analyst Trainee")
        assertThat(job.company).isEqualTo("Orbit Analytics")
    }

    @Test
    fun extractsTitleAndCompanyFromLabels() = runTest {
        val job = analyze("jd_finance.txt")
        assertThat(job.title).isEqualTo("Accounts Executive (Fresher)")
        assertThat(job.company).isEqualTo("Sharma & Associates")
    }

    @Test
    fun extractsTitleAndCompanyFromAtSign() = runTest {
        val job = analyze("jd_marketing.txt")
        assertThat(job.title).isEqualTo("Digital Marketing Intern")
        assertThat(job.company).isEqualTo("BrightLeaf Media")
    }

    @Test
    fun extractsCompanyFromHiringSentence() = runTest {
        val job = analyzer.analyze("Acme Robotics is looking for a Junior Python Developer to join our team.\nRequirements\n- Python")
        assertThat(job.company).isEqualTo("Acme Robotics")
        assertThat(job.title).isEqualTo("Junior Python Developer")
    }

    @Test
    fun fallsBackToEmptyStringsWhenNothingLooksLikeATitle() = runTest {
        val job = analyzer.analyze("We are a fast growing company and we love building great products for everyone every day.\nRequirements\n- SQL")
        assertThat(job.title).isEmpty()
        assertThat(job.company).isEmpty()
        assertThat(job.requirements.map { it.keywords }).containsExactly(listOf("sql"))
    }

    @Test
    fun emptyTextGivesNoRequirements() = runTest {
        val job = analyzer.analyze("   \n\n")
        assertThat(job.requirements).isEmpty()
        assertThat(job.title).isEmpty()
    }

    @Test
    fun requirementIdsAreStableAndSequential() = runTest {
        val job = analyze("jd_android.txt")
        assertThat(job.requirements.map { it.id })
            .containsExactlyElementsIn((1..job.requirements.size).map { "req-$it" }).inOrder()
        assertThat(analyze("jd_android.txt")).isEqualTo(job)
    }

    @Test
    fun aboutUsSectionIsNotAnalysed() = runTest {
        val job = analyze("jd_android.txt")
        assertThat(job.requirements.none { it.text.contains("fintech") }).isTrue()
    }

    @Test
    fun requirementsSectionIsMustHaveAndPreferredSectionIsNiceToHave() = runTest {
        val job = analyze("jd_android.txt")
        assertThat(job.requirementText("Strong knowledge of Kotlin and Android SDK").priority)
            .isEqualTo(RequirementPriority.MUST_HAVE)
        assertThat(job.requirementWith("rest api").priority).isEqualTo(RequirementPriority.MUST_HAVE)
        assertThat(job.requirementWith("firebase").priority).isEqualTo(RequirementPriority.NICE_TO_HAVE)
    }

    @Test
    fun plusCueMakesRequirementNiceToHaveEvenInsideRequirementsSection() = runTest {
        val job = analyzer.analyze("Requirements\n- Python\n- Knowledge of Docker is a plus")
        assertThat(job.requirementWith("python").priority).isEqualTo(RequirementPriority.MUST_HAVE)
        assertThat(job.requirementWith("docker").priority).isEqualTo(RequirementPriority.NICE_TO_HAVE)
    }

    @Test
    fun mustCueOverridesResponsibilitiesSection() = runTest {
        val job = analyzer.analyze("Responsibilities\n- Must know Git\n- Use Jira daily")
        assertThat(job.requirementWith("git").priority).isEqualTo(RequirementPriority.MUST_HAVE)
        assertThat(job.requirementWith("jira").priority).isEqualTo(RequirementPriority.NICE_TO_HAVE)
    }

    @Test
    fun textWithoutSectionsDefaultsToMustHave() = runTest {
        val job = analyzer.analyze("Looking for a candidate with Python and SQL skills.")
        assertThat(job.requirements.single().priority).isEqualTo(RequirementPriority.MUST_HAVE)
        assertThat(job.requirements.single().keywords).containsExactly("python", "sql").inOrder()
    }

    @Test
    fun goodToHaveHeaderWithColonMakesNiceToHave() = runTest {
        val job = analyze("jd_data_analyst.txt")
        assertThat(job.requirementWith("pandas").priority).isEqualTo(RequirementPriority.NICE_TO_HAVE)
        assertThat(job.requirementWith("machine learning").priority).isEqualTo(RequirementPriority.NICE_TO_HAVE)
        assertThat(job.requirementWith("sql").priority).isEqualTo(RequirementPriority.MUST_HAVE)
    }

    @Test
    fun classifiesRequirementTypesWithTheLexicon() = runTest {
        val job = analyze("jd_android.txt")
        assertThat(job.requirementWith("b.tech").type).isEqualTo(RequirementType.EDUCATION)
        assertThat(job.requirementWith("communication").type).isEqualTo(RequirementType.SOFT_SKILL)
        assertThat(job.requirementWith("firebase").type).isEqualTo(RequirementType.TOOL)
        assertThat(job.requirementWith("rest api").type).isEqualTo(RequirementType.SKILL)
    }

    @Test
    fun keywordsAreNormalisedAndInOrderOfAppearance() = runTest {
        val job = analyze("jd_android.txt")
        assertThat(job.requirementWith("b.tech").keywords)
            .containsExactly("b.tech", "b.e.", "computer science").inOrder()
        assertThat(job.requirementText("Strong knowledge of Kotlin and Android SDK").keywords)
            .containsExactly("kotlin", "android").inOrder()
    }

    @Test
    fun inlineEligibilityHeaderYieldsEducationRequirement() = runTest {
        val job = analyze("jd_data_analyst.txt")
        val education = job.requirementWith("bca")
        assertThat(education.type).isEqualTo(RequirementType.EDUCATION)
        assertThat(education.keywords).containsExactly("b.sc", "b.tech", "bca").inOrder()
    }

    @Test
    fun splitsMultipleSentencesOnOneLine() = runTest {
        val job = analyze("jd_data_analyst.txt")
        assertThat(job.requirementWith("problem solving").text).isEqualTo("Strong problem-solving skills.")
    }

    @Test
    fun linesWithoutKeywordsButWithExperienceCuesBecomeRequirements() = runTest {
        val job = analyzer.analyze("Requirements\n- Prior experience handling customer complaints\n- Loves memes")
        val requirement = job.requirements.single()
        assertThat(requirement.text).isEqualTo("Prior experience handling customer complaints")
        assertThat(requirement.type).isEqualTo(RequirementType.EXPERIENCE)
        assertThat(requirement.keywords).containsExactly("prior", "handling", "customer").inOrder()
    }

    @Test
    fun linesWithoutKeywordsButWithEducationCuesBecomeEducationRequirements() = runTest {
        val job = analyzer.analyze("Requirements\n- Final year students pursuing any degree")
        assertThat(job.requirements.single().type).isEqualTo(RequirementType.EDUCATION)
    }

    @Test
    fun yearsOfExperienceLineMakesExperienceType() = runTest {
        val job = analyzer.analyze("Requirements\n- 1-2 years of experience in Python")
        assertThat(job.requirements.single().type).isEqualTo(RequirementType.EXPERIENCE)
        assertThat(job.requirements.single().keywords).containsExactly("python")
    }

    @Test
    fun freshersWelcomeLineIsNotARequirement() = runTest {
        val job = analyze("jd_marketing.txt")
        assertThat(job.requirements.none { it.text.contains("Freshers", ignoreCase = true) }).isTrue()
    }

    @Test
    fun bulletMarkersAreRemovedFromRequirementText() = runTest {
        val job = analyze("jd_trap_cloud.txt")
        assertThat(job.requirements.map { it.text }).contains("Proficiency in Kotlin")
    }

    @Test
    fun trapJobExtractsAllThreeMissingSkillFamilies() = runTest {
        val job = analyze("jd_trap_cloud.txt")
        val keywords = job.requirements.flatMap { it.keywords }
        assertThat(keywords).containsAtLeast("mba", "kubernetes", "docker", "aws", "kotlin", "terraform")
        assertThat(job.requirementWith("terraform").priority).isEqualTo(RequirementPriority.NICE_TO_HAVE)
        assertThat(job.requirementWith("aws").priority).isEqualTo(RequirementPriority.MUST_HAVE)
    }

    @Test
    fun rawTextIsPreserved() = runTest {
        val raw = resourceText("jd_finance.txt")
        assertThat(analyzer.analyze(raw).rawText).isEqualTo(raw)
    }
}
