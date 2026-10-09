package com.tailormyresume.feature.settings.impl.yourdata

import java.io.File
import kotlin.time.Instant

enum class YourDataDestination { PROFILE, APPLICATIONS, PURCHASES }

enum class YourDataExport { IDLE, PREPARING, FAILED }

enum class YourDataDeletion { IDLE, CONFIRMING, DELETING, FAILED }

data class YourDataApplication(
    val id: String,
    val title: String,
    val company: String,
)

data class YourDataPurchase(
    val credits: Int?,
    val priceInPaise: Long?,
    val currencyCode: String?,
    val purchasedAt: Instant,
    val isPending: Boolean,
)

sealed interface YourDataUiState {
    data object Loading : YourDataUiState

    data class Content(
        val profileFactCount: Int,
        val confirmedFactCount: Int,
        val userStatedFactCount: Int,
        val applications: List<YourDataApplication>,
        val purchases: List<YourDataPurchase>,
        val isOffline: Boolean,
        val export: YourDataExport,
        val deleteTarget: YourDataApplication?,
        val deletion: YourDataDeletion = YourDataDeletion.IDLE,
    ) : YourDataUiState
}

sealed interface YourDataEvent {
    data class ShareArchive(val file: File) : YourDataEvent
}
