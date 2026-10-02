package com.hirehop.core.data.repository

import com.hirehop.core.common.network.Dispatcher
import com.hirehop.core.common.network.HhDispatchers.IO
import com.hirehop.core.data.model.asEntity
import com.hirehop.core.data.model.asExternalModel
import com.hirehop.core.database.dao.JobApplicationDao
import com.hirehop.core.database.model.JobApplicationEntity
import com.hirehop.core.model.ApplicationStatus
import com.hirehop.core.model.JobApplication
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Clock

internal class OfflineFirstApplicationRepository @Inject constructor(
    private val jobApplicationDao: JobApplicationDao,
    private val clock: Clock,
    private val cleanup: ApplicationCleanup,
    @param:Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : ApplicationRepository {

    override fun observeApplications(): Flow<List<JobApplication>> =
        jobApplicationDao.observeApplications()
            .map { entities -> entities.map(JobApplicationEntity::asExternalModel) }
            .flowOn(ioDispatcher)

    override fun observeApplication(id: String): Flow<JobApplication?> =
        jobApplicationDao.observeApplication(id)
            .map { entity -> entity?.asExternalModel() }
            .flowOn(ioDispatcher)

    override suspend fun upsertApplication(application: JobApplication) =
        jobApplicationDao.upsertApplication(application.asEntity())

    override suspend fun updateStatus(id: String, status: ApplicationStatus) =
        jobApplicationDao.updateStatus(id = id, status = status, updatedAt = clock.now())

    override suspend fun updateNotes(id: String, notes: String) =
        jobApplicationDao.updateNotes(id = id, notes = notes, updatedAt = clock.now())

    override suspend fun deleteApplication(id: String) {
        jobApplicationDao.deleteApplication(id)
        cleanup.clearFor(id)
    }
}
