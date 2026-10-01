package com.hirehop.core.ui.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteItemDialogTest {

    @Test
    fun theApplicationDialogUsesTheDesignTitleBodyAndNote() {
        val content = applicationSpec().toContent()

        assertEquals("Delete Associate Analyst · Northwind GCC?", content.title)
        assertEquals(
            "This deletes the JD text, the gap analysis, the tailored resume, 4 prep tasks, " +
                "10 prep questions, the cover letter, and your notes for this application.",
            content.body,
        )
        assertEquals(
            "Your 18 profile facts stay. Files you already shared don't change.",
            content.note,
        )
    }

    @Test
    fun theNoteLineIsSubjectDependentAndComesFromTheData() {
        val application = applicationSpec().toContent()
        val fact = profileFactSpec().toContent()

        assertEquals("Your 18 profile facts stay. Files you already shared don't change.", application.note)
        assertEquals("Your 4 applications stay. Files you already shared don't change.", fact.note)
        assertEquals(DeleteItemKind.APPLICATION, application.kind)
        assertEquals(DeleteItemKind.PROFILE_FACT, fact.kind)
    }

    @Test
    fun theNoteLineAgreesWithTheCountItIsGiven() {
        val oneApplication = applicationSpec(keptProfileFacts = 1).toContent()
        val manyApplications = applicationSpec(keptProfileFacts = 2).toContent()
        val oneFact = profileFactSpec(keptApplications = 1).toContent()

        assertEquals("Your 1 profile fact stays. Files you already shared don't change.", oneApplication.note)
        assertEquals("Your 2 profile facts stay. Files you already shared don't change.", manyApplications.note)
        assertEquals("Your 1 application stays. Files you already shared don't change.", oneFact.note)
    }

    @Test
    fun bothActionsAreTheSameSize() {
        val actions = applicationSpec().toContent().actions

        assertEquals(2, actions.size)
        assertEquals(
            listOf(DeleteItemActionLayout.FULL_WIDTH_EQUAL, DeleteItemActionLayout.FULL_WIDTH_EQUAL),
            actions.map { it.layout },
        )
    }

    @Test
    fun onlyTheDestructiveActionIsMarkedDestructive() {
        val content = applicationSpec().toContent()

        assertEquals("Keep application", content.keepAction.label)
        assertEquals("Delete application", content.deleteAction.label)
        assertFalse(content.keepAction.isDestructive)
        assertTrue(content.deleteAction.isDestructive)
        assertEquals(1, content.actions.count { it.isDestructive })
    }

    @Test
    fun theBodyReadsAsOneSentenceWhateverTheNumberOfParts() {
        assertEquals("This deletes a.", applicationSpec(deletedParts = listOf("a")).toContent().body)
        assertEquals(
            "This deletes a and b.",
            applicationSpec(deletedParts = listOf("a", "b")).toContent().body,
        )
        assertEquals(
            "This deletes a, b, and c.",
            applicationSpec(deletedParts = listOf("a", "b", "c")).toContent().body,
        )
    }

    private fun applicationSpec(
        keptProfileFacts: Int = 18,
        deletedParts: List<String> = APPLICATION_PARTS,
    ): DeleteItemDialogSpec = DeleteItemDialogSpec(
        kind = DeleteItemKind.APPLICATION,
        subject = "Associate Analyst · Northwind GCC",
        deletedParts = deletedParts,
        keptCount = keptProfileFacts,
        keptNounSingular = "profile fact",
        keptNounPlural = "profile facts",
        keptVerbSingular = "stays",
        keptVerbPlural = "stay",
        copy = DeleteItemDialogCopy(
            titlePrefix = "Delete",
            bodyIntro = "This deletes",
            notePrefix = "Your",
            sharedFilesNote = "Files you already shared don't change.",
            keepActionLabel = "Keep application",
            deleteActionLabel = "Delete application",
        ),
    )

    private fun profileFactSpec(keptApplications: Int = 4): DeleteItemDialogSpec = DeleteItemDialogSpec(
        kind = DeleteItemKind.PROFILE_FACT,
        subject = "SQL, 2 years · Acme Bank",
        deletedParts = listOf("the fact, and the bullets under it"),
        keptCount = keptApplications,
        keptNounSingular = "application",
        keptNounPlural = "applications",
        keptVerbSingular = "stays",
        keptVerbPlural = "stay",
        copy = DeleteItemDialogCopy(
            titlePrefix = "Delete",
            bodyIntro = "This deletes",
            notePrefix = "Your",
            sharedFilesNote = "Files you already shared don't change.",
            keepActionLabel = "Keep it",
            deleteActionLabel = "Delete fact",
        ),
    )

    private companion object {
        val APPLICATION_PARTS = listOf(
            "the JD text",
            "the gap analysis",
            "the tailored resume",
            "4 prep tasks",
            "10 prep questions",
            "the cover letter",
            "your notes for this application",
        )
    }
}
