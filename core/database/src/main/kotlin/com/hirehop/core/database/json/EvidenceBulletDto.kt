package com.hirehop.core.database.json

import kotlinx.serialization.Serializable

@Serializable
data class EvidenceBulletDto(
    val id: String,
    val text: String,
)
