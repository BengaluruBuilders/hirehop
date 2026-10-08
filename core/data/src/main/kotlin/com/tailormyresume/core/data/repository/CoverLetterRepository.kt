package com.tailormyresume.core.data.repository

import com.tailormyresume.core.model.WrittenCoverLetter
import kotlinx.coroutines.flow.Flow

interface CoverLetterRepository {
    fun observeLetter(applicationId: String): Flow<WrittenCoverLetter?>

    suspend fun save(applicationId: String, letter: WrittenCoverLetter)

    suspend fun clearFor(applicationId: String)
}
