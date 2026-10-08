package com.tailormyresume.feature.tailor.impl.export.docx

import com.google.common.truth.Truth.assertThat
import com.tailormyresume.feature.tailor.impl.export.ExportFileName
import org.junit.Test

class DocxFileNameTest {

    @Test
    fun build_usesTheDocxExtension() {
        assertThat(DocxFileName.build("Ada Lovelace", "Acme", "Engineer")).endsWith(".docx")
    }

    @Test
    fun build_keepsTheSameBaseAsThePdfName() {
        val docx = DocxFileName.build("Ada Lovelace", "Acme", "Engineer")

        assertThat(docx).isEqualTo(ExportFileName.build("Ada Lovelace", "Acme", "Engineer").dropLast(4) + ".docx")
    }

    @Test
    fun build_fallsBackToResumeWhenEveryPartIsEmpty() {
        assertThat(DocxFileName.build("", "", "")).isEqualTo("Resume.docx")
    }

    @Test
    fun build_sanitisesEveryPart() {
        val docx = DocxFileName.build("Ada/Lovelace", "Acme & Co", "Senior Engineer")

        assertThat(docx).isEqualTo("Ada_Lovelace_Acme_Co_Senior_Engineer.docx")
    }

    @Test
    fun build_producesASingleExtension() {
        val docx = DocxFileName.build("Ada", "Acme", "Engineer")

        assertThat(docx.split(".")).hasSize(2)
    }
}
