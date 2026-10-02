package com.hirehop.core.testing.repository

import com.hirehop.core.data.repository.CoverLetterRepository
import com.hirehop.core.model.WrittenCoverLetter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class TestCoverLetterRepository : CoverLetterRepository {

    private val letters = MutableStateFlow<Map<String, WrittenCoverLetter>>(emptyMap())

    override fun observeLetter(applicationId: String): Flow<WrittenCoverLetter?> = letters.map { it[applicationId] }

    override suspend fun save(applicationId: String, letter: WrittenCoverLetter) {
        letters.update { it + (applicationId to letter) }
    }

    override suspend fun clearFor(applicationId: String) {
        letters.update { it - applicationId }
    }
}
