package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.domain.ConsentUploader
import com.tailormyresume.core.domain.FirebaseUidProvider
import com.tailormyresume.core.domain.account.ServerAccountDeleter
import com.tailormyresume.core.model.ConsentRecord
import javax.inject.Inject

class OfflineConsentUploader @Inject constructor() : ConsentUploader {
    override suspend fun upload(record: ConsentRecord): Result<Unit> = Result.success(Unit)
}

class OfflineServerAccountDeleter @Inject constructor() : ServerAccountDeleter {
    override suspend fun delete(): Result<Unit> = Result.success(Unit)
}

class OfflineFirebaseUidProvider @Inject constructor() : FirebaseUidProvider {
    override fun uid(): String? = null
}
