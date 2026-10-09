package com.tailormyresume.core.domain.account

import com.tailormyresume.core.data.repository.ApplicationRepository
import com.tailormyresume.core.data.repository.ExportHistoryRepository
import com.tailormyresume.core.data.repository.ProfileRepository
import com.tailormyresume.core.data.repository.SessionRepository
import javax.inject.Inject

class DeleteMyDataUseCase @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val profileRepository: ProfileRepository,
    private val exportHistoryRepository: ExportHistoryRepository,
    private val sessionRepository: SessionRepository,
    private val exportedFiles: ExportedFiles,
    private val transientData: TransientDataCleaner,
) {
    suspend operator fun invoke(): Result<Unit> = Result.success(Unit)
}
