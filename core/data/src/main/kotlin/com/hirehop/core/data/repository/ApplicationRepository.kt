package com.hirehop.core.data.repository

import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.JobApplication
import kotlinx.coroutines.flow.Flow

interface ApplicationRepository {
    fun observeApplications(): Flow<List<JobApplication>>

    fun observeApplication(id: String): Flow<JobApplication?>

    suspend fun upsertApplication(application: JobApplication)

    suspend fun updateStatus(id: String, status: ApplicationStatus)

    suspend fun updateNotes(id: String, notes: String)

    suspend fun deleteApplication(id: String)
}
