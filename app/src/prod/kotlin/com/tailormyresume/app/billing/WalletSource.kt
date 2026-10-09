package com.tailormyresume.app.billing

import com.tailormyresume.core.network.TailorMyResumeApi
import com.tailormyresume.core.network.apiResult
import com.tailormyresume.core.network.dto.WalletDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletSource @Inject constructor(private val api: TailorMyResumeApi) {
    private val state = MutableStateFlow<WalletDto?>(null)
    private val lock = Any()
    private var generation = 0

    val wallet: Flow<WalletDto?> get() = state

    val cached: WalletDto? get() = state.value

    suspend fun refresh(): WalletDto {
        val started = generation()
        val answer = apiResult { api.wallet().wallet }.getOrThrow()
        check(update(answer, started)) { "The wallet answer arrived after sign-out" }
        return answer
    }

    suspend fun refreshOrCached(): WalletDto? {
        val started = generation()
        val answer = apiResult { api.wallet().wallet }.getOrNull()
        return if (answer != null && update(answer, started)) answer else state.value
    }

    fun generation(): Int = synchronized(lock) { generation }

    fun update(wallet: WalletDto) {
        synchronized(lock) { state.value = wallet }
    }

    fun update(wallet: WalletDto, started: Int): Boolean = synchronized(lock) {
        (started == generation).also { current -> if (current) state.value = wallet }
    }

    fun clear() {
        synchronized(lock) {
            generation++
            state.value = null
        }
    }
}
