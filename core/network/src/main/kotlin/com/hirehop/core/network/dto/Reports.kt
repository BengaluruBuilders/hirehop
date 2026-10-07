package com.hirehop.core.network.dto

import com.hirehop.core.model.ReportedItemKind
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ContentReportRequest(
    val applicationId: String,
    val itemKind: ReportedItemKind,
    val itemId: String,
    val generationId: String?,
    val itemText: String,
)

@Serializable
data class ContentReportDto(val id: String, val reportedAt: String)

@Serializable
data class ContentReportResponse(val report: ContentReportDto)

@Serializable
data class ExportUserDto(val id: String, val createdAt: String)

@Serializable
data class ServerExportDto(
    val generatedAt: String,
    val user: ExportUserDto,
    val consents: List<JsonElement>,
    val wallet: JsonElement,
    val unlocks: List<JsonElement>,
    val purchases: List<JsonElement>,
    val contentReports: List<JsonElement>,
)

@Serializable
data class ServerExportResponse(val export: ServerExportDto)
