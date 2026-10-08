package com.tailormyresume.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.tailormyresume.core.database.model.JobApplicationEntity
import com.tailormyresume.core.model.ApplicationStatus
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface JobApplicationDao {
    @Query("SELECT * FROM job_applications ORDER BY updatedAt DESC, id ASC")
    fun observeApplications(): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE id = :id")
    fun observeApplication(id: String): Flow<JobApplicationEntity?>

    @Upsert
    suspend fun upsertApplication(application: JobApplicationEntity)

    @Query("UPDATE job_applications SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: ApplicationStatus, updatedAt: Instant)

    @Query("UPDATE job_applications SET notes = :notes, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateNotes(id: String, notes: String, updatedAt: Instant)

    @Query("DELETE FROM job_applications WHERE id = :id")
    suspend fun deleteApplication(id: String)
}
