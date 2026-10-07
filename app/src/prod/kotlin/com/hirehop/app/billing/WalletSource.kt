package com.hirehop.app.billing

import com.hirehop.core.network.HirehopApi
import com.hirehop.core.network.apiResult
import com.hirehop.core.network.dto.WalletDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletSource @Inject constructor(private val api: HirehopApi) {
    private val state = MutableStateFlow<WalletDto?>(null)

    val wallet: Flow<WalletDto> get() = state.filterNotNull()

    val cached: WalletDto? get() = state.value

    suspend fun refresh(): WalletDto = apiResult { api.wallet().wallet }.getOrThrow().also(::update)

    suspend fun refreshOrCached(): WalletDto? = apiResult { api.wallet().wallet }.getOrNull()?.also(::update) ?: state.value

    fun update(wallet: WalletDto) {
        state.value = wallet
    }

    fun clear() {
        state.value = null
    }
}
