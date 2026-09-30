package com.hirehop.core.database.util

import androidx.room.TypeConverter
import com.hirehop.core.database.json.EvidenceBulletDto
import com.hirehop.core.database.json.GapAnalysisDto
import com.hirehop.core.database.json.HhJson
import com.hirehop.core.database.json.JobRequirementDto
import com.hirehop.core.database.json.TailoredResumeDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer

internal class JsonConverters {
    @TypeConverter
    fun stringListToJson(value: List<String>): String =
        HhJson.encode(ListSerializer(String.serializer()), value)

    @TypeConverter
    fun jsonToStringList(json: String): List<String> =
        HhJson.decode(ListSerializer(String.serializer()), json)

    @TypeConverter
    fun evidenceBulletsToJson(value: List<EvidenceBulletDto>): String =
        HhJson.encode(ListSerializer(EvidenceBulletDto.serializer()), value)

    @TypeConverter
    fun jsonToEvidenceBullets(json: String): List<EvidenceBulletDto> =
        HhJson.decode(ListSerializer(EvidenceBulletDto.serializer()), json)

    @TypeConverter
    fun jobRequirementsToJson(value: List<JobRequirementDto>): String =
        HhJson.encode(ListSerializer(JobRequirementDto.serializer()), value)

    @TypeConverter
    fun jsonToJobRequirements(json: String): List<JobRequirementDto> =
        HhJson.decode(ListSerializer(JobRequirementDto.serializer()), json)

    @TypeConverter
    fun gapAnalysisToJson(value: GapAnalysisDto?): String? =
        value?.let { HhJson.encode(GapAnalysisDto.serializer(), it) }

    @TypeConverter
    fun jsonToGapAnalysis(json: String?): GapAnalysisDto? =
        json?.let { HhJson.decode(GapAnalysisDto.serializer(), it) }

    @TypeConverter
    fun tailoredResumeToJson(value: TailoredResumeDto?): String? =
        value?.let { HhJson.encode(TailoredResumeDto.serializer(), it) }

    @TypeConverter
    fun jsonToTailoredResume(json: String?): TailoredResumeDto? =
        json?.let { HhJson.decode(TailoredResumeDto.serializer(), it) }
}
