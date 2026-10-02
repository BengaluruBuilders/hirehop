package com.hirehop.core.data.repository

import com.hirehop.core.data.mock.MockStateStore
import com.hirehop.core.data.mock.observeValue
import com.hirehop.core.data.mock.writeValue
import com.hirehop.core.model.WrittenCoverLetter
import com.hirehop.core.model.WrittenParagraph
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

@Singleton
internal class StoredCoverLetterRepository @Inject constructor(
    private val store: MockStateStore,
) : CoverLetterRepository {

    override fun observeLetter(applicationId: String): Flow<WrittenCoverLetter?> =
        store.observeValue(keyOf(applicationId), LetterDto.serializer()).map { it?.toModel() }

    override suspend fun save(applicationId: String, letter: WrittenCoverLetter) =
        store.writeValue(keyOf(applicationId), LetterDto.serializer(), letter.toDto())

    override suspend fun clearFor(applicationId: String) = store.remove(keyOf(applicationId))

    private fun keyOf(applicationId: String) = "coverletter.$applicationId"
}

@Serializable
private data class LetterDto(val paragraphs: List<ParagraphDto>, val writtenAtMillis: Long) {
    fun toModel() = WrittenCoverLetter(
        paragraphs = paragraphs.map { WrittenParagraph(it.text, it.isGreeting, it.isUserEdited) },
        writtenAt = Instant.fromEpochMilliseconds(writtenAtMillis),
    )
}

@Serializable
private data class ParagraphDto(val text: String, val isGreeting: Boolean, val isUserEdited: Boolean)

private fun WrittenCoverLetter.toDto() = LetterDto(
    paragraphs = paragraphs.map { ParagraphDto(it.text, it.isGreeting, it.isUserEdited) },
    writtenAtMillis = writtenAt.toEpochMilliseconds(),
)
