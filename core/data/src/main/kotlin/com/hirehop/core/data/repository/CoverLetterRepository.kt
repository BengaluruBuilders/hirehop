package com.hirehop.core.data.repository

import com.hirehop.core.model.WrittenCoverLetter
import kotlinx.coroutines.flow.Flow

interface CoverLetterRepository {
    fun observeLetter(applicationId: String): Flow<WrittenCoverLetter?>

    suspend fun save(applicationId: String, letter: WrittenCoverLetter)

    suspend fun clearFor(applicationId: String)
}
