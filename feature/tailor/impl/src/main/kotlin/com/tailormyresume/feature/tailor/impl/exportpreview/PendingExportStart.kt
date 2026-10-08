package com.tailormyresume.feature.tailor.impl.exportpreview

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PendingExportStart @Inject constructor() {

    private val pending = MutableStateFlow<String?>(null)

    val applicationId: StateFlow<String?> = pending.asStateFlow()

    fun request(applicationId: String) {
        pending.value = applicationId
    }

    fun consume(applicationId: String): Boolean = pending.compareAndSet(applicationId, null)
}
