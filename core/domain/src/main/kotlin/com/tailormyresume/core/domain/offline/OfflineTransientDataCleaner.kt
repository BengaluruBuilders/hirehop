package com.tailormyresume.core.domain.offline

import com.tailormyresume.core.domain.account.TransientDataCleaner
import javax.inject.Inject

class OfflineTransientDataCleaner @Inject constructor() : TransientDataCleaner {
    override suspend fun clear() = Unit
}
