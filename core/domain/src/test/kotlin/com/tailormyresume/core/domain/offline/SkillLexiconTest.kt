package com.tailormyresume.core.domain.offline

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SkillLexiconTest {
    @Test
    fun javaDoesNotMatchInsideJavaScript() {
        assertThat(SkillLexicon.canonicalsIn("Experience with Java")).containsExactly("java")
        assertThat(SkillLexicon.canonicalsIn("JavaScript developer")).containsExactly("javascript")
        assertThat(SkillLexicon.canonicalsIn("Java and JavaScript")).containsExactly("java", "javascript").inOrder()
    }

    @Test
    fun cCppAndCSharpAreDistinct() {
        assertThat(SkillLexicon.canonicalsIn("Proficient in C, C++ and C#"))
            .containsExactly("c", "c++", "c#").inOrder()
        assertThat(SkillLexicon.canonicalsIn("Worked on C++ only")).containsExactly("c++")
        assertThat(SkillLexicon.canonicalsIn("Wrote C# services")).containsExactly("c#")
    }

    @Test
    fun singleLetterLanguageDoesNotMatchOrdinaryText() {
        assertThat(SkillLexicon.canonicalsIn("I ate a c biscuit and read a book about cricket")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Worked in R&D and Q&A teams")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("C-level stakeholders")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Skills: R, Python")).containsExactly("r", "python").inOrder()
        assertThat(SkillLexicon.canonicalsIn("Statistics in R.")).containsExactly("statistics", "r").inOrder()
    }

    @Test
    fun matchingIsCaseInsensitive() {
        assertThat(SkillLexicon.canonicalsIn("PYTHON and sql and Power bi"))
            .containsExactly("python", "sql", "power bi").inOrder()
    }

    @Test
    fun aliasesNormaliseToCanonicalTerms() {
        assertThat(SkillLexicon.canonicalsIn("Built the js frontend")).containsExactly("javascript")
        assertThat(SkillLexicon.canonicalsIn("Deployed on K8s")).containsExactly("kubernetes")
        assertThat(SkillLexicon.canonicalsIn("Reports in MS Excel")).containsExactly("excel")
        assertThat(SkillLexicon.canonicalsIn("Basic ML models")).containsExactly("machine learning")
        assertThat(SkillLexicon.canonicalsIn("Amazon Web Services")).containsExactly("aws")
    }

    @Test
    fun longestOverlappingTermWins() {
        assertThat(SkillLexicon.canonicalsIn("Node.js backend")).containsExactly("node.js")
        assertThat(SkillLexicon.canonicalsIn("Stored procedures in PL/SQL")).containsExactly("pl/sql")
        assertThat(SkillLexicon.canonicalsIn("React Native app")).containsExactly("react native")
        assertThat(SkillLexicon.canonicalsIn("Spring Boot service")).containsExactly("spring boot")
        assertThat(SkillLexicon.canonicalsIn("Passed CA Inter")).containsExactly("ca inter")
    }

    @Test
    fun excelAsVerbIsNotATool() {
        assertThat(SkillLexicon.canonicalsIn("Ability to excel under pressure")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Advanced Excel and VLOOKUP")).containsExactly("excel", "vlookup")
    }

    @Test
    fun excelAsVerbNeverMatches() {
        listOf(
            "Students who excel in academics",
            "Candidates must excel at teamwork",
            "must excel at working in teams",
            "Excel in academics",
            "excel",
        ).forEach { assertThat(SkillLexicon.canonicalsIn(it)).doesNotContain("excel") }
        assertThat(SkillLexicon.canonicalsIn("Proficient in Excel")).containsExactly("excel")
        assertThat(SkillLexicon.canonicalsIn("Reports in MS Excel")).containsExactly("excel")
        assertThat(SkillLexicon.canonicalsIn("Advanced Excel")).containsExactly("excel")
        assertThat(SkillLexicon.canonicalsIn("excellent communicator")).doesNotContain("excel")
    }

    @Test
    fun ambiguousEnglishWordsMatchOnlyInAToolContext() {
        assertThat(SkillLexicon.canonicalsIn("Swift resolution of customer tickets")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("swift and reliable delivery")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Built iOS apps in Swift")).containsExactly("ios", "swift").inOrder()
        assertThat(SkillLexicon.canonicalsIn("Spring 2024 intern")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Spring internship at a bank")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Spring Boot and Spring framework")).containsExactly("spring boot", "spring")
        assertThat(SkillLexicon.canonicalsIn("Spark interest in coding")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Ran Spark jobs")).containsExactly("apache spark")
        assertThat(SkillLexicon.canonicalsIn("Worked at Oracle as an intern")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Queried Oracle DB")).containsExactly("oracle database")
        assertThat(SkillLexicon.canonicalsIn("Go to market strategy")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Services in Golang")).containsExactly("go")
    }

    @Test
    fun bareCaAndBeAreNotDegrees() {
        assertThat(SkillLexicon.canonicalsIn("Sunnyvale, CA")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("MUST BE A TEAM PLAYER")).doesNotContain("b.e.")
        assertThat(SkillLexicon.canonicalsIn("MUST BE A TEAM PLAYER")).doesNotContain("chartered accountant")
        assertThat(SkillLexicon.canonicalsIn("B.E. in Electronics")).containsExactly("b.e.")
        assertThat(SkillLexicon.canonicalsIn("Cleared CA Final")).containsExactly("ca final")
        assertThat(SkillLexicon.canonicalsIn("Qualified Chartered Accountant")).containsExactly("chartered accountant")
    }

    @Test
    fun commerceDoesNotMatchInsideECommerce() {
        assertThat(SkillLexicon.canonicalsIn("Built an e-commerce site")).isEmpty()
        assertThat(SkillLexicon.canonicalsIn("Bachelor of Commerce")).containsExactly("b.com")
        assertThat(SkillLexicon.canonicalsIn("Studied Commerce")).containsExactly("commerce")
    }

    @Test
    fun curlyApostropheMatchesDegreeTerm() {
        assertThat(SkillLexicon.canonicalsIn("Bachelor’s degree in any discipline"))
            .containsExactly("bachelor's degree")
    }

    @Test
    fun unitTestsAreRecognisedAsUnitTestingAndImplySoftwareTesting() {
        assertThat(SkillLexicon.canonicalsIn("Wrote unit tests for the API")).containsExactly("unit testing")
        assertThat(SkillLexicon.withImplied(listOf("unit testing"))).contains("software testing")
    }

    @Test
    fun degreeImpliesGenericDegree() {
        assertThat(SkillLexicon.withImplied(listOf("b.tech"))).contains("bachelor's degree")
        assertThat(SkillLexicon.withImplied(listOf("mysql"))).contains("sql")
        assertThat(SkillLexicon.withImplied(listOf("github"))).contains("git")
    }

    @Test
    fun normaliseResolvesAliasesAndRejectsUnknownTerms() {
        assertThat(SkillLexicon.normalise("K8S")).isEqualTo("kubernetes")
        assertThat(SkillLexicon.normalise("  ms excel ")).isEqualTo("excel")
        assertThat(SkillLexicon.normalise("c")).isEqualTo("c")
        assertThat(SkillLexicon.normalise("underwater basket weaving")).isNull()
    }

    @Test
    fun everyFormResolvesBackToItsOwnEntry() {
        SkillLexicon.entries.forEach { entry ->
            entry.allForms.forEach { form ->
                assertThat(SkillLexicon.normalise(form)).isEqualTo(entry.canonical)
            }
        }
    }

    @Test
    fun impliedTermsExistInTheLexicon() {
        SkillLexicon.entries.flatMap { it.implies }.forEach { assertThat(SkillLexicon.isKnown(it)).isTrue() }
    }

    @Test
    fun canonicalTermsAreUnique() {
        val canonicals = SkillLexicon.entries.map { it.canonical }
        assertThat(canonicals).containsNoDuplicates()
    }
}
