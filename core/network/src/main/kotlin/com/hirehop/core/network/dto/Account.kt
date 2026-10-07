package com.hirehop.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MeUserDto(val id: String, val createdAt: String)

@Serializable
data class MeResponse(val user: MeUserDto)

@Serializable
data class ConsentRequest(val purpose: String, val granted: Boolean, val policyVersion: String)

@Serializable
data class ConsentDto(
    val purpose: String,
    val granted: Boolean,
    val policyVersion: String,
    val recordedAt: String,
)

@Serializable
data class ConsentResponse(val consent: ConsentDto)

@Serializable
data class DeletionDto(val status: String)

@Serializable
data class DeletionResponse(val deletion: DeletionDto)
