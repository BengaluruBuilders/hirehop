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

    val wallet: Flow<WalletDto?> get() = state

    val cached: WalletDto? get() = state.value

    suspend fun refresh(): WalletDto = apiResult { api.wallet().wallet }.getOrThrow().also(::update)

    suspend fun refreshOrCached(): WalletDto? = apiResult { api.wallet().wallet }.getOrNull()?.also(::update) ?: state.value

    fun update(wallet: WalletDto) {
        state.value = wallet
    }

    fun generation(): Int = 0

    fun update(wallet: WalletDto, started: Int) = update(wallet)

    fun clear() {
        state.value = null
    }
}
