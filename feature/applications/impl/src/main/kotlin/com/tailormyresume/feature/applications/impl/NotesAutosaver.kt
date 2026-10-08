package com.tailormyresume.feature.applications.impl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal const val NOTES_DEBOUNCE_MILLIS = 500L

@OptIn(FlowPreview::class)
internal class NotesAutosaver(
    scope: CoroutineScope,
    private val flushScope: CoroutineScope,
    private val save: suspend (String) -> Unit,
) {
    private val unsavedNotes = MutableStateFlow<String?>(null)

    init {
        scope.launch {
            unsavedNotes
                .debounce(NOTES_DEBOUNCE_MILLIS)
                .filterNotNull()
                .collect { saveAndClear(it) }
        }
    }

    fun onNotesChanged(notes: String) {
        unsavedNotes.value = notes
    }

    fun discard() {
        unsavedNotes.value = null
    }

    fun flush() {
        val notes = unsavedNotes.value ?: return
        flushScope.launch { save(notes) }
    }

    private suspend fun saveAndClear(notes: String) {
        save(notes)
        unsavedNotes.compareAndSet(notes, null)
    }
}
