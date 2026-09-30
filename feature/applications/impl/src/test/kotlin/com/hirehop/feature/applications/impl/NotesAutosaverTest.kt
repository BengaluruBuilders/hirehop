package com.hirehop.feature.applications.impl

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NotesAutosaverTest {

    private val savedNotes = mutableListOf<String>()

    private fun createAutosaver(scope: CoroutineScope) = NotesAutosaver(
        scope = scope,
        flushScope = scope,
        save = { savedNotes += it },
    )

    @Test
    fun onNotesChanged_savesOnlyAfterDebounce() = runTest {
        val autosaver = createAutosaver(backgroundScope)

        autosaver.onNotesChanged("a")
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS - 1)
        runCurrent()
        assertThat(savedNotes).isEmpty()

        advanceTimeBy(1)
        runCurrent()
        assertThat(savedNotes).containsExactly("a")
    }

    @Test
    fun discard_dropsPendingNotesWithoutSaving() = runTest(UnconfinedTestDispatcher()) {
        val autosaver = createAutosaver(backgroundScope)

        autosaver.onNotesChanged("a")
        autosaver.discard()
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS * 2)
        runCurrent()
        autosaver.flush()

        assertThat(savedNotes).isEmpty()
    }

    @Test
    fun flush_savesPendingNotesImmediately() = runTest(UnconfinedTestDispatcher()) {
        val autosaver = createAutosaver(backgroundScope)

        autosaver.onNotesChanged("a")
        autosaver.flush()

        assertThat(savedNotes).containsExactly("a")
    }

    @Test
    fun flush_afterSave_doesNothing() = runTest(UnconfinedTestDispatcher()) {
        val autosaver = createAutosaver(backgroundScope)

        autosaver.onNotesChanged("a")
        advanceTimeBy(NOTES_DEBOUNCE_MILLIS)
        runCurrent()
        autosaver.flush()

        assertThat(savedNotes).containsExactly("a")
    }
}
