package com.hirehop.feature.tailor.impl.export

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExportFileNameTest {

    @Test
    fun build_joinsNameCompanyAndRole() {
        val fileName = ExportFileName.build("Priya Sharma", "Acme", "Backend Engineer")

        assertThat(fileName).isEqualTo("Priya_Sharma_Acme_Backend_Engineer.pdf")
    }

    @Test
    fun build_removesPathAndSpecialCharacters() {
        val fileName = ExportFileName.build("Priya/Sharma", "Acme, Inc.", "Dev: Backend\\API?")

        assertThat(fileName).isEqualTo("Priya_Sharma_Acme_Inc_Dev_Backend_API.pdf")
    }

    @Test
    fun build_skipsBlankParts() {
        val fileName = ExportFileName.build("Priya Sharma", "  ", "Backend Engineer")

        assertThat(fileName).isEqualTo("Priya_Sharma_Backend_Engineer.pdf")
    }

    @Test
    fun build_usesFallbackWhenEveryPartIsBlank() {
        assertThat(ExportFileName.build("", " ", "***")).isEqualTo("Resume.pdf")
    }

    @Test
    fun build_limitsTheLength() {
        val fileName = ExportFileName.build("A".repeat(200), "Acme", "Dev")

        assertThat(fileName.length).isAtMost(104)
        assertThat(fileName).endsWith(".pdf")
    }

    @Test
    fun build_keepsNonAsciiLetters() {
        assertThat(ExportFileName.build("Zoë Müller", "Acme", "Dev")).isEqualTo("Zoë_Müller_Acme_Dev.pdf")
    }

    @Test
    fun sanitise_collapsesRepeatedSeparators() {
        assertThat(ExportFileName.sanitise("  a -- b  ")).isEqualTo("a_b")
    }

    @Test
    fun sanitise_neverKeepsDots() {
        assertThat(ExportFileName.sanitise("../secret.pdf")).isEqualTo("secret_pdf")
    }
}
